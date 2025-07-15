package com.zvicraft.theJailBreakShow.GUI;

import com.zvicraft.theJailBreakShow.FreeDay.FreeDayManager;
import com.zvicraft.theJailBreakShow.Teams.Teams;
import com.zvicraft.theJailBreakShow.Teams.teamsManagers;
import com.zvicraft.theJailBreakShow.TheJailBreakShow;
import com.zvicraft.theJailBreakShow.utils.LanguageManager;
import com.zvicraft.theJailBreakShow.utils.MessageUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class GUIEventHandler implements Listener {

    public static void setPlugin(TheJailBreakShow theJailBreakShow) {
        theJailBreakShow.getServer().getPluginManager().registerEvents(new GUIEventHandler(), theJailBreakShow);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle() == null) return;

        String title = event.getView().getTitle();

        // Check if the inventory is one of our GUIs
        if (title.equals(ChatColor.GOLD + "Team Selection")) {
            handleTeamGUIClick(event);
        } else if (title.equals(ChatColor.GOLD + "Gold Management")) {
            handleGoldGUIClick(event);
        } else if (title.equals(ChatColor.GOLD + "Free Day Control")) {
            handleFreeDayGUIClick(event);
        }
    }

    private void handleTeamGUIClick(InventoryClickEvent event) {
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        int slot = event.getSlot();

        switch (slot) {
            case 11: // Prisoners
                teamsManagers.setPlayerTeam(player, Teams.Prisoners);
                player.sendMessage(ChatColor.GREEN + "You joined the Prisoners team!");
                player.closeInventory();
                break;

            case 13: // Guards
                // Check if there are already max guards
                if (teamsManagers.getNumberOfGuards() >= teamsManagers.getMaxGuards() &&
                        teamsManagers.getPlayerTeam(player) != Teams.Guards) {
                    player.sendMessage(ChatColor.RED + "The guards team is full!");
                    player.closeInventory();
                    return;
                }
                teamsManagers.setPlayerTeam(player, Teams.Guards);
                player.sendMessage(ChatColor.GREEN + "You joined the Guards team!");
                player.closeInventory();
                break;

            case 15: // Spectators
                teamsManagers.setPlayerTeam(player, Teams.Spectators);
                player.sendMessage(ChatColor.GREEN + "You joined the Spectators team!");
                player.closeInventory();
                break;
        }
    }

    private void handleGoldGUIClick(InventoryClickEvent event) {
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        int slot = event.getSlot();

        if (slot == 15 && (player.hasPermission("thejailbreakshow.gold.give") || player.hasPermission("thejailbreakshow.gold.take"))) {
            player.sendMessage(ChatColor.YELLOW + "Use commands for admin actions:");
            player.sendMessage(ChatColor.YELLOW + "/gold give <player> <amount>");
            player.sendMessage(ChatColor.YELLOW + "/gold take <player> <amount>");
            player.closeInventory();
        }
    }

    private void handleFreeDayGUIClick(InventoryClickEvent event) {
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        int slot = event.getSlot();

        if (slot == 13) {
            // Only guards can trigger free day
            if (teamsManagers.getPlayerTeam(player) != Teams.Guards) {
                player.sendMessage(ChatColor.RED + "Only guards can trigger a Free Day!");
                player.closeInventory();
                return;
            }

            // Check if player has permission
            if (!player.hasPermission("thejailbreakshow.freeday")) {
                player.sendMessage(ChatColor.RED + "You don't have permission to trigger a Free Day!");
                player.closeInventory();
                return;
            }

            // Trigger free day
            FreeDayManager.triggerFreeDay(player);
            player.closeInventory();
        }
    }
}