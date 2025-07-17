package com.notpatch.nLeague.model;

import lombok.Data;

import java.util.UUID;

@Data
public class PlayerData {

    private final UUID playerUUID;
    private int points;
    private Boost boost;

    private String currentLeagueID;

    public static PlayerData createDefault(UUID playerUuid, String startingLeagueId, Boost boost) {
        PlayerData data = new PlayerData(playerUuid);
        data.setPoints(0);
        data.setBoost(boost);
        data.setCurrentLeagueID(startingLeagueId);
        return data;
    }

    public int addPoints(int amount) {
        double multiplier = 1.0;
        if(getBoost().hasBoost()){
            multiplier = boost.getMultiplier();
        }
        amount = (int) (amount * multiplier);
        this.points += amount;
        return amount;
    }

    public void decrementPoints(int amount) {
        this.points -= amount;
    }

}
