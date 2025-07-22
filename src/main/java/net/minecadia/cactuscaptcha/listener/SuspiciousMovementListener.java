package net.minecadia.cactuscaptcha.listener;

import net.minecadia.cactuscaptcha.CactusCaptcha;
import net.minecadia.cactuscaptcha.storage.StorageManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Detects suspicious movement repetition patterns and alerts admins.
 * Tracks movement packets in a sliding window to detect unnatural repetition.
 */
public class SuspiciousMovementListener implements Listener {
    
    private final CactusCaptcha plugin;
    
    // Store movement packets for each player (position + rotation)
    private final Map<UUID, LinkedList<MovementPacket>> playerMovements = new ConcurrentHashMap<>();
    
    // Track players currently attacking entities (to filter out combat movements)
    private final Map<UUID, Long> playerAttackTimes = new ConcurrentHashMap<>();
    
    // Cooldown to prevent spam alerts (10 seconds)
    private final Map<UUID, Long> alertCooldowns = new ConcurrentHashMap<>();
    
    // Configuration constants - will be loaded from config
    private int sampleSize = 20;
    private double repeatThreshold = 0.75;
    private int suspicionIncrease = 15;
    private boolean enabled = true;
    
    // Other constants
    private static final long ALERT_COOLDOWN_MS = 10000; // 10 seconds
    private static final double MOVEMENT_THRESHOLD = 1.0; // Minimum movement distance (1 block)
    private static final long ATTACK_FILTER_MS = 2000; // Filter movements for 2 seconds after attack
    
    public SuspiciousMovementListener(CactusCaptcha plugin) {
        this.plugin = plugin;
        loadConfig();
    }
    
    /**
     * Loads configuration values from config.yml
     */
    private void loadConfig() {
        if (plugin.getConfig().contains("movement-repetition")) {
            enabled = plugin.getConfig().getBoolean("movement-repetition.enabled", true);
            sampleSize = plugin.getConfig().getInt("movement-repetition.sample-size", 20);
            repeatThreshold = plugin.getConfig().getDouble("movement-repetition.repeat-threshold", 0.75);
            suspicionIncrease = plugin.getConfig().getInt("movement-repetition.suspicion-increase", 15);
        }
    }
    
    /**
     * Represents a movement packet with position and rotation data.
     */
    private static class MovementPacket {
        final double x, y, z;
        final float yaw, pitch;
        final long timestamp;
        
        MovementPacket(Location location) {
            this.x = location.getX();
            this.y = location.getY();
            this.z = location.getZ();
            this.yaw = location.getYaw();
            this.pitch = location.getPitch();
            this.timestamp = System.currentTimeMillis();
        }
        
        /**
         * Checks if this movement packet is identical to another.
         */
        boolean isIdenticalTo(MovementPacket other) {
            return Math.abs(x - other.x) < 0.001 &&
                   Math.abs(y - other.y) < 0.001 &&
                   Math.abs(z - other.z) < 0.001 &&
                   Math.abs(yaw - other.yaw) < 0.001 &&
                   Math.abs(pitch - other.pitch) < 0.001;
        }
        
        /**
         * Calculates distance from another movement packet.
         */
        double distanceFrom(MovementPacket other) {
            double dx = x - other.x;
            double dy = y - other.y;
            double dz = z - other.z;
            return Math.sqrt(dx * dx + dy * dy + dz * dz);
        }
        
        /**
         * Checks if this represents actual movement (not just rotation).
         */
        boolean hasPositionChange(MovementPacket previous) {
            return distanceFrom(previous) >= MOVEMENT_THRESHOLD;
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!enabled) return;
        
        Player player = event.getPlayer();
        
        // Skip if player has bypass permission
        if (player.hasPermission("cactuscaptcha.bypass")) {
            return;
        }
        
        Location from = event.getFrom();
        Location to = event.getTo();
        
        // Skip if no actual movement (just head turning)
        if (to == null || (from.getX() == to.getX() && from.getY() == to.getY() && from.getZ() == to.getZ())) {
            return;
        }
        
        UUID playerId = player.getUniqueId();
        
        // Skip if player is currently attacking (filter out combat movements)
        Long lastAttack = playerAttackTimes.get(playerId);
        if (lastAttack != null && (System.currentTimeMillis() - lastAttack) < ATTACK_FILTER_MS) {
            return;
        }
        
        // Get or create movement history for this player
        LinkedList<MovementPacket> movements = playerMovements.computeIfAbsent(playerId, k -> new LinkedList<>());
        
        // Create new movement packet
        MovementPacket newMovement = new MovementPacket(to);
        
