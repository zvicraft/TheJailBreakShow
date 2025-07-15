package com.zvicraft.theJailBreakShow;

import com.zvicraft.theJailBreakShow.Commands.*;
import com.zvicraft.theJailBreakShow.Currency.GoldEvents;
import com.zvicraft.theJailBreakShow.Currency.GoldManager;
import com.zvicraft.theJailBreakShow.FreeDay.FreeDayEvents;
import com.zvicraft.theJailBreakShow.GUI.GUIEventHandler;
import com.zvicraft.theJailBreakShow.HUD.HUDManager;
import com.zvicraft.theJailBreakShow.Rounds.GuardChallengeManager;
import com.zvicraft.theJailBreakShow.Rounds.RoundsSystems;
import com.zvicraft.theJailBreakShow.Teams.teamsManagers;
import com.zvicraft.theJailBreakShow.utils.ConfigManager;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

public final class TheJailBreakShow extends JavaPlugin {
    private GuardChallengeManager guardChallengeManager;
    private static TheJailBreakShow instance;
    private ConfigManager configManager;
    private GoldManager goldManager;
    private HUDManager hudManager;
    private GoldEvents goldEvents;

    @Override
    public void onEnable() {
        // Set instance
        instance = this;

        // Initialize managers
        configManager = new ConfigManager(this);
        goldManager = new GoldManager(this);
        hudManager = new HUDManager(this);

        // Setup teams
        teamsManagers.setupTeams();

        // Create event listeners
        goldEvents = new GoldEvents(this, goldManager);

        // Initialize rounds system
        RoundsSystems.initialize(this);

        // Register commands and tab completers
        getCommand("test").setExecutor(new TestCommand());
        getCommand("freeday").setExecutor(new FreeDayCommand());
        getCommand("reload").setExecutor(new ReloadCommand(this));

        // Gold command with tab completion
        GoldCommand goldCommand = new GoldCommand(this);
        getCommand("gold").setExecutor(goldCommand);
        getCommand("gold").setTabCompleter(goldCommand);

        // Team command with tab completion
        TeamCommand teamCommand = new TeamCommand();
        getCommand("team").setExecutor(teamCommand);
        getCommand("team").setTabCompleter(teamCommand);

        // Initialize the guard challenge manager
        guardChallengeManager = new GuardChallengeManager(this);

        // Register commands
        getCommand("guardchallenge").setExecutor(new GuardChallengeCommand(this));

        // Register event listeners
        getServer().getPluginManager().registerEvents(new FreeDayEvents(this), this);
        getServer().getPluginManager().registerEvents(goldEvents, this);
        getServer().getPluginManager().registerEvents(new GUIEventHandler(), this);

        getLogger().info("TheJailBreakShow has been enabled!");
    }

    public GuardChallengeManager getGuardChallengeManager() {
        return guardChallengeManager;
    }

    /**
     * Gets the instance of the plugin
     *
     * @return The plugin instance
     */
    public static TheJailBreakShow getInstance() {
        return instance;
    }

    /**
     * Gets the gold manager
     *
     * @return The gold manager
     */
    public GoldManager getGoldManager() {
        return goldManager;
    }

    /**
     * Gets the HUD manager
     *
     * @return The HUD manager
     */
    public HUDManager getHUDManager() {
        return hudManager;
    }

    /**
     * Gets the config manager
     *
     * @return The config manager
     */
    public ConfigManager getConfigManager() {
        return configManager;
    }

    /**
     * Gets the gold events handler
     *
     * @return The gold events handler
     */
    public GoldEvents getGoldEvents() {
        return goldEvents;
    }

    /**
     * Reloads the plugin configuration and reinitializes systems
     */
    public void reloadPlugin() {
        // Reload configuration
        configManager.reloadConfig();

        // Reinitialize systems that depend on configuration
        teamsManagers.setupTeams();
        RoundsSystems.initialize(this);

        getLogger().info("TheJailBreakShow configuration has been reloaded!");
    }

    @Override
    public void onDisable() {
        // Save data
        if (goldManager != null) {
            goldManager.saveData();
        }

        if (configManager != null) {
            configManager.saveConfig();
        }

        getLogger().info("TheJailBreakShow has been disabled!");
    }

    public void setSpectatorSpawnLocation(Location spectatorSpawn) {
        spectatorSpawn.setPitch(0);
    }
}