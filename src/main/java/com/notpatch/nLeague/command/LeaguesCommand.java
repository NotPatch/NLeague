package com.notpatch.nLeague.command;

import com.notpatch.nLeague.util.LangUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class TestCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        Player player = (Player) commandSender;
        List<String> list = LangUtil.getInfoMessage(player);
        for(String message : list){
            player.sendMessage(message);
        }
        return false;
    }
}
