package com.zvicraft.theJailBreakShow.Teams;

import com.zvicraft.theJailBreakShow.TheJailBreakShow;
import com.zvicraft.theJailBreakShow.utils.LanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Manages chat challenges for guard selection
 */
public class ChatChallengeManager {
    private static TheJailBreakShow plugin;
    private static final Random random = new Random();
    private static Map<ChallengeType, List<Challenge>> challenges = Map.of();
    private final List<ChallengeType> enabledTypes;

    /**
     * Challenge types that can be used for guard selection
     */
    public enum ChallengeType {
        MATH,
        TRIVIA,
        WORD_UNSCRAMBLE,
        SPEED_TYPE
    }

    /**
     * Represents a challenge (question and answer)
     */
    private static class Challenge {
        private final String question;
        private final String answer;
        private final String[] variants;

        public Challenge(String question, String answer) {
            this.question = question;
            this.answer = answer;
            this.variants = new String[0];
        }

        public Challenge(String question, String answer, String[] variants) {
            this.question = question;
            this.answer = answer;
            this.variants = variants;
        }

        public String getQuestion() {
            return question;
        }

        public String getAnswer() {
            return answer;
        }

        public String[] getVariants() {
            return variants;
        }
    }

    /**
     * Creates a new ChatChallengeManager
     *
     * @param plugin The plugin instance
     */
    public ChatChallengeManager(TheJailBreakShow plugin) {
        this.plugin = plugin;
        this.challenges = new HashMap<>();
        this.enabledTypes = new ArrayList<>();

        // Initialize challenge maps for each type
        for (ChallengeType type : ChallengeType.values()) {
            challenges.put(type, new ArrayList<>());
        }

        // Load challenges from configuration
        loadChallenges();
    }

    /**
     * Loads challenges from configuration
     */
    private void loadChallenges() {
        // Clear existing challenges
        for (List<Challenge> challengeList : challenges.values()) {
            challengeList.clear();
        }
        enabledTypes.clear();

        // Try to load from challenges.yml file
        File challengesFile = new File(plugin.getDataFolder(), "challenges.yml");

        if (!challengesFile.exists()) {
            // Create default challenges file
            createDefaultChallengesFile(challengesFile);
        }

        try {
            FileConfiguration config = YamlConfiguration.loadConfiguration(challengesFile);

            // Load enabled challenge types
            List<String> enabledTypesStr = config.getStringList("enabled_types");
            for (String typeStr : enabledTypesStr) {
                try {
                    ChallengeType type = ChallengeType.valueOf(typeStr.toUpperCase());
                    enabledTypes.add(type);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Unknown challenge type: " + typeStr);
                }
            }

            // If no types are enabled, enable all types
            if (enabledTypes.isEmpty()) {
                for (ChallengeType type : ChallengeType.values()) {
                    enabledTypes.add(type);
                }
            }

            // Load each challenge type
            for (ChallengeType type : ChallengeType.values()) {
                String typePath = type.name().toLowerCase();
                ConfigurationSection typeSection = config.getConfigurationSection(typePath);

                if (typeSection != null) {
                    for (String key : typeSection.getKeys(false)) {
                        ConfigurationSection challengeSection = typeSection.getConfigurationSection(key);
                        if (challengeSection == null) continue;

                        String question = challengeSection.getString("question", "");
                        String answer = challengeSection.getString("answer", "");
                        List<String> variantsList = challengeSection.getStringList("variants");
                        String[] variants = variantsList.toArray(new String[0]);

                        if (!question.isEmpty() && !answer.isEmpty()) {
                            challenges.get(type).add(new Challenge(question, answer, variants));
                        }
                    }
                }
            }

        } catch (Exception e) {
            plugin.getLogger().severe("Error loading challenges: " + e.getMessage());
            e.printStackTrace();

            // If loading fails, create default challenges
            createDefaultChallenges();
        }

        // Verify we have at least some challenges for each enabled type
        for (ChallengeType type : enabledTypes.toArray(new ChallengeType[0])) {
            if (challenges.get(type).isEmpty()) {
                plugin.getLogger().warning("No challenges found for type: " + type + ". Creating defaults.");
                createDefaultChallengesForType(type);
            }
        }
    }

