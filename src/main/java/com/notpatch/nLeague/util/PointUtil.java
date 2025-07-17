package com.notpatch.nLeague.util;

import com.notpatch.nLeague.NLeague;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class PointUtil {

    public static int getKillPoint(Player player) {
        NLeague main = NLeague.getInstance();
        Configuration config = main.getConfig();

        ConfigurationSection pointSection = config.getConfigurationSection("point");

        if (pointSection == null) {
            return 5;
        }

        for (String key : pointSection.getKeys(false)) {
            if (key.equalsIgnoreCase("default")) {
                continue;
            }

            if (player.hasPermission("group." + key)) {
                return pointSection.getInt(key + ".kill");
            }
        }

        return pointSection.getInt("default.kill");
    }

    public static int getDeathPoint(Player player) {
        NLeague main = NLeague.getInstance();
        Configuration config = main.getConfig();

        ConfigurationSection pointSection = config.getConfigurationSection("point");

        if (pointSection == null) {
            return 5;
        }

        for (String key : pointSection.getKeys(false)) {
            if (key.equalsIgnoreCase("default")) {
                continue;
            }

            if (player.hasPermission("group." + key)) {
                return pointSection.getInt(key + ".death");
            }
        }

        return pointSection.getInt("default.death");
    }



}
