package com.zvicraft.theJailBreakShow.Commands;

import com.zvicraft.theJailBreakShow.Rounds.RoundsSystems;
import com.zvicraft.theJailBreakShow.TheJailBreakShow;
import com.zvicraft.theJailBreakShow.utils.FadeToBlack;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TestCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (commandSender.hasPermission("thejailbreakshow.test")) {
            if (strings.length > 0 && strings[0].equalsIgnoreCase("fade")) {
                // Test just the fade effect without ending the round
                if (commandSender instanceof Player) {
                    commandSender.sendMessage("Testing fade to black effect...");
                    FadeToBlack.fade(TheJailBreakShow.getInstance());
                }
            } else if (strings.length > 0 && strings[0].equalsIgnoreCase("round")) {
                // Test ending the round (includes fade effect)
                commandSender.sendMessage("Testing round end...");
                RoundsSystems.endRound();
            } else if (strings.length > 0 && strings[0].equalsIgnoreCase("start")) {
                // Test starting a new round
                commandSender.sendMessage("Starting a new round...");
                RoundsSystems.startRound();
            } else {
                // Default test message
                commandSender.sendMessage("Test command ran! Use /test fade to test fade effect, /test round to test round ending, or /test start to start a new round.");
            }
            return true;
        }
        return false;
    }
}
