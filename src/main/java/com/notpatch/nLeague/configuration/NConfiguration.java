package com.notpatch.nLeague.configuration;

import com.notpatch.nLeague.NLeague;
import com.notpatch.nLeague.util.NLogger;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public abstract class NConfiguration extends YamlConfiguration {

    protected NLeague main;
    protected String name;
    protected File file;
    @Getter
    protected FileConfiguration configuration;

    public NConfiguration(NLeague main, String name) {
        this.name = name;
        this.main = main;
        file = new File(main.getDataFolder(), name);
        configuration = YamlConfiguration.loadConfiguration(file);
    }

    private void checkConfig() {
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            try (InputStream inputStream = main.getResource(name)) {
                if (inputStream != null) {
                    Files.copy(inputStream, file.toPath());
                } else {
                    file.createNewFile();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public void loadConfiguration() {
        checkConfig();
        configuration = YamlConfiguration.loadConfiguration(file);
        NLogger.info("| Configuration " + "'" + name + "'" + " loaded successful!");
    }

    public void saveConfiguration() {
        try {
            configuration.save(file);

            NLogger.info("| Configuration " + "'" + name + "'" + " saved successfully!");
        } catch (IOException exception) {
            NLogger.error("| Configuration " + "'" + name + "'" + " couldn't be saved!");
            exception.printStackTrace();
        }
    }

    public void saveConfigurationSilent() {
        try {
            configuration.save(file);

        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }


    public void reloadConfiguration() {
        loadConfiguration();
        NLogger.info("| Configuration " + "'" + name + "'" + " reloaded successfully!");
    }

}



