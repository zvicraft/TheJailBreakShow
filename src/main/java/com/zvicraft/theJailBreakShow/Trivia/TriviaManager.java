package com.zvicraft.theJailBreakShow.Trivia;

import com.zvicraft.theJailBreakShow.TheJailBreakShow;
import com.zvicraft.theJailBreakShow.utils.LanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * Manages trivia questions and rewards for the plugin
 */
public class TriviaManager {
    private final TheJailBreakShow plugin;
    private final List<TriviaQuestion> triviaQuestions;
    private final Map<String, TriviaCategory> categories;
    private final Random random;
    private TriviaQuestion currentQuestion;
    private boolean triviaActive;
    private int taskId;
    private int timeLeft;
    private int defaultReward;
    private int defaultTimeLimit;

    /**
     * Represents a trivia question category
     */
    public static class TriviaCategory {
        private final String id;
        private final String nameKey;
        private final ChatColor color;

        public TriviaCategory(String id, String nameKey, ChatColor color) {
            this.id = id;
            this.nameKey = nameKey;
            this.color = color;
        }

        public String getId() {
            return id;
        }

        public String getNameKey() {
            return nameKey;
        }

        public ChatColor getColor() {
            return color;
        }
    }

    /**
     * Represents a trivia question
     */
    public static class TriviaQuestion {
        private final String question;
        private final List<String> answers;
        private final int reward;
        private final String category;
        private final int timeLimit;

        public TriviaQuestion(String question, List<String> answers, int reward, String category, int timeLimit) {
            this.question = question;
            this.answers = answers;
            this.reward = reward;
            this.category = category;
            this.timeLimit = timeLimit;
        }

        public String getQuestion() {
            return question;
        }

        public List<String> getAnswers() {
            return answers;
        }

        public int getReward() {
            return reward;
        }

        public String getCategory() {
            return category;
        }

        public int getTimeLimit() {
            return timeLimit;
        }

        /**
         * Checks if the given answer is correct
         *
         * @param playerAnswer The player's answer
         * @return True if the answer is correct, false otherwise
         */
        public boolean isCorrectAnswer(String playerAnswer) {
            String normalizedPlayerAnswer = playerAnswer.toLowerCase().trim();

            for (String correctAnswer : answers) {
                if (normalizedPlayerAnswer.equalsIgnoreCase(correctAnswer.toLowerCase().trim())) {
                    return true;
                }
            }

            return false;
        }
    }

    /**
     * Creates a new TriviaManager
     *
     * @param plugin The plugin instance
     */
    public TriviaManager(TheJailBreakShow plugin) {
        this.plugin = plugin;
        this.triviaQuestions = new ArrayList<>();
        this.categories = new HashMap<>();
        this.random = new Random();
        this.triviaActive = false;
        this.defaultReward = 50;
        this.defaultTimeLimit = 30;

        // Load trivia questions and categories
        loadCategories();
        loadQuestions();
    }

    /**
     * Loads trivia categories from configuration
     */
    private void loadCategories() {
        // Clear existing categories
        categories.clear();

        // Get categories from config
        ConfigurationSection categoriesSection = plugin.getConfigManager().getConfig().getConfigurationSection("trivia.categories");

        if (categoriesSection == null) {
            // Create default categories
            createDefaultCategories();
            return;
        }

        // Load each category
        for (String categoryId : categoriesSection.getKeys(false)) {
            ConfigurationSection categorySection = categoriesSection.getConfigurationSection(categoryId);
            if (categorySection == null) continue;

            String nameKey = categorySection.getString("name_key", "trivia.category." + categoryId);
            String colorName = categorySection.getString("color", "GREEN");

            ChatColor color;
            try {
                color = ChatColor.valueOf(colorName);
            } catch (IllegalArgumentException e) {
                color = ChatColor.GREEN;
            }

            categories.put(categoryId, new TriviaCategory(categoryId, nameKey, color));
        }

        // If no categories were loaded, create defaults
        if (categories.isEmpty()) {
            createDefaultCategories();
        }
    }

    /**
     * Creates default trivia categories
     */
    private void createDefaultCategories() {
        categories.put("general", new TriviaCategory("general", "trivia.category.general", ChatColor.GREEN));
        categories.put("minecraft", new TriviaCategory("minecraft", "trivia.category.minecraft", ChatColor.GOLD));
        categories.put("history", new TriviaCategory("history", "trivia.category.history", ChatColor.BLUE));
        categories.put("science", new TriviaCategory("science", "trivia.category.science", ChatColor.AQUA));
        categories.put("geography", new TriviaCategory("geography", "trivia.category.geography", ChatColor.DARK_GREEN));

        // Save default categories to config
        saveDefaultCategories();
    }

