package com.notpatch.nLeague.command;

import com.notpatch.nLeague.gui.LeagueGui;
import com.notpatch.nLeague.util.LangUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LeagueCommand implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if(!(commandSender instanceof Player player)){
            commandSender.sendMessage("You must be a player to use this command");
            return true;
        }
        List<String> list = LangUtil.getInfoMessage(player);
        for(String message : list){
            player.sendMessage(message);
        }
        new LeagueGui(player, 0).open(player);
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return List.of();
    }
}
