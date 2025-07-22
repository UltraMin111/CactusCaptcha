package net.minecadia.cactuscaptcha.listener;

import net.minecadia.cactuscaptcha.CactusCaptcha;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles the captcha timer system that periodically forces captcha challenges.
 * Maintains a countdown for each player and triggers challenges when the timer reaches zero.
 */
public class TimerRunnable extends BukkitRunnable {
    
    private final CactusCaptcha plugin;
    private final Map<UUID, Integer> playerCountdowns;
    
    public TimerRunnable(CactusCaptcha plugin) {
        this.plugin = plugin;
        this.playerCountdowns = new ConcurrentHashMap<>();
    }
    
    @Override
    public void run() {
        // Check if timer is enabled
        if (!plugin.getConfig().getBoolean("captchaTimer.enabled", true)) {
            return;
        }
        
        int secondsBetweenCaptchas = plugin.getConfig().getInt("captchaTimer.secondsBetweenCaptchas", 60);
        
        // Process all online players
        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID playerId = player.getUniqueId();
            
            // Skip players with bypass permission
            if (player.hasPermission("cactuscaptcha.bypass")) {
                continue;
            }
            
            // Skip players who already have an active challenge
            if (plugin.getCaptchaManager().hasActiveChallenge(player)) {
                continue;
            }
            
            // Get or initialize countdown for this player
            Integer countdown = playerCountdowns.get(playerId);
            if (countdown == null) {
                // New player - set initial countdown
                countdown = secondsBetweenCaptchas;
                playerCountdowns.put(playerId, countdown);
                continue;
            }
            
            // Decrement countdown
            countdown--;
            
            if (countdown <= 0) {
                // Time's up - force captcha challenge
                plugin.getCaptchaManager().startChallenge(player, player.getLocation());
                
                // Reset countdown for next challenge
                playerCountdowns.put(playerId, secondsBetweenCaptchas);
            } else {
                // Update countdown
                playerCountdowns.put(playerId, countdown);
            }
        }
        
        // Clean up countdowns for offline players
        playerCountdowns.entrySet().removeIf(entry -> 
            Bukkit.getPlayer(entry.getKey()) == null
        );
    }
    
    /**
     * Resets the countdown for a specific player.
     * Called when a player successfully completes a captcha.
     * 
     * @param playerId The player's UUID
     */
    public void resetCountdown(UUID playerId) {
        int secondsBetweenCaptchas = plugin.getConfig().getInt("captchaTimer.secondsBetweenCaptchas", 60);
        playerCountdowns.put(playerId, secondsBetweenCaptchas);
    }
    
    /**
     * Gets the remaining time until the next captcha for a player.
     * 
     * @param playerId The player's UUID
     * @return Remaining seconds, or -1 if player not found
     */
    public int getRemainingTime(UUID playerId) {
        Integer countdown = playerCountdowns.get(playerId);
        return countdown != null ? countdown : -1;
    }
    
    /**
     * Forces an immediate captcha for a player by setting their countdown to 0.
     * 
     * @param playerId The player's UUID
     */
    public void forceCaptcha(UUID playerId) {
        playerCountdowns.put(playerId, 0);
    }
    
    /**
     * Removes a player from the timer system.
     * 
     * @param playerId The player's UUID
     */
    public void removePlayer(UUID playerId) {
        playerCountdowns.remove(playerId);
    }
    
    /**
     * Gets all player countdowns (for admin GUI display).
     * 
     * @return Map of player UUIDs to remaining seconds
     */
    public Map<UUID, Integer> getAllCountdowns() {
        return new ConcurrentHashMap<>(playerCountdowns);
    }
}