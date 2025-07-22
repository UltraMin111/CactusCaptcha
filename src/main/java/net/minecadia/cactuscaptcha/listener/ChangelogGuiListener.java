package net.minecadia.cactuscaptcha.listener;

import net.minecadia.cactuscaptcha.CactusCaptcha;
import net.minecadia.cactuscaptcha.gui.ChangelogGui;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

/**
 * Handles clicks in the Changelog GUI.
 */
public class ChangelogGuiListener implements Listener {
    
    private final CactusCaptcha plugin;
    
    public ChangelogGuiListener(CactusCaptcha plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // Check if the clicked inventory is the changelog GUI
        if (event.getView().getTitle().equals("§bCactusCaptcha Changelog")) {
            event.setCancelled(true); // Prevent item movement
            
            if (!(event.getWhoClicked() instanceof Player)) {
                return;
            }
            
            Player player = (Player) event.getWhoClicked();
            int slot = event.getSlot();
            
            // Handle the click using ChangelogGui
            ChangelogGui changelogGui = new ChangelogGui(plugin);
            changelogGui.handleClick(player, slot);
        }
    }
}