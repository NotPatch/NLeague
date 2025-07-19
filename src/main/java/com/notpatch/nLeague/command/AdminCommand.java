package com.notpatch.nLeague.command;

import com.notpatch.nLeague.NLeague;
import com.notpatch.nLeague.manager.LeagueManager;
import com.notpatch.nLeague.manager.PlayerDataManager;
import com.notpatch.nLeague.model.League;
import com.notpatch.nLeague.model.PlayerData;
import com.notpatch.nLeague.util.LangUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class AdminCommand implements TabExecutor {

    private static final List<String> SUB_COMMANDS = Arrays.asList("reload","addpoint", "removepoint", "setpoint", "setleague", "setboost");
    private static final List<String> POINT_SUGGESTIONS = Arrays.asList("1", "10", "50", "100");
    private static final List<String> BOOST_MULTIPLIER_SUGGESTIONS = Arrays.asList("1.5", "2.0", "2.5", "3.0");
    private static final List<String> BOOST_DURATION_SUGGESTIONS = Arrays.asList("1", "3", "6", "12", "24");


    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;
            if (!player.hasPermission("nleague.admin")) {
                player.sendMessage(LangUtil.getMessage("no-permission"));
                return true;
            }
        }

        if (!commandSender.hasPermission("nleague.admin")) {
            commandSender.sendMessage(LangUtil.getMessage("no-permission"));
            return true;
        }

        LeagueManager leagueManager = NLeague.getInstance().getLeagueManager();

        if(args.length == 1 && args[0].equalsIgnoreCase("reload")){
            NLeague.getInstance().reloadConfig();
            NLeague.getInstance().saveDefaultConfig();
            NLeague.getInstance().saveConfig();

            NLeague.getInstance().getConfigurationManager().getLeagueConfiguration().reloadConfiguration();
            NLeague.getInstance().getLanguageLoader().loadLangs();
            leagueManager.loadLeagues();
            commandSender.sendMessage(LangUtil.getMessage("reload"));
            return true;
        }

        if (args.length < 2) {
            commandSender.sendMessage(LangUtil.getMessage("wrong-usage"));
            return true;
        }

        String subCommand = args[0].toLowerCase();

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            commandSender.sendMessage(LangUtil.getMessage("player-not-found").replace("%player%", args[1]));
            return true;
        }

        PlayerDataManager dataManager = NLeague.getInstance().getPlayerDataManager();
        PlayerData playerData = dataManager.getPlayerData(target.getUniqueId());
        String targetName = target.getName();


        switch (subCommand) {
            case "addpoint":
            case "removepoint":
            case "setpoint":
                if (args.length != 3) {
                    commandSender.sendMessage(LangUtil.getMessage("wrong-usage"));
                    return true;
                }
                try {
                    int points = Integer.parseInt(args[2]);
                    if (points < 0) {
                        commandSender.sendMessage(LangUtil.getMessage("point-negative"));
                        return true;
                    }

                    if (subCommand.equals("addpoint")) {
                        playerData.addPoints(points);
                        commandSender.sendMessage(LangUtil.getMessage("point-added-admin").replace("%player%", targetName).replace("%points%", String.valueOf(points)) );
                        target.sendMessage(LangUtil.getMessage("point-added-player").replace("%points%", String.valueOf(points)) );
                    } else if (subCommand.equals("removepoint")) {
                        playerData.decrementPoints(points);
                        commandSender.sendMessage(LangUtil.getMessage("point-removed-admin").replace("%player%", targetName).replace("%points%", String.valueOf(points)) );
                        target.sendMessage(LangUtil.getMessage("point-removed-player").replace("%points%", String.valueOf(points)));
                    } else {
                        playerData.setPoints(points);
                        commandSender.sendMessage(LangUtil.getMessage("point-set-admin").replace("%player%", targetName).replace("%points%", String.valueOf(points)));
                        target.sendMessage(LangUtil.getMessage("point-set-player").replace("%points%", String.valueOf(points)));
                        leagueManager.executeLeagueWorks(target);
                    }
                } catch (NumberFormatException e) {
                    commandSender.sendMessage(LangUtil.getMessage("invalid-number"));
                    return true;
                }
                break;

            case "setleague":
                if (args.length != 3) {
                    commandSender.sendMessage(LangUtil.getMessage("wrong-usage"));
                    return true;
                }
                String leagueId = args[2];
                League league = NLeague.getInstance().getLeagueManager().getLeagueById(leagueId);
                if (league == null) {
                    commandSender.sendMessage(LangUtil.getMessage("invalid-league"));
                    return true;
                }
                playerData.setCurrentLeagueID(leagueId);
                playerData.setPoints(league.getRequiredPoints());
                commandSender.sendMessage(LangUtil.getMessage("league-set-admin").replace("%player%", targetName).replace("%league%", league.getDisplayName()));
                target.sendMessage(LangUtil.getMessage("league-set-player").replace("%league%", league.getDisplayName()));
                break;

            case "setboost":
                if (args.length != 4) {
                    commandSender.sendMessage(LangUtil.getMessage("wrong-usage") );
                    return true;
                }
                try {
                    double multiplier = Double.parseDouble(args[2]);
                    int hours = Integer.parseInt(args[3]);

                    if (multiplier <= 0 || hours <= 0) {
                        commandSender.sendMessage(LangUtil.getMessage("invalid-number"));
                        return true;
                    }

                    NLeague.getInstance().getBoostManager().giveBoost(target, multiplier, hours*3600);

                    commandSender.sendMessage(LangUtil.getMessage("boost-give-admin").replace("%player%", targetName).replace("%multiplier%", String.valueOf(multiplier)).replace("%hours%", String.valueOf(hours)));
                    target.sendMessage(LangUtil.getMessage("boost-give-player").replace("%multiplier%", String.valueOf(multiplier)).replace("%hours%", String.valueOf(hours)));

                } catch (NumberFormatException e) {
                    commandSender.sendMessage(LangUtil.getMessage("invalid-number") );
                    return true;
                }
                break;

            default:
                commandSender.sendMessage(LangUtil.getMessage("wrong-usage"));
                return true;
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        final List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            StringUtil.copyPartialMatches(args[0], SUB_COMMANDS, completions);
        } else if (args.length == 2) {
            List<String> playerNames = Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .collect(Collectors.toList());
            StringUtil.copyPartialMatches(args[1], playerNames, completions);
        } else if (args.length == 3) {
            String subCommand = args[0].toLowerCase();
            switch (subCommand) {
                case "addpoint":
                case "removepoint":
                case "setpoint":
                    StringUtil.copyPartialMatches(args[2], POINT_SUGGESTIONS, completions);
                    break;
                case "setleague":
                    List<String> leagueIds = NLeague.getInstance().getLeagueManager().getSortedLeagues().stream()
                            .map(League::getId).collect(Collectors.toList());
                    StringUtil.copyPartialMatches(args[2], leagueIds, completions);
                    break;
                case "setboost":
                    StringUtil.copyPartialMatches(args[2], BOOST_MULTIPLIER_SUGGESTIONS, completions);
                    break;
            }
        }
        else if (args.length == 4) {
            if (args[0].equalsIgnoreCase("setboost")) {
                StringUtil.copyPartialMatches(args[3], BOOST_DURATION_SUGGESTIONS, completions);
            }
        }

        Collections.sort(completions);
        return completions;
    }
}