package com.zvicraft.theJailBreakShow.Commands;

import com.zvicraft.theJailBreakShow.GUI.GuardSelectionGUI;
import com.zvicraft.theJailBreakShow.TheJailBreakShow;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GuardSelectionCommand implements CommandExecutor {
    private final TheJailBreakShow plugin;

    public GuardSelectionCommand(TheJailBreakShow plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;

        // Check if player has permission
        if (!player.isOp() && !player.hasPermission("jailbreakshow.admin")) {
            player.sendMessage(ChatColor.RED + "אין לך הרשאה לבצע פעולה זו!");
            return true;
        }

        // Open guard selection GUI
        GuardSelectionGUI.openGuardSelectionGUI(player, plugin);
        return true;
    }
}
