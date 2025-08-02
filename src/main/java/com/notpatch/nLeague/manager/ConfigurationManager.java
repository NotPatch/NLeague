package com.notpatch.nLeague.manager;

import com.notpatch.nLeague.configuration.NConfiguration;
import com.notpatch.nLeague.configuration.impl.LeagueConfiguration;
import com.notpatch.nLeague.configuration.impl.MenuConfiguration;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class ConfigurationManager {

    private final List<NConfiguration> configurations = new ArrayList<>();

    private final LeagueConfiguration leagueConfiguration;
    private final MenuConfiguration menuConfiguration;

    public ConfigurationManager(){
        configurations.add(leagueConfiguration = new LeagueConfiguration());
        configurations.add(menuConfiguration = new MenuConfiguration());
    }

    public void loadConfigurations() {
        for (NConfiguration configuration : configurations) {
            configuration.loadConfiguration();
        }
    }

    public void saveConfigurations() {
        for (NConfiguration configuration : configurations) {
            configuration.saveConfiguration();
        }
    }

}
