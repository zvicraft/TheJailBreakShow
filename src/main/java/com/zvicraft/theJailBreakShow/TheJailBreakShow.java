package com.zvicraft.theJailBreakShow;

import com.zvicraft.theJailBreakShow.Commands.*;
import com.zvicraft.theJailBreakShow.Currency.GoldEvents;
import com.zvicraft.theJailBreakShow.Currency.GoldManager;
import com.zvicraft.theJailBreakShow.FreeDay.FreeDayEvents;
import com.zvicraft.theJailBreakShow.GUI.GUIEventHandler;
import com.zvicraft.theJailBreakShow.GUI.GUIManager;
import com.zvicraft.theJailBreakShow.GUI.GuardChallengeGUI;
import com.zvicraft.theJailBreakShow.GUI.GuardSelectionGUI;
import com.zvicraft.theJailBreakShow.HUD.HUDManager;
import com.zvicraft.theJailBreakShow.Listeners.ChatChallengeListener;
import com.zvicraft.theJailBreakShow.Listeners.PlayerInventoryListener;
import com.zvicraft.theJailBreakShow.Listeners.TriviaChatListener;
import com.zvicraft.theJailBreakShow.Rounds.GuardChallengeManager;
import com.zvicraft.theJailBreakShow.Rounds.RoundsSystems;
import com.zvicraft.theJailBreakShow.Teams.ChatChallengeManager;
import com.zvicraft.theJailBreakShow.Teams.teamsManagers;
import com.zvicraft.theJailBreakShow.Trivia.TriviaManager;
import com.zvicraft.theJailBreakShow.utils.ConfigManager;
import com.zvicraft.theJailBreakShow.utils.LanguageManager;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

public final class TheJailBreakShow extends JavaPlugin {
    private GuardChallengeManager guardChallengeManager;
    private static TheJailBreakShow instance;
    private ConfigManager configManager;
    private GoldManager goldManager;
    private HUDManager hudManager;
    private GoldEvents goldEvents;
    private LanguageManager languageManager;
    private TriviaManager triviaManager;
    private ChatChallengeManager chatChallengeManager;
    private GUIManager guiManager;
    private boolean gameActive = false;

    @Override
    public void onEnable() {
        // Set instance
        instance = this;

        // Initialize managers
        configManager = new ConfigManager(this);
        languageManager = new LanguageManager(this);
        goldManager = new GoldManager(this);
        hudManager = new HUDManager(this);
        guiManager = new GUIManager(this);

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
        getCommand("endgame").setExecutor(new EndGameCommand(this));

        // Register commands
        getCommand("guardchallenge").setExecutor(new GuardChallengeCommand(this));

        // Guard selection command with tab completion
        GuardSelectionCommand guardSelectionCommand = new GuardSelectionCommand(this);
        getCommand("selectguard").setExecutor(guardSelectionCommand);
        getCommand("selectguard").setTabCompleter(guardSelectionCommand);

        getCommand("lr").setExecutor(new LRCommand(this));

        // Register language command
        LanguageCommand languageCommand = new LanguageCommand(this);
        getCommand("language").setExecutor(languageCommand);
        getCommand("language").setTabCompleter(languageCommand);
        getServer().getPluginManager().registerEvents(new GuardChallengeGUI(this), this);
        getServer().getPluginManager().registerEvents(new GuardSelectionGUI(this), this);

        // Register event listeners
        getServer().getPluginManager().registerEvents(new FreeDayEvents(this), this);
        getServer().getPluginManager().registerEvents(goldEvents, this);

        // Register GUI event handler
        GUIEventHandler guiEventHandler = new GUIEventHandler();
        GUIEventHandler.setPlugin(this);
        getServer().getPluginManager().registerEvents(guiEventHandler, this);

        // Initialize trivia manager
        triviaManager = new TriviaManager(this);

        // Register trivia command
        TriviaCommand triviaCommand = new TriviaCommand(this, triviaManager);
        getCommand("trivia").setExecutor(triviaCommand);
        getCommand("trivia").setTabCompleter(triviaCommand);

        // Initialize chat challenge manager
        chatChallengeManager = new ChatChallengeManager(this);

        // Register trivia chat listener
        getServer().getPluginManager().registerEvents(new TriviaChatListener(this, triviaManager), this);

        // Register chat challenge listener
        getServer().getPluginManager().registerEvents(new ChatChallengeListener(this), this);

        // Register player inventory listener
        getServer().getPluginManager().registerEvents(new PlayerInventoryListener(this), this);

        // ... other initialization code ...
        goldManager.loadData();

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
        if (instance == null) {
            throw new IllegalStateException("Plugin instance is not initialized yet or has been disabled!");
        }
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
     * Gets the language manager
     *
     * @return The language manager
     */
    public LanguageManager getLanguageManager() {
        return languageManager;
    }

    /**
     * Gets the trivia manager
     *
     * @return The trivia manager
     */
    public TriviaManager getTriviaManager() {
        return triviaManager;
    }

    /**
     * Gets the chat challenge manager
     *
     * @return The chat challenge manager
     */
    public ChatChallengeManager getChatChallengeManager() {
        return chatChallengeManager;
    }

    /**
     * Gets the GUI manager
     *
     * @return The GUI manager
     */
    public GUIManager getGUIManager() {
        return guiManager;
    }

    /**
     * Reloads the plugin configuration and reinitializes systems
     */
    public void reloadPlugin() {
        // Reload configuration
        configManager.reloadConfig();

        // Reload language files
        if (languageManager != null) {
            languageManager.reload();
            getLogger().info("Language files reloaded. Current language: " + languageManager.getCurrentLanguage());
            getLogger().info("Available languages: " + String.join(", ", languageManager.getAvailableLanguages()));
        }

        // Reinitialize systems that depend on configuration
        teamsManagers.setupTeams();
        RoundsSystems.initialize(this);

        // Reload challenges
        if (chatChallengeManager != null) {
            chatChallengeManager.reload();
        }

        // Don't disable/enable the plugin - this can cause issues
        // Just reload the necessary components

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

        // ... other cleanup code ...
        goldManager.saveData();

        getLogger().info("TheJailBreakShow has been disabled!");
    }

    public void setSpectatorSpawnLocation(Location spectatorSpawn) {
        spectatorSpawn.setPitch(0);
    }

    /**
     * Gets the teams manager
     *
     * @return The teams manager instance
     */
    public teamsManagers getTeamsManager() {
        return new teamsManagers();
    }

    /**
     * Check if the game is currently active
     *
     * @return true if the game is active, false otherwise
     */
    public boolean isGameActive() {
        return gameActive;
    }

    /**
     * Set the game active state
     *
     * @param active true to set the game as active, false otherwise
     */
    public void setGameActive(boolean active) {
        this.gameActive = active;
    }
}