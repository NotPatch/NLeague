package com.notpatch.nLeague.util;

import net.md_5.bungee.api.ChatColor;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ColorUtil {

    private static final Pattern pattern = Pattern.compile("#[a-fA-F0-9]{6}");

    public static String hexColor(String message) {
        Matcher matcher = pattern.matcher(message);

        while (matcher.find()) {
            String color = message.substring(matcher.start(), matcher.end());
            message = message.replace(color, "" + ChatColor.of(color));
            matcher = pattern.matcher(message);
        }

        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static List<String> getColoredList(List<String> list) {
        return list.stream()
                .map(ColorUtil::hexColor)
                .collect(Collectors.toList());
    }

    public static List<String> getColoredList(List<String> list, String replace, String replacement) {
        return list.stream()
                .map(ColorUtil::hexColor)
                .map(s -> s.replace(replace, replacement))
                .collect(Collectors.toList());
    }

    public static List<String> getColoredList(List<String> list, String replace, String replacement, String replace2, String replacement2) {
        return list.stream()
                .map(ColorUtil::hexColor)
                .map(s -> s.replace(replace, replacement))
                .map(s -> s.replace(replace2, replacement2))
                .collect(Collectors.toList());
    }

    public static List<String> getColoredList(List<String> list, String replace, String replacement, String replace2, String replacement2, String replace3, String replacement3) {
        return list.stream()
                .map(ColorUtil::hexColor)
                .map(s -> s.replace(replace, replacement))
                .map(s -> s.replace(replace2, replacement2))
                .map(s -> s.replace(replace3, replacement3))
                .collect(Collectors.toList());
    }

}