package com.zvicraft.theJailBreakShow.Rounds;

import com.zvicraft.theJailBreakShow.TheJailBreakShow;
import com.zvicraft.theJailBreakShow.Teams.Teams;
import com.zvicraft.theJailBreakShow.Teams.teamsManagers;
import com.zvicraft.theJailBreakShow.utils.FadeToBlack;
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
    private static void checkTeamCounts() {
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

        // End round if a team is eliminated or only one prisoner remains
        if (aliveGuards == 0) {
//            Bukkit.broadcastMessage(ChatColor.RED + "All guards have been eliminated! Round ending...");
            endRound();
            //        } else if (alivePrisoners == 1) {
            ////            Bukkit.broadcastMessage(ChatColor.GOLD + "Only one prisoner remains! Round ending...");
            //            endRound();
        } else if (alivePrisoners == 0) {
//            Bukkit.broadcastMessage(ChatColor.RED + "All prisoners have been eliminated! Round ending...");
            endRound();
        }
    }

    private static final int MIN_PLAYERS = 2;

    /**
     * Starts the game
     */
    public static void startGame() {
        int onlinePlayers = Bukkit.getOnlinePlayers().size();
        if (onlinePlayers < MIN_PLAYERS) {
            Bukkit.broadcastMessage(ChatColor.RED + "Not enough players to start the game! (Minimum: " + MIN_PLAYERS + ", Current: " + onlinePlayers + ")");
            return;
        }

        // Set all players as prisoners initially
        for (Player player : Bukkit.getOnlinePlayers()) {
            teamsManagers.setPlayerTeam(player, Teams.Prisoners);
        }

        isGameActive = true;
        currentRound = 1;
        Bukkit.broadcastMessage(ChatColor.GREEN + "The game has started!");
        startRound();
    }

    /**
     * Stops the game
     */
    public static void stopGame() {
        isGameActive = false;
        currentRound = 0;
        Bukkit.broadcastMessage(ChatColor.RED + "The game has been stopped!");
        if (roundTask != null) {
            roundTask.cancel();
            roundTask = null;
        }
    }

    public static void startRound() {
        if (!isGameActive) {
            return; // Don't start rounds if game isn't active
        }

        currentRound++;

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
        if (currentRound == resetAfterRound) {
            isFreeDayActive = false;
            currentRound = 0;
        }

        // Display round message to all players
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.sendTitle(
                            ChatColor.GOLD + "Day " + currentRound,
                            isFreeDayActive ? ChatColor.GREEN + "Free Day is active!" : "",
                            10, 70, 20
                    );
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
                        Bukkit.broadcastMessage(ChatColor.GOLD + "Round time is up!");
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

        FadeToBlack.fade(plugin);

        // Start a new round only if auto-progress is enabled
        boolean autoProgress = plugin.getConfigManager().getConfig().getBoolean("rounds.auto-progress", true);
        if (autoProgress) {
            startRound();
        }
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