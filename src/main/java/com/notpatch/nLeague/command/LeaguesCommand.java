package com.notpatch.nLeague.command;

import com.notpatch.nLeague.NLeague;
import com.notpatch.nLeague.model.League;
import com.notpatch.nLeague.util.LangUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LeaguesCommand implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if(!(commandSender instanceof Player player)){
            commandSender.sendMessage("You must be a player to use this command");
            return true;
        }
        String base = LangUtil.getMessage("leagues-info");
        for(League league : NLeague.getInstance().getLeagueManager().getSortedLeagues()){
            player.sendMessage(base.replace("%points%", league.getRequiredPoints()+"").replace("%league%", league.getDisplayName()));
        }
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return List.of();
    }
}
