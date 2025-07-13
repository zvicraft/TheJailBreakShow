package com.zvicraft.theJailBreakShow.Commands;

import com.zvicraft.theJailBreakShow.TheJailBreakShow;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class ReloadCommand implements CommandExecutor {
    private final TheJailBreakShow plugin;

    public ReloadCommand(TheJailBreakShow plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("thejailbreakshow.reload")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        sender.sendMessage(ChatColor.YELLOW + "Reloading TheJailBreakShow configuration...");
        plugin.reloadPlugin();
        sender.sendMessage(ChatColor.GREEN + "Configuration reloaded successfully!");
        
        return true;
    }
}