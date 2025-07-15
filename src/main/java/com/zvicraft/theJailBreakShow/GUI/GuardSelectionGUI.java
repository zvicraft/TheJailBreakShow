package com.zvicraft.theJailBreakShow.GUI;

import com.zvicraft.theJailBreakShow.TheJailBreakShow;
import com.zvicraft.theJailBreakShow.Teams.ChatChallengeManager;
import com.zvicraft.theJailBreakShow.Teams.Teams;
import com.zvicraft.theJailBreakShow.Teams.teamsManagers;
import com.zvicraft.theJailBreakShow.utils.LanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GuardSelectionGUI implements Listener {
    private final TheJailBreakShow plugin;
    private String guiTitle;
    private static final String DEFAULT_TITLE = ChatColor.DARK_BLUE + "Guard Selection";

    /**
     * Gets the localized GUI title
     *
     * @return The localized GUI title
     */
    private String getGuiTitle() {
        if (guiTitle == null) {
            guiTitle = ChatColor.DARK_BLUE + plugin.getLanguageManager().getMessage("gui.guard_selection_title");
        }
        return guiTitle;
    }

    public GuardSelectionGUI(TheJailBreakShow plugin) {
        this.plugin = plugin;
    }

    /**
     * Opens the guard selection GUI for an admin
     *
     * @param admin  The admin opening the GUI
     * @param plugin The plugin instance
     */
    public static void openGuardSelectionGUI(Player admin, TheJailBreakShow plugin) {
        List<Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
        int inventorySize = ((onlinePlayers.size() / 9) + 1) * 9;
        inventorySize = Math.min(54, Math.max(9, inventorySize)); // Between 9 and 54

        // Get localized title
        String title = DEFAULT_TITLE;
        if (plugin != null && plugin.getLanguageManager() != null) {
            title = ChatColor.DARK_BLUE + plugin.getLanguageManager().getMessage("gui.guard_selection_title");
        }

        Inventory gui = Bukkit.createInventory(null, inventorySize, title);

        // Add player heads to the GUI
        int slot = 0;
        for (Player player : onlinePlayers) {
            if (slot >= inventorySize) break;

            ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD, 1);
            SkullMeta meta = (SkullMeta) playerHead.getItemMeta();
            meta.setOwningPlayer(player);
            meta.setDisplayName(ChatColor.GREEN + player.getName());

            // Add lore with current team
            List<String> lore = new ArrayList<>();
            Teams playerTeam = teamsManagers.getPlayerTeam(player);
            String teamDisplay;

            // Get localized team names
            if (plugin != null && plugin.getLanguageManager() != null) {
                LanguageManager lang = plugin.getLanguageManager();
                switch (playerTeam) {
                    case Guards:
                        teamDisplay = ChatColor.BLUE + lang.getMessage("teams.guard");
                        break;
                    case Prisoners:
                        teamDisplay = ChatColor.RED + lang.getMessage("teams.prisoner");
                        break;
                    case Spectators:
                        teamDisplay = ChatColor.GRAY + lang.getMessage("teams.spectator");
                        break;
                    default:
                        teamDisplay = ChatColor.WHITE + lang.getMessage("teams.none");
                        break;
                }

                lore.add(ChatColor.YELLOW + lang.getMessage("gui.current_team") + " " + teamDisplay);
                lore.add(ChatColor.GOLD + lang.getMessage("gui.click_to_select_guard"));
            } else {
                // Fallback if language manager not available
                switch (playerTeam) {
                    case Guards:
                        teamDisplay = ChatColor.BLUE + "Guard";
                        break;
                    case Prisoners:
                        teamDisplay = ChatColor.RED + "Prisoner";
                        break;
                    case Spectators:
                        teamDisplay = ChatColor.GRAY + "Spectator";
                        break;
                    default:
                        teamDisplay = ChatColor.WHITE + "None";
                        break;
                }

                lore.add(ChatColor.YELLOW + "Current Team: " + teamDisplay);
                lore.add(ChatColor.GOLD + "Click to select as guard");
            }
            meta.setLore(lore);

            playerHead.setItemMeta(meta);
            gui.setItem(slot, playerHead);
            slot++;
        }

        // Add random selection button
        ItemStack randomButton;
        if (plugin != null && plugin.getLanguageManager() != null) {
            LanguageManager lang = plugin.getLanguageManager();
            randomButton = createGuiItem(Material.NETHER_STAR,
                    ChatColor.LIGHT_PURPLE + lang.getMessage("gui.random_selection"),
                    ChatColor.GRAY + lang.getMessage("gui.random_selection_desc"));
        } else {
            randomButton = createGuiItem(Material.NETHER_STAR,
                    ChatColor.LIGHT_PURPLE + "Random Selection",
                    ChatColor.GRAY + "Select a random guard");
        }
        gui.setItem(inventorySize - 5, randomButton);

        // Add chat challenge button
        ItemStack challengeButton;
        if (plugin != null && plugin.getLanguageManager() != null) {
            LanguageManager lang = plugin.getLanguageManager();
            challengeButton = createGuiItem(Material.BOOK,
                    ChatColor.GOLD + lang.getMessage("gui.chat_challenge"),
                    ChatColor.GRAY + lang.getMessage("gui.chat_challenge_desc"));
        } else {
            challengeButton = createGuiItem(Material.BOOK,
                    ChatColor.GOLD + "Chat Challenge",
                    ChatColor.GRAY + "First to answer correctly becomes the guard");
        }
        gui.setItem(inventorySize - 1, challengeButton);

        admin.openInventory(gui);
    }

    private static ItemStack createGuiItem(Material material, String name, String... lore) {
        return GUIUtils.createGuiItem(material, name, lore);
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

        Player admin = (Player) event.getWhoClicked();

        // Check if player is op or has the required permission
        if (!admin.isOp() && !admin.hasPermission("thejailbreakshow.admin")) {
            admin.sendMessage(ChatColor.RED + plugin.getLanguageManager().getMessage("general.no_permission"));
            admin.closeInventory();
            return;
        }

        // Random selection button
        if (event.getCurrentItem().getType() == Material.NETHER_STAR) {
            randomSelectGuard(admin);
            admin.closeInventory();
            return;
        }

        // Chat challenge button
        if (event.getCurrentItem().getType() == Material.BOOK) {
            // Use the ChatChallengeManager through the plugin
            ChatChallengeManager chatManager = plugin.getChatChallengeManager();
            ChatChallengeManager.ChallengeType type = chatManager.getRandomType();
            String answer = chatManager.startChallenge(type);

            if (answer != null && !answer.isEmpty()) {
                // Start challenge was successful
                admin.sendMessage(ChatColor.GREEN + plugin.getLanguageManager().getMessage("guard_selection.chat_challenge_started"));
                // Save the answer for checking in chat listener
                teamsManagers.setChatChallengeAnswer(answer);
            } else {
                admin.sendMessage(ChatColor.RED + plugin.getLanguageManager().getMessage("guard_selection.chat_challenge_error"));
            }
            admin.closeInventory();
            return;
        }

        // Player head selection
        if (event.getCurrentItem().getType() == Material.PLAYER_HEAD) {
            SkullMeta meta = (SkullMeta) event.getCurrentItem().getItemMeta();
            if (meta.getOwningPlayer() != null) {
                Player selectedPlayer = Bukkit.getPlayer(meta.getOwningPlayer().getUniqueId());
                if (selectedPlayer != null && selectedPlayer.isOnline()) {
                    selectGuard(selectedPlayer, admin);
                }
            }
            admin.closeInventory();
        }
    }

    /**
     * Selects a player to be the guard
     *
     * @param guardPlayer The player to make guard
     * @param admin       The admin who selected the guard
     */
    private void selectGuard(Player guardPlayer, Player admin) {
        // Set the selected player as guard
        teamsManagers.setPlayerTeam(guardPlayer, Teams.Guards);

        // Set all other players as prisoners
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.equals(guardPlayer) && teamsManagers.getPlayerTeam(player) != Teams.Guards) {
                teamsManagers.setPlayerTeam(player, Teams.Prisoners);
            }
        }

        LanguageManager lang = plugin.getLanguageManager();

        // Broadcast announcement
        Bukkit.broadcastMessage(ChatColor.BLUE + "=== " + lang.getMessage("general.system_message") + " ===");
        Bukkit.broadcastMessage(ChatColor.YELLOW + guardPlayer.getName() + ChatColor.GREEN + " " +
                lang.getMessage("guard_selection.player_selected"));

        // Tell the admin
        admin.sendMessage(ChatColor.GREEN + lang.getMessage("guard_selection.admin_selected",
                "%player%", guardPlayer.getName()));
    }

    /**
     * Randomly selects a player to be the guard
     *
     * @param admin The admin who triggered the random selection
     */
    private void randomSelectGuard(Player admin) {
        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        if (players.isEmpty()) {
            admin.sendMessage(ChatColor.RED + plugin.getLanguageManager().getMessage("guard_selection.no_players"));
            return;
        }

        // Choose a random player
        Player randomPlayer = players.get((int) (Math.random() * players.size()));

        // Set as guard
        selectGuard(randomPlayer, admin);

        admin.sendMessage(ChatColor.GREEN + plugin.getLanguageManager().getMessage("guard_selection.random_selected",
                "%player%", randomPlayer.getName()));
    }
}
