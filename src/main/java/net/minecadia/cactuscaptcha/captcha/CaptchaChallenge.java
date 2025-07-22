package net.minecadia.cactuscaptcha.captcha;

import net.minecadia.cactuscaptcha.CactusCaptcha;
import net.minecadia.cactuscaptcha.challenge.CaptchaQuestion;
import net.minecadia.cactuscaptcha.challenge.QuestionPool;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Represents a single text-logic captcha challenge for a player.
 * Contains the question, randomized choice positions, and tracks the correct slot.
 */
public class CaptchaChallenge {

    private final CactusCaptcha plugin;
    private final UUID playerId;
    private final CaptchaQuestion question;
    private final List<String> shuffledChoices;
    private final int correctSlot;
    private final long startTime;

    /**
     * Creates a new text-logic captcha challenge for the specified player.
     * 
     * @param plugin The plugin instance
     * @param player The player who will receive this challenge
     */
    public CaptchaChallenge(CactusCaptcha plugin, Player player) {
        this.plugin = plugin;
        this.playerId = player.getUniqueId();
        this.question = QuestionPool.getRandomQuestion();
        this.startTime = System.currentTimeMillis();
        
        // Create a shuffled copy of choices and track where the correct answer ends up
        this.shuffledChoices = new ArrayList<>(question.getChoices());
        String correctAnswer = question.getCorrectAnswer();
        Collections.shuffle(shuffledChoices);
        this.correctSlot = shuffledChoices.indexOf(correctAnswer);
    }

    /**
     * Gets the question for this challenge.
     * 
     * @return The CaptchaQuestion
     */
    public CaptchaQuestion getQuestion() {
        return question;
    }

    /**
     * Gets the question prompt text.
     * 
     * @return The question prompt
     */
    public String getPrompt() {
        return question.getPrompt();
    }

    /**
     * Gets the shuffled choices for display in the GUI.
     * The choices are randomized so the correct answer isn't always in the same position.
     * 
     * @return List of 4 shuffled answer choices
     */
    public List<String> getShuffledChoices() {
        return new ArrayList<>(shuffledChoices);
    }

    /**
     * Gets the slot index (0-3) where the correct answer is located after shuffling.
     * 
     * @return The slot index of the correct answer
     */
    public int getCorrectSlot() {
        return correctSlot;
    }

    /**
     * Checks if the given slot contains the correct answer.
     * 
     * @param slot The slot index to check (0-3)
     * @return true if the slot contains the correct answer, false otherwise
     */
    public boolean isCorrectAnswer(int slot) {
        return slot == correctSlot;
    }

    /**
     * Gets the player ID for this challenge.
     * 
     * @return The player's UUID
     */
    public UUID getPlayerId() {
        return playerId;
    }

    /**
     * Gets the time when this challenge was created.
     * 
     * @return The start time in milliseconds
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * Checks if this challenge has timed out based on the configured timeout.
     * 
     * @return true if the challenge has timed out, false otherwise
     */
    public boolean hasTimedOut() {
        int timeoutSeconds = plugin.getConfig().getInt("captchaTimeoutSeconds", 3);
        long timeoutMillis = timeoutSeconds * 1000L;
        return (System.currentTimeMillis() - startTime) > timeoutMillis;
    }

    /**
     * Gets the remaining time in seconds before this challenge times out.
     * 
     * @return Remaining seconds, or 0 if already timed out
     */
    public int getRemainingSeconds() {
        int timeoutSeconds = plugin.getConfig().getInt("captchaTimeoutSeconds", 3);
        long elapsed = (System.currentTimeMillis() - startTime) / 1000L;
        return Math.max(0, timeoutSeconds - (int) elapsed);
    }

    @Override
    public String toString() {
        return "CaptchaChallenge{" +
                "playerId=" + playerId +
                ", question='" + question.getPrompt() + '\'' +
                ", correctSlot=" + correctSlot +
                ", startTime=" + startTime +
                '}';
    }
}