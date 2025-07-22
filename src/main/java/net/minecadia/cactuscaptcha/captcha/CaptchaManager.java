package net.minecadia.cactuscaptcha.captcha;

import net.minecadia.cactuscaptcha.CactusCaptcha;
import net.minecadia.cactuscaptcha.gui.CaptchaGUI;
import net.minecadia.cactuscaptcha.manager.WatchManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CaptchaManager {

    private static CaptchaManager instance;
    private final CactusCaptcha plugin;
    private final Map<UUID, CaptchaChallenge> activeChallenges;
    private final Map<UUID, Location> pendingPlacements;
    private final Map<UUID, Long> lastCaptchaTime;
    
    // Behavioral detection tracking
    private final Map<UUID, Long> captchaStartTimes;
    private final Map<UUID, List<Integer>> recentFailedSlots;
    private final Map<UUID, List<Long>> solveTimes;

    public CaptchaManager(CactusCaptcha plugin) {
        this.plugin = plugin;
        this.activeChallenges = new ConcurrentHashMap<>();
        this.pendingPlacements = new HashMap<>();
        this.lastCaptchaTime = new ConcurrentHashMap<>();
        
        // Initialize behavioral detection tracking
        this.captchaStartTimes = new ConcurrentHashMap<>();
        this.recentFailedSlots = new ConcurrentHashMap<>();
        this.solveTimes = new ConcurrentHashMap<>();
        
        instance = this;
    }

    public static CaptchaManager get() {
        return instance;
    }

    public boolean shouldChallenge(Player player) {
        long last = lastCaptchaTime.getOrDefault(player.getUniqueId(), 0L);
        return (Instant.now().getEpochSecond() - last) >= 300;
    }

    public void startChallenge(Player player, Location cactusLocation) {
        openCaptcha(player, cactusLocation);
    }

    public void openCaptcha(Player player, Location cactusLocation) {
        UUID playerId = player.getUniqueId();
        
        // Close any existing captcha for this player
        if (activeChallenges.containsKey(playerId)) {
            closeCaptcha(player, false);
        }

        // Store the pending placement location
        pendingPlacements.put(playerId, cactusLocation);

        // Create new captcha challenge
        CaptchaChallenge challenge = new CaptchaChallenge(plugin, player);
        activeChallenges.put(playerId, challenge);

        // Start tracking solve time for behavioral detection
        captchaStartTimes.put(playerId, System.currentTimeMillis());

        // Create and open GUI
        Inventory gui = CaptchaGUI.createCaptchaGUI(challenge);
        player.openInventory(gui);

        // Start timeout timer
        int timeoutSeconds = plugin.getConfig().getInt("captchaTimeoutSeconds", 3);
        new BukkitRunnable() {
            int timeLeft = timeoutSeconds;

            @Override
            public void run() {
                if (!activeChallenges.containsKey(playerId)) {
                    cancel();
                    return;
                }

                if (timeLeft <= 0) {
                    // Timeout - send timeout message and mark as fail
                    if (!plugin.getConfig().getBoolean("silentMode", false)) {
                        player.sendMessage(plugin.getMessage("timeout"));
                    }
                    closeCaptcha(player, false);
                    cancel();
                    return;
                }

                // Update timer display
                if (player.getOpenInventory() != null) {
                    CaptchaGUI.updateTimer(player.getOpenInventory().getTopInventory(), timeLeft);
                }
                timeLeft--;
            }
        }.runTaskTimer(plugin, 0L, 20L); // Run every second
    }


    public boolean handleGuiClick(Player player, int slot) {
        UUID playerId = player.getUniqueId();
        CaptchaChallenge challenge = activeChallenges.get(playerId);
        
        if (challenge == null) {
            return false;
        }

        // Check for honeytoken slot clicks (slots 5, 6, 7)
        if (slot >= 5 && slot <= 7) {
            WatchManager.notifyIfWatched(player, "Clicked honeytoken slot " + slot + " (potential automation)");
            plugin.getLogger().info("Player " + player.getName() + " clicked honeytoken slot " + slot);
            // Still allow the click to proceed as if it was a wrong answer
            handleWrongAnswer(player);
            return false;
        }

        // Check if clicked slot is one of the answer quadrants (0-3)
        if (slot < 0 || slot > 3) {
            return false;
        }

        boolean correct = challenge.isCorrectAnswer(slot);
        
        if (correct) {
            // Correct answer - calculate solve time and update behavioral stats
            handleCorrectAnswer(player);
            return true;
        } else {
            // Wrong answer - track slot click repetition
            trackFailedSlotClick(player, slot);
            handleWrongAnswer(player);
            return false;
        }
    }

    private void handleCorrectAnswer(Player player) {
        UUID playerId = player.getUniqueId();
        
        // Track solve time for behavioral detection
        trackSolveTime(player);
        
        // Record the timestamp of successful CAPTCHA completion
        lastCaptchaTime.put(playerId, Instant.now().getEpochSecond());
        
        // Increment passes in storage
        plugin.getStorageManager().incrementPasses(playerId);
        
        // Reset fail counters on success
        plugin.getStorageManager().resetCaptchaFails(playerId);
        plugin.getStorageManager().resetFailChain(playerId);
        
        // Reset timer countdown for successful completion
        if (plugin.getTimerRunnable() != null) {
            plugin.getTimerRunnable().resetCountdown(playerId);
        }
        
        // Note: No longer auto-placing blocks - let original placement event proceed
        
        // Send success message and sound
        if (!plugin.getConfig().getBoolean("silentMode", false)) {
            player.sendMessage(plugin.getMessage("success"));
            player.playSound(player.getLocation(), org.bukkit.Sound.LEVEL_UP, 1.0f, 1.0f);
        }
        
        // Close captcha
        closeCaptcha(player, true);
    }

    private void handleWrongAnswer(Player player) {
        UUID playerId = player.getUniqueId();
        
        // Increment fails in storage for overall statistics
        plugin.getStorageManager().incrementFails(playerId);
        
        // Get player stats and increment captcha fails for this instance
        plugin.getStorageManager().incrementCaptchaFails(playerId);
        
        // Send fail message
        if (!plugin.getConfig().getBoolean("silentMode", false)) {
            player.sendMessage(plugin.getMessage("fail"));
        }
        
        // Notify staff if player is being watched
        WatchManager.notifyIfWatched(player, "Failed CAPTCHA attempt");
        
        // Check if max tries reached for this CAPTCHA instance
        int maxTries = plugin.getConfig().getInt("captcha.maxTries", 3);
        int currentFails = plugin.getStorageManager().getCaptchaFails(playerId);
        
        if (currentFails >= maxTries) {
            // Reset current captcha fails and record failure chain
            plugin.getStorageManager().resetCaptchaFails(playerId);
            plugin.getStorageManager().recordFailureChain(playerId);

            // Calculate cooldown based on failure chain count
            int chain = plugin.getStorageManager().getFailChainCount(playerId) - 1;
            java.util.List<Integer> punishments = plugin.getConfig().getIntegerList("captcha.punishmentCooldownsMinutes");
            if (punishments.isEmpty()) {
                punishments = java.util.Arrays.asList(3, 10, 60, 120); // fallback
            }
            int minutes = punishments.get(Math.min(chain, punishments.size() - 1));

            // Set cooldown
            plugin.getStorageManager().setCooldownUntil(playerId, System.currentTimeMillis() + (minutes * 60 * 1000L));
            if (!plugin.getConfig().getBoolean("silentMode", false)) {
                player.sendMessage("§cYou've failed the CAPTCHA 3 times. Cactus & sand are now disabled for " + minutes + " minutes.");
            }
            
            // Notify staff if player is being watched - this is a serious event
            WatchManager.notifyIfWatched(player, "Failed CAPTCHA " + maxTries + " times - " + minutes + " minute cooldown applied");
            
            closeCaptcha(player, false);
        } else {
            // Generate new challenge for retry
            CaptchaChallenge newChallenge = new CaptchaChallenge(plugin, player);
            activeChallenges.put(playerId, newChallenge);
            
            // Update GUI with new challenge
            Inventory gui = CaptchaGUI.createCaptchaGUI(newChallenge);
            player.openInventory(gui);
        }
    }

    public void closeCaptcha(Player player, boolean success) {
        UUID playerId = player.getUniqueId();
        
        activeChallenges.remove(playerId);
        if (!success) {
            pendingPlacements.remove(playerId);
        }
        
        player.closeInventory();
    }

    public boolean hasActiveChallenge(Player player) {
        return activeChallenges.containsKey(player.getUniqueId());
    }
    
    /**
     * Tracks failed slot clicks for behavioral detection
     */
    private void trackFailedSlotClick(Player player, int slot) {
        UUID playerId = player.getUniqueId();
        List<Integer> failedSlots = recentFailedSlots.computeIfAbsent(playerId, k -> new LinkedList<>());
        
        // Keep only the last 10 failed clicks
        if (failedSlots.size() >= 10) {
            failedSlots.remove(0);
        }
        failedSlots.add(slot);
        
        // Check for slot click repetition (same slot clicked 2+ times in a row)
        if (failedSlots.size() >= 2) {
            int lastSlot = failedSlots.get(failedSlots.size() - 1);
            int secondLastSlot = failedSlots.get(failedSlots.size() - 2);
            
            if (lastSlot == secondLastSlot) {
                WatchManager.notifyIfWatched(player, "Slot click repetition detected (slot " + slot + " clicked repeatedly)");
            }
        }
    }
    
    /**
     * Calculates and categorizes solve time for behavioral detection
     */
    private void trackSolveTime(Player player) {
        UUID playerId = player.getUniqueId();
        Long startTime = captchaStartTimes.get(playerId);
        
        if (startTime == null) {
            return;
        }
        
        long solveTimeMs = System.currentTimeMillis() - startTime;
        double solveTimeSeconds = solveTimeMs / 1000.0;
        
        // Store solve time
        List<Long> playerSolveTimes = solveTimes.computeIfAbsent(playerId, k -> new LinkedList<>());
        if (playerSolveTimes.size() >= 10) {
            playerSolveTimes.remove(0);
        }
        playerSolveTimes.add(solveTimeMs);
        
        // Categorize solve time
        String category;
        if (solveTimeSeconds < 1.0) {
            category = "FAST (<1s)";
        } else if (solveTimeSeconds <= 2.0) {
            category = "MEDIUM (1-2s)";
        } else if (solveTimeSeconds <= 5.0) {
            category = "SLOW (2-5s)";
        } else {
            category = "HUMAN (>5s)";
        }
        
        // Check for suspicious fast solving pattern
        if (playerSolveTimes.size() >= 10) {
            long fastSolves = playerSolveTimes.stream().mapToLong(time -> time < 1000 ? 1 : 0).sum();
            if (fastSolves >= 10) {
                WatchManager.notifyIfWatched(player, "Suspicious solve speed pattern (10 CAPTCHAs solved in <1s)");
            }
        }
        
        plugin.getLogger().info("Player " + player.getName() + " solved CAPTCHA in " + 
                               String.format("%.2f", solveTimeSeconds) + "s (" + category + ")");
        
        // Clean up tracking data
        captchaStartTimes.remove(playerId);
    }

    public void shutdown() {
        activeChallenges.clear();
        pendingPlacements.clear();
        lastCaptchaTime.clear();
        captchaStartTimes.clear();
        recentFailedSlots.clear();
        solveTimes.clear();
    }
}