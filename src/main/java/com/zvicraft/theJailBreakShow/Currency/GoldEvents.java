package com.zvicraft.theJailBreakShow.Currency;

import com.zvicraft.theJailBreakShow.TheJailBreakShow;
import com.zvicraft.theJailBreakShow.Teams.Teams;
import com.zvicraft.theJailBreakShow.Teams.teamsManagers;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class GoldEvents implements Listener {
    private final TheJailBreakShow plugin;
    private final GoldManager goldManager;
    
    // Gold reward amounts
    private static final int PRISONER_KILLS_GUARD_REWARD = 50;
    private static final int GUARD_KILLS_PRISONER_REWARD = 10;
    
    public GoldEvents(TheJailBreakShow plugin, GoldManager goldManager) {
        this.plugin = plugin;
        this.goldManager = goldManager;
    }
    
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
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
            goldManager.addGold(killer, PRISONER_KILLS_GUARD_REWARD);
            killer.sendMessage(ChatColor.GOLD + "You received " + PRISONER_KILLS_GUARD_REWARD + " gold for killing a guard!");
        }
        
        // Guard kills Prisoner
        if (killerTeam == Teams.Guards && victimTeam == Teams.Prisoners) {
            goldManager.addGold(killer, GUARD_KILLS_PRISONER_REWARD);
            killer.sendMessage(ChatColor.GOLD + "You received " + GUARD_KILLS_PRISONER_REWARD + " gold for killing a prisoner!");
        }
    }
}