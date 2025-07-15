package com.zvicraft.theJailBreakShow.utils;

import com.zvicraft.theJailBreakShow.TheJailBreakShow;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class LanguageManager {
    private final TheJailBreakShow plugin;
    private YamlConfiguration langConfig;
    private String currentLang;
    private final Map<String, YamlConfiguration> languageFiles = new HashMap<>();
    private final Map<String, String> cachedMessages = new HashMap<>();

    // Default available languages
    private final String[] defaultLanguages = {"en", "he"};

    public LanguageManager(TheJailBreakShow plugin) {
        this.plugin = plugin;
        loadLanguage();
    }

    /**
     * Loads the language file based on configuration
     */
    public void loadLanguage() {
        // Clear any existing cache
        cachedMessages.clear();
        languageFiles.clear();

        // Create language directory if it doesn't exist
        File langFolder = new File(plugin.getDataFolder(), "lang");
        if (!langFolder.exists()) {
            langFolder.mkdirs();
        }

        // First, save all default language files if they don't exist
        for (String lang : defaultLanguages) {
            saveDefaultLanguageFile(lang);
        }

        // Load all language files from the directory
        File[] langFiles = langFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (langFiles != null) {
            for (File file : langFiles) {
                String langCode = file.getName().replace(".yml", "");
                YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                languageFiles.put(langCode, config);
                plugin.getLogger().info("Loaded language file: " + file.getName());
            }
        }

        // Get language from config, default to English
        String configLang = plugin.getConfigManager().getConfig().getString("language", "en");
        this.currentLang = configLang;

        // Check if the configured language exists, otherwise fall back to English
        if (!languageFiles.containsKey(currentLang)) {
            plugin.getLogger().warning("Configured language '" + currentLang + "' not found. Falling back to English.");
            currentLang = "en";

            // If English doesn't exist either, create a warning
            if (!languageFiles.containsKey("en")) {
                plugin.getLogger().severe("English language file not found! Messages will be missing.");
            }
        }

        // Set the current language config
        langConfig = languageFiles.get(currentLang);
    }

    /**
     * Saves the default language file from resources
     */
    private void saveDefaultLanguageFile(String lang) {
        File langFolder = new File(plugin.getDataFolder(), "lang");
        if (!langFolder.exists()) {
            langFolder.mkdirs();
        }

        File langFile = new File(langFolder, lang + ".yml");
        if (!langFile.exists()) {
            try {
                InputStream resource = plugin.getResource("lang/" + lang + ".yml");
                if (resource != null) {
                    plugin.saveResource("lang/" + lang + ".yml", false);
                    plugin.getLogger().info("Saved default language file: " + lang + ".yml");
                } else {
                    plugin.getLogger().warning("Could not find language resource: lang/" + lang + ".yml");
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to save language file: " + lang + ".yml", e);
            }
        }
    }

    /**
     * Gets a message from the language file
     *
     * @param key The message key
     * @return The translated message
     */
    public String getMessage(String key) {
        // Check cache first
        String cacheKey = currentLang + "." + key;
        if (cachedMessages.containsKey(cacheKey)) {
            return cachedMessages.get(cacheKey);
        }

        // Try to get message from current language
        String message = null;
        if (langConfig != null) {
            message = langConfig.getString(key);
        }

        // If message not found in current language and current language is not English, try English
        if (message == null && !currentLang.equals("en") && languageFiles.containsKey("en")) {
            message = languageFiles.get("en").getString(key);
            if (message != null) {
                plugin.getLogger().info("Using English fallback for key: " + key);
            }
        }

        // If still not found, use a placeholder
        if (message == null) {
            message = "Missing message: " + key;
            plugin.getLogger().warning("Missing language key: " + key + " in " + currentLang + ".yml and fallbacks");
        }

        // Process color codes
        message = ChatColor.translateAlternateColorCodes('&', message);

        // Cache the message
        cachedMessages.put(cacheKey, message);

        return message;
    }

    /**
     * Gets a message from the language file with placeholders
     *
     * @param key          The message key
     * @param replacements The placeholder replacements (placeholder1, value1, placeholder2, value2, ...)
     * @return The translated message with replaced placeholders
     */
    public String getMessage(String key, Object... replacements) {
        String message = getMessage(key);

        // Replace placeholders
        if (replacements != null && replacements.length >= 2) {
            for (int i = 0; i < replacements.length; i += 2) {
                if (i + 1 < replacements.length) {
                    String placeholder = String.valueOf(replacements[i]);
                    String replacement = String.valueOf(replacements[i + 1]);
                    message = message.replace(placeholder, replacement);
                }
            }
        }

        return message;
    }

    /**
     * Reloads the language configuration
     */
    public void reload() {
        loadLanguage();
        plugin.getLogger().info("Language files reloaded. Current language: " + currentLang);
    }

    /**
     * Gets the current language code
     *
     * @return The current language code
     */
    public String getCurrentLanguage() {
        return currentLang;
    }

    /**
     * Gets all available language codes
     *
     * @return Array of available language codes
     */
    public String[] getAvailableLanguages() {
        return languageFiles.keySet().toArray(new String[0]);
    }

    /**
     * Checks if a language is available
     *
     * @param langCode The language code to check
     * @return True if the language is available
     */
    public boolean isLanguageAvailable(String langCode) {
        return languageFiles.containsKey(langCode);
    }
}
