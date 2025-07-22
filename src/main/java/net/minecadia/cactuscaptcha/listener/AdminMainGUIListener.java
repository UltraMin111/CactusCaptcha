package net.minecadia.cactuscaptcha.listener;

import net.minecadia.cactuscaptcha.CactusCaptcha;
import net.minecadia.cactuscaptcha.gui.AdminMainGUI;
import net.minecadia.cactuscaptcha.gui.ConfigToolsGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

/**
 * Handles interactions with the AdminMainGUI.
 */
public class AdminMainGUIListener implements Listener {

    private final CactusCaptcha plugin;

    public AdminMainGUIListener(CactusCaptcha plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        // Check if this is the admin main GUI or config tools GUI
        if (AdminMainGUI.isAdminMainGUI(event.getInventory())) {
            handleAdminMainGUIClick(event, player);
        } else if (ConfigToolsGUI.isConfigToolsGUI(event.getInventory())) {
            handleConfigToolsGUIClick(event, player);
        }
    }

    /**
     * Handles clicks in the Admin Main GUI.
     */
    private void handleAdminMainGUIClick(InventoryClickEvent event, Player player) {
        // Cancel the event to prevent item movement
        event.setCancelled(true);

        // Handle clicks only in the top inventory
        int slot = event.getRawSlot();
        if (slot < 0 || slot >= event.getInventory().getSize()) {
            return;
        }

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }

        // Handle different types of clicks
        AdminMainGUI adminGUI = new AdminMainGUI(plugin);
        
        // Check if it's the Config & Tools button
        if (isConfigToolsButton(clickedItem, slot)) {
            player.openInventory(ConfigToolsGUI.build(player));
            return;
        }
        
        // Check if it's a control item
        if (isRefreshButton(clickedItem, slot, event.getInventory().getSize())) {
            // Refresh the GUI
            adminGUI.openGUI(player);
            return;
        }
        
        if (isCloseButton(clickedItem, slot, event.getInventory().getSize())) {
            // Close the GUI
            player.closeInventory();
            return;
        }
        
        if (isStatsButton(clickedItem, slot, event.getInventory().getSize())) {
            // Stats button - no action needed, just display
            return;
        }

        // Check if it's a player head (for player management)
        UUID playerId = adminGUI.getPlayerFromSlot(slot);
        if (playerId != null) {
            // Handle player management - for now just show info
            player.sendMessage("§7Player management for " + playerId.toString() + " - Feature coming soon!");
            return;
        }
    }

    /**
     * Handles clicks in the Config & Tools GUI.
     */
    private void handleConfigToolsGUIClick(InventoryClickEvent event, Player player) {
        // Cancel the event to prevent item movement
        event.setCancelled(true);

        // Handle clicks only in the top inventory
        int slot = event.getRawSlot();
        if (slot < 0 || slot >= event.getInventory().getSize()) {
            return;
        }

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }

        String displayName = clickedItem.getItemMeta().getDisplayName();
        
        if (displayName.contains("Live Config Editor")) {
            // Trigger command or open a new GUI for live config editing
            player.performCommand("cc configedit");
            player.sendMessage("§a[CactusCaptcha] Live Config Editor - Feature coming soon!");
        } else if (displayName.contains("Reload Config")) {
            // Reload config directly
            plugin.reloadConfig();
            player.sendMessage("§a[CactusCaptcha] Config reloaded successfully!");
        } else if (displayName.contains("Config Save Tool")) {
            // Save config directly
            plugin.saveConfig();
            plugin.reloadConfig();
            player.sendMessage("§a[CactusCaptcha] Config saved and reloaded successfully!");
        }
    }

    /**
     * Checks if the clicked item is the refresh button.
     */
    private boolean isRefreshButton(ItemStack item, int slot, int guiSize) {
        return item.getType() == Material.EMERALD && slot == guiSize - 9;
    }

    /**
     * Checks if the clicked item is the close button.
     */
    private boolean isCloseButton(ItemStack item, int slot, int guiSize) {
        return item.getType() == Material.BARRIER && slot == guiSize - 1;
    }

    /**
     * Checks if the clicked item is the statistics button.
     */
    private boolean isStatsButton(ItemStack item, int slot, int guiSize) {
        return item.getType() == Material.BOOK && slot == guiSize - 5;
    }

    /**
     * Checks if the clicked item is the Config & Tools button.
     */
    private boolean isConfigToolsButton(ItemStack item, int slot) {
        return item.getType() == Material.COMMAND && slot == 47 && // GUI_SIZE - 7 (last row)
               item.hasItemMeta() && item.getItemMeta().getDisplayName().contains("Config");
    }
}