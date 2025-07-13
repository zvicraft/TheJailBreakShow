package com.zvicraft.theJailBreakShow.Commands;

import com.zvicraft.theJailBreakShow.GUI.GUIManager;
import com.zvicraft.theJailBreakShow.Teams.Teams;
import com.zvicraft.theJailBreakShow.Teams.teamsManagers;
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

public class TeamCommand implements CommandExecutor, TabCompleter {
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // Suggest subcommands
            List<String> subcommands = Arrays.asList("join", "leave", "list");
            return filterCompletions(subcommands, args[0]);
        } else if (args.length == 2 && args[0].equalsIgnoreCase("join")) {
            // Suggest team names for the join subcommand
            List<String> teams = Arrays.asList("prisoners", "guards", "spectators");
            return filterCompletions(teams, args[1]);
        }

        return completions;
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
            // Open team GUI if sender is a player
            if (sender instanceof Player) {
                Player player = (Player) sender;
                GUIManager.openTeamGUI(player);
            } else {
                // Show help message for console
                sender.sendMessage(ChatColor.GOLD + "=== Team Commands ===");
                sender.sendMessage(ChatColor.YELLOW + "/team join <prisoners|guards|spectators>" + ChatColor.WHITE + " - Join a team");
                sender.sendMessage(ChatColor.YELLOW + "/team leave" + ChatColor.WHITE + " - Leave your current team");
                sender.sendMessage(ChatColor.YELLOW + "/team list" + ChatColor.WHITE + " - List all teams and their members");
            }
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "join":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "Console cannot join teams!");
                    return true;
                }

                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /team join <prisoners|guards|spectators>");
                    return true;
                }

                Player player = (Player) sender;
                String teamName = args[1].toLowerCase();

                switch (teamName) {
                    case "prisoners":
                    case "prisoner":
                        teamsManagers.setPlayerTeam(player, Teams.Prisoners);
                        break;
                    case "guards":
                    case "guard":
                        // Check if there are already max guards
                        if (teamsManagers.getNumberOfGuards() >= teamsManagers.getMaxGuards() && 
                            teamsManagers.getPlayerTeam(player) != Teams.Guards) {
                            player.sendMessage(ChatColor.RED + "The guards team is full!");
                            return true;
                        }
                        teamsManagers.setPlayerTeam(player, Teams.Guards);
                        break;
                    case "spectators":
                    case "spectator":
                        teamsManagers.setPlayerTeam(player, Teams.Spectators);
                        break;
                    default:
                        player.sendMessage(ChatColor.RED + "Unknown team: " + teamName);
                        return true;
                }
                break;

            case "leave":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "Console cannot leave teams!");
                    return true;
                }

                Player player2 = (Player) sender;
                if (!teamsManagers.isPlayerInTeam(player2)) {
                    player2.sendMessage(ChatColor.RED + "You are not in a team!");
                    return true;
                }

                teamsManagers.setPlayerTeam(player2, Teams.Spectators);
                player2.sendMessage(ChatColor.YELLOW + "You left your team and are now a spectator.");
                break;

            case "list":
                sender.sendMessage(ChatColor.GOLD + "=== Teams ===");

                // Guards
                sender.sendMessage(ChatColor.BLUE + "Guards (" + teamsManagers.getNumberOfGuards() + "):");
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (teamsManagers.isPlayerInGuardTeam(p)) {
                        sender.sendMessage(ChatColor.BLUE + "- " + p.getName());
                    }
                }

                // Prisoners
                sender.sendMessage(ChatColor.RED + "Prisoners (" + teamsManagers.getNumberOfPrisoners() + "):");
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (teamsManagers.isPlayerInPrisonerTeam(p)) {
                        sender.sendMessage(ChatColor.RED + "- " + p.getName());
                    }
                }

                // Spectators
                sender.sendMessage(ChatColor.GRAY + "Spectators (" + teamsManagers.getNumberOfSpectators() + "):");
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (teamsManagers.isPlayerInSpectatorTeam(p)) {
                        sender.sendMessage(ChatColor.GRAY + "- " + p.getName());
                    }
                }
                break;

            default:
                sender.sendMessage(ChatColor.RED + "Unknown subcommand: " + args[0]);
                break;
        }

        return true;
    }
}
