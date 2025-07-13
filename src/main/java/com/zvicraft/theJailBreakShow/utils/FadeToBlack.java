package com.zvicraft.theJailBreakShow.utils;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import com.zvicraft.theJailBreakShow.TheJailBreakShow;

public class FadeToBlack {
    private static TheJailBreakShow plugin;

    public static void fade(TheJailBreakShow mainPlugin) {
        plugin = mainPlugin;

        Bukkit.getOnlinePlayers().forEach(player -> {
            // Store the player's current helmet
            ItemStack originalHelmet = player.getInventory().getHelmet();

            // Apply blindness effect to darken the screen
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 140, 1, false, false, true));

            // Send an empty title to make the transition smoother
            player.sendTitle(" ", " ", 10, 120, 10);

            // Start fade-in animation using black stained glass
            new BukkitRunnable() {
                int step = 0;
                final int maxSteps = 5;

                @Override
                public void run() {
                    if (step > maxSteps) {
                        // After fade is complete, schedule fade-out
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                // Start fade-out animation after round title is shown
                                fadeOut(player, originalHelmet);
                            }
                        }.runTaskLater(plugin, 80); // Start fade-out after 4 seconds
                        this.cancel();
                        return;
                    }

                    // Create black stained glass with increasing opacity effect
                    if (step > 0) { // Skip first step to create a smoother start
                        ItemStack blackOverlay = new ItemStack(Material.BLACK_STAINED_GLASS);
                        ItemMeta meta = blackOverlay.getItemMeta();
                        if (meta != null) {
                            // Use a simple name that doesn't rely on custom model data
                            meta.setDisplayName(" ");
                            blackOverlay.setItemMeta(meta);
                        }

                        // Put the helmet on the player's head
                        player.getInventory().setHelmet(blackOverlay);
                    }

                    step++;
                }
            }.runTaskTimer(plugin, 0L, 5L); // Every 5 ticks (0.25 seconds) for faster fade
        });
    }

    /**
     * Creates a fade-out effect for a player
     * @param player The player to fade out
     * @param originalHelmet The player's original helmet to restore
     */
    private static void fadeOut(Player player, ItemStack originalHelmet) {
        // Apply a shorter blindness effect for the fade-out
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 0, false, false, true));

        // Start fade-out animation
        new BukkitRunnable() {
            int step = 0;
            final int maxSteps = 3; // Fewer steps for fade-out (faster)

            @Override
            public void run() {
                if (step > maxSteps) {
                    // Restore the player's original helmet
                    player.getInventory().setHelmet(originalHelmet);
                    this.cancel();
                    return;
                }

                // For the last step, restore the original helmet
                if (step == maxSteps) {
                    player.getInventory().setHelmet(originalHelmet);
                } else {
                    // Use gray glass for intermediate steps (getting lighter)
                    Material glassMaterial;
                    switch (step) {
                        case 0:
                            glassMaterial = Material.BLACK_STAINED_GLASS;
                            break;
                        case 1:
                            glassMaterial = Material.GRAY_STAINED_GLASS;
                            break;
                        default:
                            glassMaterial = Material.LIGHT_GRAY_STAINED_GLASS;
                            break;
                    }

                    ItemStack glassHelmet = new ItemStack(glassMaterial);
                    ItemMeta meta = glassHelmet.getItemMeta();
                    if (meta != null) {
                        meta.setDisplayName(" ");
                        glassHelmet.setItemMeta(meta);
                    }

                    player.getInventory().setHelmet(glassHelmet);
                }

                step++;
            }
        }.runTaskTimer(plugin, 0L, 5L); // Every 5 ticks (0.25 seconds)
    }
}
