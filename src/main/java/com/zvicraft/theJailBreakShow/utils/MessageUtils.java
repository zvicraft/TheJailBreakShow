package com.zvicraft.theJailBreakShow.utils;

import com.zvicraft.theJailBreakShow.TheJailBreakShow;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Utility class for handling messages
 */
public class MessageUtils {

    /**
     * Sends a language key message to a player/command sender
     *
     * @param sender       The command sender to receive the message
     * @param key          The language key
     * @param replacements Optional replacements (placeholder, value, placeholder2, value2, ...)
     */
    public static void sendMessage(CommandSender sender, String key, Object... replacements) {
        LanguageManager lang = TheJailBreakShow.getInstance().getLanguageManager();
        sender.sendMessage(lang.getMessage(key, replacements));
    }

    /**
     * Broadcasts a language key message to all players
     *
     * @param key          The language key
     * @param replacements Optional replacements (placeholder, value, placeholder2, value2, ...)
     */
    public static void broadcast(String key, Object... replacements) {
        LanguageManager lang = TheJailBreakShow.getInstance().getLanguageManager();
        Bukkit.broadcastMessage(lang.getMessage(key, replacements));
    }

    /**
     * Sends a title to a player using language keys
     *
     * @param player       The player to send the title to
     * @param titleKey     The language key for the title
     * @param subtitleKey  The language key for the subtitle
     * @param fadeIn       Fade in time in ticks
     * @param stay         Stay time in ticks
     * @param fadeOut      Fade out time in ticks
     * @param replacements Optional replacements (placeholder, value, placeholder2, value2, ...)
     */
    public static void sendTitle(Player player, String titleKey, String subtitleKey,
                                 int fadeIn, int stay, int fadeOut, Object... replacements) {
        LanguageManager lang = TheJailBreakShow.getInstance().getLanguageManager();
        String title = lang.getMessage(titleKey, replacements);
        String subtitle = subtitleKey != null ? lang.getMessage(subtitleKey, replacements) : "";
        player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
    }

    /**
     * Checks if the command sender has permission
     *
     * @param sender     The command sender
     * @param permission The permission to check
     * @return True if has permission, false otherwise
     */
    public static boolean hasPermission(CommandSender sender, String permission) {
        if (sender.hasPermission(permission)) {
            return true;
        } else {
            sendMessage(sender, "general.no_permission");
            return false;
        }
    }

    /**
     * Checks if the sender is a player and sends message if not
     *
     * @param sender The command sender
     * @return True if is player, false otherwise
     */
    public static boolean isPlayer(CommandSender sender) {
        if (sender instanceof Player) {
            return true;
        } else {
            sendMessage(sender, "general.player_only");
            return false;
        }
    }
}
