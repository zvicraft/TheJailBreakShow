package com.zvicraft.theJailBreakShow.GUI;

import com.zvicraft.theJailBreakShow.Currency.GoldManager;
import com.zvicraft.theJailBreakShow.FreeDay.FreeDayManager;
import com.zvicraft.theJailBreakShow.Teams.Teams;
import com.zvicraft.theJailBreakShow.Teams.teamsManagers;
import com.zvicraft.theJailBreakShow.TheJailBreakShow;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class GUIManager {
    private static final TheJailBreakShow plugin = TheJailBreakShow.getInstance();
    
    /**
     * Opens the team selection GUI for a player
     * @param player The player to open the GUI for
     */
    public static void openTeamGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, ChatColor.GOLD + "Team Selection");
        
        // Prisoners team item
        ItemStack prisonersItem = createItem(Material.RED_WOOL, ChatColor.RED + "Prisoners", 
                ChatColor.GRAY + "Click to join the Prisoners team", 
                ChatColor.GRAY + "Current members: " + teamsManagers.getNumberOfPrisoners());
        
        // Guards team item
        ItemStack guardsItem = createItem(Material.BLUE_WOOL, ChatColor.BLUE + "Guards", 
                ChatColor.GRAY + "Click to join the Guards team", 
                ChatColor.GRAY + "Current members: " + teamsManagers.getNumberOfGuards() + "/" + teamsManagers.getMaxGuards());
        
        // Spectators team item
        ItemStack spectatorsItem = createItem(Material.GRAY_WOOL, ChatColor.GRAY + "Spectators", 
                ChatColor.GRAY + "Click to join the Spectators team", 
                ChatColor.GRAY + "Current members: " + teamsManagers.getNumberOfSpectators());
        
        // Set items in the inventory
        gui.setItem(11, prisonersItem);
        gui.setItem(13, guardsItem);
        gui.setItem(15, spectatorsItem);
        
        // Open the inventory for the player
        player.openInventory(gui);
    }
    
    /**
     * Opens the gold management GUI for a player
     * @param player The player to open the GUI for
     */
    public static void openGoldGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, ChatColor.GOLD + "Gold Management");
        
        GoldManager goldManager = plugin.getGoldManager();
        int playerGold = goldManager.getGold(player);
        
        // Player's gold balance item
        ItemStack balanceItem = createItem(Material.GOLD_INGOT, ChatColor.GOLD + "Your Gold Balance", 
                ChatColor.YELLOW + "Current balance: " + playerGold);
        
        // Set items in the inventory
        gui.setItem(13, balanceItem);
        
        // If player has admin permissions, add admin options
        if (player.hasPermission("thejailbreakshow.gold.give") || player.hasPermission("thejailbreakshow.gold.take")) {
            ItemStack adminItem = createItem(Material.COMMAND_BLOCK, ChatColor.RED + "Admin Options", 
                    ChatColor.GRAY + "Use commands for admin actions:",
                    ChatColor.YELLOW + "/gold give <player> <amount>",
                    ChatColor.YELLOW + "/gold take <player> <amount>");
            
            gui.setItem(15, adminItem);
        }
        
        // Open the inventory for the player
        player.openInventory(gui);
    }
    
    /**
     * Opens the free day GUI for a player
     * @param player The player to open the GUI for
     */
    public static void openFreeDayGUI(Player player) {
        // Only guards can use this GUI
        if (teamsManagers.getPlayerTeam(player) != Teams.Guards) {
            player.sendMessage(ChatColor.RED + "Only guards can access the Free Day menu!");
            return;
        }
        
        // Check if player has permission
        if (!player.hasPermission("thejailbreakshow.freeday")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this menu!");
            return;
        }
        
        Inventory gui = Bukkit.createInventory(null, 27, ChatColor.GOLD + "Free Day Control");
        
        // Trigger free day item
        ItemStack triggerItem = createItem(Material.EMERALD_BLOCK, ChatColor.GREEN + "Trigger Free Day", 
                ChatColor.GRAY + "Click to trigger a Free Day");
        
        // Set items in the inventory
        gui.setItem(13, triggerItem);
        
        // Open the inventory for the player
        player.openInventory(gui);
    }
    
    /**
     * Creates an item for a GUI
     * @param material The material of the item
     * @param name The name of the item
     * @param lore The lore of the item
     * @return The created item
     */
    private static ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        meta.setDisplayName(name);
        
        List<String> loreList = new ArrayList<>();
        for (String line : lore) {
            loreList.add(line);
        }
        
        meta.setLore(loreList);
        item.setItemMeta(meta);
        
        return item;
    }
}