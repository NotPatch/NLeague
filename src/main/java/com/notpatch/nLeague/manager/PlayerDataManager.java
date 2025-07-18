package com.notpatch.nLeague.manager;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.notpatch.nLeague.NLeague;
import com.notpatch.nLeague.model.PlayerData;
import com.notpatch.nLeague.util.NLogger;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class PlayerDataManager {

    private final NLeague main;
    private final DatabaseManager databaseManager;

    private final Cache<UUID, PlayerData> cache;

    public PlayerDataManager(NLeague main) {
        this.main = main;
        this.databaseManager = main.getDatabaseManager();
        cache = Caffeine.newBuilder()
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .maximumSize(10_000)
                .build();
    }

    public CompletableFuture<Void> loadPlayer(UUID uuid) {
        if (cache.getIfPresent(uuid) != null) {
            return CompletableFuture.completedFuture(null);
        }

        return databaseManager.loadPlayerData(uuid).thenAccept(playerData -> {
            if (playerData != null) {
                this.cache.put(uuid, playerData);
            } else {
                NLogger.warn("Failed to load player data for " + uuid + "!");
            }
        });
    }

    public CompletableFuture<Void> savePlayer(UUID uuid) {
        PlayerData playerData = cache.getIfPresent(uuid);
        if (playerData == null) {
            return CompletableFuture.completedFuture(null);
        }

        return databaseManager.savePlayerData(playerData).thenRun(() -> {
            this.cache.invalidate(uuid);
        });
    }

    public PlayerData getPlayerData(UUID uuid) {
        return cache.getIfPresent(uuid);
    }

    public void saveAllCachedData() {
        CompletableFuture<?>[] futures = cache.asMap().values().stream()
                .map(databaseManager::savePlayerData)
                .toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(futures)
                .exceptionally(throwable -> {
                    NLogger.error("Error saving cached player data: " + throwable.getMessage());
                    return null;
                })
                .join();
    }

}
