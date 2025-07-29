package com.notpatch.nLeague;

import com.notpatch.nLeague.command.AdminCommand;
import com.notpatch.nLeague.command.LeagueCommand;
import com.notpatch.nLeague.command.LeaguesCommand;
import com.notpatch.nLeague.hook.PlaceholderHook;
import com.notpatch.nLeague.listener.EntityDeathListener;
import com.notpatch.nLeague.listener.PlayerJoinListener;
import com.notpatch.nLeague.listener.PlayerQuitListener;
import com.notpatch.nLeague.manager.*;
import com.notpatch.nLeague.task.AutoSaveTask;
import com.notpatch.nLeague.util.NLogger;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public final class NLeague extends JavaPlugin {

    @Getter private static NLeague instance;

    @Getter private ConfigurationManager configurationManager;
    @Getter private LeagueManager leagueManager;
    @Getter private PlayerDataManager playerDataManager;
    @Getter private DatabaseManager databaseManager;
    @Getter private SettingsManager settingsManager;
    @Getter private BoostManager boostManager;
    @Getter private LanguageLoader languageLoader;
    private BukkitRunnable autoSaveTask;

    @Override
    public void onEnable() {
        instance = this;

        String serverVersion = Bukkit.getVersion();
        boolean isFolia = serverVersion.contains("Folia") || serverVersion.contains("Luminol");

        int minecraftMajorVersion = 0;
        try {
            String minecraftVersion = Bukkit.getBukkitVersion().split("-")[0];
            String[] versionParts = minecraftVersion.split("\\.");
            if (versionParts.length >= 2) {
                minecraftMajorVersion = Integer.parseInt(versionParts[1]);
            }
        } catch (NumberFormatException e) {
            NLogger.error("Failed to parse Minecraft version: " + e.getMessage());
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        if (isFolia) {
            if (minecraftMajorVersion < 20) {
                NLogger.error("NLeague requires Minecraft 1.20 or higher to run on Folia.");
                Bukkit.getPluginManager().disablePlugin(this);
                return;
            }
        } else {
            if (minecraftMajorVersion < 16) {
                NLogger.error("NLeague requires Minecraft 1.16 or higher to run on Paper/Spigot.");
                Bukkit.getPluginManager().disablePlugin(this);
                return;
            }
        }

        saveDefaultConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();

        settingsManager = new SettingsManager(this);
        settingsManager.loadSettings();

        databaseManager = new DatabaseManager(this);
        databaseManager.connect();
        databaseManager.initializeTables();

        languageLoader = new LanguageLoader();
        languageLoader.loadLangs();

        configurationManager = new ConfigurationManager();
        configurationManager.loadConfigurations();

        leagueManager = new LeagueManager(this);
        leagueManager.loadLeagues();

        boostManager = new BoostManager(this);

        playerDataManager = new PlayerDataManager(this);

        getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(), this);
        getServer().getPluginManager().registerEvents(new EntityDeathListener(this), this);

        getCommand("nleague").setExecutor(new AdminCommand());
        getCommand("league").setExecutor(new LeagueCommand());
        getCommand("leagues").setExecutor(new LeaguesCommand());

        if(getServer().getPluginManager().getPlugin("PlaceholderAPI")  != null) {
            new PlaceholderHook(this).register();
            NLogger.info("PlaceholderAPI hooked!");
        }

        autoSaveTask = new AutoSaveTask(this);
        autoSaveTask.runTaskTimerAsynchronously(this, 0, 20L*600);

    }

    @Override
    public void onDisable() {
        playerDataManager.saveAllCachedData();
        databaseManager.disconnect();
        configurationManager.saveConfigurations();
    }
}
