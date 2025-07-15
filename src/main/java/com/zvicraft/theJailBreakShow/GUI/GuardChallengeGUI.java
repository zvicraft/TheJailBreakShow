package com.zvicraft.theJailBreakShow.GUI;

import com.zvicraft.theJailBreakShow.TheJailBreakShow;
import com.zvicraft.theJailBreakShow.utils.LanguageManager;
import com.zvicraft.theJailBreakShow.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;


public class GuardChallengeGUI implements Listener {
    private final TheJailBreakShow plugin;
    private final Map<String, ChallengeInfo> challengeInfoMap = new HashMap<>();
    private String guiTitle;
    private static final String DEFAULT_TITLE = ChatColor.DARK_PURPLE + "Guard Challenges";
    private static final Pattern COST_PATTERN = Pattern.compile("Cost: (\\d+) gold");

    /**
     * Represents challenge information for GUI processing
     */
    private static class ChallengeInfo {
        private final String effectType;
        private final int duration;
        private final int amplifier;
        private final int cost;

        public ChallengeInfo(String effectType, int duration, int amplifier, int cost) {
            this.effectType = effectType;
            this.duration = duration;
            this.amplifier = amplifier;
            this.cost = cost;
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

    public GuardChallengeGUI(TheJailBreakShow plugin) {
        this.plugin = plugin;
        loadChallenges();
    }

    /**
     * Loads challenges from configuration
     */
    private void loadChallenges() {
        ConfigurationSection challengesSection = plugin.getConfigManager().getConfig().getConfigurationSection("guard-challenges");

        if (challengesSection == null) {
            // Create default challenges if section doesn't exist
            createDefaultChallenges();
            return;
        }

        // Load each challenge from config
        for (String challengeId : challengesSection.getKeys(false)) {
            ConfigurationSection challengeSection = challengesSection.getConfigurationSection(challengeId);
            if (challengeSection == null) continue;

            String effect = challengeSection.getString("effect", "SPEED");
            int duration = challengeSection.getInt("duration", 20) * 20; // Convert seconds to ticks
            int amplifier = challengeSection.getInt("amplifier", 0);
            int cost = challengeSection.getInt("cost", 100);

            challengeInfoMap.put(challengeId, new ChallengeInfo(effect, duration, amplifier, cost));
        }
    }

    /**
     * Creates default challenges if none are configured
     */
    private void createDefaultChallenges() {
        challengeInfoMap.put("speed", new ChallengeInfo("SPEED", 30 * 20, 0, 100));
        challengeInfoMap.put("strength", new ChallengeInfo("INCREASE_DAMAGE", 20 * 20, 0, 150));
        challengeInfoMap.put("jump", new ChallengeInfo("JUMP", 25 * 20, 0, 75));
    }

    /**
     * Opens the guard challenge GUI for a player
     *
     * @param player The player to open the GUI for
     */
    public void openGuardChallengeGUI(Player player) {
        // Get localized title
        String title = getGuiTitle();
        LanguageManager lang = plugin.getLanguageManager();

        // Create inventory
        Inventory gui = Bukkit.createInventory(null, 27, title);

        // Speed challenge
        ChallengeInfo speedChallenge = challengeInfoMap.get("speed");
        gui.setItem(10, GUIUtils.createGuiItem(Material.FEATHER,
                ChatColor.GOLD + lang.getMessage("gui.challenge.speed_title"),
                ChatColor.GRAY + lang.getMessage("gui.challenge.speed_desc"),
                ChatColor.YELLOW + lang.getMessage("gui.challenge.cost", "%amount%",
                        String.valueOf(speedChallenge.cost))));

        // Strength challenge
        ChallengeInfo strengthChallenge = challengeInfoMap.get("strength");
        gui.setItem(13, GUIUtils.createGuiItem(Material.DIAMOND_SWORD,
                ChatColor.RED + lang.getMessage("gui.challenge.strength_title"),
                ChatColor.GRAY + lang.getMessage("gui.challenge.strength_desc"),
                ChatColor.YELLOW + lang.getMessage("gui.challenge.cost", "%amount%",
                        String.valueOf(strengthChallenge.cost))));

        // Jump challenge
        ChallengeInfo jumpChallenge = challengeInfoMap.get("jump");
        gui.setItem(16, GUIUtils.createGuiItem(Material.RABBIT_FOOT,
                ChatColor.GREEN + lang.getMessage("gui.challenge.jump_title"),
                ChatColor.GRAY + lang.getMessage("gui.challenge.jump_desc"),
                ChatColor.YELLOW + lang.getMessage("gui.challenge.cost", "%amount%",
                        String.valueOf(jumpChallenge.cost))));

        player.openInventory(gui);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // Check if this is our GUI - either using the default title or the localized title
        if (!event.getView().getTitle().equals(getGuiTitle()) &&
                !event.getView().getTitle().equals(DEFAULT_TITLE)) {
            return;
        }

        event.setCancelled(true);

        if (event.getCurrentItem() == null) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        String itemName = event.getCurrentItem().getItemMeta().getDisplayName();
        LanguageManager lang = plugin.getLanguageManager();

        // Get challenge info based on clicked item
        ChallengeInfo challenge = null;
        int cost = 0;
        PotionEffectType effectType = null;
        int duration = 0;
        int amplifier = 0;
        String challengeName = "";

        // Identify which challenge was clicked
        if (itemName.equals(ChatColor.GOLD + lang.getMessage("gui.challenge.speed_title")) ||
                itemName.equals(ChatColor.GOLD + "Speed Challenge")) {
            challenge = challengeInfoMap.get("speed");
            challengeName = "speed";
        } else if (itemName.equals(ChatColor.RED + lang.getMessage("gui.challenge.strength_title")) ||
                itemName.equals(ChatColor.RED + "Strength Challenge")) {
            challenge = challengeInfoMap.get("strength");
            challengeName = "strength";
        } else if (itemName.equals(ChatColor.GREEN + lang.getMessage("gui.challenge.jump_title")) ||
                itemName.equals(ChatColor.GREEN + "Jump Challenge")) {
            challenge = challengeInfoMap.get("jump");
            challengeName = "jump";
        }

        // Process challenge if found
        if (challenge != null) {
            cost = challenge.cost;
            effectType = PotionEffectType.getByName(challenge.effectType);
            duration = challenge.duration;
            amplifier = challenge.amplifier;

            if (effectType == null) {
                player.sendMessage(ChatColor.RED + "Error: Invalid effect type: " + challenge.effectType);
                plugin.getLogger().warning("Invalid effect type: " + challenge.effectType + " for challenge: " + challengeName);
                return;
            }

            if (plugin.getGoldManager().removeGold(player, cost)) {
                player.addPotionEffect(new PotionEffect(effectType, duration, amplifier));
                player.sendMessage(ChatColor.GREEN + lang.getMessage("challenge.activated",
                        "%challenge%", lang.getMessage("gui.challenge." + challengeName + "_title"),
                        "%cost%", String.valueOf(cost)));
            } else {
                player.sendMessage(ChatColor.RED + lang.getMessage("gold.not_enough",
                        "%amount%", String.valueOf(cost)));
            }
        }
    }


}