package com.zvicraft.theJailBreakShow.Listeners;

import com.zvicraft.theJailBreakShow.Teams.teamsManagers;
import com.zvicraft.theJailBreakShow.TheJailBreakShow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

/**
 * Listener for chat challenge answers
 */
public class ChatChallengeListener implements Listener {
    private final TheJailBreakShow plugin;

    /**
     * Creates a new ChatChallengeListener
     *
     * @param plugin The plugin instance
     */
    public ChatChallengeListener(TheJailBreakShow plugin) {
        this.plugin = plugin;
    }

    /**
     * Handles player chat events to check for chat challenge answers
     *
     * @param event The chat event
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();

        // Check answer on the main thread
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            teamsManagers.checkChatChallengeAnswer(player, message);
        });
    }
}
