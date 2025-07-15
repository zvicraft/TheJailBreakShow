package com.zvicraft.theJailBreakShow.Rounds;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * Handles the Last Round (LR) system functionality
 * This system provides rewards for prisoners who kill guards
 */
public class LrSystems {
    private static final int PRISONER_KILLS_GUARD_REWARD = 50;
    private static final int BONUS_REWARD_PER_KILL = 10; // Additional reward per previous kill
    private static final int MAX_BONUS_REWARD = 50; // Maximum bonus reward

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
     * @param killer The player who killed the guard
     * @param killerKills The number of kills the prisoner already has
     * @return The total reward amount
     */
    public static int handlePrisonerKillReward(Player killer, int killerKills) {
        int reward = rewardPrisonerKill(killerKills);

        // Send message to the killer
        String message = ChatColor.GOLD + "You received " + reward + " gold for killing a guard!";
        if (killerKills > 0) {
            message += ChatColor.YELLOW + " (Includes bonus for " + killerKills + " previous kills)";
        }
        killer.sendMessage(message);

        return reward;
    }
}
