package net.minecadia.cactuscaptcha.storage;

import net.minecadia.cactuscaptcha.CactusCaptcha;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class StorageManager {

    private final CactusCaptcha plugin;
    private final ConcurrentHashMap<UUID, PlayerStats> playerStats;
    private final StorageBackend storageBackend;
    private BukkitRunnable autoSaveTask;

    public StorageManager(CactusCaptcha plugin) {
        this.plugin = plugin;
        this.playerStats = new ConcurrentHashMap<>();
        
        // Initialize storage backend based on config
        boolean mysqlEnabled = plugin.getConfig().getBoolean("mysql.enabled", false);
        if (mysqlEnabled) {
            this.storageBackend = new MySqlStorage(plugin);
        } else {
            this.storageBackend = new YamlStorage(plugin);
        }
        
        // Load existing data
        loadAllData();
        
        // Start auto-save task (every 30 seconds)
        startAutoSave();
    }

    private void loadAllData() {
        // Load data asynchronously
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    storageBackend.loadAllData(playerStats);
                    plugin.getLogger().info("Loaded player statistics from storage");
                } catch (Exception e) {
                    plugin.getLogger().severe("Failed to load player statistics: " + e.getMessage());
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    private void startAutoSave() {
        autoSaveTask = new BukkitRunnable() {
            @Override
            public void run() {
                saveAllData();
            }
        };
        autoSaveTask.runTaskTimerAsynchronously(plugin, 600L, 600L); // Every 30 seconds (600 ticks)
    }

    public void saveAllData() {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    storageBackend.saveAllData(playerStats);
                } catch (Exception e) {
                    plugin.getLogger().severe("Failed to save player statistics: " + e.getMessage());
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    public PlayerStats getPlayerStats(UUID playerId) {
        return playerStats.computeIfAbsent(playerId, k -> new PlayerStats(playerId));
    }

    public void incrementPasses(UUID playerId) {
        PlayerStats stats = getPlayerStats(playerId);
        stats.passes++;
        stats.totalAttempts++;
    }

    public void incrementFails(UUID playerId) {
        PlayerStats stats = getPlayerStats(playerId);
        stats.fails++;
        stats.totalAttempts++;
    }

    public void setPenalty(UUID playerId, long penaltyUntil) {
        PlayerStats stats = getPlayerStats(playerId);
        stats.penaltyUntil = penaltyUntil;
    }

    public boolean isPenalized(UUID playerId) {
        PlayerStats stats = getPlayerStats(playerId);
        return stats.penaltyUntil > System.currentTimeMillis();
    }

    public long getPenaltyTimeRemaining(UUID playerId) {
        PlayerStats stats = getPlayerStats(playerId);
        long remaining = stats.penaltyUntil - System.currentTimeMillis();
        return Math.max(0, remaining);
    }

    public void resetPlayerStats(UUID playerId) {
        PlayerStats stats = getPlayerStats(playerId);
        stats.passes = 0;
        stats.fails = 0;
        stats.totalAttempts = 0;
        stats.penaltyUntil = 0;
    }

    public void removePenalty(UUID playerId) {
        PlayerStats stats = getPlayerStats(playerId);
        stats.penaltyUntil = 0;
    }

    public int getTotalPasses() {
        return playerStats.values().stream().mapToInt(stats -> stats.passes).sum();
    }

    public int getTotalFails() {
        return playerStats.values().stream().mapToInt(stats -> stats.fails).sum();
    }

    public long getPenaltyUntil(UUID playerId) {
        PlayerStats stats = getPlayerStats(playerId);
        return stats.penaltyUntil;
    }

    public java.util.List<UUID> getAllPlayersWithData() {
        return new java.util.ArrayList<>(playerStats.keySet());
    }

    public ConcurrentHashMap<UUID, PlayerStats> getAllPlayerStats() {
        return new ConcurrentHashMap<>(playerStats);
    }
    
    // New methods for retry limit and cooldown system
    public void incrementCaptchaFails(UUID playerId) {
        PlayerStats stats = getPlayerStats(playerId);
        stats.incrementFail();
    }
    
    public void resetCaptchaFails(UUID playerId) {
        PlayerStats stats = getPlayerStats(playerId);
        stats.resetFails();
    }
    
    public int getCaptchaFails(UUID playerId) {
        PlayerStats stats = getPlayerStats(playerId);
        return stats.getCaptchaFails();
    }
    
    public void recordFailureChain(UUID playerId) {
        PlayerStats stats = getPlayerStats(playerId);
        stats.recordFailureChain();
    }
    
    public int getFailChainCount(UUID playerId) {
        PlayerStats stats = getPlayerStats(playerId);
        return stats.getFailChainCount();
    }
    
    public void resetFailChain(UUID playerId) {
        PlayerStats stats = getPlayerStats(playerId);
        stats.resetFailChain();
    }
    
    public void setCooldownUntil(UUID playerId, long timestamp) {
        PlayerStats stats = getPlayerStats(playerId);
        stats.setCooldownUntil(timestamp);
    }
    
    public long getCooldownUntil(UUID playerId) {
        PlayerStats stats = getPlayerStats(playerId);
        return stats.getCooldownUntil();
    }
    
    public boolean isOnCooldown(UUID playerId) {
        PlayerStats stats = getPlayerStats(playerId);
        return stats.isOnCooldown();
    }

    public void shutdown() {
        if (autoSaveTask != null) {
            autoSaveTask.cancel();
        }
        
        // Final save
        try {
            storageBackend.saveAllData(playerStats);
            plugin.getLogger().info("Final save completed");
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to perform final save: " + e.getMessage());
        }
        
        storageBackend.close();
    }

    // Inner class for player statistics
    public static class PlayerStats {
        public final UUID uuid;
        public int totalAttempts;
        public int passes;
        public int fails;
        public long penaltyUntil; // epoch ms
        public int movementPatternPercent; // repetition percentage
        
        // New fields for retry limit and cooldown system
        public int failChainCount = 0;
        public int captchaFails = 0;
        public long cooldownUntil = 0L;

        public PlayerStats(UUID uuid) {
            this.uuid = uuid;
            this.totalAttempts = 0;
            this.passes = 0;
            this.fails = 0;
            this.penaltyUntil = 0;
            this.movementPatternPercent = 0;
        }

        public PlayerStats(UUID uuid, int totalAttempts, int passes, int fails, long penaltyUntil) {
            this.uuid = uuid;
            this.totalAttempts = totalAttempts;
            this.passes = passes;
            this.fails = fails;
            this.penaltyUntil = penaltyUntil;
            this.movementPatternPercent = 0;
        }

        public double getSuccessRate() {
            if (totalAttempts == 0) return 0.0;
            return (double) passes / totalAttempts;
        }

        public boolean isPenalized() {
            return penaltyUntil > System.currentTimeMillis();
        }

        public long getPenaltyTimeRemaining() {
            long remaining = penaltyUntil - System.currentTimeMillis();
            return Math.max(0, remaining);
        }

        public int getMovementPatternPercent() {
            return movementPatternPercent;
        }

        public void setMovementPatternPercent(int movementPatternPercent) {
            this.movementPatternPercent = movementPatternPercent;
        }
        
        // New methods for retry limit and cooldown system
        public void incrementFail() { 
            captchaFails++; 
        }
        
        public void resetFails() { 
            captchaFails = 0; 
        }

        public int getCaptchaFails() { 
            return captchaFails; 
        }

        public void recordFailureChain() { 
            failChainCount++; 
        }
        
        public int getFailChainCount() { 
            return failChainCount; 
        }
        
        public void resetFailChain() { 
            failChainCount = 0; 
        }

        public void setCooldownUntil(long ts) { 
            cooldownUntil = ts; 
        }
        
        public long getCooldownUntil() { 
            return cooldownUntil; 
        }

        public boolean isOnCooldown() {
            return System.currentTimeMillis() < cooldownUntil;
        }
    }

    // Interface for storage backends
    public interface StorageBackend {
        void loadAllData(ConcurrentHashMap<UUID, PlayerStats> playerStats) throws Exception;
        void saveAllData(ConcurrentHashMap<UUID, PlayerStats> playerStats) throws Exception;
        void close();
    }
}