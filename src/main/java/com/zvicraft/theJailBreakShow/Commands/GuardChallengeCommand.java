package com.zvicraft.theJailBreakShow.Commands;

import com.zvicraft.theJailBreakShow.TheJailBreakShow;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class GuardChallengeCommand implements CommandExecutor {
    private final TheJailBreakShow plugin;

    public GuardChallengeCommand(TheJailBreakShow plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        plugin.getGuardChallengeManager().startGuardChallenge();
        return true;
    }
}