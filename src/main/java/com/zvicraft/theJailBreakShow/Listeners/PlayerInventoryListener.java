package com.zvicraft.theJailBreakShow.Listeners;

import com.zvicraft.theJailBreakShow.TheJailBreakShow;
import com.zvicraft.theJailBreakShow.utils.LanguageManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Handles player inventory interactions related to gold and other custom items
 */
public class PlayerInventoryListener implements Listener {

    private final TheJailBreakShow plugin;

    public PlayerInventoryListener(TheJailBreakShow plugin) {
        this.plugin = plugin;
    }

    /**
     * Handles player interactions with gold items in their inventory
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        // Check if player is interacting with a gold ingot
        if (item != null && item.getType() == Material.GOLD_INGOT) {
            LanguageManager lang = plugin.getLanguageManager();
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                lang.getMessage("gui.gold.inventory_help")));

            // Show player their current gold balance
            int gold = plugin.getGoldManager().getGold(player);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                lang.getMessage("gold.balance", "%amount%", String.valueOf(gold))));

            // Cancel the event to prevent normal item usage
            event.setCancelled(true);
        }
    }
}
