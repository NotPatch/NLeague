package com.notpatch.nLeague.configuration.impl;

import com.notpatch.nLeague.NLeague;
import com.notpatch.nLeague.configuration.NConfiguration;

public class LeagueConfiguration extends NConfiguration {

    public LeagueConfiguration() {
        super(NLeague.getInstance(), "leagues.yml");
    }
}
