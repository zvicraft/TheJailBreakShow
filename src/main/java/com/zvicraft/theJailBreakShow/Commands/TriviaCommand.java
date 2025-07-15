package com.zvicraft.theJailBreakShow.Commands;

import com.zvicraft.theJailBreakShow.Rounds.RoundsSystems;
import com.zvicraft.theJailBreakShow.TheJailBreakShow;
import com.zvicraft.theJailBreakShow.Trivia.TriviaManager;
import com.zvicraft.theJailBreakShow.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Command handler for trivia commands
 */
public class TriviaCommand implements CommandExecutor, TabCompleter {
    private final TheJailBreakShow plugin;
    private final TriviaManager triviaManager;

    /**
     * Creates a new TriviaCommand instance
     *
     * @param plugin The plugin instance
     * @param triviaManager The trivia manager instance
     */
    public TriviaCommand(TheJailBreakShow plugin, TriviaManager triviaManager) {
        this.plugin = plugin;
        this.triviaManager = triviaManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check if game is active for certain operations
        if (!RoundsSystems.isGameActive()) {
            MessageUtils.sendMessage(sender, "general.game_not_active");
            return true;
        }

        // Show help if no args
        if (args.length == 0) {
            showHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "start":
                // Check permission
                if (!MessageUtils.hasPermission(sender, "thejailbreakshow.trivia.start")) {
                    return true;
                }

                // Start a new trivia
                if (triviaManager.isTriviaActive()) {
                    MessageUtils.sendMessage(sender, "trivia.already_active");
                    return true;
                }

                if (triviaManager.startTrivia()) {
                    MessageUtils.sendMessage(sender, "trivia.started");
                } else {
                    MessageUtils.sendMessage(sender, "trivia.error_starting");
                }
                break;

            case "cancel":
                // Check permission
                if (!MessageUtils.hasPermission(sender, "thejailbreakshow.trivia.cancel")) {
                    return true;
                }

                // Cancel current trivia
                if (!triviaManager.isTriviaActive()) {
                    MessageUtils.sendMessage(sender, "trivia.not_active");
                    return true;
                }

                triviaManager.endTrivia(null);
                MessageUtils.sendMessage(sender, "trivia.cancelled");
                break;

            case "answer":
                // Must be a player to answer
                if (!(sender instanceof Player)) {
                    MessageUtils.sendMessage(sender, "general.player_only");
                    return true;
                }

                // Check if trivia is active
                if (!triviaManager.isTriviaActive()) {
                    MessageUtils.sendMessage(sender, "trivia.not_active");
                    return true;
                }

                // Need an answer
                if (args.length < 2) {
                    MessageUtils.sendMessage(sender, "trivia.provide_answer");
                    return true;
                }

                // Combine all remaining arguments as the answer
                StringBuilder answerBuilder = new StringBuilder(args[1]);
                for (int i = 2; i < args.length; i++) {
                    answerBuilder.append(" ").append(args[i]);
                }
                String answer = answerBuilder.toString();

                // Check the answer
                if (triviaManager.checkAnswer((Player) sender, answer)) {
                    // The trivia manager will handle rewards and messages
                } else {
                    MessageUtils.sendMessage(sender, "trivia.incorrect_answer");
                }
                break;

            case "reload":
                // Check permission
                if (!MessageUtils.hasPermission(sender, "thejailbreakshow.trivia.reload")) {
                    return true;
                }

                // Reload trivia questions
                plugin.reloadPlugin();
                MessageUtils.sendMessage(sender, "trivia.reloaded");
                break;

            default:
                showHelp(sender);
                break;
        }

        return true;
    }

    /**
     * Shows command help to the sender
     *
     * @param sender The command sender
     */
    private void showHelp(CommandSender sender) {
        MessageUtils.sendMessage(sender, "trivia.help.header");

        // Show all commands if has admin permission
        if (sender.hasPermission("thejailbreakshow.trivia.start")) {
            MessageUtils.sendMessage(sender, "trivia.help.start");
            MessageUtils.sendMessage(sender, "trivia.help.cancel");
            MessageUtils.sendMessage(sender, "trivia.help.reload");
        }

        // Always show answer command
        MessageUtils.sendMessage(sender, "trivia.help.answer");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // First argument - show subcommands based on permissions
            if (sender.hasPermission("thejailbreakshow.trivia.start")) {
                completions.add("start");
            }
            if (sender.hasPermission("thejailbreakshow.trivia.cancel")) {
                completions.add("cancel");
            }
            if (sender.hasPermission("thejailbreakshow.trivia.reload")) {
                completions.add("reload");
            }
            completions.add("answer");
        }

        return completions;
    }
}
