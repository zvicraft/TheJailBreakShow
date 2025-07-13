package com.zvicraft.theJailBreakShow.FreeDay;

import com.zvicraft.theJailBreakShow.TheJailBreakShow;
import com.zvicraft.theJailBreakShow.Teams.Teams;
import com.zvicraft.theJailBreakShow.Teams.teamsManagers;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class FreeDayEvents implements Listener {
    private final TheJailBreakShow plugin;

    public FreeDayEvents(TheJailBreakShow plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        // If it's not Free Day, don't interfere
        if (!FreeDayManager.isFreeDayActive()) {
            return;
        }

        // Check if both entities are players
        if (!(event.getEntity() instanceof Player) || !(event.getDamager() instanceof Player)) {
            return;
        }

        Player victim = (Player) event.getEntity();
        Player attacker = (Player) event.getDamager();

        // Cancel damage during Free Day
        event.setCancelled(true);
        attacker.sendMessage(ChatColor.RED + "You cannot harm players during Free Day!");
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Additional Free Day-specific interactions can be handled here
        // For example, special Free Day activities or restrictions
        
    }
}