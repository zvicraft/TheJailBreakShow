package com.zvicraft.theJailBreakShow.Commands;

import com.zvicraft.theJailBreakShow.Currency.GoldManager;
import com.zvicraft.theJailBreakShow.GUI.GUIManager;
import com.zvicraft.theJailBreakShow.TheJailBreakShow;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GoldCommand implements CommandExecutor, TabCompleter {
    private final TheJailBreakShow plugin;

    public GoldCommand(TheJailBreakShow plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // Suggest subcommands
            List<String> subcommands = new ArrayList<>();
            subcommands.add("balance");

            // Only add admin commands if player has permission
            if (sender.hasPermission("thejailbreakshow.gold.give")) {
                subcommands.add("give");
            }
            if (sender.hasPermission("thejailbreakshow.gold.take")) {
                subcommands.add("take");
            }

            return filterCompletions(subcommands, args[0]);
        } else if (args.length == 2) {
            // For balance, give, and take subcommands, suggest player names
            if (args[0].equalsIgnoreCase("balance") && sender.hasPermission("thejailbreakshow.gold.balance.others") ||
                args[0].equalsIgnoreCase("give") && sender.hasPermission("thejailbreakshow.gold.give") ||
                args[0].equalsIgnoreCase("take") && sender.hasPermission("thejailbreakshow.gold.take")) {

                return getOnlinePlayerNames(args[1]);
            }
        } else if (args.length == 3) {
            // For give and take subcommands, suggest some common amounts
            if ((args[0].equalsIgnoreCase("give") && sender.hasPermission("thejailbreakshow.gold.give")) ||
                (args[0].equalsIgnoreCase("take") && sender.hasPermission("thejailbreakshow.gold.take"))) {

                List<String> amounts = Arrays.asList("10", "50", "100", "500", "1000");
                return filterCompletions(amounts, args[2]);
            }
        }

        return completions;
    }

    private List<String> getOnlinePlayerNames(String input) {
        List<String> playerNames = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            playerNames.add(player.getName());
        }
        return filterCompletions(playerNames, input);
    }

    private List<String> filterCompletions(List<String> completions, String input) {
        if (input.isEmpty()) {
            return completions;
        }

        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(input.toLowerCase()))
                .collect(Collectors.toList());
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            // Open gold GUI if sender is a player
            if (sender instanceof Player) {
                Player player = (Player) sender;
                GUIManager.openGoldGUI(player);
            } else {
                // Show help message for console
                sender.sendMessage(ChatColor.GOLD + "=== Gold Commands ===");
                sender.sendMessage(ChatColor.YELLOW + "/gold balance [player]" + ChatColor.WHITE + " - Check your or another player's gold balance");
                sender.sendMessage(ChatColor.YELLOW + "/gold give <player> <amount>" + ChatColor.WHITE + " - Give gold to a player (Admin only)");
                sender.sendMessage(ChatColor.YELLOW + "/gold take <player> <amount>" + ChatColor.WHITE + " - Take gold from a player (Admin only)");
            }
            return true;
        }

        GoldManager goldManager = plugin.getGoldManager();

        switch (args[0].toLowerCase()) {
            case "balance":
                // Check balance
                if (args.length == 1) {
                    // Check own balance
                    if (!(sender instanceof Player)) {
                        sender.sendMessage(ChatColor.RED + "Console cannot check gold balance!");
                        return true;
                    }

                    Player player = (Player) sender;
                    int gold = goldManager.getGold(player);
                    player.sendMessage(ChatColor.GOLD + "Your gold balance: " + ChatColor.YELLOW + gold);
                } else {
                    // Check another player's balance
                    if (!sender.hasPermission("thejailbreakshow.gold.balance.others")) {
                        sender.sendMessage(ChatColor.RED + "You don't have permission to check other players' gold balance!");
                        return true;
                    }

                    Player target = Bukkit.getPlayer(args[1]);
                    if (target == null) {
                        sender.sendMessage(ChatColor.RED + "Player not found!");
                        return true;
                    }

                    int gold = goldManager.getGold(target);
                    sender.sendMessage(ChatColor.GOLD + target.getName() + "'s gold balance: " + ChatColor.YELLOW + gold);
                }
                break;

            case "give":
                // Give gold to a player
                if (!sender.hasPermission("thejailbreakshow.gold.give")) {
                    sender.sendMessage(ChatColor.RED + "You don't have permission to give gold!");
                    return true;
                }

                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Usage: /gold give <player> <amount>");
                    return true;
                }

                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage(ChatColor.RED + "Player not found!");
                    return true;
                }

                try {
                    int amount = Integer.parseInt(args[2]);
                    if (amount <= 0) {
                        sender.sendMessage(ChatColor.RED + "Amount must be positive!");
                        return true;
                    }

                    goldManager.addGold(target, amount);
                    sender.sendMessage(ChatColor.GREEN + "Gave " + amount + " gold to " + target.getName());
                    target.sendMessage(ChatColor.GREEN + "You received " + amount + " gold from " + sender.getName());
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "Invalid amount!");
                }
                break;

            case "take":
                // Take gold from a player
                if (!sender.hasPermission("thejailbreakshow.gold.take")) {
                    sender.sendMessage(ChatColor.RED + "You don't have permission to take gold!");
                    return true;
                }

                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Usage: /gold take <player> <amount>");
                    return true;
                }

                target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage(ChatColor.RED + "Player not found!");
                    return true;
                }

                try {
                    int amount = Integer.parseInt(args[2]);
                    if (amount <= 0) {
                        sender.sendMessage(ChatColor.RED + "Amount must be positive!");
                        return true;
                    }

                    if (goldManager.removeGold(target, amount)) {
                        sender.sendMessage(ChatColor.GREEN + "Took " + amount + " gold from " + target.getName());
                        target.sendMessage(ChatColor.RED + "You lost " + amount + " gold");
                    } else {
                        sender.sendMessage(ChatColor.RED + target.getName() + " doesn't have enough gold!");
                    }
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "Invalid amount!");
                }
                break;

            default:
                sender.sendMessage(ChatColor.RED + "Unknown subcommand: " + args[0]);
                break;
        }

        return true;
    }
}
