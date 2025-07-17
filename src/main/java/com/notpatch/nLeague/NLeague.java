package com.notpatch.nLeague;

import com.notpatch.nLeague.command.AdminCommand;
import com.notpatch.nLeague.command.LeagueCommand;
import com.notpatch.nLeague.command.LeaguesCommand;
import com.notpatch.nLeague.hook.PlaceholderHook;
import com.notpatch.nLeague.listener.EntityDeathListener;
import com.notpatch.nLeague.listener.PlayerJoinListener;
import com.notpatch.nLeague.listener.PlayerQuitListener;
import com.notpatch.nLeague.manager.*;
import com.notpatch.nLeague.util.NLogger;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

public final class NLeague extends JavaPlugin {

    @Getter private static NLeague instance;

    @Getter private ConfigurationManager configurationManager;
    @Getter private LeagueManager leagueManager;
    @Getter private PlayerDataManager playerDataManager;
    @Getter private DatabaseManager databaseManager;
    @Getter private BoostManager boostManager;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        saveConfig();

        databaseManager = new DatabaseManager(this);
        databaseManager.connect();
        databaseManager.initializeTables();

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

    }

    @Override
    public void onDisable() {
        playerDataManager.saveAllCachedData();
        databaseManager.disconnect();
        configurationManager.saveConfigurations();
    }
}
