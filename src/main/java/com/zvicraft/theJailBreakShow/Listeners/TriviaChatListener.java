package com.zvicraft.theJailBreakShow.Listeners;

import com.zvicraft.theJailBreakShow.TheJailBreakShow;
import com.zvicraft.theJailBreakShow.Trivia.TriviaManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

/**
 * Listener for trivia answers in chat
 */
public class TriviaChatListener implements Listener {
    private final TheJailBreakShow plugin;
    private final TriviaManager triviaManager;

    /**
     * Creates a new TriviaChatListener
     *
     * @param plugin The plugin instance
     * @param triviaManager The trivia manager instance
     */
    public TriviaChatListener(TheJailBreakShow plugin, TriviaManager triviaManager) {
        this.plugin = plugin;
        this.triviaManager = triviaManager;
    }

    /**
     * Handles player chat events to check for trivia answers
     *
     * @param event The chat event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        // Check if trivia is active
        if (!triviaManager.isTriviaActive()) {
            return;
        }

        Player player = event.getPlayer();
        String message = event.getMessage();

        // Check if this is a correct answer
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            if (triviaManager.checkAnswer(player, message)) {
                // Don't cancel the event - let everyone see the correct answer
                // The trivia manager will handle rewards and messages
            }
        });
    }
}