    /**
     * Saves default categories to configuration
     */
    private void saveDefaultCategories() {
        for (Map.Entry<String, TriviaCategory> entry : categories.entrySet()) {
            String categoryId = entry.getKey();
            TriviaCategory category = entry.getValue();

            String basePath = "trivia.categories." + categoryId + ".";
            plugin.getConfigManager().getConfig().set(basePath + "name_key", category.getNameKey());
            plugin.getConfigManager().getConfig().set(basePath + "color", category.getColor().name());
        }

        plugin.getConfigManager().saveConfig();
    }

    /**
     * Loads trivia questions from configuration
     */
    private void loadQuestions() {
        // Clear existing questions
        triviaQuestions.clear();

        // Load default settings
        defaultReward = plugin.getConfigManager().getConfig().getInt("trivia.default_reward", 50);
        defaultTimeLimit = plugin.getConfigManager().getConfig().getInt("trivia.default_time_limit", 30);

        // Try to load from trivia.yml file
        File triviaFile = new File(plugin.getDataFolder(), "trivia.yml");

        if (!triviaFile.exists()) {
            // Create default trivia questions file
            createDefaultTriviaFile(triviaFile);
        }

        try {
            FileConfiguration triviaConfig = YamlConfiguration.loadConfiguration(triviaFile);
            ConfigurationSection questionsSection = triviaConfig.getConfigurationSection("questions");

            if (questionsSection == null) {
                plugin.getLogger().warning("No questions found in trivia.yml");
                return;
            }

            // Load each question
            for (String questionKey : questionsSection.getKeys(false)) {
                ConfigurationSection questionSection = questionsSection.getConfigurationSection(questionKey);
                if (questionSection == null) continue;

                String question = questionSection.getString("question", "");
                List<String> answers = questionSection.getStringList("answers");
                int reward = questionSection.getInt("reward", defaultReward);
                String category = questionSection.getString("category", "general");
                int timeLimit = questionSection.getInt("time_limit", defaultTimeLimit);

                // Validate the question
                if (question.isEmpty() || answers.isEmpty()) {
                    plugin.getLogger().warning("Invalid trivia question found: " + questionKey);
                    continue;
                }

                // Check if category exists, if not use default
                if (!categories.containsKey(category)) {
                    category = "general";
                }

                triviaQuestions.add(new TriviaQuestion(question, answers, reward, category, timeLimit));
            }

        } catch (Exception e) {
            plugin.getLogger().severe("Error loading trivia questions: " + e.getMessage());
            e.printStackTrace();
        }

        plugin.getLogger().info("Loaded " + triviaQuestions.size() + " trivia questions");
    }

