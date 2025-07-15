package com.zvicraft.theJailBreakShow.GUI;

import com.zvicraft.theJailBreakShow.FreeDay.FreeDayManager;
import com.zvicraft.theJailBreakShow.Teams.Teams;
import com.zvicraft.theJailBreakShow.Teams.teamsManagers;
import com.zvicraft.theJailBreakShow.TheJailBreakShow;
import com.zvicraft.theJailBreakShow.utils.LanguageManager;
import com.zvicraft.theJailBreakShow.utils.MessageUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
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
        LanguageManager lang = TheJailBreakShow.getInstance().getLanguageManager();
        String teamTitle = ChatColor.GOLD + lang.getMessage("gui.team_control_title");
        String goldTitle = ChatColor.GOLD + lang.getMessage("gui.gold.title");
        String freeDayTitle = ChatColor.GOLD + lang.getMessage("gui.freeday_control_title");

        // Check if the inventory is one of our GUIs
        if (title.equals(teamTitle) || title.equals(ChatColor.GOLD + "Team Selection")) {
            handleTeamGUIClick(event);
        } else if (title.equals(goldTitle) || title.equals(ChatColor.GOLD + "Gold Management")) {
            handleGoldGUIClick(event);
        } else if (title.equals(freeDayTitle) || title.equals(ChatColor.GOLD + "Free Day Control")) {
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
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        TheJailBreakShow.getInstance().getLanguageManager().getMessage("teams.player_joined_prisoners")));
                player.closeInventory();
                break;

            case 13: // Guards
                // Check if there are already max guards
                if (teamsManagers.getNumberOfGuards() >= teamsManagers.getMaxGuards() &&
                        teamsManagers.getPlayerTeam(player) != Teams.Guards) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            TheJailBreakShow.getInstance().getLanguageManager().getMessage("guard_selection.max_guards")));
                    player.closeInventory();
                    return;
                }
                teamsManagers.setPlayerTeam(player, Teams.Guards);
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        TheJailBreakShow.getInstance().getLanguageManager().getMessage("teams.player_joined_guards")));
                player.closeInventory();
                break;

            case 15: // Spectators
                teamsManagers.setPlayerTeam(player, Teams.Spectators);
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        TheJailBreakShow.getInstance().getLanguageManager().getMessage("teams.player_joined_spectators")));
                player.closeInventory();
                break;
        }
    }

    private void handleGoldGUIClick(InventoryClickEvent event) {
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        LanguageManager lang = TheJailBreakShow.getInstance().getLanguageManager();

        int slot = event.getSlot();

        // Message to display when player tries to interact with inventory items
        if (event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.GOLD_INGOT) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    lang.getMessage("gui.gold.inventory_help")));
            return;
        }

        if (slot == 15 && (player.hasPermission("thejailbreakshow.gold.give") || player.hasPermission("thejailbreakshow.gold.take"))) {
            player.sendMessage(ChatColor.YELLOW + lang.getMessage("gui.gold.admin_desc"));
            player.sendMessage(ChatColor.YELLOW + lang.getMessage("gui.gold.admin_give_cmd"));
            player.sendMessage(ChatColor.YELLOW + lang.getMessage("gui.gold.admin_take_cmd"));
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
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        TheJailBreakShow.getInstance().getLanguageManager().getMessage("freeday.guards_only")));
                player.closeInventory();
                return;
            }

            // Check if player has permission
            if (!player.hasPermission("thejailbreakshow.freeday")) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        TheJailBreakShow.getInstance().getLanguageManager().getMessage("general.no_permission")));
                player.closeInventory();
                return;
            }

            // Trigger free day
            FreeDayManager.triggerFreeDay(player);
            player.closeInventory();
        }
    }
}