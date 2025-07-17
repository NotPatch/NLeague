package com.notpatch.nLeague.manager;

import com.notpatch.nLeague.NLeague;
import com.notpatch.nLeague.model.Boost;
import com.notpatch.nLeague.model.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BoostManager {

    private final NLeague main;

    private final Map<UUID, BukkitTask> activeBoosts = new ConcurrentHashMap<>();

    public BoostManager(NLeague main) {
        this.main = main;
    }

    public void giveBoost(Player player, double multiplier, int duration) {
        PlayerData playerData = main.getPlayerDataManager().getPlayerData(player.getUniqueId());
        if (playerData == null) {
            return;
        }

        Boost boost = new Boost(multiplier, duration);
        playerData.setBoost(boost);

        startBoostCountdown(player);
    }

    public void startBoostCountdown(Player player) {
        UUID uuid = player.getUniqueId();
        PlayerData playerData = main.getPlayerDataManager().getPlayerData(uuid);

        if (playerData == null || !playerData.getBoost().hasBoost()) {
            return;
        }

        cancelBoostTask(uuid);

        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                PlayerData currentData = main.getPlayerDataManager().getPlayerData(uuid);

                if (currentData == null || !player.isOnline()) {
                    cancelBoostTask(uuid);
                    return;
                }

                Boost currentBoost = currentData.getBoost();

                if (!currentBoost.hasBoost() || currentBoost.getRemainingSeconds() <= 0) {
                    currentBoost.setMultiplier(1.0);
                    currentBoost.setRemainingSeconds(0);
                    cancelBoostTask(uuid);
                    return;
                }

                currentBoost.setRemainingSeconds(currentBoost.getRemainingSeconds() - 1);
            }
        }.runTaskTimer(main, 20L, 20L);

        activeBoosts.put(uuid, task);
    }


    public void cancelBoostTask(UUID playerUuid) {
        if (activeBoosts.containsKey(playerUuid)) {
            activeBoosts.get(playerUuid).cancel();
            activeBoosts.remove(playerUuid);
        }
    }

}
