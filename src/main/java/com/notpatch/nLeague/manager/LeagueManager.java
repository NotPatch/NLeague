package com.notpatch.nLeague.manager;

import com.notpatch.nLeague.NLeague;
import com.notpatch.nLeague.model.League;
import com.notpatch.nLeague.model.PlayerData;
import com.notpatch.nLeague.util.LangUtil;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.*;

public class LeagueManager {

    private final NLeague main;

    private final Map<String, League> leaguesByID = new HashMap<>();
    private List<League> sortedLeagues = new ArrayList<>();

    public LeagueManager(NLeague main){
        this.main = main;

    }

    public void loadLeagues() {
        leaguesByID.clear();
        sortedLeagues.clear();

        Configuration configuration = main.getConfigurationManager().getLeagueConfiguration().getConfiguration();

        ConfigurationSection leaguesSection = configuration.getConfigurationSection("leagues");
        if (leaguesSection == null) {
            main.getLogger().warning("leagues.yml is corrupted or missing! Please redownload the plugin and try again!");
            return;
        }

        for (String leagueId : leaguesSection.getKeys(false)) {
            String path = "leagues." + leagueId;

            String displayName = configuration.getString(path + ".display-name");
            int requiredPoints = configuration.getInt(path + ".required-points");
            List<String> promotionCommands = configuration.getStringList(path + ".promotion-commands");
            List<String> demotionCommands = configuration.getStringList(path + ".demotion-commands");

            League league = new League(leagueId, displayName, requiredPoints, promotionCommands, demotionCommands);

            leaguesByID.put(leagueId, league);
            sortedLeagues.add(league);
        }

        sortedLeagues.sort(Comparator.comparingInt(League::getRequiredPoints));

    }

    public League getLeagueForPoints(int playerPoints) {
        League currentLeague = null;
        for (int i = sortedLeagues.size() - 1; i >= 0; i--) {
            League league = sortedLeagues.get(i);
            if (playerPoints >= league.getRequiredPoints()) {
                currentLeague = league;
                break;
            }
        }
        return currentLeague;
    }

    public boolean isLeaguePointsEquals(Player player) {
        PlayerData playerData = main.getPlayerDataManager().getPlayerData(player.getUniqueId());
        League currentLeague = getLeagueById(playerData.getCurrentLeagueID());
        if(getLeagueForPoints(playerData.getPoints()).getId().equalsIgnoreCase(currentLeague.getId())){
            return true;
        }
        return false;
    }

    public void executeLeagueWorks(Player player){
        if(!isLeaguePointsEquals(player)){
            PlayerData playerData = main.getPlayerDataManager().getPlayerData(player.getUniqueId());
            League newLeague = getLeagueForPoints(playerData.getPoints());
            if(newLeague != null){
                String previousLeagueId = playerData.getCurrentLeagueID();
                if(previousLeagueId.equalsIgnoreCase(newLeague.getId())){
                    return;
                }

                int previousRank = -1;
                int newRank = -1;

                for (int i = 0; i < sortedLeagues.size(); i++) {
                    String currentId = sortedLeagues.get(i).getId();
                    if (currentId.equals(previousLeagueId)) {
                        previousRank = i;
                    }
                    if (currentId.equals(newLeague.getId())) {
                        newRank = i;
                    }
                }

                if (previousRank == -1 || newRank == -1) {
                    return;
                }

                playerData.setCurrentLeagueID(newLeague.getId());

                if(newRank > previousRank){
                    String title = LangUtil.getMessage("league-up-title").split("~")[0].replace("%league%", newLeague.getDisplayName());
                    String subtitle = LangUtil.getMessage("league-up-title").split("~")[1].replace("%league%", newLeague.getDisplayName());
                    player.sendTitle(title, subtitle, 10, 20, 10);
                    executePromotionCommands(player, newLeague);
                }else if(newRank < previousRank){
                    String title = LangUtil.getMessage("league-down-title").split("~")[0].replace("%league%", newLeague.getDisplayName());
                    String subtitle = LangUtil.getMessage("league-down-title").split("~")[1].replace("%league%", newLeague.getDisplayName());
                    player.sendTitle(title, subtitle, 10,20,10);
                    executeDemotionCommands(player, getLeagueById(previousLeagueId));
                }
            }
        }
    }

    public League getLeagueById(String id) {
        return leaguesByID.get(id);
    }

    public List<League> getSortedLeagues() {
        return Collections.unmodifiableList(sortedLeagues);
    }

    public League getFirstLeague() {
        return sortedLeagues.get(0);
    }

    public League getNextLeague(League currentLeague) {
        int currentIndex = sortedLeagues.indexOf(currentLeague);

        if (currentIndex == -1 || currentIndex >= sortedLeagues.size() - 1) {
            return currentLeague;
        }

        if(sortedLeagues.get(currentIndex + 1) == null){
            return currentLeague;
        }

        return sortedLeagues.get(currentIndex + 1);
    }

    public League getPreviousLeague(League league){
        return sortedLeagues.get(sortedLeagues.indexOf(league) - 1);
    }


    public int getNextLeaguePoints(Player player){
        PlayerData playerData = main.getPlayerDataManager().getPlayerData(player.getUniqueId());
        int currentLeaguePoints = getLeagueForPoints(playerData.getPoints()).getRequiredPoints();
        int nextLeaguePoints = 0;
        for(int i = 0; i < sortedLeagues.size(); i++){
            if(sortedLeagues.get(i).getRequiredPoints() > currentLeaguePoints){
                nextLeaguePoints = sortedLeagues.get(i).getRequiredPoints();
                break;
            }
        }
        return nextLeaguePoints;
    }

    public double getProgress(Player player) {
        UUID uuid = player.getUniqueId();
        PlayerDataManager playerDataManager = main.getPlayerDataManager();
        PlayerData playerData = playerDataManager.getPlayerData(uuid);

        if (playerData == null) {
            return 0.0D;
        }

        League currentLeague = getLeagueById(playerData.getCurrentLeagueID());
        if (currentLeague != null) {
            League nextLeague = getNextLeague(currentLeague);

            if (nextLeague == null) {
                return 100.0D;
            }
            double minPoints = currentLeague.getRequiredPoints();

            double maxPoints = nextLeague.getRequiredPoints();

            double playerPoints = playerData.getPoints();

            if (maxPoints <= minPoints) {
                return playerPoints >= maxPoints ? 100.0 : 0.0;
            }

            double pointsInThisLeague = playerPoints - minPoints;

            double totalPointsForThisLeague = maxPoints - minPoints;

            double progress = (pointsInThisLeague / totalPointsForThisLeague) * 100.0;

            return Math.max(0.0, Math.min(progress, 100.0));
        }

        return 0.0D;
    }

    private void executePromotionCommands(Player player, League newLeague) {
        newLeague.getPromotionCommands().forEach(command -> {
            String formattedCommand = command.replace("%player%", player.getName());
            main.getServer().dispatchCommand(main.getServer().getConsoleSender(), formattedCommand);
        });
    }

    private void executeDemotionCommands(Player player, League oldLeague) {
        oldLeague.getDemotionCommands().forEach(command -> {
            String formattedCommand = command.replace("%player%", player.getName());
            main.getServer().dispatchCommand(main.getServer().getConsoleSender(), formattedCommand);
        });
    }
}
