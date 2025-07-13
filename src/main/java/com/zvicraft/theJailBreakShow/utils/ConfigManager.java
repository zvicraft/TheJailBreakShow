package com.zvicraft.theJailBreakShow.utils;

import com.zvicraft.theJailBreakShow.TheJailBreakShow;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class ConfigManager {
    private final TheJailBreakShow plugin;
    private FileConfiguration config;
    private File configFile;

    public ConfigManager(TheJailBreakShow plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    /**
     * Loads the configuration file
     */
    public void loadConfig() {
        // Create config file if it doesn't exist
        if (configFile == null) {
            configFile = new File(plugin.getDataFolder(), "config.yml");
        }

        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }

        config = YamlConfiguration.loadConfiguration(configFile);

        // Set default values if they don't exist
        setDefaults();
    }

    /**
     * Sets default configuration values
     */
    private void setDefaults() {
        // Teams Configuration
        if (!config.contains("teams.max-guards")) {
            config.set("teams.max-guards", 2);
        }
        if (!config.contains("teams.default-guards")) {
            config.set("teams.default-guards", 1);
        }
        if (!config.contains("teams.guard-prisoner-ratio")) {
            config.set("teams.guard-prisoner-ratio", 11); // 1 guard for every 11 prisoners
        }

        // Rounds Configuration
        if (!config.contains("rounds.free-day-round")) {
            config.set("rounds.free-day-round", 6);
        }
        if (!config.contains("rounds.reset-after-round")) {
            config.set("rounds.reset-after-round", 12);
        }
        if (!config.contains("rounds.round-duration")) {
            config.set("rounds.round-duration", 600); // in seconds
        }

        // Free Day Configuration
        if (!config.contains("free-day.duration")) {
            config.set("free-day.duration", 300); // in seconds
        }

        // Currency Configuration
        if (!config.contains("currency.initial-gold")) {
            config.set("currency.initial-gold", 100);
        }
        if (!config.contains("currency.kill-reward.prisoner-kills-guard")) {
            config.set("currency.kill-reward.prisoner-kills-guard", 50);
        }
        if (!config.contains("currency.kill-reward.guard-kills-prisoner")) {
            config.set("currency.kill-reward.guard-kills-prisoner", 10);
        }

        // HUD Configuration
        if (!config.contains("hud.update-interval")) {
            config.set("hud.update-interval", 20); // in ticks
        }

        // Spawn Locations Configuration
        if (!config.contains("spawn-locations.guard")) {
            config.createSection("spawn-locations.guard");
            config.set("spawn-locations.guard.world", "world");
            config.set("spawn-locations.guard.x", 0);
            config.set("spawn-locations.guard.y", 64);
            config.set("spawn-locations.guard.z", 0);
            config.set("spawn-locations.guard.yaw", 0);
            config.set("spawn-locations.guard.pitch", 0);
        }

        if (!config.contains("spawn-locations.prisoners")) {
            config.createSection("spawn-locations.prisoners");
            // Create default prisoner cells
            for (int i = 1; i <= 5; i++) {
                config.createSection("spawn-locations.prisoners.cell" + i);
                config.set("spawn-locations.prisoners.cell" + i + ".world", "world");
                config.set("spawn-locations.prisoners.cell" + i + ".x", i * 5); // Spread cells out
                config.set("spawn-locations.prisoners.cell" + i + ".y", 64);
                config.set("spawn-locations.prisoners.cell" + i + ".z", 10);
                config.set("spawn-locations.prisoners.cell" + i + ".yaw", 0);
                config.set("spawn-locations.prisoners.cell" + i + ".pitch", 0);
            }
        }

        saveConfig();
    }

    /**
     * Saves the configuration file
     */
    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save config to " + configFile);
            e.printStackTrace();
        }
    }

    /**
     * Gets the configuration file
     * @return The configuration file
     */
    public FileConfiguration getConfig() {
        if (config == null) {
            loadConfig();
        }
        return config;
    }

    /**
     * Reloads the configuration file
     */
    public void reloadConfig() {
        if (configFile == null) {
            configFile = new File(plugin.getDataFolder(), "config.yml");
        }
        config = YamlConfiguration.loadConfiguration(configFile);

        // Set default values for any missing configuration options
        setDefaults();
    }
}
