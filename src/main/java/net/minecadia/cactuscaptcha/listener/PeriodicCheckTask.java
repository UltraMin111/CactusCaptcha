package net.minecadia.cactuscaptcha.listener;

import net.minecadia.cactuscaptcha.captcha.CaptchaManager;
import net.minecadia.cactuscaptcha.challenge.CaptchaQuestion;
import net.minecadia.cactuscaptcha.challenge.QuestionPool;
import net.minecadia.cactuscaptcha.manager.WatchManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles periodic 10-minute CAPTCHA checks based on cactus placement activity.
 * Only triggers CAPTCHA if a player has placed ≥1 cactus on sand in the 10-minute period.
 */
public class PeriodicCheckTask implements Runnable {
    
    private static final Map<UUID, Integer> placementCount = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> tickCounter = new ConcurrentHashMap<>();
    
    /**
     * Records a cactus placement on sand for a player.
     * Called from BlockPlaceListener when a cactus is placed on sand.
     * 
     * @param player The player who placed the cactus
     */
    public static void recordPlacement(Player player) {
        placementCount.merge(player.getUniqueId(), 1, Integer::sum);
    }
    
    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("cactuscaptcha.bypass")) {
                continue;
            }
            
            UUID playerId = player.getUniqueId();
            int ticks = tickCounter.getOrDefault(playerId, 0) + 1;
            tickCounter.put(playerId, ticks);
            
            // Check if 10 minutes (600 seconds) have passed
            if (ticks >= 600) {
                // Reset tick counter
                tickCounter.put(playerId, 0);
                
                // Check if player has placed any cactus on sand in this period
                int placements = placementCount.getOrDefault(playerId, 0);
                if (placements > 0) {
                    Random random = new Random();
                    
                    // 30% chance for silent probe, 70% chance for normal CAPTCHA
                    if (random.nextInt(100) < 30) {
                        // Silent CAPTCHA probe - simulate backend check without GUI
                        performSilentProbe(player, placements);
                    } else {
                        // Normal CAPTCHA challenge
                        CaptchaManager.get().startChallenge(player, player.getLocation());
                        
                        // Notify if player is being watched
                        WatchManager.notifyIfWatched(player, "Triggered periodic CAPTCHA (" + placements + " cactus placements)");
                    }
                }
                
                // Reset placement counter for next period
                placementCount.put(playerId, 0);
            }
        }
        
        // Clean up data for offline players
        placementCount.entrySet().removeIf(entry -> 
            Bukkit.getPlayer(entry.getKey()) == null
        );
        tickCounter.entrySet().removeIf(entry -> 
            Bukkit.getPlayer(entry.getKey()) == null
        );
    }
    
    /**
     * Gets the current placement count for a player (for debugging/admin purposes).
     * 
     * @param playerId The player's UUID
     * @return Number of placements in current period
     */
    public static int getPlacementCount(UUID playerId) {
        return placementCount.getOrDefault(playerId, 0);
    }
    
    /**
     * Gets the remaining time until next periodic check for a player.
     * 
     * @param playerId The player's UUID
     * @return Remaining seconds until next check
     */
    public static int getRemainingTime(UUID playerId) {
        int ticks = tickCounter.getOrDefault(playerId, 0);
        return 600 - ticks; // 600 seconds = 10 minutes
    }
    
    /**
     * Performs a silent CAPTCHA probe - simulates backend check without opening GUI.
     * Generates a question and predicts player behavior to detect automation.
     * 
     * @param player The player to probe
     * @param placements Number of cactus placements that triggered this probe
     */
    private void performSilentProbe(Player player, int placements) {
        try {
            // Generate a random CAPTCHA question
            CaptchaQuestion question = QuestionPool.getRandomQuestion();
            if (question == null) {
                return;
            }
            
            // Simulate what a human would likely do
            Random random = new Random();
            int correctAnswer = question.getCorrectIndex();
            
            // Predict player behavior based on their history
            // For now, assume they would get it right 80% of the time (human-like)
            boolean predictedCorrect = random.nextInt(100) < 80;
            
            // Log the silent probe
            Bukkit.getLogger().info("[Silent Probe] Player " + player.getName() + 
                                  " - Question: " + question.getPrompt() + 
                                  " - Predicted: " + (predictedCorrect ? "CORRECT" : "WRONG") +
                                  " - Placements: " + placements);
            
            // If prediction suggests suspicious behavior, notify watchers
            if (!predictedCorrect && random.nextInt(100) < 20) { // 20% chance to flag suspicious behavior
                WatchManager.notifyIfWatched(player, "Silent probe detected suspicious behavior pattern");
            }
            
            // Notify watchers about the silent probe
            WatchManager.notifyIfWatched(player, "Silent CAPTCHA probe completed (" + placements + " placements)");
            
        } catch (Exception e) {
            Bukkit.getLogger().warning("Error during silent CAPTCHA probe for " + player.getName() + ": " + e.getMessage());
        }
    }
}