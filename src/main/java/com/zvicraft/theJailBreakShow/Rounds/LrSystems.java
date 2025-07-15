package com.zvicraft.theJailBreakShow.Rounds;

import com.zvicraft.theJailBreakShow.Teams.Teams;
import com.zvicraft.theJailBreakShow.Teams.teamsManagers;
import com.zvicraft.theJailBreakShow.TheJailBreakShow;
import com.zvicraft.theJailBreakShow.utils.LanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Handles the Last Round (LR) system functionality
 * This system provides rewards for prisoners who kill guards
 */
public class LrSystems {
    private static final int PRISONER_KILLS_GUARD_REWARD = 50;
    private static final int BONUS_REWARD_PER_KILL = 10; // Additional reward per previous kill
    private static final int MAX_BONUS_REWARD = 50; // Maximum bonus reward

    private static boolean isLRActive = false;
    private static final List<UUID> playersWithLR = new ArrayList<>();

    /**
     * Calculates the reward for a prisoner killing a guard
     * The reward increases based on the number of previous kills
     *
     * @param killerKills The number of kills the prisoner already has
     * @return The total reward amount
     */
    public static int rewardPrisonerKill(int killerKills) {
        // Base reward
        int reward = PRISONER_KILLS_GUARD_REWARD;

        // Calculate bonus reward based on previous kills
        int bonusReward = Math.min(killerKills * BONUS_REWARD_PER_KILL, MAX_BONUS_REWARD);

        // Return total reward
        return reward + bonusReward;
    }

    /**
     * Handles the reward process for a prisoner killing a guard
     *
     * @param killer      The player who killed the guard
     * @param killerKills The number of kills the prisoner already has
     * @return The total reward amount
     */
    public static int handlePrisonerKillReward(Player killer, int killerKills) {
        int reward = rewardPrisonerKill(killerKills);

        // Get language manager
        LanguageManager lang = TheJailBreakShow.getInstance().getLanguageManager();

        // Send message to the killer
        String message = lang.getMessage("lr.reward", "%amount%", String.valueOf(reward));
        if (killerKills > 0) {
            message += lang.getMessage("lr.reward_bonus", "%kills%", String.valueOf(killerKills));
        }
        killer.sendMessage(message);

        return reward;
    }

    /**
     * Activates LR for the entire game
     */
    public static void activateLR() {
        isLRActive = true;

        // Grant LR to all prisoners
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (teamsManagers.isPlayerInPrisonerTeam(player)) {
                grantLRStatus(player);
            }
        }

        // Get language manager instance
        TheJailBreakShow plugin = TheJailBreakShow.getInstance();
        LanguageManager lang = plugin.getLanguageManager();

        // Broadcast message
        Bukkit.broadcastMessage(lang.getMessage("lr.header"));
        Bukkit.broadcastMessage(lang.getMessage("lr.activated"));
    }

    /**
     * Deactivates LR for the entire game
     */
    public static void deactivateLR() {
        isLRActive = false;
        playersWithLR.clear();

        // Remove LR effects from all players
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (teamsManagers.isPlayerInPrisonerTeam(player)) {
                player.removePotionEffect(PotionEffectType.SPEED);
            }
        }

        // Get message from language file
        LanguageManager lang = TheJailBreakShow.getInstance().getLanguageManager();
        Bukkit.broadcastMessage(lang.getMessage("lr.deactivated"));
    }

    /**
     * Grants LR status to a specific player
     *
     * @param player The player to grant LR status to
     */
    public static void grantLRStatus(Player player) {
        if (!playersWithLR.contains(player.getUniqueId())) {
            playersWithLR.add(player.getUniqueId());

            // Apply LR effects - for example, a small speed boost
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0, false, false));

            // Notify the player with message from language file
            LanguageManager lang = TheJailBreakShow.getInstance().getLanguageManager();
            player.sendMessage(lang.getMessage("lr.granted"));
        }
    }

    /**
     * Removes LR status from a specific player
     *
     * @param player The player to remove LR status from
     */
    public static void revokeLRStatus(Player player) {
        if (playersWithLR.contains(player.getUniqueId())) {
            playersWithLR.remove(player.getUniqueId());

            // Remove LR effects
            player.removePotionEffect(PotionEffectType.SPEED);

            // Notify the player with message from language file
            LanguageManager lang = TheJailBreakShow.getInstance().getLanguageManager();
            player.sendMessage(lang.getMessage("lr.revoked"));
        }
    }

    /**
     * Checks if a player has LR status
     *
     * @param player The player to check
     * @return True if the player has LR status, false otherwise
     */
    public static boolean hasLRStatus(Player player) {
        return isLRActive || playersWithLR.contains(player.getUniqueId());
    }

    /**
     * Checks if LR is active for the entire game
     *
     * @return True if LR is active, false otherwise
     */
    public static boolean isLRActive() {
        return isLRActive;
    }

    /**
     * Resets all LR statuses
     */
    public static void resetLRStatus() {
        isLRActive = false;
        playersWithLR.clear();
    }
}
