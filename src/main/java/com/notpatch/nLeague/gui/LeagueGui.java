package com.notpatch.nLeague.gui;

import com.notpatch.nLeague.NLeague;
import com.notpatch.nLeague.manager.LeagueManager;
import com.notpatch.nLeague.manager.PlayerDataManager;
import com.notpatch.nLeague.model.League;
import com.notpatch.nLeague.util.ColorUtil;
import com.notpatch.nLeague.util.NLogger;
import fr.mrmicky.fastinv.FastInv;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class LeagueGui extends FastInv {

    private final Player player;
    private final int page;
    private final FileConfiguration config;

    private final List<Integer> leagueSlots = new ArrayList<>();

    private final List<League> leagues = new ArrayList<>();

    private final LeagueManager leagueManager;
    private final PlayerDataManager playerDataManager;

    public LeagueGui(Player player, int page) {
        super(NLeague.getInstance().getConfigurationManager().getMenuConfiguration().getConfiguration().getInt("size"), ColorUtil.hexColor(NLeague.getInstance().getConfigurationManager().getMenuConfiguration().getConfiguration().getString("title")));

        this.player = player;
        this.page = page;
        this.leagueManager = NLeague.getInstance().getLeagueManager();
        this.playerDataManager = NLeague.getInstance().getPlayerDataManager();
        this.leagues.addAll(leagueManager.getSortedLeagues());
        this.config = NLeague.getInstance().getConfigurationManager().getMenuConfiguration().getConfiguration();

        loadPattern();
        setLeagueItems();

    }

    private void loadPattern(){
        leagueSlots.clear();
        List<String> pattern = config.getStringList("pattern");
        ConfigurationSection section = config.getConfigurationSection("items");

        for(int row = 0; row < pattern.size(); row++){
            String[] chars = pattern.get(row).split(" ");
            for(int column = 0; column < chars.length; column++){
                int slot = row * 9 + column;
                String symbol = chars[column];

                if(symbol.equalsIgnoreCase("%")){
                    leagueSlots.add(slot);
                    continue;
                }

                if(section.contains(symbol)){
                    ItemStack itemStack = parseItem(section.getConfigurationSection(symbol));
                    if(symbol.equalsIgnoreCase("<")){
                        setItem(slot, itemStack, e ->{
                            if(page > 0){
                                new LeagueGui(player, page - 1).open(player);
                            }
                        });
                    }else if(symbol.equalsIgnoreCase(">")){
                        if((page + 1) * leagueSlots.size() < leagues.size()){
                            setItem(slot, itemStack, e ->{
                                new LeagueGui(player, page + 1).open(player);
                            });
                        }
                    }else{
                        setItem(slot, itemStack);
                    }
                }

            }
        }

    }

    private void setLeagueItems() {
        int start = page * leagueSlots.size();
        int end = Math.min(start + leagueSlots.size(), leagues.size());

        League current = leagueManager.getLeagueById(playerDataManager.getPlayerData(player.getUniqueId()).getCurrentLeagueID());

        List<League> pageLeagues = leagues.subList(start, end);
        int count = Math.min(leagueSlots.size(), pageLeagues.size());

        for (int i = 0; i < count; i++) {
            League league = pageLeagues.get(i);

            ItemStack item;

            if (league.getRequiredPoints() < current.getRequiredPoints()) {
                item = parseLeagueItem(config.getConfigurationSection("items.%.completed"), league);
            } else if (league.getRequiredPoints() == current.getRequiredPoints()) {
                item = parseLeagueItem(config.getConfigurationSection("items.%.current"), league);
            } else {
                item = parseLeagueItem(config.getConfigurationSection("items.%.not-completed"), league);
            }

            setItem(leagueSlots.get(i), item);
        }
    }

    private ItemStack parseItem(ConfigurationSection section){
        Material mat = Material.valueOf(section.getString("type"));
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;


        if (section.contains("name"))
            meta.setDisplayName(ColorUtil.hexColor(section.getString("name")));

        if (section.contains("lore"))
            meta.setLore(section.getStringList("lore").stream()
                    .map(ColorUtil::hexColor).collect(Collectors.toList()));

        if (section.contains("model-data"))
            meta.setCustomModelData(section.getInt("model-data"));

        item.setItemMeta(meta);
        return item;
    }

    private ItemStack parseLeagueItem(ConfigurationSection section, League league){
        Material mat = Material.valueOf(section.getString("type"));
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        League newLeague = leagueManager.getNextLeague(league);

        if (section.contains("name"))
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', section.getString("name")).replace("%league_name%", league.getDisplayName()));

        if (section.contains("lore"))
            meta.setLore(section.getStringList("lore").stream()
                    .map(s -> s.replace("%points%", playerDataManager.getPlayerData(player.getUniqueId()).getPoints() + ""))
                    .map(s -> s.replace("%league_required_points%", newLeague.getRequiredPoints() + ""))
                    .map(s -> s.replace("%points_to_next_league%", newLeague.getRequiredPoints() - playerDataManager.getPlayerData(player.getUniqueId()).getPoints() + ""))
                    .map(s -> ChatColor.translateAlternateColorCodes('&', s)).collect(Collectors.toList()));

        if (section.contains("model-data"))
            meta.setCustomModelData(section.getInt("model-data"));

        item.setItemMeta(meta);
        return item;
    }

}
