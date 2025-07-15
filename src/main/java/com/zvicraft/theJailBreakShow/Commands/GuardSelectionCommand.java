package com.zvicraft.theJailBreakShow.Commands;

import com.zvicraft.theJailBreakShow.GUI.GuardSelectionGUI;
import com.zvicraft.theJailBreakShow.TheJailBreakShow;
import com.zvicraft.theJailBreakShow.utils.LanguageManager;
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

public class GuardSelectionCommand implements CommandExecutor, TabCompleter {
    private final TheJailBreakShow plugin;

    public GuardSelectionCommand(TheJailBreakShow plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        LanguageManager lang = plugin.getLanguageManager();

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + lang.getMessage("reload.player_only"));
            return true;
        }

        Player player = (Player) sender;

        // Check if player has permission
        if (!player.isOp() && !player.hasPermission("jailbreakshow.admin")) {
            player.sendMessage(ChatColor.RED + lang.getMessage("general.no_permission"));
            return true;
        }

        // Process arguments if provided
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("help")) {
                // Show help message
                sendHelpMessage(player);
                return true;
            }
            // Allow forcing the GUI to open for admins with the 'force' argument
            if (args[0].equalsIgnoreCase("force") && (player.isOp() || player.hasPermission("jailbreakshow.admin"))) {
                GuardSelectionGUI.openGuardSelectionGUI(player, plugin);
                return true;
            }
        }

        // Check if game is active before allowing command
        if (!plugin.isGameActive()) {
            player.sendMessage(ChatColor.RED + lang.getMessage("general.game_not_active"));
            return true;
        }

        // Open guard selection GUI
        GuardSelectionGUI.openGuardSelectionGUI(player, plugin);
        return true;
    }

    /**
     * Send help message to player
     *
     * @param player The player to send the help message to
     */
    private void sendHelpMessage(Player player) {
        player.sendMessage(ChatColor.GOLD + "==== Guard Selection Commands ====");
        player.sendMessage(ChatColor.YELLOW + "/selectguard" + ChatColor.WHITE + " - Open guard selection GUI");
        player.sendMessage(ChatColor.YELLOW + "/selectguard help" + ChatColor.WHITE + " - Show this help message");

        // Show admin commands only to players with permission
        if (player.isOp() || player.hasPermission("jailbreakshow.admin")) {
            player.sendMessage(ChatColor.YELLOW + "/selectguard force" + ChatColor.WHITE + " - Force open guard selection GUI even if game is inactive");
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // First argument suggestions
            List<String> options = new ArrayList<>(Arrays.asList("help"));

            // Add force option for admins only
            if (sender instanceof Player &&
                    (((Player) sender).isOp() || ((Player) sender).hasPermission("jailbreakshow.admin"))) {
                options.add("force");
            }

            return filterCompletions(options, args[0]);
        }

        return completions;
    }

    /**
     * Filter completions based on input
     *
     * @param completions List of possible completions
     * @param input       Current input to filter against
     * @return Filtered list of completions
     */
    private List<String> filterCompletions(List<String> completions, String input) {
        if (input.isEmpty()) {
            return completions;
        }

        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(input.toLowerCase()))
                .collect(Collectors.toList());
    }
}
