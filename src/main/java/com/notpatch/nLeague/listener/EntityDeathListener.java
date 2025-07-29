package com.notpatch.nLeague.listener;

import com.notpatch.nLeague.NLeague;
import com.notpatch.nLeague.manager.LeagueManager;
import com.notpatch.nLeague.manager.PlayerDataManager;
import com.notpatch.nLeague.manager.SettingsManager;
import com.notpatch.nLeague.model.KillData;
import com.notpatch.nLeague.model.KillPair;
import com.notpatch.nLeague.model.PlayerData;
import com.notpatch.nLeague.util.LangUtil;
import com.notpatch.nLeague.util.PointUtil;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.HashMap;
import java.util.Map;

public class EntityDeathListener implements Listener {

    private final NLeague main;

    private final Map<KillPair, KillData> killTracker = new HashMap<>();
    private final LeagueManager leagueManager;
    private final PlayerDataManager  playerDataManager;
    private final SettingsManager settingsManager;

    public EntityDeathListener(NLeague main){
        this.main = main;
        this.playerDataManager = main.getPlayerDataManager();
        this.leagueManager = main.getLeagueManager();
        this.settingsManager = main.getSettingsManager();
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e){
        if(e.getEntity().getKiller() == null) return;

        Player killer = e.getEntity().getKiller();
        Player victim = e.getEntity();

        if(killer.equals(victim)) return;

        if(settingsManager.getBlacklistWorlds().contains(killer.getWorld().getName())){
            return;
        }

        if(main.getSettingsManager().isSameIpEnabled()){
            if(main.getSettingsManager().isPlayersSame(killer, victim)){
                main.getSettingsManager().executeSameIpCommands(killer);
                main.getSettingsManager().executeSameIpCommands(victim);
                return;
            }
        }

        if(main.getSettingsManager().isKillLimitEnabled()){
            KillPair pair = new KillPair(killer.getUniqueId(), victim.getUniqueId());
            KillData data = killTracker.get(pair);
            long now = System.currentTimeMillis();
            KillData newData;

            if(data != null){
                if (now - data.timestamp() > main.getSettingsManager().getResetTimeInMillis()) {
                    newData = new KillData(1, now);
                } else {
                    if (data.killCount() >= main.getSettingsManager().getKillLimit()) {
                        main.getSettingsManager().executeKillLimitCommands(killer);
                        return;
                    }
                    newData = new KillData(data.killCount() + 1, now);
                }
            }else{
                newData = new KillData(1, now);
            }
            killTracker.put(pair, newData);
        }

        PlayerData killerData = playerDataManager.getPlayerData(killer.getUniqueId());
        PlayerData victimData = playerDataManager.getPlayerData(victim.getUniqueId());

        int pointGain = PointUtil.getKillPoint(killer);
        int pointLoss = PointUtil.getDeathPoint(victim);

        if(victimData.getPoints() - pointLoss >= 0){
            victimData.decrementPoints(pointLoss);
            victim.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy(LangUtil.getMessage("actionbar-point-loss").replace("%total_points%", String.valueOf(victimData.getPoints())).replace("%points%", pointLoss+"")));
        }

        int finalGained = killerData.addPoints(pointGain);
        killer.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy(LangUtil.getMessage("actionbar-point-gain").replace("%total_points%", String.valueOf(killerData.getPoints())).replace("%points%", String.valueOf(finalGained))));

        leagueManager.executeLeagueWorks(killer);
        leagueManager.executeLeagueWorks(victim);

    }

}
