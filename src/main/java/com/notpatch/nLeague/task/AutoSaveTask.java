package com.notpatch.nLeague.task;

import com.notpatch.nLeague.NLeague;
import com.notpatch.nLeague.manager.PlayerDataManager;
import org.bukkit.scheduler.BukkitRunnable;

public class AutoSaveTask extends BukkitRunnable {

    private final NLeague main;
    private final PlayerDataManager playerDataManager;

    public AutoSaveTask(NLeague main) {
        this.main = main;
        this.playerDataManager = main.getPlayerDataManager();
    }

    @Override
    public void run() {
        playerDataManager.saveAllCachedDataAsync();
    }
}
