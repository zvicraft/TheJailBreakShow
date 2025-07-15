package com.zvicraft.theJailBreakShow.Rounds;

import com.zvicraft.theJailBreakShow.Teams.Teams;
import com.zvicraft.theJailBreakShow.Teams.teamsManagers;
import com.zvicraft.theJailBreakShow.TheJailBreakShow;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GuardChallengeManager {
    private static final int MIN_PLAYERS = 2;
    private static final Random random = new Random();
    private final TheJailBreakShow plugin;
    private boolean isChallengeActive = false;


    public GuardChallengeManager(TheJailBreakShow plugin) {
        this.plugin = plugin;
    }

    public void startGuardChallenge() {
        if (!RoundsSystems.isGameActive()) {
            return;
        }

        int onlinePlayers = Bukkit.getOnlinePlayers().size();
        if (onlinePlayers < MIN_PLAYERS) {
            return; // Silently return without message to avoid spam
        }

        if (isChallengeActive) {
            return;
        }

        // Rest of your existing code for selecting and converting a prisoner to guard
        List<Player> prisoners = getEligiblePrisoners();
        isChallengeActive = true;
        Player selectedPrisoner = prisoners.get(random.nextInt(prisoners.size()));


        // Announce the challenge
        Bukkit.broadcastMessage(ChatColor.GOLD + "===================");
        Bukkit.broadcastMessage(ChatColor.GREEN + "Guard Challenge has begun!");
        Bukkit.broadcastMessage(ChatColor.YELLOW + "Player " + selectedPrisoner.getName() + " has been chosen to become a Guard!");
        Bukkit.broadcastMessage(ChatColor.GOLD + "===================");

        // Change the player's team
        teamsManagers.setPlayerTeam(selectedPrisoner, Teams.Guards);

        // Reset challenge status after a delay
        new BukkitRunnable() {
            @Override
            public void run() {
                isChallengeActive = false;
            }
        }.runTaskLater(plugin, 100);
    }

    private List<Player> getEligiblePrisoners() {
        List<Player> eligiblePrisoners = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (teamsManagers.getPlayerTeam(player) == Teams.Prisoners) {
                eligiblePrisoners.add(player);
            }
        }
        return eligiblePrisoners;
    }


    public boolean isChallengeActive() {
        return isChallengeActive;
    }
}