    /**
     * Creates a default challenges file
     *
     * @param file The file to create
     */
    private void createDefaultChallengesFile(File file) {
        try {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }

            if (!file.exists()) {
                file.createNewFile();
            }

            FileConfiguration config = YamlConfiguration.loadConfiguration(file);

            // Enable all challenge types by default
            List<String> enabledTypes = new ArrayList<>();
            for (ChallengeType type : ChallengeType.values()) {
                enabledTypes.add(type.name());
            }
            config.set("enabled_types", enabledTypes);

            // Add default math challenges
            config.set("math.example.question", "What is 5 + 3?");
            config.set("math.example.answer", "8");

            // Add default trivia challenges
            config.set("trivia.q1.question", "What is the name of Minecraft's main character?");
            config.set("trivia.q1.answer", "steve");
            config.set("trivia.q1.variants", List.of("steve"));

            config.set("trivia.q2.question", "What block do you use to make a Nether portal?");
            config.set("trivia.q2.answer", "obsidian");
            config.set("trivia.q2.variants", List.of("obsidian"));

            // Add default word unscramble challenges
            config.set("word_unscramble.w1.question", "diaomnd");
            config.set("word_unscramble.w1.answer", "diamond");

            config.set("word_unscramble.w2.question", "creeepr");
            config.set("word_unscramble.w2.answer", "creeper");

            // Add default speed type challenges
            config.set("speed_type.s1.question", "the quick brown fox jumps over the lazy dog");
            config.set("speed_type.s1.answer", "the quick brown fox jumps over the lazy dog");

            config.set("speed_type.s2.question", "minecraft is a game about breaking and placing blocks");
            config.set("speed_type.s2.answer", "minecraft is a game about breaking and placing blocks");

            config.save(file);
            plugin.getLogger().info("Created default challenges.yml file");

        } catch (IOException e) {
            plugin.getLogger().severe("Could not create default challenges.yml file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Creates default challenges for all types
     */
    private void createDefaultChallenges() {
        for (ChallengeType type : ChallengeType.values()) {
            createDefaultChallengesForType(type);
        }
    }

    /**
     * Creates default challenges for a specific type
     *
     * @param type The challenge type
     */
    private static void createDefaultChallengesForType(ChallengeType type) {
        List<Challenge> challengeList = challenges.get(type);
        challengeList.clear();

        switch (type) {
            case MATH:
                // Math challenges are generated dynamically
                break;

            case TRIVIA:
                challengeList.add(new Challenge("What is the name of Minecraft's main character?", "steve"));
                challengeList.add(new Challenge("What block do you use to make a Nether portal?", "obsidian"));
                challengeList.add(new Challenge("What material are diamonds made from in real life?", "carbon"));
                challengeList.add(new Challenge("How many sides does a Minecraft block have?", "6"));
                challengeList.add(new Challenge("What mob only attacks when you look at it?", "enderman"));
                challengeList.add(new Challenge("What is the rarest ore in Minecraft?", "netherite"));
                challengeList.add(new Challenge("What is the main food source in Minecraft?", "bread"));
                challengeList.add(new Challenge("What is the max stack size for most items?", "64"));
                challengeList.add(new Challenge("How many hearts does a player have by default?", "10"));
                challengeList.add(new Challenge("What material is used to tame wolves?", "bone"));
                break;

            case WORD_UNSCRAMBLE:
                String[] words = {"diamond", "creeper", "minecraft", "zombie", "skeleton", "enderman",
                        "spider", "pickaxe", "shovel", "sword", "crafting", "mining", "building"};

                for (String word : words) {
                    challengeList.add(new Challenge(scrambleWord(word), word));
                }
                break;

            case SPEED_TYPE:
                String[] phrases = {
                        "the quick brown fox jumps over the lazy dog",
                        "minecraft is a game about breaking and placing blocks",
                        "diamonds are a player's best friend",
                        "never dig straight down in minecraft",
                        "crafting is an essential skill for survival",
                        "watch out for creepers they might blow up",
                        "the nether is a dangerous place to explore"
                };

                for (String phrase : phrases) {
                    challengeList.add(new Challenge(phrase, phrase));
                }
                break;
        }
    }

    /**
     * Starts a new chat challenge
     *
     * @param type The type of challenge to start
     * @return The answer to the challenge
     */
    public static String startChallenge(ChallengeType type) {
        String answer = "";
        LanguageManager lang = plugin.getLanguageManager(); // Assuming you have access to the language manager

        switch (type) {
            case MATH:
                answer = createMathChallenge(lang);
                break;
            case TRIVIA:
                answer = createTriviaChallenge(lang);
                break;
            case WORD_UNSCRAMBLE:
                answer = createWordUnscrambleChallenge(lang);
                break;
            case SPEED_TYPE:
                answer = createSpeedTypeChallenge(lang);
                break;
            default:
                answer = createMathChallenge(lang);
                break;
        }

        return answer;
    }

    /**
     * Creates a simple math challenge
     *
     * @param lang The language manager for localization
     * @return The answer to the math challenge
     */
    private static String createMathChallenge(LanguageManager lang) {
        int num1 = random.nextInt(15) + 1;
        int num2 = random.nextInt(15) + 1;
        String operation;
        String answer;

        switch (random.nextInt(3)) {
            case 0: // Addition
                operation = "+";
                answer = String.valueOf(num1 + num2);
                break;
            case 1: // Subtraction
                // Ensure num1 >= num2 to avoid negative results
                if (num1 < num2) {
                    int temp = num1;
                    num1 = num2;
                    num2 = temp;
                }
                operation = "-";
                answer = String.valueOf(num1 - num2);
                break;
            case 2: // Multiplication
                // Use smaller numbers for multiplication
                num1 = random.nextInt(10) + 1;
                num2 = random.nextInt(10) + 1;
                operation = "*";
                answer = String.valueOf(num1 * num2);
                break;
            default:
                operation = "+";
                answer = String.valueOf(num1 + num2);
        }

        // Announce the math question
        String question = lang.getMessage("guard_selection.math_question",
                "%num1%", String.valueOf(num1),
                "%operation%", operation,
                "%num2%", String.valueOf(num2));
        Bukkit.broadcastMessage(ChatColor.AQUA + question);

        return answer;
    }

    /**
     * Creates a trivia challenge
     *
     * @param lang The language manager for localization
     * @return The answer to the trivia challenge
     */
    private static String createTriviaChallenge(LanguageManager lang) {
        List<Challenge> triviaList = challenges.get(ChallengeType.TRIVIA);

        // Check if we have any trivia questions
        if (triviaList.isEmpty()) {
            createDefaultChallengesForType(ChallengeType.TRIVIA);
            triviaList = challenges.get(ChallengeType.TRIVIA);

            // If still empty, fall back to math challenge
            if (triviaList.isEmpty()) {
                return createMathChallenge(lang);
            }
        }

        // Select a random question
        Challenge challenge = triviaList.get(random.nextInt(triviaList.size()));
        String question = challenge.getQuestion();
        String answer = challenge.getAnswer();

        // Announce the trivia question
        Bukkit.broadcastMessage(ChatColor.AQUA + lang.getMessage("guard_selection.trivia_prefix") + " " + question);

        return answer;
    }

    /**
     * Creates a word unscramble challenge
     *
     * @param lang The language manager for localization
     * @return The answer to the unscramble challenge
     */
    private static String createWordUnscrambleChallenge(LanguageManager lang) {
        List<Challenge> wordList = challenges.get(ChallengeType.WORD_UNSCRAMBLE);

        // Check if we have any word unscramble challenges
        if (wordList.isEmpty()) {
            // If no pre-defined challenges, create some with random words
            String[] words = {"diamond", "creeper", "minecraft", "zombie", "skeleton", "enderman",
                    "spider", "pickaxe", "shovel", "sword", "crafting", "mining", "building"};

            for (String word : words) {
                wordList.add(new Challenge(scrambleWord(word), word));
            }

            // If still empty, fall back to math challenge
            if (wordList.isEmpty()) {
                return createMathChallenge(lang);
            }
        }

        // Select a random challenge
        Challenge challenge = wordList.get(random.nextInt(wordList.size()));
        String scrambled = challenge.getQuestion();
        String answer = challenge.getAnswer();

        // For word unscramble, the question is already the scrambled word
        // But we can also regenerate it to avoid repetition
        if (random.nextBoolean()) {
            scrambled = scrambleWord(answer);
        }

        // Announce the unscramble challenge
        Bukkit.broadcastMessage(ChatColor.AQUA + lang.getMessage("guard_selection.unscramble_prefix") + " " + scrambled);

        return answer;
    }

    /**
     * Scrambles a word by randomizing its letters
     *
     * @param word The word to scramble
     * @return The scrambled word
     */
    private static String scrambleWord(String word) {
        char[] chars = word.toCharArray();

        // Fisher-Yates shuffle
        for (int i = chars.length - 1; i > 0; i--) {
            int index = random.nextInt(i + 1);
            char temp = chars[index];
            chars[index] = chars[i];
            chars[i] = temp;
        }

        // Make sure the scrambled word is different from the original
        String scrambled = new String(chars);
        if (scrambled.equals(word)) {
            return scrambleWord(word); // Try again if we got the same word
        }

        return scrambled;
    }

    /**
     * Creates a speed typing challenge
     *
     * @param lang The language manager for localization
     * @return The answer to the speed typing challenge
     */
    private static String createSpeedTypeChallenge(LanguageManager lang) {
        List<Challenge> phraseList = challenges.get(ChallengeType.SPEED_TYPE);

        // Check if we have any speed type challenges
        if (phraseList.isEmpty()) {
            createDefaultChallengesForType(ChallengeType.SPEED_TYPE);
            phraseList = challenges.get(ChallengeType.SPEED_TYPE);

            // If still empty, fall back to math challenge
            if (phraseList.isEmpty()) {
                return createMathChallenge(lang);
            }
        }

        // Select a random challenge
        Challenge challenge = phraseList.get(random.nextInt(phraseList.size()));
        String phrase = challenge.getQuestion();

        // Announce the speed typing challenge
        Bukkit.broadcastMessage(ChatColor.AQUA + lang.getMessage("guard_selection.type_prefix") + " " + phrase);

        return phrase;
    }

    /**
     * Gets a random challenge type from the enabled types
     *
     * @return A random challenge type
     */
    public ChallengeType getRandomType() {
        if (enabledTypes.isEmpty()) {
            // Enable all types if none are enabled
            for (ChallengeType type : ChallengeType.values()) {
                enabledTypes.add(type);
            }
        }

        return enabledTypes.get(random.nextInt(enabledTypes.size()));
    }

    /**
     * Reloads challenges from configuration
     */
    public void reload() {
        loadChallenges();
    }
}