package net.minecadia.cactuscaptcha.storage;

import net.minecadia.cactuscaptcha.CactusCaptcha;

import java.sql.*;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MySqlStorage implements StorageManager.StorageBackend {

    private final CactusCaptcha plugin;
    private Connection connection;
    private final String host;
    private final int port;
    private final String database;
    private final String username;
    private final String password;

    public MySqlStorage(CactusCaptcha plugin) {
        this.plugin = plugin;
        this.host = plugin.getConfig().getString("mysql.host", "localhost");
        this.port = plugin.getConfig().getInt("mysql.port", 3306);
        this.database = plugin.getConfig().getString("mysql.database", "cactuscaptcha");
        this.username = plugin.getConfig().getString("mysql.user", "root");
        this.password = plugin.getConfig().getString("mysql.password", "password");
        
        try {
            connect();
            createTables();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to initialize MySQL storage: " + e.getMessage());
        }
    }

    private void connect() throws SQLException {
        String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&autoReconnect=true";
        connection = DriverManager.getConnection(url, username, password);
        plugin.getLogger().info("Connected to MySQL database");
    }

    private void createTables() throws SQLException {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS cactuscaptcha_players (" +
                "uuid VARCHAR(36) PRIMARY KEY, " +
                "total_attempts INT DEFAULT 0, " +
                "passes INT DEFAULT 0, " +
                "fails INT DEFAULT 0, " +
                "penalty_until BIGINT DEFAULT 0, " +
                "last_updated BIGINT DEFAULT 0" +
                ")";
        
        try (PreparedStatement stmt = connection.prepareStatement(createTableSQL)) {
            stmt.executeUpdate();
            plugin.getLogger().info("MySQL tables created/verified");
        }
    }

    @Override
    public void loadAllData(ConcurrentHashMap<UUID, StorageManager.PlayerStats> playerStats) throws Exception {
        ensureConnection();
        
        String selectSQL = "SELECT uuid, total_attempts, passes, fails, penalty_until FROM cactuscaptcha_players";
        
        try (PreparedStatement stmt = connection.prepareStatement(selectSQL);
             ResultSet rs = stmt.executeQuery()) {
            
            int loadedCount = 0;
            while (rs.next()) {
                try {
                    UUID uuid = UUID.fromString(rs.getString("uuid"));
                    int totalAttempts = rs.getInt("total_attempts");
                    int passes = rs.getInt("passes");
                    int fails = rs.getInt("fails");
                    long penaltyUntil = rs.getLong("penalty_until");
                    
                    StorageManager.PlayerStats stats = new StorageManager.PlayerStats(uuid, totalAttempts, passes, fails, penaltyUntil);
                    playerStats.put(uuid, stats);
                    loadedCount++;
                    
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to load player data from MySQL: " + e.getMessage());
                }
            }
            
            plugin.getLogger().info("Loaded " + loadedCount + " player records from MySQL storage");
        }
    }

    @Override
    public void saveAllData(ConcurrentHashMap<UUID, StorageManager.PlayerStats> playerStats) throws Exception {
        ensureConnection();
        
        String upsertSQL = "INSERT INTO cactuscaptcha_players (uuid, total_attempts, passes, fails, penalty_until, last_updated) " +
                "VALUES (?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE " +
                "total_attempts = VALUES(total_attempts), " +
                "passes = VALUES(passes), " +
                "fails = VALUES(fails), " +
                "penalty_until = VALUES(penalty_until), " +
                "last_updated = VALUES(last_updated)";
        
        try (PreparedStatement stmt = connection.prepareStatement(upsertSQL)) {
            connection.setAutoCommit(false);
            
            for (StorageManager.PlayerStats stats : playerStats.values()) {
                stmt.setString(1, stats.uuid.toString());
                stmt.setInt(2, stats.totalAttempts);
                stmt.setInt(3, stats.passes);
                stmt.setInt(4, stats.fails);
                stmt.setLong(5, stats.penaltyUntil);
                stmt.setLong(6, System.currentTimeMillis());
                stmt.addBatch();
            }
            
            stmt.executeBatch();
            connection.commit();
            connection.setAutoCommit(true);
            
        } catch (SQLException e) {
            connection.rollback();
            connection.setAutoCommit(true);
            throw e;
        }
    }

    public void savePlayerData(StorageManager.PlayerStats stats) throws SQLException {
        ensureConnection();
        
        String upsertSQL = "INSERT INTO cactuscaptcha_players (uuid, total_attempts, passes, fails, penalty_until, last_updated) " +
                "VALUES (?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE " +
                "total_attempts = VALUES(total_attempts), " +
                "passes = VALUES(passes), " +
                "fails = VALUES(fails), " +
                "penalty_until = VALUES(penalty_until), " +
                "last_updated = VALUES(last_updated)";
        
        try (PreparedStatement stmt = connection.prepareStatement(upsertSQL)) {
            stmt.setString(1, stats.uuid.toString());
            stmt.setInt(2, stats.totalAttempts);
            stmt.setInt(3, stats.passes);
            stmt.setInt(4, stats.fails);
            stmt.setLong(5, stats.penaltyUntil);
            stmt.setLong(6, System.currentTimeMillis());
            stmt.executeUpdate();
        }
    }

    public void savePlayerDataAsync(StorageManager.PlayerStats stats) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                savePlayerData(stats);
            } catch (SQLException e) {
                plugin.getLogger().warning("Failed to save player data to MySQL: " + e.getMessage());
            }
        });
    }

    private void ensureConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connect();
        }
    }

    @Override
    public void close() {
        if (connection != null) {
            try {
                connection.close();
                plugin.getLogger().info("MySQL connection closed");
            } catch (SQLException e) {
                plugin.getLogger().warning("Error closing MySQL connection: " + e.getMessage());
            }
        }
    }

    /**
     * Clean up old player data (optional maintenance method)
     */
    public void cleanupOldData(long maxAgeMs) {
        try {
            ensureConnection();
            
            String deleteSQL = "DELETE FROM cactuscaptcha_players WHERE last_updated < ?";
            long cutoffTime = System.currentTimeMillis() - maxAgeMs;
            
            try (PreparedStatement stmt = connection.prepareStatement(deleteSQL)) {
                stmt.setLong(1, cutoffTime);
                int deletedCount = stmt.executeUpdate();
                
                if (deletedCount > 0) {
                    plugin.getLogger().info("Cleaned up " + deletedCount + " old player records from MySQL");
                }
            }
            
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to cleanup old MySQL data: " + e.getMessage());
        }
    }

    /**
     * Get the number of stored player records
     */
    public int getStoredPlayerCount() {
        try {
            ensureConnection();
            
            String countSQL = "SELECT COUNT(*) FROM cactuscaptcha_players";
            try (PreparedStatement stmt = connection.prepareStatement(countSQL);
                 ResultSet rs = stmt.executeQuery()) {
                
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to get player count from MySQL: " + e.getMessage());
        }
        
        return 0;
    }

    /**
     * Check if data exists for a specific player
     */
    public boolean hasPlayerData(UUID uuid) {
        try {
            ensureConnection();
            
            String existsSQL = "SELECT 1 FROM cactuscaptcha_players WHERE uuid = ? LIMIT 1";
            try (PreparedStatement stmt = connection.prepareStatement(existsSQL)) {
                stmt.setString(1, uuid.toString());
                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next();
                }
            }
            
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to check player data existence in MySQL: " + e.getMessage());
        }
        
        return false;
    }

    /**
     * Delete data for a specific player
     */
    public boolean deletePlayerData(UUID uuid) {
        try {
            ensureConnection();
            
            String deleteSQL = "DELETE FROM cactuscaptcha_players WHERE uuid = ?";
            try (PreparedStatement stmt = connection.prepareStatement(deleteSQL)) {
                stmt.setString(1, uuid.toString());
                return stmt.executeUpdate() > 0;
            }
            
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to delete player data from MySQL: " + e.getMessage());
        }
        
        return false;
    }
}