    /**
     * Creates a default trivia questions file
     *
     * @param file The file to create
     */
    private void createDefaultTriviaFile(File file) {
        try {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }

            if (!file.exists()) {
                file.createNewFile();
            }

            FileConfiguration config = YamlConfiguration.loadConfiguration(file);

            // Add some default questions
            config.set("questions.q1.question", "What is the main mineral that redstone ore drops?");
            config.set("questions.q1.answers", List.of("redstone", "redstone dust"));
            config.set("questions.q1.reward", 50);
            config.set("questions.q1.category", "minecraft");
            config.set("questions.q1.time_limit", 30);

            config.set("questions.q2.question", "What year was Minecraft first released?");
            config.set("questions.q2.answers", List.of("2009", "2009 year"));
            config.set("questions.q2.reward", 75);
            config.set("questions.q2.category", "minecraft");
            config.set("questions.q2.time_limit", 30);

            config.set("questions.q3.question", "Which planet is known as the Red Planet?");
            config.set("questions.q3.answers", List.of("mars"));
            config.set("questions.q3.reward", 50);
            config.set("questions.q3.category", "science");
            config.set("questions.q3.time_limit", 30);

            config.set("questions.q4.question", "What is the capital of France?");
            config.set("questions.q4.answers", List.of("paris"));
            config.set("questions.q4.reward", 50);
            config.set("questions.q4.category", "geography");
            config.set("questions.q4.time_limit", 30);

            config.set("questions.q5.question", "In which year did World War II end?");
            config.set("questions.q5.answers", List.of("1945", "1945 year"));
            config.set("questions.q5.reward", 75);
            config.set("questions.q5.category", "history");
            config.set("questions.q5.time_limit", 30);

            config.save(file);
            plugin.getLogger().info("Created default trivia.yml file");

        } catch (IOException e) {
            plugin.getLogger().severe("Could not create default trivia.yml file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Starts a trivia question
     *
     * @return True if the trivia was started, false if already active or no questions available
     */
    public boolean startTrivia() {
        if (triviaActive) {
            return false;
        }

        if (triviaQuestions.isEmpty()) {
            plugin.getLogger().warning("No trivia questions available");
            return false;
        }

        // Select a random question
        currentQuestion = triviaQuestions.get(random.nextInt(triviaQuestions.size()));
        triviaActive = true;
        timeLeft = currentQuestion.getTimeLimit();

        // Get category
        TriviaCategory category = categories.get(currentQuestion.getCategory());
        if (category == null) {
            category = categories.get("general"); // Fallback to general category
        }

        // Get localized category name
        String categoryName = plugin.getLanguageManager().getMessage(category.getNameKey());

        // Broadcast the question
        Bukkit.broadcastMessage(plugin.getLanguageManager().getMessage("trivia.header"));
        Bukkit.broadcastMessage(category.getColor() + plugin.getLanguageManager().getMessage("trivia.category", "%category%", categoryName));
        Bukkit.broadcastMessage(ChatColor.YELLOW + plugin.getLanguageManager().getMessage("trivia.question", "%question%", currentQuestion.getQuestion()));
        Bukkit.broadcastMessage(ChatColor.GREEN + plugin.getLanguageManager().getMessage("trivia.reward", "%amount%", String.valueOf(currentQuestion.getReward())));
        Bukkit.broadcastMessage(ChatColor.GOLD + plugin.getLanguageManager().getMessage("trivia.time_limit", "%seconds%", String.valueOf(currentQuestion.getTimeLimit())));

        // Start the timer
        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this::updateTimer, 20L, 20L); // Run every second

        return true;
    }

    /**
     * Updates the trivia timer
     */
    private void updateTimer() {
        timeLeft--;

        // Time's up, end the trivia
        if (timeLeft <= 0) {
            endTrivia(null);
            return;
        }

        // Show time remaining at specific intervals
        if (timeLeft == 10 || timeLeft == 5 || timeLeft <= 3) {
            Bukkit.broadcastMessage(ChatColor.GOLD + plugin.getLanguageManager().getMessage("trivia.time_remaining", "%seconds%", String.valueOf(timeLeft)));
        }
    }

    /**
     * Ends the current trivia question
     *
     * @param winner The player who answered correctly, or null if time ran out
     */
    public void endTrivia(Player winner) {
        if (!triviaActive) {
            return;
        }

        triviaActive = false;
        Bukkit.getScheduler().cancelTask(taskId);

        if (winner != null) {
            // Award the player
            plugin.getGoldManager().addGold(winner, currentQuestion.getReward());

            // Announce the winner
            Bukkit.broadcastMessage(plugin.getLanguageManager().getMessage("trivia.correct_answer", 
                    "%player%", winner.getName(), 
                    "%amount%", String.valueOf(currentQuestion.getReward())));
        } else {
            // No one answered correctly
            Bukkit.broadcastMessage(plugin.getLanguageManager().getMessage("trivia.no_correct_answer", "%answer%", currentQuestion.getAnswers().get(0)));
        }

        currentQuestion = null;
    }

    /**
     * Checks a player's answer to the current trivia question
     *
     * @param player The player answering
     * @param answer The player's answer
     * @return True if the answer was correct, false otherwise
     */
    public boolean checkAnswer(Player player, String answer) {
        if (!triviaActive || currentQuestion == null) {
            return false;
        }

        if (currentQuestion.isCorrectAnswer(answer)) {
            endTrivia(player);
            return true;
        }

        return false;
    }

    /**
     * Checks if trivia is currently active
     *
     * @return True if trivia is active, false otherwise
     */
    public boolean isTriviaActive() {
        return triviaActive;
    }

    /**
     * Gets a list of all available trivia categories
     *
     * @return A list of trivia categories
     */
    public List<TriviaCategory> getCategories() {
        return new ArrayList<>(categories.values());
    }

    /**
     * Gets the current trivia question
     *
     * @return The current question or null if no trivia is active
     */
    public TriviaQuestion getCurrentQuestion() {
        return currentQuestion;
    }

    /**
     * Gets the remaining time for the current question
     *
     * @return The remaining time in seconds
     */
    public int getTimeLeft() {
        return timeLeft;
    }
}
