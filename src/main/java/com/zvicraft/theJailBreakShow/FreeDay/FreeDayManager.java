package com.zvicraft.theJailBreakShow.FreeDay;

import com.zvicraft.theJailBreakShow.TheJailBreakShow;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class FreeDayManager {
    private static boolean isFreeDayActive = false;
    private static final int FREE_DAY_DURATION = 300; // 5 minutes (in seconds)
    
    public static void triggerFreeDay(Player initiator) {
        if (isFreeDayActive) {
            initiator.sendMessage(ChatColor.RED + "Free Day is already active!");
            return;
        }
        
        isFreeDayActive = true;
        
        // Announce free day to all players
        Bukkit.broadcastMessage(ChatColor.GREEN + "Free Day has been activated by " + initiator.getName() + "!");
        
        // Schedule end of free day
        Bukkit.getScheduler().runTaskLater(TheJailBreakShow.getInstance(), () -> {
            endFreeDay();
        }, FREE_DAY_DURATION * 20L); // Convert seconds to ticks (20 ticks = 1 second)
    }
    
    public static void triggerFreeDayAuto() {
        if (isFreeDayActive) {
            return;
        }
        
        isFreeDayActive = true;
        
        // Announce free day to all players
        Bukkit.broadcastMessage(ChatColor.GREEN + "Free Day has been automatically activated for this round!");
        
        // Schedule end of free day
        Bukkit.getScheduler().runTaskLater(TheJailBreakShow.getInstance(), () -> {
            endFreeDay();
        }, FREE_DAY_DURATION * 20L); // Convert seconds to ticks (20 ticks = 1 second)
    }
    
    public static void endFreeDay() {
        isFreeDayActive = false;
        Bukkit.broadcastMessage(ChatColor.RED + "Free Day has ended!");
    }
    
    public static boolean isFreeDayActive() {
        return isFreeDayActive;
    }
}