package com.zvicraft.theJailBreakShow.Rounds;

import com.zvicraft.theJailBreakShow.TheJailBreakShow;
import com.zvicraft.theJailBreakShow.Teams.Teams;
import com.zvicraft.theJailBreakShow.Teams.teamsManagers;
import com.zvicraft.theJailBreakShow.utils.FadeToBlack;
import com.zvicraft.theJailBreakShow.utils.LanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class RoundsSystems {
    private static boolean isGameActive = false;
    private static int currentRound = 1;
    private static boolean isFreeDayActive = false;
    private static BukkitTask roundTask;
    private static final Random random = new Random();
    private static TheJailBreakShow plugin;
    private static Location guardSpawnLocation;
    private static List<Location> prisonerSpawnLocations = new ArrayList<>();
    private static GuardChallengeManager guardChallengeManager;
    private static final int GUARD_CHALLENGE_INTERVAL = 3; // Every 3 rounds

    private static boolean guardEliminationMessageSent = false;
    private static long lastMessageTime = 0;
    private static final long MESSAGE_COOLDOWN = 5000; // 5 seconds

    // Add this method to check if we should send the message
    private static boolean shouldSendGuardEliminationMessage() {
        long currentTime = System.currentTimeMillis();
        if (!guardEliminationMessageSent || (currentTime - lastMessageTime) > MESSAGE_COOLDOWN) {
            guardEliminationMessageSent = true;
            lastMessageTime = currentTime;
            return true;
        }
        return false;
    }

    // Modify your existing guard elimination check method
    public static void checkGuardElimination() {
        if (!shouldSendGuardEliminationMessage()) {
            return;
        }

        // Set game as inactive
        plugin.setGameActive(false);

        // Set game as active
        plugin.setGameActive(true);

        // Get your language manager messages here
        Bukkit.broadcastMessage(TheJailBreakShow.getInstance().getLanguageManager()
                .getMessage("rounds.all_guards_eliminated"));
        Bukkit.broadcastMessage(TheJailBreakShow.getInstance().getLanguageManager()
                .getMessage("rounds.waiting_for_players"));
    }

    // Add this method to reset the message state when starting new rounds
    public static void resetGuardEliminationMessage() {
        guardEliminationMessageSent = false;
        lastMessageTime = 0;
    }

    /**
     * Initializes the RoundsSystems with the plugin instance
     *
     * @param mainPlugin The plugin instance
     */
    public static void initialize(TheJailBreakShow mainPlugin) {
        plugin = mainPlugin;
        loadSpawnLocations();
        startPlayerCountCheck();
        guardChallengeManager = new GuardChallengeManager(mainPlugin);
    }

    /**
     * Loads spawn locations from config
     */
    private static void loadSpawnLocations() {
        FileConfiguration config = plugin.getConfigManager().getConfig();

        // Load guard spawn location
        if (config.contains("spawn-locations.guard")) {
            ConfigurationSection guardSection = config.getConfigurationSection("spawn-locations.guard");
            if (guardSection != null) {
                World world = Bukkit.getWorld(guardSection.getString("world", "world"));
                double x = guardSection.getDouble("x", 0);
                double y = guardSection.getDouble("y", 0);
                double z = guardSection.getDouble("z", 0);
                float yaw = (float) guardSection.getDouble("yaw", 0);
                float pitch = (float) guardSection.getDouble("pitch", 0);

                if (world != null) {
                    guardSpawnLocation = new Location(world, x, y, z, yaw, pitch);
                }
            }
        }

        // Load prisoner spawn locations
        if (config.contains("spawn-locations.prisoners")) {
            ConfigurationSection prisonersSection = config.getConfigurationSection("spawn-locations.prisoners");
            if (prisonersSection != null) {
                for (String key : prisonersSection.getKeys(false)) {
                    ConfigurationSection cellSection = prisonersSection.getConfigurationSection(key);
                    if (cellSection != null) {
                        World world = Bukkit.getWorld(cellSection.getString("world", "world"));
                        double x = cellSection.getDouble("x", 0);
                        double y = cellSection.getDouble("y", 0);
                        double z = cellSection.getDouble("z", 0);
                        float yaw = (float) cellSection.getDouble("yaw", 0);
                        float pitch = (float) cellSection.getDouble("pitch", 0);

                        if (world != null) {
                            prisonerSpawnLocations.add(new Location(world, x, y, z, yaw, pitch));
                        }
                    }
                }
            }
        }

        // If no spawn locations are defined, log a warning
        if (guardSpawnLocation == null) {
            plugin.getLogger().warning("Guard spawn location not defined in config!");
        }

        if (prisonerSpawnLocations.isEmpty()) {
            plugin.getLogger().warning("No prisoner spawn locations defined in config!");
        }
    }

    /**
     * Starts checking for player counts
     */
    private static void startPlayerCountCheck() {
        new BukkitRunnable() {
            @Override
            public void run() {
                checkTeamCounts();
            }
        }.runTaskTimer(plugin, 20L, 20L); // Check every second
    }

    /**
     * Checks if either team has been eliminated
     */
    public static void checkTeamCounts() {
        int aliveGuards = 0;
        int alivePrisoners = 0;

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.isDead()) continue;

            if (teamsManagers.isPlayerInGuardTeam(player)) {
                aliveGuards++;
            } else if (teamsManagers.isPlayerInPrisonerTeam(player)) {
                alivePrisoners++;
            }
        }

        // End round if a team is eliminated
        if (aliveGuards == 0) {
            Bukkit.broadcastMessage(plugin.getLanguageManager().getMessage("rounds.all_guards_eliminated"));
            new BukkitRunnable() {
                @Override
                public void run() {
                    endRound();

                    // Check if there are enough players before starting next round
                    int playerCount = Bukkit.getOnlinePlayers().size();
                    if (playerCount > 2) {
                        startRound(); // Only automatically start next round if more than 2 players
                    } else {
                        // If only 2 players, wait for more to join
                        Bukkit.broadcastMessage(plugin.getLanguageManager().getMessage("rounds.waiting_for_players"));
                    }
                }
            }.runTaskLater(plugin, 60L); // 3 second delay
        } else if (alivePrisoners == 0) {
            Bukkit.broadcastMessage(plugin.getLanguageManager().getMessage("rounds.all_prisoners_eliminated"));
            new BukkitRunnable() {
                @Override
                public void run() {
                    endRound();

                    // Auto-progression is handled in endRound() method
                    // We don't need to call startRound() here as it would increment the day counter twice
                }
            }.runTaskLater(plugin, 60L); // 3 second delay
        }
    }

    private static final int MIN_PLAYERS = 2;

    /**
     * Starts the game
     */
    public static void startGame() {
        int onlinePlayers = Bukkit.getOnlinePlayers().size();
        if (onlinePlayers < MIN_PLAYERS) {
            // Use language manager for message
            String message = plugin.getLanguageManager().getMessage("rounds.not_enough_players", "%min%", String.valueOf(MIN_PLAYERS), "%current%", String.valueOf(onlinePlayers));
            Bukkit.broadcastMessage(message);
            return;
        }

        // Set all players as prisoners initially
        for (Player player : Bukkit.getOnlinePlayers()) {
            teamsManagers.setPlayerTeam(player, Teams.Prisoners);
        }

        isGameActive = true;
        currentRound = 0; // Set to 0 so the first startRound() will make it day 1
        Bukkit.broadcastMessage(plugin.getLanguageManager().getMessage("rounds.game_started"));
        startRound();
    }

    /**
     * Stops the game
     */
    public static void stopGame() {
        isGameActive = false;
        currentRound = 0;
        Bukkit.broadcastMessage(plugin.getLanguageManager().getMessage("rounds.game_stopped"));
        if (roundTask != null) {
            roundTask.cancel();
            roundTask = null;
        }
    }

    public static void startRound() {
        if (!isGameActive) {
            return; // Don't start rounds if game isn't active
        }

        // Check if there are enough players to start a round
        int playerCount = Bukkit.getOnlinePlayers().size();
        if (playerCount <= 2) {
            // If only 2 or fewer players, wait for more to join
            Bukkit.broadcastMessage(plugin.getLanguageManager().getMessage("rounds.waiting_for_players"));
            return;
        }

        // Only increment round when startRound is explicitly called, not when auto-progressing
        if (roundTask == null) {
            currentRound++;
        }

        // Check if it's time for a guard challenge (every GUARD_CHALLENGE_INTERVAL rounds)
        if (currentRound % GUARD_CHALLENGE_INTERVAL == 0) {
            guardChallengeManager.startGuardChallenge();
        }

        int freeDayRound = plugin.getConfigManager().getConfig().getInt("rounds.free-day-round", 6);
        int resetAfterRound = plugin.getConfigManager().getConfig().getInt("rounds.reset-after-round", 12);
        int roundDuration = plugin.getConfigManager().getConfig().getInt("rounds.round-duration", 6000);

        if (currentRound == freeDayRound) {
            isFreeDayActive = true;
        }
        if (currentRound >= resetAfterRound) {
            isFreeDayActive = false;
            // Reset to day 1 instead of 0 to maintain proper counting
            currentRound = 1;
        }

        // Display round message to all players
        new BukkitRunnable() {
            @Override
            public void run() {
                // Broadcast current day to server chat
                String dayMessage = plugin.getLanguageManager().getMessage("rounds.day_announcement", "%round%", String.valueOf(currentRound));
                Bukkit.broadcastMessage(ChatColor.GOLD + "=== " + dayMessage + " ===");

                // Also display as title
                for (Player player : Bukkit.getOnlinePlayers()) {
                    String title = plugin.getLanguageManager().getMessage("rounds.round_start", "%round%", String.valueOf(currentRound));
                    String subtitle = isFreeDayActive ? plugin.getLanguageManager().getMessage("rounds.free_day_subtitle") : "";
                    player.sendTitle(title, subtitle, 10, 70, 20);
                }

                // Teleport players to spawn locations
                teleportPlayersToSpawns();

                // Schedule round end after the configured duration
                if (roundTask != null) {
                    roundTask.cancel();
                }

                roundTask = new BukkitRunnable() {
                    @Override
                    public void run() {
                        Bukkit.broadcastMessage(plugin.getLanguageManager().getMessage("rounds.round_end", "%round%", String.valueOf(currentRound)));
                        endRound();
                    }
                }.runTaskLater(plugin, roundDuration * 20L); // Convert seconds to ticks (20 ticks = 1 second)
            }
        }.runTaskLater(plugin, 100L); // 5 seconds after fade effect starts
    }

    /**
     * Teleports players to their spawn locations
     */
    private static void teleportPlayersToSpawns() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (teamsManagers.isPlayerInGuardTeam(player) && guardSpawnLocation != null) {
                player.teleport(guardSpawnLocation);
            } else if (teamsManagers.isPlayerInPrisonerTeam(player) && !prisonerSpawnLocations.isEmpty()) {
                // Get a random spawn location for prisoners
                Location spawnLoc = prisonerSpawnLocations.get(random.nextInt(prisonerSpawnLocations.size()));
                player.teleport(spawnLoc);
            }
        }
    }

    /**
     * Ends the current round
     */
    public static void endRound() {
        // Cancel the current round timer if it exists
        if (roundTask != null) {
            roundTask.cancel();
            roundTask = null;
        }

        // Reset kill counts for the next round
        if (plugin.getGoldEvents() != null) {
            plugin.getGoldEvents().resetKillCounts();
        }

        // Reset LR status
        LrSystems.resetLRStatus();

        FadeToBlack.fade(plugin);

        // Start a new round only if auto-progress is enabled
        boolean autoProgress = plugin.getConfigManager().getConfig().getBoolean("rounds.auto-progress", true);
        if (autoProgress) {
            // Schedule the next round start with a delay
            new BukkitRunnable() {
                @Override
                public void run() {
                    // Don't increment round counter here, it will be done in startRound
                    continueToNextRound();
                }
            }.runTaskLater(plugin, 100L); // 5 seconds delay
        }
    }

    /**
     * Continues to the next round without incrementing the round counter
     * This is used for auto-progression between rounds
     */
    private static void continueToNextRound() {
        if (!isGameActive) {
            return; // Don't continue if game isn't active
        }

        // Check if there are enough players to start a round
        int playerCount = Bukkit.getOnlinePlayers().size();
        if (playerCount <= 2) {
            // If only 2 or fewer players, wait for more to join
            Bukkit.broadcastMessage(plugin.getLanguageManager().getMessage("rounds.waiting_for_players"));
            return;
        }

        // Check if we need to activate free day or reset rounds
        int freeDayRound = plugin.getConfigManager().getConfig().getInt("rounds.free-day-round", 6);
        int resetAfterRound = plugin.getConfigManager().getConfig().getInt("rounds.reset-after-round", 12);
        int roundDuration = plugin.getConfigManager().getConfig().getInt("rounds.round-duration", 6000);

        if (currentRound == freeDayRound) {
            isFreeDayActive = true;
        }
        if (currentRound >= resetAfterRound) {
            isFreeDayActive = false;
            // Reset to day 1 instead of 0 to maintain proper counting
            currentRound = 1;
        }

        // Display round message to all players
        for (Player player : Bukkit.getOnlinePlayers()) {
            String title = plugin.getLanguageManager().getMessage("rounds.round_start", "%round%", String.valueOf(currentRound));
            String subtitle = isFreeDayActive ? plugin.getLanguageManager().getMessage("rounds.free_day_subtitle") : "";
            player.sendTitle(title, subtitle, 10, 70, 20);
        }

        // Teleport players to spawn locations
        teleportPlayersToSpawns();

        // Schedule round end after the configured duration
        roundTask = new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.broadcastMessage(plugin.getLanguageManager().getMessage("rounds.round_end", "%round%", String.valueOf(currentRound)));
                endRound();
            }
        }.runTaskLater(plugin, roundDuration * 20L); // Convert seconds to ticks (20 ticks = 1 second)
    }

    public static boolean isFreeDayActive() {
        return isFreeDayActive;
    }

    public static int getCurrentRound() {
        return currentRound;
    }

    /**
     * Gets a random prisoner spawn location
     *
     * @return A random prisoner spawn location
     */
    public static Location getRandomPrisonerSpawn() {
        if (prisonerSpawnLocations.isEmpty()) {
            return null;
        }
        return prisonerSpawnLocations.get(random.nextInt(prisonerSpawnLocations.size()));
    }

    /**
     * Gets the guard spawn location
     *
     * @return The guard spawn location
     */
    public static Location getGuardSpawn() {
        return guardSpawnLocation;
    }

    /**
     * Gets all prisoner spawn locations
     *
     * @return List of prisoner spawn locations
     */
    public static List<Location> getPrisonerSpawnLocations() {
        return prisonerSpawnLocations;
    }

    public static boolean isGameActive() {
        return isGameActive;
    }

}