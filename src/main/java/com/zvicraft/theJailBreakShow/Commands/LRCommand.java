package com.zvicraft.theJailBreakShow.Commands;

import com.zvicraft.theJailBreakShow.Rounds.LrSystems;
import com.zvicraft.theJailBreakShow.Rounds.RoundsSystems;
import com.zvicraft.theJailBreakShow.TheJailBreakShow;
import com.zvicraft.theJailBreakShow.Teams.Teams;
import com.zvicraft.theJailBreakShow.Teams.teamsManagers;
import com.zvicraft.theJailBreakShow.utils.LanguageManager;
import com.zvicraft.theJailBreakShow.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LRCommand implements CommandExecutor {
    private final TheJailBreakShow plugin;

    public LRCommand(TheJailBreakShow plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        LanguageManager lang = plugin.getLanguageManager();

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + lang.getMessage("general.player_only"));
            return true;
        }

        Player player = (Player) sender;

        // Check if player has permission or is in guard team
        if (!player.isOp() && !player.hasPermission("jailbreakshow.lr") &&
                !teamsManagers.isPlayerInGuardTeam(player)) {
            player.sendMessage(ChatColor.RED + lang.getMessage("general.no_permission"));
            return true;
        }

        // If there are arguments, it's for a specific prisoner
        if (args.length > 0) {
            String targetName = args[0];
            Player target = Bukkit.getPlayer(targetName);

            if (target == null || !target.isOnline()) {
                player.sendMessage(ChatColor.RED + lang.getMessage("lr.player_not_online", "%player%", targetName));
                return true;
            }

            if (!teamsManagers.isPlayerInPrisonerTeam(target)) {
                player.sendMessage(ChatColor.RED + lang.getMessage("lr.not_prisoner"));
                return true;
            }

            // Give the target LR status
            giveLRStatus(target);
            player.sendMessage(ChatColor.GREEN + lang.getMessage("lr.given", "%player%", target.getName()));
            return true;
        }

        // No arguments - announce LR for all remaining prisoners
        announceLR();
        return true;
    }

    /**
     * Announces Last Round (LR) to all players
     */
    private void announceLR() {
        LanguageManager lang = plugin.getLanguageManager();

        // Activate LR using the LrSystems
        com.zvicraft.theJailBreakShow.Rounds.LrSystems.activateLR();

        // Count remaining prisoners
        int prisonerCount = 0;
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (teamsManagers.isPlayerInPrisonerTeam(p)) {
                prisonerCount++;
            }
        }

        if (prisonerCount == 0) {
            Bukkit.broadcastMessage(ChatColor.YELLOW + lang.getMessage("lr.no_prisoners"));
        } else {
            Bukkit.broadcastMessage(ChatColor.YELLOW + lang.getMessage("lr.prisoner_count", "%count%", String.valueOf(prisonerCount)));
        }
    }

    /**
     * Gives LR status to a specific prisoner
     *
     * @param prisoner The prisoner to give LR status to
     */
    private void giveLRStatus(Player prisoner) {
        LanguageManager lang = plugin.getLanguageManager();

        // Use LrSystems to grant LR status
        com.zvicraft.theJailBreakShow.Rounds.LrSystems.grantLRStatus(prisoner);

        // Broadcast announcement
        Bukkit.broadcastMessage(ChatColor.GOLD + lang.getMessage("lr.player_granted", "%player%", prisoner.getName()));
    }
}
