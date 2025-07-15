package com.zvicraft.theJailBreakShow.Commands;

import com.zvicraft.theJailBreakShow.TheJailBreakShow;
import com.zvicraft.theJailBreakShow.Teams.Teams;
import com.zvicraft.theJailBreakShow.Teams.teamsManagers;
import com.zvicraft.theJailBreakShow.utils.LanguageManager;
import com.zvicraft.theJailBreakShow.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EndGameCommand implements CommandExecutor {
    private final TheJailBreakShow plugin;
    private static final int MIN_PLAYERS_TO_END = 3;

    public EndGameCommand(TheJailBreakShow plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!MessageUtils.hasPermission(sender, "thejailbreakshow.endgame")) {
            return true;
        }

        // End the current game and set all players to spectator
        boolean gameEnded = teamsManagers.endGame();

        if (!gameEnded) {
            MessageUtils.sendMessage(sender, "endgame.min_players", "%min%", "3");
            MessageUtils.sendMessage(sender, "endgame.waiting");
            return true;
        }

        // Broadcast the end of the game
        MessageUtils.broadcast("endgame.header");
        MessageUtils.broadcast("endgame.ended");
        MessageUtils.broadcast("endgame.spectator_message");
        MessageUtils.broadcast("endgame.footer");

        return true;
    }
}
