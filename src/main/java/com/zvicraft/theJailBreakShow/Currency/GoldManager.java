package com.zvicraft.theJailBreakShow.Currency;

import com.zvicraft.theJailBreakShow.TheJailBreakShow;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GoldManager {
    private final TheJailBreakShow plugin;
    private final Map<UUID, Integer> playerGold = new HashMap<>();
    private final int initialGold = 100; // Default initial gold

    public GoldManager(TheJailBreakShow plugin) {
        this.plugin = plugin;
    }

    /**
     * Gets the amount of gold a player has
     *
     * @param player The player
     * @return The amount of gold
     */
    public int getGold(Player player) {
        return playerGold.getOrDefault(player.getUniqueId(), initialGold);
    }

    /**
     * Sets the amount of gold a player has
     *
     * @param player The player
     * @param amount The amount of gold
     */
    public void setGold(Player player, int amount) {
        playerGold.put(player.getUniqueId(), amount);
    }

    /**
     * Adds gold to a player
     *
     * @param player The player
     * @param amount The amount of gold to add
     */
    public void addGold(Player player, int amount) {
        int currentGold = getGold(player);
        setGold(player, currentGold + amount);
    }

    /**
     * Removes gold from a player
     *
     * @param player The player
     * @param amount The amount of gold to remove
     * @return True if the player had enough gold, false otherwise
     */
    public boolean removeGold(Player player, int amount) {
        int currentGold = getGold(player);
        if (currentGold >= amount) {
            setGold(player, currentGold - amount);
            return true;
        }
        return false;
    }

    /**
     * Saves gold data to config or database
     */
    public void saveData() {
        // Save gold data to config or database
        // This is a placeholder for future implementation


    }

    /**
     * Loads gold data from config or database
     */
    public void loadData() {
        // Load gold data from config or database
        // This is a placeholder for future implementation
    }
}