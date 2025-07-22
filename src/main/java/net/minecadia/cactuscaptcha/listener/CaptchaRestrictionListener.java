package net.minecadia.cactuscaptcha.listener;

import net.minecadia.cactuscaptcha.CactusCaptcha;
import net.minecadia.cactuscaptcha.captcha.CaptchaManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * Prevents players from moving, chatting, or using commands during active CAPTCHA challenges.
 * Sends reminder messages to encourage solving the challenge.
 */
public class CaptchaRestrictionListener implements Listener {
    
    private final CactusCaptcha plugin;
    
    public CaptchaRestrictionListener(CactusCaptcha plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        
        // Check if player has an active CAPTCHA challenge
        if (CaptchaManager.get().hasActiveChallenge(player)) {
            // Cancel movement
            event.setCancelled(true);
            
            // Send reminder message (throttled to avoid spam)
            sendThrottledMessage(player, plugin.getMessage("start"));
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        
        // Check if player has an active CAPTCHA challenge
        if (CaptchaManager.get().hasActiveChallenge(player)) {
            // Cancel chat
            event.setCancelled(true);
            
            // Send reminder message (throttled to avoid spam)
            sendThrottledMessage(player, plugin.getMessage("start"));
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        
        // Check if player has an active CAPTCHA challenge
        if (CaptchaManager.get().hasActiveChallenge(player)) {
            // Cancel command
            event.setCancelled(true);
            
            // Send reminder message (throttled to avoid spam)
            sendThrottledMessage(player, plugin.getMessage("start"));
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getPlayer();
        
        // Check if player has an active CAPTCHA challenge
        if (CaptchaManager.get().hasActiveChallenge(player)) {
            // Prevent closing the CAPTCHA GUI by reopening it after a short delay
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (CaptchaManager.get().hasActiveChallenge(player)) {
                    // Reopen the CAPTCHA GUI
                    CaptchaManager.get().startChallenge(player, player.getLocation());
                    if (!plugin.getConfig().getBoolean("silentMode", false)) {
                        player.sendMessage(ChatColor.RED + "You must solve the CAPTCHA to continue!");
                    }
                }
            }, 1L);
        }
    }
    
    /**
     * Sends a throttled message to prevent spam.
     * Only sends the message if enough time has passed since the last message.
     * 
     * @param player The player to send the message to
     * @param message The message to send
     */
    private void sendThrottledMessage(Player player, String message) {
        // Simple throttling: only send message every 3 seconds
        long currentTime = System.currentTimeMillis();
        String key = "lastMessage_" + player.getUniqueId();
        
        // Store last message time in player metadata or use a simple map
        Long lastMessageTime = (Long) player.getMetadata(key).stream()
            .findFirst()
            .map(metadataValue -> metadataValue.asLong())
            .orElse(0L);
        
        if (currentTime - lastMessageTime > 3000) { // 3 seconds
            if (!plugin.getConfig().getBoolean("silentMode", false)) {
                player.sendMessage(message);
            }
            // Update last message time using metadata
            player.setMetadata(key, new org.bukkit.metadata.FixedMetadataValue(plugin, currentTime));
        }
    }
}