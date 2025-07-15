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
            sender.sendMessage(ChatColor.RED + plugin.getLanguageManager().getMessage("general.no_permission"));
            return true;
        }

        sender.sendMessage(ChatColor.YELLOW + plugin.getLanguageManager().getMessage("reload.reloading"));
        plugin.reloadPlugin();
        sender.sendMessage(ChatColor.GREEN + plugin.getLanguageManager().getMessage("general.reload"));

        return true;
    }
}