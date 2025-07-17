package com.notpatch.nLeague.listener;

import com.notpatch.nLeague.NLeague;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        NLeague.getInstance().getPlayerDataManager().loadPlayer(player.getUniqueId()).thenAccept(v ->{
            NLeague.getInstance().getBoostManager().startBoostCountdown(player);
        });
    }

}
