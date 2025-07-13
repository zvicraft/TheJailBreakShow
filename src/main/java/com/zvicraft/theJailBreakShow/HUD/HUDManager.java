package com.zvicraft.theJailBreakShow.HUD;

import com.zvicraft.theJailBreakShow.FreeDay.FreeDayManager;
import com.zvicraft.theJailBreakShow.Rounds.RoundsSystems;
import com.zvicraft.theJailBreakShow.Teams.Teams;
import com.zvicraft.theJailBreakShow.Teams.teamsManagers;
import com.zvicraft.theJailBreakShow.TheJailBreakShow;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

public class HUDManager {
    private final TheJailBreakShow plugin;

    public HUDManager(TheJailBreakShow plugin) {
        this.plugin = plugin;

        // Start HUD update task
        Bukkit.getScheduler().runTaskTimer(plugin, this::updateAllHUDs, 20L, 20L); // Update every second
    }

    /**
     * Updates the HUD for all online players
     */
    public void updateAllHUDs() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            updateHUD(player);
        }
    }

    /**
     * Updates the HUD for a specific player
     *
     * @param player The player to update the HUD for
     */
    public void updateHUD(Player player) {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard board = manager.getNewScoreboard();

        Objective objective = board.registerNewObjective("hud", "dummy", ChatColor.GOLD + "The Jail Break Show");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        // Add information to scoreboard
        int score = 15;

        objective.getScore(ChatColor.YELLOW + "Day: " + ChatColor.WHITE + RoundsSystems.getCurrentRound()).setScore(score--);
        objective.getScore(ChatColor.YELLOW + "Free Day: " + ChatColor.WHITE + (FreeDayManager.isFreeDayActive() ? "Yes" : "No")).setScore(score--);
        objective.getScore(" ").setScore(score--);

        Teams playerTeam = teamsManagers.getPlayerTeam(player);
        objective.getScore(ChatColor.YELLOW + "Your Team: " + getTeamColor(playerTeam) + playerTeam).setScore(score--);

        objective.getScore("  ").setScore(score--);
        objective.getScore(ChatColor.YELLOW + "Gold: " + ChatColor.GOLD + plugin.getGoldManager().getGold(player)).setScore(score--);

        objective.getScore("   ").setScore(score--);
        objective.getScore(ChatColor.YELLOW + "Players Online: " + ChatColor.WHITE + Bukkit.getOnlinePlayers().size()).setScore(score--);
        objective.getScore(ChatColor.BLUE + "Guards: " + ChatColor.WHITE + teamsManagers.getNumberOfGuards()).setScore(score--);
        objective.getScore(ChatColor.RED + "Prisoners: " + ChatColor.WHITE + teamsManagers.getNumberOfPrisoners()).setScore(score--);

        player.setScoreboard(board);
    }

    /**
     * Gets the color for a team
     *
     * @param team The team
     * @return The color for the team
     */
    private ChatColor getTeamColor(Teams team) {
        switch (team) {
            case Prisoners:
                return ChatColor.RED;
            case Guards:
                return ChatColor.BLUE;
            case Spectators:
                return ChatColor.GRAY;
            default:
                return ChatColor.WHITE;
        }
    }
}
