package com.zvicraft.theJailBreakShow.GUI;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

/**
 * Utility class for GUI-related functions
 */
public class GUIUtils {

    /**
     * Creates an ItemStack for use in a GUI
     *
     * @param material Material of the item
     * @param name Display name of the item
     * @param lore Lore (description) lines for the item
     * @return The created ItemStack
     */
    public static ItemStack createGuiItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);
        return item;
    }
}
