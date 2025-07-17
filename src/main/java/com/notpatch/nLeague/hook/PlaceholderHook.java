package com.notpatch.nLeague.hook;

import com.notpatch.nLeague.NLeague;
import com.notpatch.nLeague.manager.LeagueManager;
import com.notpatch.nLeague.manager.PlayerDataManager;
import com.notpatch.nLeague.model.PlayerData;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PlaceholderHook extends PlaceholderExpansion {

    private final NLeague main;
    private final PlayerDataManager playerDataManager;
    private final LeagueManager leagueManager;

    public PlaceholderHook(NLeague main) {
        this.main = main;
        this.playerDataManager = main.getPlayerDataManager();
        this.leagueManager = main.getLeagueManager();
    }

    @Override
    public String getAuthor() {
        return "NotPatch";
    }

    @Override
    public String getIdentifier() {
        return "nleague";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        String[] split = params.split("_");

        PlayerData data = playerDataManager.getPlayerData(player.getUniqueId());

        if(data == null){
            return "";
        }

        if(split.length == 1){
            if(split[0].equalsIgnoreCase("points")){
                return data.getPoints()+"";
            }else if(split[0].equalsIgnoreCase("progress")){
                return leagueManager.getProgress((Player) player)+"";
            }
        }

        if(split.length == 2){
            if(split[0].equalsIgnoreCase("league")){
                if(split[1].equalsIgnoreCase("current")){
                    return leagueManager.getLeagueById(data.getCurrentLeagueID()).getDisplayName();
                }else if(split[1].equalsIgnoreCase("next")){
                    return leagueManager.getNextLeague(leagueManager.getLeagueById(data.getCurrentLeagueID())).getDisplayName();
                }
            }

            if(split[0].equalsIgnoreCase("boost")){
                if(split[1].equalsIgnoreCase("multiplier")){
                    return data.getBoost().getMultiplier()+"";
                }else if(split[1].equalsIgnoreCase("remaining")){
                    return (data.getBoost().getRemainingSeconds()/60)+"";
                }
            }
        }

        return "";
    }



}
