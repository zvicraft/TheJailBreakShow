package com.zvicraft.theJailBreakShow.Teams;

import com.zvicraft.theJailBreakShow.TheJailBreakShow;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class teamsManagers {
    private static final Map<UUID, Teams> playerTeams = new HashMap<>();
    private static int MAX_GUARDS = 2; // Will be loaded from config
    private static int DEFAULT_GUARDS = 1; // Will be loaded from config
    private static int GUARD_PRISONER_RATIO = 11; // Will be loaded from config

    public static void setupTeams() {
        // Load configuration values
        TheJailBreakShow plugin = TheJailBreakShow.getInstance();
        MAX_GUARDS = plugin.getConfigManager().getConfig().getInt("teams.max-guards", 2);
        DEFAULT_GUARDS = plugin.getConfigManager().getConfig().getInt("teams.default-guards", 1);
        GUARD_PRISONER_RATIO = plugin.getConfigManager().getConfig().getInt("teams.guard-prisoner-ratio", 11);

        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

        // Create teams if they don't exist
        createTeam(scoreboard, "prisoners", ChatColor.RED + "Prisoner");
        createTeam(scoreboard, "guards", ChatColor.BLUE + "Guard");
        createTeam(scoreboard, "spectators", ChatColor.GRAY + "Spectator");

        // Initialize default guards
        initializeDefaultGuards();
    }

    /**
     * Initializes the default number of guards based on configuration
     */
    private static void initializeDefaultGuards() {
        List<Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
        if (onlinePlayers.isEmpty()) {
            return; // No players online, nothing to do
        }

        // Shuffle the list to randomize guard selection
        Random random = new Random();
        for (int i = onlinePlayers.size() - 1; i > 0; i--) {
            int index = random.nextInt(i + 1);
            Player temp = onlinePlayers.get(index);
            onlinePlayers.set(index, onlinePlayers.get(i));
            onlinePlayers.set(i, temp);
        }

        // Determine how many guards to assign based on special cases and ratio
        int totalPlayers = onlinePlayers.size();
        int guardsToAssign = 0;

        // Special case: If there's only one player, they should be a prisoner
        if (totalPlayers <= 1) {
            guardsToAssign = 0;
        }
        // Special case: If there are only 2 players, assign one guard and one prisoner
        else if (totalPlayers == 2) {
            guardsToAssign = 1;
        }
        // Normal case: Calculate based on ratio (1 guard for every GUARD_PRISONER_RATIO prisoners)
        else {
            int guardsBasedOnRatio = Math.max(1, totalPlayers / GUARD_PRISONER_RATIO);
            guardsToAssign = Math.min(MAX_GUARDS, guardsBasedOnRatio);
        }

        // Assign guards
        for (int i = 0; i < guardsToAssign; i++) {
            setPlayerTeam(onlinePlayers.get(i), Teams.Guards);
        }

        // Assign remaining players as prisoners
        for (int i = guardsToAssign; i < onlinePlayers.size(); i++) {
            setPlayerTeam(onlinePlayers.get(i), Teams.Prisoners);
        }
    }

    private static void createTeam(Scoreboard scoreboard, String teamName, String prefix) {
        Team team = scoreboard.getTeam(teamName);
        if (team == null) {
            team = scoreboard.registerNewTeam(teamName);
        }
        team.setPrefix(prefix + " ");
    }

    public static void setPlayerTeam(Player player, Teams team) {
        playerTeams.put(player.getUniqueId(), team);
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

        // Remove from all teams first
        for (Team t : scoreboard.getTeams()) {
            t.removeEntry(player.getName());
        }

        // Add to new team
        switch (team) {
            case Prisoners:
                scoreboard.getTeam("prisoners").addEntry(player.getName());
                player.sendMessage(ChatColor.RED + "You are now a Prisoner!");
                break;
            case Guards:
                scoreboard.getTeam("guards").addEntry(player.getName());
                player.sendMessage(ChatColor.BLUE + "You are now a Guard!");
                break;
            case Spectators:
                scoreboard.getTeam("spectators").addEntry(player.getName());
                player.sendMessage(ChatColor.GRAY + "You are now a Spectator!");
                break;
            default:
                scoreboard.getTeam("spectators").addEntry(player.getName());
                player.sendMessage(ChatColor.GRAY + "You are now a Spectator!");
                break;
        }
        player.setScoreboard(scoreboard);
    }

    public static Teams getPlayerTeam(Player player) {
        return playerTeams.getOrDefault(player.getUniqueId(), Teams.Unknown);
    }

    public static boolean isPlayerInTeam(Player player) {
        return playerTeams.containsKey(player.getUniqueId());
    }

    public static boolean isPlayerInGuardTeam(Player player) {
        return playerTeams.get(player.getUniqueId()) == Teams.Guards;
    }

    public static boolean isPlayerInPrisonerTeam(Player player) {
        return playerTeams.get(player.getUniqueId()) == Teams.Prisoners;
    }

    public static boolean isPlayerInSpectatorTeam(Player player) {
        return playerTeams.get(player.getUniqueId()) == Teams.Spectators;
    }

    public static int getNumberOfGuards() {
        int totalPlayers = Bukkit.getOnlinePlayers().size();

        // Special case: If there's only one player, they should be a prisoner
        if (totalPlayers <= 1) {
            return 0;
        }

        // Special case: If there are only 2 players, assign one guard and one prisoner
        if (totalPlayers == 2) {
            return 1;
        }

        // Calculate guards based on ratio (1 guard for every GUARD_PRISONER_RATIO prisoners)
        int guardsBasedOnRatio = Math.max(1, totalPlayers / GUARD_PRISONER_RATIO);

        // Ensure we don't exceed the maximum number of guards
        return Math.min(MAX_GUARDS, guardsBasedOnRatio);
    }

    public static int getNumberOfPrisoners() {
        return Bukkit.getOnlinePlayers().size() - getNumberOfGuards();
    }

    public static int getNumberOfSpectators() {
        return Bukkit.getOnlinePlayers().size() - getNumberOfGuards() - getNumberOfPrisoners();
    }

    public static int getNumberOfPlayers() {
        return Bukkit.getOnlinePlayers().size();
    }

    public static int getNumberOfTeams() {
        return 3;
    }

    /**
     * Gets the maximum number of guards allowed
     * @return The maximum number of guards
     */
    public static int getMaxGuards() {
        return MAX_GUARDS;
    }

    /**
     * Gets the default number of guards
     * @return The default number of guards
     */
    public static int getDefaultGuards() {
        return DEFAULT_GUARDS;
    }

    /**
     * Gets the guard-to-prisoner ratio
     * @return The number of prisoners per guard
     */
    public static int getGuardPrisonerRatio() {
        return GUARD_PRISONER_RATIO;
    }
}
