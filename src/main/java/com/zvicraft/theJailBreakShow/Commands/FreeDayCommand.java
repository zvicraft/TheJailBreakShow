package com.zvicraft.theJailBreakShow.Commands;

import com.zvicraft.theJailBreakShow.FreeDay.FreeDayManager;
import com.zvicraft.theJailBreakShow.GUI.GUIManager;
import com.zvicraft.theJailBreakShow.Rounds.RoundsSystems;
import com.zvicraft.theJailBreakShow.Teams.Teams;
import com.zvicraft.theJailBreakShow.Teams.teamsManagers;
import com.zvicraft.theJailBreakShow.TheJailBreakShow;
import com.zvicraft.theJailBreakShow.utils.MessageUtils;
import com.zvicraft.theJailBreakShow.utils.LanguageManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class FreeDayCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;

        // Check if player is a guard
        if (teamsManagers.getPlayerTeam(player) != Teams.Guards) {
            player.sendMessage(ChatColor.RED + "Only guards can use this command!");
            return true;
        }

        // Check if player has permission
        if (!player.hasPermission("thejailbreakshow.freeday")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }

        // Open free day GUI
        GUIManager.openFreeDayGUI(player);
        return true;
    }
}
