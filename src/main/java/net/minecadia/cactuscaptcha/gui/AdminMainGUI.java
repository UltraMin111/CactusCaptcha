package net.minecadia.cactuscaptcha.gui;

import net.minecadia.cactuscaptcha.CactusCaptcha;
import net.minecadia.cactuscaptcha.storage.StorageManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Main admin GUI that displays all players with their captcha statistics.
 * Shows player heads in a 6-row inventory with detailed information in lore.
 */
public class AdminMainGUI {
    
    private static final String GUI_TITLE = "§6CactusCaptcha Admin Panel";
    private static final int GUI_SIZE = 54; // 6 rows
    
    private final CactusCaptcha plugin;
    
    public AdminMainGUI(CactusCaptcha plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Creates and opens the admin main GUI for the specified player.
     * 
     * @param admin The admin player to show the GUI to
     */
    public void openGUI(Player admin) {
        Inventory gui = createMainGUI();
        admin.openInventory(gui);
    }
    
    /**
     * Creates the main admin GUI inventory.
     * 
     * @return The created inventory
     */
    private Inventory createMainGUI() {
        Inventory gui = Bukkit.createInventory(null, GUI_SIZE, GUI_TITLE);
        
        // Get all players who have captcha data
        List<UUID> playersWithData = new ArrayList<>(plugin.getStorageManager().getAllPlayerStats().keySet());
        
        int slot = 0;
        for (UUID playerId : playersWithData) {
            if (slot >= GUI_SIZE - 9) { // Leave bottom row for navigation/controls
                break;
            }
            
            ItemStack playerHead = createPlayerHeadItem(playerId);
            gui.setItem(slot, playerHead);
            slot++;
        }
        
        // Add control items in bottom row
        addControlItems(gui);
        
        return gui;
    }
    
    /**
     * Creates a player head item with captcha statistics in the lore.
     * 
     * @param playerId The player's UUID
     * @return The player head ItemStack
     */
    private ItemStack createPlayerHeadItem(UUID playerId) {
        ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short) 3); // Player head
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerId);
        String playerName = player.getName() != null ? player.getName() : "Unknown Player";
        
        meta.setOwner(playerName);
        meta.setDisplayName("§e" + playerName);
        
        // Get player statistics
        StorageManager.PlayerStats stats = plugin.getStorageManager().getPlayerStats(playerId);
        
        // Get timer information
        int remainingTime = -1;
        if (plugin.getTimerRunnable() != null) {
            remainingTime = plugin.getTimerRunnable().getRemainingTime(playerId);
        }
        
        // Get penalty information
        long penaltyUntil = plugin.getStorageManager().getPenaltyUntil(playerId);
        long currentTime = System.currentTimeMillis();
        boolean hasPenalty = penaltyUntil > currentTime;
        long penaltyMinutesLeft = hasPenalty ? (penaltyUntil - currentTime) / (60 * 1000) : 0;
        
        // Create lore with statistics
        List<String> lore = new ArrayList<>();
        lore.add("§7Player Statistics:");
        lore.add("§aPasses: §f" + stats.passes);
        lore.add("§cFails: §f" + stats.fails);
        lore.add("§7Repetition % Detected: §f" + stats.getMovementPatternPercent() + "%");
        
        if (hasPenalty) {
            lore.add("§4Penalty: §f" + penaltyMinutesLeft + "m remaining");
        } else {
            lore.add("§2Penalty: §fNone");
        }
        
        if (remainingTime >= 0) {
            lore.add("§bTimer: §f" + remainingTime + "s till next CAPTCHA");
        } else {
            lore.add("§bTimer: §fNot tracked");
        }
        
        // Add Suspicion Sources section
        lore.add("");
        lore.add("§6Suspicion Sources:");
        
        // Solve Time (placeholder - would need actual implementation)
        lore.add("§7• Solve Time: §fNormal");
        
        // Honeytoken Triggers (placeholder - would need actual implementation)
        lore.add("§7• Honeytoken Triggers: §f0");
        
        // Movement Repetition Flags
        int repetitionPercent = stats.getMovementPatternPercent();
        if (repetitionPercent >= 75) {
            lore.add("§7• Movement Repetition: §c" + repetitionPercent + "% (FLAGGED)");
        } else if (repetitionPercent > 0) {
            lore.add("§7• Movement Repetition: §e" + repetitionPercent + "%");
        } else {
            lore.add("§7• Movement Repetition: §a0%");
        }
        
        // Failed Pattern Clicks (placeholder - would need actual implementation)
        lore.add("§7• Failed Pattern Clicks: §f0");
        
        lore.add("");
        lore.add("§7Click to manage this player");
        
        meta.setLore(lore);
        head.setItemMeta(meta);
        
        return head;
    }
    
    /**
     * Adds control items to the bottom row of the GUI.
     * 
     * @param gui The GUI inventory
     */
    private void addControlItems(Inventory gui) {
        // Refresh button
        ItemStack refreshItem = new ItemStack(Material.EMERALD);
        ItemMeta refreshMeta = refreshItem.getItemMeta();
        refreshMeta.setDisplayName("§aRefresh");
        refreshMeta.setLore(Arrays.asList("§7Click to refresh the player list"));
        refreshItem.setItemMeta(refreshMeta);
        gui.setItem(GUI_SIZE - 9, refreshItem);
        
        // Close button
        ItemStack closeItem = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = closeItem.getItemMeta();
        closeMeta.setDisplayName("§cClose");
        closeMeta.setLore(Arrays.asList("§7Click to close this GUI"));
        closeItem.setItemMeta(closeMeta);
        gui.setItem(GUI_SIZE - 1, closeItem);
        
        // Statistics summary
        ItemStack statsItem = new ItemStack(Material.BOOK);
        ItemMeta statsMeta = statsItem.getItemMeta();
        statsMeta.setDisplayName("§6Server Statistics");
        
        int totalPasses = plugin.getStorageManager().getTotalPasses();
        int totalFails = plugin.getStorageManager().getTotalFails();
        int totalPlayers = plugin.getStorageManager().getAllPlayersWithData().size();
        
        List<String> statsLore = new ArrayList<>();
        statsLore.add("§7Total Players: §f" + totalPlayers);
        statsLore.add("§7Total Passes: §f" + totalPasses);
        statsLore.add("§7Total Fails: §f" + totalFails);
        if (totalPasses + totalFails > 0) {
            double successRate = (double) totalPasses / (totalPasses + totalFails) * 100;
            statsLore.add("§7Success Rate: §f" + String.format("%.1f%%", successRate));
        }
        
        statsMeta.setLore(statsLore);
        statsItem.setItemMeta(statsMeta);
        gui.setItem(GUI_SIZE - 5, statsItem);
        
        // Config & Tools button (placed in last row for better visibility)
        ItemStack configTools = new ItemStack(Material.COMMAND);
        ItemMeta configMeta = configTools.getItemMeta();
        configMeta.setDisplayName("§b🧪 Config & Tools");
        configMeta.setLore(Arrays.asList(
            "§7Edit config values, reload config,",
            "§7and save configuration changes."
        ));
        configTools.setItemMeta(configMeta);
        gui.setItem(GUI_SIZE - 7, configTools); // Slot 47 in last row
    }
    
    /**
     * Checks if the given inventory is the admin main GUI.
     * 
     * @param inventory The inventory to check
     * @return true if it's the admin main GUI, false otherwise
     */
    public static boolean isAdminMainGUI(Inventory inventory) {
        return inventory != null && 
               inventory.getSize() == GUI_SIZE && 
               GUI_TITLE.equals(inventory.getTitle());
    }
    
    /**
     * Gets the player UUID from a clicked slot in the admin GUI.
     * 
     * @param slot The clicked slot
     * @return The player UUID, or null if not a player head slot
     */
    public UUID getPlayerFromSlot(int slot) {
        if (slot < 0 || slot >= GUI_SIZE - 9) { // Not in player area
            return null;
        }
        
        List<UUID> playersWithData = plugin.getStorageManager().getAllPlayersWithData();
        if (slot >= playersWithData.size()) {
            return null;
        }
        
        return playersWithData.get(slot);
    }
}