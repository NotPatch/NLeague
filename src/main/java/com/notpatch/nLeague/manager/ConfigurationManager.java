package com.notpatch.nLeague.manager;

import com.notpatch.nLeague.configuration.NConfiguration;
import com.notpatch.nLeague.configuration.impl.LeagueConfiguration;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class ConfigurationManager {

    private final List<NConfiguration> configurations = new ArrayList<>();

    private final LeagueConfiguration leagueConfiguration;

    public ConfigurationManager(){
        configurations.add(leagueConfiguration = new LeagueConfiguration());
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
