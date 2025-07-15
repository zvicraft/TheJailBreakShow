package com.zvicraft.theJailBreakShow.Currency;

import com.zvicraft.theJailBreakShow.TheJailBreakShow;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GoldManager {
    private final TheJailBreakShow plugin;
    private final Map<UUID, Integer> playerGold = new HashMap<>();
    private final int initialGold = 100;
    private final File goldFile;
    private FileConfiguration goldConfig;

    public GoldManager(TheJailBreakShow plugin) {
        this.plugin = plugin;
        this.goldFile = new File(plugin.getDataFolder(), "gold.yml");
        loadData();
    }

    public int getGold(Player player) {
        return playerGold.getOrDefault(player.getUniqueId(), initialGold);
    }

    public void setGold(Player player, int amount) {
        playerGold.put(player.getUniqueId(), amount);
        saveData(); // Save automatically when gold is modified
    }

    public void addGold(Player player, int amount) {
        int currentGold = getGold(player);
        setGold(player, currentGold + amount);
    }

    public boolean removeGold(Player player, int amount) {
        int currentGold = getGold(player);
        if (currentGold >= amount) {
            setGold(player, currentGold - amount);
            return true;
        }
        return false;
    }

    public void saveData() {
        try {
            if (!goldFile.exists()) {
                goldFile.getParentFile().mkdirs();
                goldFile.createNewFile();
            }

            goldConfig = new YamlConfiguration();

            // Save all player gold data
            for (Map.Entry<UUID, Integer> entry : playerGold.entrySet()) {
                goldConfig.set("gold." + entry.getKey().toString(), entry.getValue());
            }

            goldConfig.save(goldFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save gold data: " + e.getMessage());
        }
    }

    public void loadData() {
        if (!goldFile.exists()) {
            return;
        }

        goldConfig = YamlConfiguration.loadConfiguration(goldFile);

        if (goldConfig.contains("gold")) {
            for (String uuidString : goldConfig.getConfigurationSection("gold").getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(uuidString);
                    int amount = goldConfig.getInt("gold." + uuidString);
                    playerGold.put(uuid, amount);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid UUID in gold.yml: " + uuidString);
                }
            }
        }
    }
}