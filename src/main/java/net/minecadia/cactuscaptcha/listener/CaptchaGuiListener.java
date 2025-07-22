package net.minecadia.cactuscaptcha.listener;

import net.minecadia.cactuscaptcha.CactusCaptcha;
import net.minecadia.cactuscaptcha.gui.CaptchaGUI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class CaptchaGuiListener implements Listener {

    private final CactusCaptcha plugin;

    public CaptchaGuiListener(CactusCaptcha plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        
        // Check if player has an active captcha challenge
        if (!plugin.getCaptchaManager().hasActiveChallenge(player)) {
            return;
        }

        // Check if this is the captcha GUI
        if (!CaptchaGUI.isCaptchaGUI(event.getInventory())) {
            // Player has active captcha but clicked in different inventory - cancel
            event.setCancelled(true);
            return;
        }

        // Cancel the event to prevent item movement
        event.setCancelled(true);

        // Handle captcha GUI clicks
        int slot = event.getRawSlot();
        
        // Only handle clicks in the top inventory (captcha GUI)
        if (slot < 0 || slot >= event.getInventory().getSize()) {
            return;
        }

        // Handle answer clicks (slots 0-3) and honeytoken clicks (slots 5-7)
        if ((slot >= 0 && slot <= 3) || (slot >= 5 && slot <= 7)) {
            boolean correct = plugin.getCaptchaManager().handleGuiClick(player, slot);
            
            if (correct) {
                // Correct answer - captcha manager will handle closing and placing cactus
                return;
            } else {
                // Wrong answer or honeytoken click - captcha manager will handle fail logic
                return;
            }
        }

        // Ignore clicks on timer slot (slot 8) and other slots
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getPlayer();
        
        // Check if player has an active captcha challenge
        if (!plugin.getCaptchaManager().hasActiveChallenge(player)) {
            return;
        }

        // Check if this was the captcha GUI
        if (CaptchaGUI.isCaptchaGUI(event.getInventory())) {
            // Delay the close check to allow for retry GUI opening
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                // Only close if player still has active challenge and no GUI is open
                if (plugin.getCaptchaManager().hasActiveChallenge(player)) {
                    if (player.getOpenInventory() == null || 
                        !CaptchaGUI.isCaptchaGUI(player.getOpenInventory().getTopInventory())) {
                        // Player actually closed captcha GUI without retry - mark as fail
                        plugin.getCaptchaManager().closeCaptcha(player, false);
                    }
                }
            }, 1L); // 1 tick delay
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        
        // Check if player has an active captcha challenge
        if (!plugin.getCaptchaManager().hasActiveChallenge(player)) {
            return;
        }

        // Suppress movement while captcha is open
        // Only cancel if player actually moved (not just head movement)
        if (event.getFrom().getBlockX() != event.getTo().getBlockX() ||
            event.getFrom().getBlockY() != event.getTo().getBlockY() ||
            event.getFrom().getBlockZ() != event.getTo().getBlockZ()) {
            
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        
        // Check if player has an active captcha challenge
        if (plugin.getCaptchaManager().hasActiveChallenge(player)) {
            // Suppress item dropping while captcha is open
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        // Clean up any active captcha challenge when player quits
        if (plugin.getCaptchaManager().hasActiveChallenge(player)) {
            plugin.getCaptchaManager().closeCaptcha(player, false);
        }
    }
}