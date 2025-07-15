package com.zvicraft.theJailBreakShow.Currency;

import com.zvicraft.theJailBreakShow.Rounds.LrSystems;
import com.zvicraft.theJailBreakShow.TheJailBreakShow;
import com.zvicraft.theJailBreakShow.Teams.Teams;
import com.zvicraft.theJailBreakShow.Teams.teamsManagers;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GoldEvents implements Listener {
    private final TheJailBreakShow plugin;
    private final GoldManager goldManager;

    // Gold reward amounts
    private static final int GUARD_KILLS_PRISONER_REWARD = 10;

    // Track kills for each player
    private final Map<UUID, Integer> playerKills = new HashMap<>();

    public GoldEvents(TheJailBreakShow plugin, GoldManager goldManager) {
        this.plugin = plugin;
        this.goldManager = goldManager;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();

        // Change the dead player to spectator
        if (teamsManagers.isPlayerInGuardTeam(victim) || teamsManagers.isPlayerInPrisonerTeam(victim)) {
            // Schedule the team change for next tick to avoid conflicts
            Bukkit.getScheduler().runTask(plugin, () -> {
                teamsManagers.setPlayerTeam(victim, Teams.Spectators);
                victim.sendMessage(ChatColor.GRAY + plugin.getLanguageManager().getMessage("death.spectator_message"));
            });
        }

        Player killer = victim.getKiller();

        // If there's no killer or the killer is not a player, return
        if (killer == null) {
            return;
        }

        Teams victimTeam = teamsManagers.getPlayerTeam(victim);
        Teams killerTeam = teamsManagers.getPlayerTeam(killer);

        // If either player is not in a team, return
        if (victimTeam == Teams.Unknown || killerTeam == Teams.Unknown) {
            return;
        }

        // Prisoner kills Guard
        if (killerTeam == Teams.Prisoners && victimTeam == Teams.Guards) {
            // Get current kill count for this prisoner
            int killerKills = playerKills.getOrDefault(killer.getUniqueId(), 0);

            // Calculate reward using LrSystems
            int reward = LrSystems.handlePrisonerKillReward(killer, killerKills);

            // Add gold to the player
            goldManager.addGold(killer, reward);

            // Increment kill count
            playerKills.put(killer.getUniqueId(), killerKills + 1);
        }

        // Guard kills Prisoner
        if (killerTeam == Teams.Guards && victimTeam == Teams.Prisoners) {
            goldManager.addGold(killer, GUARD_KILLS_PRISONER_REWARD);
            killer.sendMessage(ChatColor.GOLD + plugin.getLanguageManager().getMessage("gold.guard_kill_reward", 
                "%amount%", String.valueOf(GUARD_KILLS_PRISONER_REWARD)));
        }
    }

    /**
     * Gets the number of kills for a player
     *
     * @param player The player
     * @return The number of kills
     */
    public int getPlayerKills(Player player) {
        return playerKills.getOrDefault(player.getUniqueId(), 0);
    }

    /**
     * Resets kill counts for all players
     */
    public void resetKillCounts() {
        playerKills.clear();
    }
}
