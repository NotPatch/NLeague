package com.notpatch.nLeague.util;

import com.notpatch.nLeague.NLeague;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.concurrent.ThreadLocalRandom;

public class PointUtil {

    private static final String BASE_PATH = "points-system.groups";

    public static int getKillPoint(Player player) {
        return getPointForAction(player, "on-kill");
    }

    public static int getDeathPoint(Player player) {
        return getPointForAction(player, "on-death");
    }

    /**
     * Returns the point value for a given action and player, supporting both a
     * random range (on-kill-min / on-kill-max) and the legacy fixed-value format
     * (on-kill). When a range is configured, a random value within [min, max] is
     * returned. The player's permission group is checked first; if no matching
     * group is found the "default" group values are used.
     *
     * @param player the player whose group permissions are checked
     * @param action the action key (e.g. "on-kill" or "on-death")
     * @return the resolved point value
     */
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

        // --- Random range format: on-kill-min / on-kill-max ---
        String defaultMinPath = BASE_PATH + ".default.actions." + action + "-min";
        String defaultMaxPath = BASE_PATH + ".default.actions." + action + "-max";

        int defaultMin = config.getInt(defaultMinPath, -1);
        int defaultMax = config.getInt(defaultMaxPath, -1);

        String groupMinPath = BASE_PATH + "." + playerBestGroup + ".actions." + action + "-min";
        String groupMaxPath = BASE_PATH + "." + playerBestGroup + ".actions." + action + "-max";

        int min = config.getInt(groupMinPath, defaultMin);
        int max = config.getInt(groupMaxPath, defaultMax);

        if (min >= 0 && max >= 0) {
            if (min > max) {
                int temp = min;
                min = max;
                max = temp;
            }
            return min == max ? min : ThreadLocalRandom.current().nextInt(min, max + 1);
        }

        // --- Legacy fixed-value format: on-kill ---
        String defaultPath = BASE_PATH + ".default.actions." + action;
        int defaultPoint = config.getInt(defaultPath, 0);

        String groupPath = BASE_PATH + "." + playerBestGroup + ".actions." + action;
        return config.getInt(groupPath, defaultPoint);
    }
}