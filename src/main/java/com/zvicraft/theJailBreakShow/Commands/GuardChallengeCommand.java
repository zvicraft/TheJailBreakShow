package com.zvicraft.theJailBreakShow.Commands;

import com.zvicraft.theJailBreakShow.Rounds.GuardChallengeManager;
import com.zvicraft.theJailBreakShow.Rounds.RoundsSystems;
import com.zvicraft.theJailBreakShow.Teams.Teams;
import com.zvicraft.theJailBreakShow.Teams.teamsManagers;
import com.zvicraft.theJailBreakShow.TheJailBreakShow;
import com.zvicraft.theJailBreakShow.utils.ConfigManager;
import com.zvicraft.theJailBreakShow.utils.LanguageManager;
import com.zvicraft.theJailBreakShow.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GuardChallengeCommand implements CommandExecutor {
    private final TheJailBreakShow plugin;
    private String guiTitle;
    private final Map<String, GuardChallenge> challenges;
    private final List<String> challengeOrder;

    /**
     * Represents a guard challenge with its properties
     */
    private static class GuardChallenge {
        private final String id;
        private final Material material;
        private final int cost;
        private final ChatColor color;
        private final String effect;
        private final int duration;
        private final int amplifier;

        public GuardChallenge(String id, Material material, int cost, ChatColor color,
                              String effect, int duration, int amplifier) {
            this.id = id;
            this.material = material;
            this.cost = cost;
            this.color = color;
            this.effect = effect;
            this.duration = duration;
            this.amplifier = amplifier;
        }
    }

    /**
     * Gets the localized GUI title
     *
     * @return The localized GUI title
     */
    private String getGuiTitle() {
        if (guiTitle == null) {
            guiTitle = ChatColor.DARK_PURPLE + plugin.getLanguageManager().getMessage("gui.guard_challenge_title");
        }
        return guiTitle;
    }

    public GuardChallengeCommand(TheJailBreakShow plugin) {
        this.plugin = plugin;
        this.challenges = new HashMap<>();
        this.challengeOrder = new ArrayList<>();

        // Load challenges from config
        loadChallenges();
    }

    /**
     * Loads challenge configurations from config.yml
     */
    private void loadChallenges() {
        ConfigManager configManager = plugin.getConfigManager();
        ConfigurationSection challengesSection = configManager.getConfig().getConfigurationSection("guard-challenges");

        if (challengesSection == null) {
            // Create default challenges if section doesn't exist
            createDefaultChallenges();
            return;
        }

        // Clear existing challenges
        challenges.clear();
        challengeOrder.clear();

        // Load each challenge from config
        for (String challengeId : challengesSection.getKeys(false)) {
            ConfigurationSection challengeSection = challengesSection.getConfigurationSection(challengeId);
            if (challengeSection == null) continue;

            String materialName = challengeSection.getString("material", "FEATHER");
            Material material = Material.getMaterial(materialName);
            if (material == null) material = Material.FEATHER;

            int cost = challengeSection.getInt("cost", 100);
            String colorName = challengeSection.getString("color", "GOLD");
            ChatColor color;
            try {
                color = ChatColor.valueOf(colorName);
            } catch (IllegalArgumentException e) {
                color = ChatColor.GOLD;
            }

            String effect = challengeSection.getString("effect", "SPEED");
            int duration = challengeSection.getInt("duration", 20) * 20; // Convert seconds to ticks
            int amplifier = challengeSection.getInt("amplifier", 0);

            GuardChallenge challenge = new GuardChallenge(
                    challengeId, material, cost, color, effect, duration, amplifier
            );

            challenges.put(challengeId, challenge);
            challengeOrder.add(challengeId);
        }

        // If no challenges were loaded, create defaults
        if (challenges.isEmpty()) {
            createDefaultChallenges();
        }
    }

    /**
     * Creates default challenges if none are configured
     */
    private void createDefaultChallenges() {
        // Create default challenges
        challenges.clear();
        challengeOrder.clear();

        challenges.put("speed", new GuardChallenge(
                "speed", Material.FEATHER, 100, ChatColor.GOLD, "SPEED", 30 * 20, 0
        ));
        challengeOrder.add("speed");

        challenges.put("strength", new GuardChallenge(
                "strength", Material.DIAMOND_SWORD, 150, ChatColor.RED, "INCREASE_DAMAGE", 20 * 20, 0
        ));
        challengeOrder.add("strength");

        challenges.put("jump", new GuardChallenge(
                "jump", Material.RABBIT_FOOT, 75, ChatColor.GREEN, "JUMP", 25 * 20, 0
        ));
        challengeOrder.add("jump");

        // Save default challenges to config
        saveDefaultChallenges();
    }

    /**
     * Saves default challenges to config.yml
     */
    private void saveDefaultChallenges() {
        ConfigManager configManager = plugin.getConfigManager();

        for (Map.Entry<String, GuardChallenge> entry : challenges.entrySet()) {
            String challengeId = entry.getKey();
            GuardChallenge challenge = entry.getValue();

            String basePath = "guard-challenges." + challengeId + ".";
            configManager.getConfig().set(basePath + "material", challenge.material.toString());
            configManager.getConfig().set(basePath + "cost", challenge.cost);
            configManager.getConfig().set(basePath + "color", challenge.color.name());
            configManager.getConfig().set(basePath + "effect", challenge.effect);
            configManager.getConfig().set(basePath + "duration", challenge.duration / 20); // Convert ticks to seconds
            configManager.getConfig().set(basePath + "amplifier", challenge.amplifier);
        }

        configManager.saveConfig();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!MessageUtils.isPlayer(sender)) {
            return true;
        }

        Player player = (Player) sender;

        // Check if the game is active
        if (!RoundsSystems.isGameActive()) {
            MessageUtils.sendMessage(player, "general.game_not_active");
            return true;
        }

        // Check if player is a guard
        if (!teamsManagers.isPlayerInGuardTeam(player)) {
            MessageUtils.sendMessage(player, "guard_challenge.guards_only");
            return true;
        }

        openChallengeGUI(player);
        return true;
    }

    private void openChallengeGUI(Player player) {
        // Use the GUIManager to display challenges
        plugin.getGUIManager().openGuardChallengeGUI(player);
    }

    private ItemStack createGuiItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);
        return item;
    }
}