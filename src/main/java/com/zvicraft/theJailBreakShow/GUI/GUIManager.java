
package com.zvicraft.theJailBreakShow.GUI;

import com.zvicraft.theJailBreakShow.Teams.Teams;
import com.zvicraft.theJailBreakShow.Teams.teamsManagers;
import com.zvicraft.theJailBreakShow.TheJailBreakShow;
import com.zvicraft.theJailBreakShow.utils.LanguageManager;
import com.zvicraft.theJailBreakShow.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * Manager class for all plugin GUIs
 */
public class GUIManager {
    private static TheJailBreakShow plugin;
    private static final Map<String, String> guiTitles = new HashMap<>();

    /**
     * Creates a new GUIManager
     *
     * @param plugin The plugin instance
     */
    public GUIManager(TheJailBreakShow plugin) {
        this.plugin = plugin;
    }

    /**
     * Gets a localized GUI title
     *
     * @param key          The language key for the title
     * @param defaultTitle The default title if localization is unavailable
     * @param color        The color for the title
     * @return The localized title
     */
    public static String getTitle(String key, String defaultTitle, ChatColor color) {
        if (!guiTitles.containsKey(key)) {
            if (plugin != null && plugin.getLanguageManager() != null) {
                guiTitles.put(key, color + plugin.getLanguageManager().getMessage(key));
            } else {
                guiTitles.put(key, color + defaultTitle);
            }
        }
        return guiTitles.get(key);
    }

    public static void openTeamGUI(Player player) {
        String title = getTitle("gui.team_control_title", "Team Control", ChatColor.GOLD);
    }

    /**
     * Opens the guard challenge GUI for a player
     *
     * @param player The player to open the GUI for
     */
    public void openGuardChallengeGUI(Player player) {
        String title = getTitle("gui.guard_challenge_title", "Guard Challenges", ChatColor.DARK_PURPLE);
        LanguageManager lang = plugin.getLanguageManager();

        Inventory gui = Bukkit.createInventory(null, 27, title);

        // Get challenge data from config
        Map<String, GuardChallengeInfo> challenges = loadChallengesFromConfig();

        // Speed challenge
        GuardChallengeInfo speedChallenge = challenges.getOrDefault("speed",
                new GuardChallengeInfo("SPEED", 30, 1, 100));
        gui.setItem(10, GUIUtils.createGuiItem(Material.FEATHER,
                ChatColor.GOLD + lang.getMessage("gui.challenge.speed_title"),
                ChatColor.GRAY + lang.getMessage("gui.challenge.speed_desc"),
                ChatColor.YELLOW + lang.getMessage("gui.challenge.cost", "%amount%",
                        String.valueOf(speedChallenge.getCost()))));

        // Strength challenge
        GuardChallengeInfo strengthChallenge = challenges.getOrDefault("strength",
                new GuardChallengeInfo("INCREASE_DAMAGE", 20, 0, 150));
        gui.setItem(13, GUIUtils.createGuiItem(Material.DIAMOND_SWORD,
                ChatColor.RED + lang.getMessage("gui.challenge.strength_title"),
                ChatColor.GRAY + lang.getMessage("gui.challenge.strength_desc"),
                ChatColor.YELLOW + lang.getMessage("gui.challenge.cost", "%amount%",
                        String.valueOf(strengthChallenge.getCost()))));

        // Jump challenge
        GuardChallengeInfo jumpChallenge = challenges.getOrDefault("jump",
                new GuardChallengeInfo("JUMP", 25, 1, 75));
        gui.setItem(16, GUIUtils.createGuiItem(Material.RABBIT_FOOT,
                ChatColor.GREEN + lang.getMessage("gui.challenge.jump_title"),
                ChatColor.GRAY + lang.getMessage("gui.challenge.jump_desc"),
                ChatColor.YELLOW + lang.getMessage("gui.challenge.cost", "%amount%",
                        String.valueOf(jumpChallenge.getCost()))));

        player.openInventory(gui);
    }

