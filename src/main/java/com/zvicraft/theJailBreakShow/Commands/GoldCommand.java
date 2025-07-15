package com.zvicraft.theJailBreakShow.Commands;

import com.zvicraft.theJailBreakShow.Currency.GoldManager;
import com.zvicraft.theJailBreakShow.GUI.GUIManager;
import com.zvicraft.theJailBreakShow.TheJailBreakShow;
import com.zvicraft.theJailBreakShow.utils.LanguageManager;
import com.zvicraft.theJailBreakShow.utils.MessageUtils;
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
        LanguageManager lang = plugin.getLanguageManager();

        if (args.length == 0) {
            // Open gold GUI if sender is a player
            if (sender instanceof Player) {
                Player player = (Player) sender;
                GUIManager.openGoldGUI(player);
            } else {
                // Show help message for console
                sender.sendMessage(ChatColor.GOLD + "=== " + lang.getMessage("gold.commands_title") + " ===");
                sender.sendMessage(ChatColor.YELLOW + "/gold balance [player]" + ChatColor.WHITE + " - " + lang.getMessage("gold.command_balance_desc"));
                sender.sendMessage(ChatColor.YELLOW + "/gold give <player> <amount>" + ChatColor.WHITE + " - " + lang.getMessage("gold.command_give_desc"));
                sender.sendMessage(ChatColor.YELLOW + "/gold take <player> <amount>" + ChatColor.WHITE + " - " + lang.getMessage("gold.command_take_desc"));
            }
            return true;
        }

        GoldManager goldManager = plugin.getGoldManager();

        switch (args[0].toLowerCase()) {
            case "balance":
                // Check balance
                if (args.length == 1) {
                    // Check own balance
                    if (!MessageUtils.isPlayer(sender)) {
                        return true;
                    }

                    Player player = (Player) sender;
                    int gold = goldManager.getGold(player);
                    player.sendMessage(ChatColor.GOLD + lang.getMessage("gold.balance", "%amount%", String.valueOf(gold)));
                } else {
                    // Check another player's balance
                    if (!sender.hasPermission("thejailbreakshow.gold.balance.others")) {
                        sender.sendMessage(ChatColor.RED + lang.getMessage("general.no_permission"));
                        return true;
                    }

                    Player target = Bukkit.getPlayer(args[1]);
                    if (target == null) {
                        sender.sendMessage(ChatColor.RED + lang.getMessage("general.player_not_found"));
                        return true;
                    }

                    // Make sure the player exists in the gold manager
                    if (!goldManager.hasPlayer(target.getUniqueId())) {
                        goldManager.setGold(target, plugin.getConfigManager().getConfig().getInt("currency.initial-gold", 100));
                    }

                    int gold = goldManager.getGold(target);
                    sender.sendMessage(ChatColor.GOLD + lang.getMessage("gold.balance_other",
                            "%player%", target.getName(),
                            "%amount%", String.valueOf(gold)));
                }
                break;

            case "give":
                // Give gold to a player
                if (!MessageUtils.hasPermission(sender, "thejailbreakshow.gold.give")) {
                    return true;
                }

                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + lang.getMessage("gold.usage_give"));
                    return true;
                }

                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage(ChatColor.RED + lang.getMessage("general.player_not_found"));
                    return true;
                }

                try {
                    int amount = Integer.parseInt(args[2]);
                    if (amount <= 0) {
                        sender.sendMessage(ChatColor.RED + lang.getMessage("general.amount_must_be_positive"));
                        return true;
                    }

                    goldManager.addGold(target, amount);
                    sender.sendMessage(ChatColor.GREEN + lang.getMessage("gold.gave",
                            "%amount%", String.valueOf(amount),
                            "%player%", target.getName()));
                    target.sendMessage(ChatColor.GREEN + lang.getMessage("gold.received_from",
                            "%amount%", String.valueOf(amount),
                            "%player%", sender.getName()));
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + lang.getMessage("general.invalid_amount"));
                }
                break;

            case "take":
                // Take gold from a player
                if (!MessageUtils.hasPermission(sender, "thejailbreakshow.gold.take")) {
                    return true;
                }

                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + lang.getMessage("gold.usage_take"));
                    return true;
                }

                target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage(ChatColor.RED + lang.getMessage("general.player_not_found"));
                    return true;
                }

                try {
                    int amount = Integer.parseInt(args[2]);
                    if (amount <= 0) {
                        sender.sendMessage(ChatColor.RED + lang.getMessage("general.amount_must_be_positive"));
                        return true;
                    }

                    if (goldManager.removeGold(target, amount)) {
                        sender.sendMessage(ChatColor.GREEN + lang.getMessage("gold.took",
                                "%amount%", String.valueOf(amount),
                                "%player%", target.getName()));
                        target.sendMessage(ChatColor.RED + lang.getMessage("gold.lost",
                                "%amount%", String.valueOf(amount)));
                    } else {
                        sender.sendMessage(ChatColor.RED + lang.getMessage("reload.not_enough_gold",
                                "%player%", target.getName()));
                    }
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + lang.getMessage("general.invalid_amount"));
                }
                break;

            default:
                sender.sendMessage(ChatColor.RED + "Unknown subcommand: " + args[0]);
                break;
        }

        return true;
    }
}
