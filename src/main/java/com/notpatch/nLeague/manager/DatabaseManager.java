package com.notpatch.nLeague.manager;

import com.notpatch.nLeague.NLeague;
import com.notpatch.nLeague.model.Boost;
import com.notpatch.nLeague.model.PlayerData;
import com.notpatch.nLeague.util.NLogger;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import org.bukkit.configuration.Configuration;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DatabaseManager {
    @Getter
    private HikariDataSource dataSource;
    @Getter
    private final ExecutorService executor;
    private final NLeague main;
    private final Configuration configuration;
    @Getter
    private boolean usingSQLite = false;

    private final String SELECT_PLAYER_SQL = "SELECT * FROM players WHERE uuid = ?";

    private final String MYSQL_SAVE_PLAYER_SQL = """
        INSERT INTO players (uuid, league, points, boostMultiplier, boostTime) 
        VALUES (?, ?, ?, ?, ?) 
        ON DUPLICATE KEY UPDATE 
        league = VALUES(league), points = VALUES(points),
        boostMultiplier = VALUES(boostMultiplier), boostTime = VALUES(boostTime)
        """;

    private final String SQLITE_SAVE_PLAYER_SQL = """
        REPLACE INTO players (uuid, league, points, boostMultiplier, boostTime) 
        VALUES (?, ?, ?, ?, ?)
        """;

    public DatabaseManager(NLeague main) {
        this.main = NLeague.getInstance();
        this.configuration = main.getConfig();
        this.executor = Executors.newFixedThreadPool(10);
    }

    public void connect() {
        if(configuration.getString("database.type").equalsIgnoreCase("mysql")){
            if(!connectToMySQL()) {
                NLogger.warn("Unable to connect to MySQL database, falling back to SQLite.");
                connectToSQLite();
            }
        } else {
            connectToSQLite();
        }
    }

    public CompletableFuture<PlayerData> loadPlayerData(UUID playerUuid) {
        return queryAsync(SELECT_PLAYER_SQL, rs -> {
            if (rs.next()) {
                PlayerData data = new PlayerData(playerUuid);
                data.setCurrentLeagueID(rs.getString("league"));
                data.setPoints(rs.getInt("points"));
                data.setBoost(new Boost(
                        rs.getDouble("boostMultiplier"),
                        rs.getInt("boostTime")
                ));
                return data;
            }
            return null;
        }, playerUuid.toString())
                .thenComposeAsync(playerData -> {
                    if (playerData != null) {
                        return CompletableFuture.completedFuture(playerData);
                    } else {
                        PlayerData defaultData = PlayerData.createDefault(
                                playerUuid,
                                main.getLeagueManager().getFirstLeague().getId(),
                                new Boost(1.0, 0)
                        );
                        return savePlayerData(defaultData).thenApply(v -> defaultData);
                    }
                }, executor);
    }

    public CompletableFuture<Void> savePlayerData(PlayerData data) {
        String sql = usingSQLite ? SQLITE_SAVE_PLAYER_SQL : MYSQL_SAVE_PLAYER_SQL;
        return updateAsync(sql,
                data.getPlayerUUID().toString(),
                data.getCurrentLeagueID(),
                data.getPoints(),
                data.getBoost().getMultiplier(),
                data.getBoost().getRemainingSeconds()
        ).thenApply(v -> null);
    }

    private boolean connectToMySQL() {
        try {
            HikariConfig config = new HikariConfig();

            String host = configuration.getString("database.host");
            String database = configuration.getString("database.database");
            String username = configuration.getString("database.username");
            String password = configuration.getString("database.password");
            int port = configuration.getInt("database.port");

            String jdbcUrl = String.format("jdbc:mysql://%s:%d/%s", host, port, database);
            config.setJdbcUrl(jdbcUrl);
            config.setUsername(username);
            config.setPassword(password);

            configureMySQLPool(config);

            dataSource = new HikariDataSource(config);
            testConnection();

            NLogger.info("Connected to MySQL");
            return true;

        } catch (Exception e) {
            return false;
        }
    }


    private void connectToSQLite() {
        try {
            Class.forName("org.sqlite.JDBC");

            File dataFolder = main.getDataFolder();
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }

            File dbFile = new File(dataFolder, "database.db");

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:sqlite:" + dbFile.getAbsolutePath());
            config.setDriverClassName("org.sqlite.JDBC");

            configureSQLitePool(config);

            dataSource = new HikariDataSource(config);
            usingSQLite = true;

            testConnection();
            NLogger.info("Connected to SQLite");

        } catch (Exception e) {
            NLogger.error("Unable to connect to SQLite database! Check your config.yml file for correct connection settings and try again. If the problem persists, contact the developer for support.!");
        }
    }

    private void configureMySQLPool(HikariConfig config) {

        int poolSize = configuration.getInt("database.pool-size");
        int minimumIdle = configuration.getInt("database.minimum-idle");
        long maxLifeTime = configuration.getLong("database.maximum-lifetime");
        int keepAliveTime = configuration.getInt("database.keepalive-time");
        long connectionTimeout = configuration.getLong("database.connection-timeout");

        config.setMaximumPoolSize(poolSize);
        config.setMinimumIdle(minimumIdle);
        config.setMaxLifetime(maxLifeTime);
        config.setKeepaliveTime(keepAliveTime);
        config.setConnectionTimeout(connectionTimeout);

        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("useLocalSessionState", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        config.addDataSourceProperty("cacheResultSetMetadata", "true");
        config.addDataSourceProperty("cacheServerConfiguration", "true");
        config.addDataSourceProperty("elideSetAutoCommits", "true");
        config.addDataSourceProperty("maintainTimeStats", "false");
    }

    private void configureSQLitePool(HikariConfig config) {
        config.setMaximumPoolSize(1);
        config.setMinimumIdle(1);
        config.setConnectionTimeout(30000);
        config.setPoolName("nleague");
    }

    public void initializeTables() {

        String createTableSQL = """
        CREATE TABLE IF NOT EXISTS players (
            uuid VARCHAR(36) PRIMARY KEY,
            league VARCHAR(16) NOT NULL,
            points INTEGER DEFAULT 0,
            boostMultiplier DOUBLE DEFAULT 1.0,
            boostTime INTEGER DEFAULT 0
        )
    """;

        executeAsync(conn -> {
            try (PreparedStatement ps = conn.prepareStatement(createTableSQL)) {
                ps.executeUpdate();
                return null;
            }
        }).exceptionally(throwable -> {
            NLogger.error("An error occurred while initializing the database tables! Please check your database settings and try again. If the problem persists, contact the developer for support.!");
            return null;
        });
    }

    public void disconnect() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
        executor.shutdown();
    }

    private void testConnection() {
        try (Connection conn = dataSource.getConnection()) {
            if (!conn.isValid(1000)) {
                NLogger.error("Database connection test failed!");
            }
        } catch (SQLException e) {
            NLogger.error("Database connection test failed!");
        }
    }

    public <T> CompletableFuture<T> executeAsync(SqlFunction<Connection, T> function) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = dataSource.getConnection()) {
                return function.apply(conn);
            } catch (SQLException e) {
                NLogger.error("Database exception: " + e.getMessage());
                throw new RuntimeException(e);
            }
        }, executor);
    }

    public CompletableFuture<Integer> updateAsync(String sql, Object... params) {
        return executeAsync(conn -> {
            try (PreparedStatement ps = prepare(conn, sql, params)) {
                return ps.executeUpdate();
            }
        });
    }

    public <T> CompletableFuture<T> queryAsync(String sql, ResultSetFunction<T> function, Object... params) {
        return executeAsync(conn -> {
            try (PreparedStatement ps = prepare(conn, sql, params);
                 ResultSet rs = ps.executeQuery()) {
                return function.apply(rs);
            }
        });
    }

    public CompletableFuture<int[]> executeBatchAsync(String sql, BatchPreparedStatementSetter setter) {
        return executeAsync(conn -> {
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (int i = 0; i < setter.getBatchSize(); i++) {
                    setter.setValues(ps, i);
                    ps.addBatch();
                }
                return ps.executeBatch();
            }
        });
    }

    private PreparedStatement prepare(Connection conn, String sql, Object... params) throws SQLException {
        if (usingSQLite) {
            sql = convertToSQLite(sql);
        }

        PreparedStatement ps = conn.prepareStatement(sql);
        for (int i = 0; i < params.length; i++) {
            ps.setObject(i + 1, params[i]);
        }
        return ps;
    }

    private String convertToSQLite(String sql) {
        return sql.replaceAll("AUTO_INCREMENT", "AUTOINCREMENT")
                .replaceAll("CURRENT_TIMESTAMP\\(\\)", "CURRENT_TIMESTAMP");
    }

    @FunctionalInterface
    public interface SqlFunction<T, R> {
        R apply(T t) throws SQLException;
    }

    @FunctionalInterface
    public interface ResultSetFunction<T> {
        T apply(ResultSet rs) throws SQLException;
    }

    public interface BatchPreparedStatementSetter {
        void setValues(PreparedStatement ps, int i) throws SQLException;
        int getBatchSize();
    }
}