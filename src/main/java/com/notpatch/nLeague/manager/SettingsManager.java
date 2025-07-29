package com.notpatch.nLeague.manager;

import com.notpatch.nLeague.NLeague;
import com.notpatch.nLeague.util.NLogger;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class SettingsManager {

    private final NLeague main;

    @Getter private boolean sameIpEnabled;
    @Getter private boolean killLimitEnabled;
    @Getter private List<String> sameIpCommands;
    @Getter private List<String> killLimitCommands;
    @Getter private int killLimit;
    @Getter private long resetTimeInMillis;

    @Getter private List<String> blacklistWorlds;


    public SettingsManager(NLeague main){
        this.main = main;
    }

    public void loadSettings(){
        main.reloadConfig();

        ConfigurationSection protectionSection = main.getConfig().getConfigurationSection("kill-protection");

        if (protectionSection == null) {
            NLogger.warn("The 'kill-protection' section is missing in config.yml! Protections will be disabled.");
            this.sameIpEnabled = false;
            this.killLimitEnabled = false;
            return;
        }

        this.sameIpEnabled = protectionSection.getBoolean("same-ip.enabled", false);
        this.killLimitEnabled = protectionSection.getBoolean("kill-limit.enabled", false);
        this.killLimit = protectionSection.getInt("kill-limit.max-kills", 5);

        this.resetTimeInMillis = parseTimeToMillis(protectionSection.getString("kill-limit.reset-time", "1h"));

        this.sameIpCommands = protectionSection.getStringList("same-ip.punish-commands");

        this.killLimitCommands = protectionSection.getStringList("kill-limit.punish-commands");

        this.blacklistWorlds = main.getConfig().getStringList("blacklist-worlds");

    }

    public boolean isPlayersSame(Player player1, Player player2){
        if(player1 == null || player2 == null) return false;
        if(player1.getAddress().getAddress().equals(player2.getAddress().getAddress())) return true;
        return false;
    }

    private long parseTimeToMillis(String timeString) {
        if (timeString == null || timeString.isEmpty()) return 0;
        try {
            char unit = timeString.charAt(timeString.length() - 1);
            long value = Long.parseLong(timeString.substring(0, timeString.length() - 1));
            return switch (Character.toLowerCase(unit)) {
                case 's' -> TimeUnit.SECONDS.toMillis(value);
                case 'm' -> TimeUnit.MINUTES.toMillis(value);
                case 'h' -> TimeUnit.HOURS.toMillis(value);
                default -> 0;
            };
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public void executeSameIpCommands(Player player){
        for(String command : sameIpCommands){
            main.getServer().dispatchCommand(main.getServer().getConsoleSender(), command.replace("%player%", player.getName()));
        }
    }

    public void executeKillLimitCommands(Player player){
        for(String command : killLimitCommands){
            main.getServer().dispatchCommand(main.getServer().getConsoleSender(), command.replace("%player%", player.getName()));
        }
    }

}
