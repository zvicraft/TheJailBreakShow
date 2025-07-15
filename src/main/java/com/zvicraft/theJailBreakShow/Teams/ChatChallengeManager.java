package com.zvicraft.theJailBreakShow.Teams;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.Random;

/**
 * Manages chat challenges for guard selection
 */
public class ChatChallengeManager {
    private static final Random random = new Random();

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
     * Starts a new chat challenge
     *
     * @param type The type of challenge to start
     * @return The answer to the challenge
     */
    public static String startChallenge(ChallengeType type) {
        String answer = "";

        switch (type) {
            case MATH:
                answer = createMathChallenge();
                break;
            case TRIVIA:
                answer = createTriviaChallenge();
                break;
            case WORD_UNSCRAMBLE:
                answer = createWordUnscrambleChallenge();
                break;
            case SPEED_TYPE:
                answer = createSpeedTypeChallenge();
                break;
            default:
                answer = createMathChallenge();
                break;
        }

        return answer;
    }

    /**
     * Creates a simple math challenge
     *
     * @return The answer to the math challenge
     */
    private static String createMathChallenge() {
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

        Bukkit.broadcastMessage(ChatColor.GOLD + "==== GUARD SELECTION CHALLENGE ====");
        Bukkit.broadcastMessage(ChatColor.YELLOW + "First to answer correctly becomes the guard!");
        Bukkit.broadcastMessage(ChatColor.AQUA + "What is " + num1 + " " + operation + " " + num2 + "?");

        return answer;
    }

    /**
     * Creates a trivia challenge
     *
     * @return The answer to the trivia challenge
     */
    private static String createTriviaChallenge() {
        String[][] triviaQuestions = {
                {"What is the name of Minecraft's main character?", "steve"},
                {"What block do you use to make a Nether portal?", "obsidian"},
                {"What material are diamonds made from in real life?", "carbon"},
                {"How many sides does a Minecraft block have?", "6"},
                {"What mob only attacks when you look at it?", "enderman"},
                {"What is the rarest ore in Minecraft?", "netherite"},
                {"What is the main food source in Minecraft?", "bread"},
                {"What is the max stack size for most items?", "64"},
                {"How many hearts does a player have by default?", "10"},
                {"What material is used to tame wolves?", "bone"}
        };

        int questionIndex = random.nextInt(triviaQuestions.length);
        String question = triviaQuestions[questionIndex][0];
        String answer = triviaQuestions[questionIndex][1];

        Bukkit.broadcastMessage(ChatColor.GOLD + "==== GUARD SELECTION CHALLENGE ====");
        Bukkit.broadcastMessage(ChatColor.YELLOW + "First to answer correctly becomes the guard!");
        Bukkit.broadcastMessage(ChatColor.AQUA + "Trivia: " + question);

        return answer;
    }

    /**
     * Creates a word unscramble challenge
     *
     * @return The answer to the unscramble challenge
     */
    private static String createWordUnscrambleChallenge() {
        String[] words = {"diamond", "creeper", "minecraft", "zombie", "skeleton", "enderman",
                "spider", "pickaxe", "shovel", "sword", "crafting", "mining", "building"};

        String word = words[random.nextInt(words.length)];
        String scrambled = scrambleWord(word);

        Bukkit.broadcastMessage(ChatColor.GOLD + "==== GUARD SELECTION CHALLENGE ====");
        Bukkit.broadcastMessage(ChatColor.YELLOW + "First to unscramble the word correctly becomes the guard!");
        Bukkit.broadcastMessage(ChatColor.AQUA + "Unscramble: " + scrambled);

        return word;
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
     * @return The answer to the speed typing challenge
     */
    private static String createSpeedTypeChallenge() {
        String[] phrases = {
                "the quick brown fox jumps over the lazy dog",
                "minecraft is a game about breaking and placing blocks",
                "diamonds are a player's best friend",
                "never dig straight down in minecraft",
                "crafting is an essential skill for survival",
                "watch out for creepers they might blow up",
                "the nether is a dangerous place to explore"
        };

        String phrase = phrases[random.nextInt(phrases.length)];

        Bukkit.broadcastMessage(ChatColor.GOLD + "==== GUARD SELECTION CHALLENGE ====");
        Bukkit.broadcastMessage(ChatColor.YELLOW + "First to type the phrase correctly becomes the guard!");
        Bukkit.broadcastMessage(ChatColor.AQUA + "Type: " + phrase);

        return phrase;
    }
}
