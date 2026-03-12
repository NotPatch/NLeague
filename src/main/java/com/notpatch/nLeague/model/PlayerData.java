package com.notpatch.nLeague.model;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
public class PlayerData {

    private final UUID playerUUID;
    private int points;
    private Boost boost;

    private String currentLeagueID;

    /**
     * Tracks which league IDs have already had their promotion-reward commands
     * executed for this player. Once a league's reward is claimed it is never
     * given again, even if the player demotes and then re-promotes to that tier.
     */
    private Set<String> claimedRewardLeagues = new HashSet<>();

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