    /**
     * Opens the gold management GUI for a player
     *
     * @param player The player to open the GUI for
     */
    public static void openGoldGUI(Player player) {
        String title = getTitle("gui.gold.title", "Gold Management", ChatColor.GOLD);
        LanguageManager lang = plugin.getLanguageManager();

        // Create inventory
        Inventory gui = Bukkit.createInventory(null, 27, title);

        // Get player's current gold balance
        int playerGold = plugin.getGoldManager().getGold(player);

        // Player's gold balance item
        ItemStack balanceItem = GUIUtils.createGuiItem(Material.GOLD_INGOT,
                ChatColor.GOLD + lang.getMessage("gui.gold.balance_title"),
                ChatColor.YELLOW + lang.getMessage("gui.gold.current_balance", "%amount%", String.valueOf(playerGold)));

        // Set items in the inventory
        gui.setItem(13, balanceItem);

        // If player has admin permissions, add admin options
        if (player.hasPermission("thejailbreakshow.gold.give") || player.hasPermission("thejailbreakshow.gold.take")) {
            ItemStack adminItem = GUIUtils.createGuiItem(Material.COMMAND_BLOCK,
                    ChatColor.RED + lang.getMessage("gui.gold.admin_title"),
                    ChatColor.GRAY + lang.getMessage("gui.gold.admin_desc"),
                    ChatColor.YELLOW + lang.getMessage("gui.gold.admin_give_cmd"),
                    ChatColor.YELLOW + lang.getMessage("gui.gold.admin_take_cmd"));

            gui.setItem(15, adminItem);
        }

        // Open the inventory for the player
        player.openInventory(gui);
    }

    /**
     * Load challenges from plugin configuration
     *
     * @return Map of challenges by ID
     */
    private Map<String, GuardChallengeInfo> loadChallengesFromConfig() {
        Map<String, GuardChallengeInfo> challenges = new HashMap<>();

        // Add default challenges
        challenges.put("speed", new GuardChallengeInfo("SPEED", 30, 1, 100));
        challenges.put("strength", new GuardChallengeInfo("INCREASE_DAMAGE", 20, 0, 150));
        challenges.put("jump", new GuardChallengeInfo("JUMP", 25, 1, 75));

        // TODO: Load from config when available
        return challenges;
    }

    /**
     * Class to hold challenge information
     */
    public static class GuardChallengeInfo {
        private final String effectType;
        private final int durationSeconds;
        private final int amplifier;
        private final int cost;

        public GuardChallengeInfo(String effectType, int durationSeconds, int amplifier, int cost) {
            this.effectType = effectType;
            this.durationSeconds = durationSeconds;
            this.amplifier = amplifier;
            this.cost = cost;
        }

        public String getEffectType() {
            return effectType;
        }

        public int getDurationTicks() {
            return durationSeconds * 20; // Convert seconds to ticks
        }

        public int getAmplifier() {
            return amplifier;
        }

        public int getCost() {
            return cost;
        }
    }

    /**
     * Opens the free day GUI for a player
     *
     * @param player The player to open the GUI for
     */
    public static void openFreeDayGUI(Player player) {
        LanguageManager lang = plugin.getLanguageManager();
        String title = getTitle("gui.freeday_control_title", "Free Day Control", ChatColor.GOLD);

        // Only guards can use this GUI
        if (teamsManagers.getPlayerTeam(player) != Teams.Guards) {
            MessageUtils.sendMessage(player, "freeday.guards_only");
            return;
        }

        // Check if player has permission
        if (!player.hasPermission("thejailbreakshow.freeday")) {
            MessageUtils.sendMessage(player, "general.no_permission");
            return;
        }

        Inventory gui = Bukkit.createInventory(null, 27, title);

        // Trigger free day item
        ItemStack triggerItem = GUIUtils.createGuiItem(Material.EMERALD_BLOCK,
                ChatColor.GREEN + lang.getMessage("gui.freeday.trigger_title"),
                ChatColor.GRAY + lang.getMessage("gui.freeday.trigger_desc"));

        // Set items in the inventory
        gui.setItem(13, triggerItem);

        // Open the inventory for the player
        player.openInventory(gui);
    }
}