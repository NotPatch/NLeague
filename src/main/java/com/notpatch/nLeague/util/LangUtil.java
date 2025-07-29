package com.notpatch.nLeague.util;

import com.notpatch.nLeague.LanguageLoader;
import com.notpatch.nLeague.NLeague;
import com.notpatch.nLeague.manager.LeagueManager;
import com.notpatch.nLeague.manager.PlayerDataManager;
import com.notpatch.nLeague.model.PlayerData;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LangUtil {

    private static final NLeague main = NLeague.getInstance();
    private static final Configuration config = main.getConfig();
    private static final PlayerDataManager playerDataManager = main.getPlayerDataManager();
    private static final LeagueManager leagueManager = main.getLeagueManager();
    private static final LanguageLoader languageLoader = main.getLanguageLoader();

    public static String getMessage(String path) {
        return languageLoader.get(path);
    }

    public static List<String> getInfoMessage(Player player) {
        List<String> messages = languageLoader.getList("league-info");
        PlayerData playerData = playerDataManager.getPlayerData(player.getUniqueId());
        if (playerData == null) return Collections.emptyList();

        List<String> newList = new ArrayList<>();
        for (String message : messages) {
            long remainingSeconds = playerData.getBoost().getRemainingSeconds();
            long minutes = (long) Math.ceil(remainingSeconds / 60.0);

            message = message.replace("%league%", leagueManager.getLeagueById(playerData.getCurrentLeagueID()).getDisplayName());
            message = message.replace("%points%", String.valueOf(playerData.getPoints()));
            message = message.replace("%multiplier%", String.valueOf(playerData.getBoost().getMultiplier()));
            message = message.replace("%minutes%", String.valueOf(minutes));
            message = message.replace("%next_league%", leagueManager.getNextLeague(leagueManager.getLeagueById(playerData.getCurrentLeagueID())).getDisplayName());
            message = message.replace("%progressBar%", getProgressBar(leagueManager.getProgress(player)));
            message = message.replace("%progress%", String.valueOf(leagueManager.getProgress(player)));

            newList.add(ColorUtil.hexColor(message));
        }
        return newList;
    }

    public static List<String> getAdminInfoMessage(Player player) {
        List<String> messages = languageLoader.getList("player-info");
        PlayerData playerData = playerDataManager.getPlayerData(player.getUniqueId());
        if (playerData == null) return Collections.emptyList();

        List<String> newList = new ArrayList<>();
        for (String message : messages) {
            long remainingSeconds = playerData.getBoost().getRemainingSeconds();
            long minutes = (long) Math.ceil(remainingSeconds / 60.0);
            message = message.replace("%player%", player.getName());
            message = message.replace("%league%", leagueManager.getLeagueById(playerData.getCurrentLeagueID()).getDisplayName());
            message = message.replace("%points%", String.valueOf(playerData.getPoints()));
            message = message.replace("%multiplier%", String.valueOf(playerData.getBoost().getMultiplier()));
            message = message.replace("%minutes%", String.valueOf(minutes));
            message = message.replace("%next_league%", leagueManager.getNextLeague(leagueManager.getLeagueById(playerData.getCurrentLeagueID())).getDisplayName());
            message = message.replace("%progressBar%", getProgressBar(leagueManager.getProgress(player)));
            message = message.replace("%progress%", String.valueOf(leagueManager.getProgress(player)));

            newList.add(ColorUtil.hexColor(message));
        }
        return newList;
    }


    public static String getProgressBar(double progress) {
        progress = Math.max(0.0D, Math.min(100.0D, progress));
        int length = config.getInt("progress-bar.length");
        String completedChar = config.getString("progress-bar.completed-char");
        String remainingChar = config.getString("progress-bar.remaining-char");
        String completedColor = config.getString("progress-bar.completed-color");
        String remainingColor = config.getString("progress-bar.remaining-color");
        int completedLength = (int)Math.round((double)length * (progress / 100.0D));
        int remainingLength = length - completedLength;
        StringBuilder bar = new StringBuilder();
        bar.append(ColorUtil.hexColor(completedColor));
        bar.append(String.valueOf(completedChar).repeat(completedLength));
        bar.append(ColorUtil.hexColor(remainingColor));
        bar.append(String.valueOf(remainingChar).repeat(remainingLength));
        return bar.toString();
    }
}