        // Skip if no significant movement from last position
        if (!movements.isEmpty() && !newMovement.hasPositionChange(movements.peekLast())) {
            return;
        }
        
        // Add new movement and maintain sliding window size
        movements.addLast(newMovement);
        if (movements.size() > sampleSize) {
            movements.removeFirst();
        }
        
        // Need enough movements to analyze
        if (movements.size() < sampleSize) {
            return;
        }
        
        // Detect repetition patterns
        double repetitionPercent = detectRepetitionPattern(movements);
        
        // Update player stats with repetition percentage
        StorageManager.PlayerStats stats = plugin.getStorageManager().getPlayerStats(playerId);
        stats.setMovementPatternPercent((int) Math.round(repetitionPercent * 100));
        
        // Check if repetition exceeds threshold
        if (repetitionPercent >= repeatThreshold) {
            handleSuspiciousMovement(player, repetitionPercent);
        }
    }
    
    /**
     * Handles entity damage events to track when players are attacking.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            playerAttackTimes.put(player.getUniqueId(), System.currentTimeMillis());
        }
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        // Clean up data for offline players
        playerMovements.remove(playerId);
        playerAttackTimes.remove(playerId);
        alertCooldowns.remove(playerId);
    }
    
    /**
     * Detects repetition patterns in movement history.
     * 
     * @param movements List of recent movement packets
     * @return Percentage of movements that are exact repetitions (0.0 to 1.0)
     */
    private double detectRepetitionPattern(LinkedList<MovementPacket> movements) {
        if (movements.size() < 2) return 0.0;
        
        int totalMovements = movements.size();
        int identicalCount = 0;
        
        // Convert to array for easier processing
        MovementPacket[] movementArray = movements.toArray(new MovementPacket[0]);
        
        // Count identical movements
        for (int i = 0; i < movementArray.length; i++) {
            for (int j = i + 1; j < movementArray.length; j++) {
                if (movementArray[i].isIdenticalTo(movementArray[j])) {
                    identicalCount++;
                    break; // Count each movement only once
                }
            }
        }
        
        return (double) identicalCount / totalMovements;
    }
    
    /**
     * Handles suspicious movement detection.
     */
    private void handleSuspiciousMovement(Player player, double repetitionPercent) {
        UUID playerId = player.getUniqueId();
        
        // Check cooldown to prevent spam
        if (isOnCooldown(playerId)) {
            return;
        }
        
        // Increase suspicion score
        // Note: This is a placeholder - implement actual suspicion scoring system if needed
        
        // Alert admins
        alertAdmins(player, repetitionPercent);
        
        // Set cooldown
        alertCooldowns.put(playerId, System.currentTimeMillis());
        
        // Log to internal activity history
        plugin.getLogger().info(String.format(
            "Movement repetition detected: %s (%.1f%% repetition)",
            player.getName(),
            repetitionPercent * 100
        ));
    }
    
    /**
     * Checks if a player is on alert cooldown.
     */
    private boolean isOnCooldown(UUID playerId) {
        Long lastAlert = alertCooldowns.get(playerId);
        if (lastAlert == null) return false;
        
        return (System.currentTimeMillis() - lastAlert) < ALERT_COOLDOWN_MS;
    }
    
    /**
     * Sends alert to all online admins with suspicious.alert permission.
     */
    private void alertAdmins(Player player, double repetitionPercent) {
        String message = String.format(
            "§c[MOVEMENT REPETITION] §e%s §7detected with §c%.1f%% §7movement repetition",
            player.getName(),
            repetitionPercent * 100
        );
        
        // Send to all online players with suspicious.alert permission
        for (Player admin : Bukkit.getOnlinePlayers()) {
            if (admin.hasPermission("suspicious.alert")) {
                admin.sendMessage(message);
            }
        }
        
        // Also log to console
        plugin.getLogger().warning(String.format(
            "Movement repetition detected: %s (%.1f%% repetition)",
            player.getName(),
            repetitionPercent * 100
        ));
    }
    
    /**
     * Gets the current repetition percentage for a player (for debugging).
     */
    public double getRepetitionPercent(UUID playerId) {
        LinkedList<MovementPacket> movements = playerMovements.get(playerId);
        if (movements == null || movements.size() < 2) {
            return 0.0;
        }
        return detectRepetitionPattern(movements);
    }
    
    /**
     * Gets the number of stored movements for a player (for debugging).
     */
    public int getStoredMovementCount(UUID playerId) {
        LinkedList<MovementPacket> movements = playerMovements.get(playerId);
        return movements != null ? movements.size() : 0;
    }
    
    /**
     * Reloads configuration values.
     */
    public void reloadConfig() {
        loadConfig();
    }
}