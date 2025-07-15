package com.zvicraft.theJailBreakShow.Commands;

import com.zvicraft.theJailBreakShow.TheJailBreakShow;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LanguageCommand implements CommandExecutor, TabCompleter {
    private final TheJailBreakShow plugin;

    public LanguageCommand(TheJailBreakShow plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("thejailbreakshow.language")) {
            sender.sendMessage(ChatColor.RED + plugin.getLanguageManager().getMessage("general.no_permission"));
            return true;
        }

        if (args.length == 0) {
            // Show current language
            String currentLang = plugin.getLanguageManager().getCurrentLanguage();
            String[] availableLangs = plugin.getLanguageManager().getAvailableLanguages();

            sender.sendMessage(ChatColor.GOLD + "=== " + plugin.getLanguageManager().getMessage("language.title") + " ===");
            sender.sendMessage(ChatColor.GOLD + plugin.getLanguageManager().getMessage("language.current") + ": " +
                    ChatColor.YELLOW + currentLang);
            sender.sendMessage(ChatColor.GOLD + plugin.getLanguageManager().getMessage("language.available") + ": " +
                    ChatColor.YELLOW + String.join(ChatColor.GRAY + ", " + ChatColor.YELLOW, availableLangs));
            sender.sendMessage(ChatColor.GOLD + plugin.getLanguageManager().getMessage("language.usage"));
            return true;
        }

        String langCode = args[0].toLowerCase();

        // Check if language is available
        if (!plugin.getLanguageManager().isLanguageAvailable(langCode)) {
            sender.sendMessage(ChatColor.RED + plugin.getLanguageManager().getMessage("language.not_available", "%lang%", langCode));
            return true;
        }

        // Change the language in config
        FileConfiguration config = plugin.getConfigManager().getConfig();
        config.set("language", langCode);
        plugin.getConfigManager().saveConfig();

        // Reload the language manager
        plugin.getLanguageManager().reload();

        // Confirm to sender
        sender.sendMessage(ChatColor.GREEN + plugin.getLanguageManager().getMessage("language.changed", "%lang%", langCode));

        // Broadcast the language change to all players if sender is op
        if (sender.isOp()) {
            String senderName = sender instanceof Player ? ((Player) sender).getDisplayName() : "Console";
            Bukkit.broadcastMessage(ChatColor.GOLD + plugin.getLanguageManager().getMessage("language.broadcast",
                    "%player%", senderName,
                    "%lang%", langCode));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            // Return available languages for tab completion
            String[] availableLangs = plugin.getLanguageManager().getAvailableLanguages();
            List<String> completions = new ArrayList<>(Arrays.asList(availableLangs));

            return completions.stream()
                    .filter(lang -> lang.toLowerCase().startsWith(args[0].toLowerCase()))
                    .toList();
        }
        return new ArrayList<>();
    }
}
