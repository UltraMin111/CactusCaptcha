package net.minecadia.cactuscaptcha.storage;

import net.minecadia.cactuscaptcha.CactusCaptcha;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class YamlStorage implements StorageManager.StorageBackend {

    private final CactusCaptcha plugin;
    private final File dataFolder;

    public YamlStorage(CactusCaptcha plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "data");
        
        // Create data directory if it doesn't exist
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
    }

    @Override
    public void loadAllData(ConcurrentHashMap<UUID, StorageManager.PlayerStats> playerStats) throws Exception {
        File[] files = dataFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        
        if (files == null) {
            return;
        }

        for (File file : files) {
            try {
                String fileName = file.getName();
                String uuidString = fileName.substring(0, fileName.length() - 4); // Remove .yml extension
                UUID uuid = UUID.fromString(uuidString);
                
                YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                
                int totalAttempts = config.getInt("totalAttempts", 0);
                int passes = config.getInt("passes", 0);
                int fails = config.getInt("fails", 0);
                long penaltyUntil = config.getLong("penaltyUntil", 0);
                
                StorageManager.PlayerStats stats = new StorageManager.PlayerStats(uuid, totalAttempts, passes, fails, penaltyUntil);
                playerStats.put(uuid, stats);
                
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load data for file " + file.getName() + ": " + e.getMessage());
            }
        }
        
        plugin.getLogger().info("Loaded " + playerStats.size() + " player records from YAML storage");
    }

    @Override
    public void saveAllData(ConcurrentHashMap<UUID, StorageManager.PlayerStats> playerStats) throws Exception {
        for (StorageManager.PlayerStats stats : playerStats.values()) {
            savePlayerData(stats);
        }
    }

    private void savePlayerData(StorageManager.PlayerStats stats) throws IOException {
        File playerFile = new File(dataFolder, stats.uuid.toString() + ".yml");
        YamlConfiguration config = new YamlConfiguration();
        
        config.set("uuid", stats.uuid.toString());
        config.set("totalAttempts", stats.totalAttempts);
        config.set("passes", stats.passes);
        config.set("fails", stats.fails);
        config.set("penaltyUntil", stats.penaltyUntil);
        config.set("lastUpdated", System.currentTimeMillis());
        
        config.save(playerFile);
    }

    public void savePlayerDataAsync(StorageManager.PlayerStats stats) {
        // Save individual player data asynchronously
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                savePlayerData(stats);
            } catch (IOException e) {
                plugin.getLogger().warning("Failed to save data for player " + stats.uuid + ": " + e.getMessage());
            }
        });
    }

    @Override
    public void close() {
        // No special cleanup needed for YAML storage
        plugin.getLogger().info("YAML storage closed");
    }

    /**
     * Clean up old player data files (optional maintenance method)
     */
    public void cleanupOldData(long maxAgeMs) {
        File[] files = dataFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        
        if (files == null) {
            return;
        }

        long currentTime = System.currentTimeMillis();
        int deletedCount = 0;

        for (File file : files) {
            try {
                YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                long lastUpdated = config.getLong("lastUpdated", 0);
                
                if (lastUpdated > 0 && (currentTime - lastUpdated) > maxAgeMs) {
                    if (file.delete()) {
                        deletedCount++;
                    }
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to check age of file " + file.getName() + ": " + e.getMessage());
            }
        }

        if (deletedCount > 0) {
            plugin.getLogger().info("Cleaned up " + deletedCount + " old player data files");
        }
    }

    /**
     * Get the number of stored player records
     */
    public int getStoredPlayerCount() {
        File[] files = dataFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        return files != null ? files.length : 0;
    }

    /**
     * Check if data exists for a specific player
     */
    public boolean hasPlayerData(UUID uuid) {
        File playerFile = new File(dataFolder, uuid.toString() + ".yml");
        return playerFile.exists();
    }

    /**
     * Delete data for a specific player
     */
    public boolean deletePlayerData(UUID uuid) {
        File playerFile = new File(dataFolder, uuid.toString() + ".yml");
        return playerFile.delete();
    }
}