package com.notpatch.nLeague.configuration.impl;

import com.notpatch.nLeague.NLeague;
import com.notpatch.nLeague.configuration.NConfiguration;

public class MenuConfiguration extends NConfiguration {

    public MenuConfiguration() {
        super(NLeague.getInstance(), "menu.yml");
    }
}
