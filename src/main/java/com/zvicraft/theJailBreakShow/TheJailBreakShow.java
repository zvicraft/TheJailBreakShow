package com.zvicraft.theJailBreakShow;

import com.zvicraft.theJailBreakShow.Commands.FreeDayCommand;
import com.zvicraft.theJailBreakShow.Commands.GoldCommand;
import com.zvicraft.theJailBreakShow.Commands.ReloadCommand;
import com.zvicraft.theJailBreakShow.Commands.TeamCommand;
import com.zvicraft.theJailBreakShow.Commands.TestCommand;
import com.zvicraft.theJailBreakShow.Currency.GoldEvents;
import com.zvicraft.theJailBreakShow.Currency.GoldManager;
import com.zvicraft.theJailBreakShow.FreeDay.FreeDayEvents;
import com.zvicraft.theJailBreakShow.GUI.GUIEventHandler;
import com.zvicraft.theJailBreakShow.HUD.HUDManager;
import com.zvicraft.theJailBreakShow.Rounds.RoundsSystems;
import com.zvicraft.theJailBreakShow.Teams.teamsManagers;
import com.zvicraft.theJailBreakShow.utils.ConfigManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class TheJailBreakShow extends JavaPlugin {
    private static TheJailBreakShow instance;
    private ConfigManager configManager;
    private GoldManager goldManager;
    private HUDManager hudManager;

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

        // Register event listeners
        getServer().getPluginManager().registerEvents(new FreeDayEvents(this), this);
        getServer().getPluginManager().registerEvents(new GoldEvents(this, goldManager), this);
        getServer().getPluginManager().registerEvents(new GUIEventHandler(), this);

        getLogger().info("TheJailBreakShow has been enabled!");
    }

    /**
     * Gets the instance of the plugin
     * @return The plugin instance
     */
    public static TheJailBreakShow getInstance() {
        return instance;
    }

    /**
     * Gets the gold manager
     * @return The gold manager
     */
    public GoldManager getGoldManager() {
        return goldManager;
    }

    /**
     * Gets the HUD manager
     * @return The HUD manager
     */
    public HUDManager getHUDManager() {
        return hudManager;
    }

    /**
     * Gets the config manager
     * @return The config manager
     */
    public ConfigManager getConfigManager() {
        return configManager;
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
}
