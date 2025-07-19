package com.notpatch.nLeague.util;

import com.notpatch.nLeague.NLeague;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class PointUtil {

    private static final String BASE_PATH = "points-system.groups";

    public static int getKillPoint(Player player) {
        return getPointForAction(player, "on-kill");
    }

    public static int getDeathPoint(Player player) {
        return getPointForAction(player, "on-death");
    }

    private static int getPointForAction(Player player, String action) {
        Configuration config = NLeague.getInstance().getConfig();

        ConfigurationSection groupsSection = config.getConfigurationSection(BASE_PATH);

        if (groupsSection == null) {
            NLogger.warn("Config file is missing the '" + BASE_PATH + "' section!");
            return 0;
        }

        String playerBestGroup = "default";

        for (String groupName : groupsSection.getKeys(false)) {
            if (player.hasPermission("group." + groupName)) {
                playerBestGroup = groupName;
                break;
            }
        }

        String defaultPath = BASE_PATH + ".default.actions." + action;
        int defaultPoint = config.getInt(defaultPath, 0);

        String groupPath = BASE_PATH + "." + playerBestGroup + ".actions." + action;

        return config.getInt(groupPath, defaultPoint);
    }
}