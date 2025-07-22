package net.minecadia.cactuscaptcha.listener;

import net.minecadia.cactuscaptcha.CactusCaptcha;
import net.minecadia.cactuscaptcha.captcha.CaptchaManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockPlaceListener implements Listener {

    private final CactusCaptcha plugin;

    public BlockPlaceListener(CactusCaptcha plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("cactuscaptcha.bypass")) return;

        Block placed = event.getBlockPlaced();
        
        // Check for cooldown on cactus and sand placement
        if (placed.getType() == Material.CACTUS || placed.getType() == Material.SAND) {
            if (plugin.getStorageManager().isOnCooldown(player.getUniqueId())) {
                long secondsLeft = (plugin.getStorageManager().getCooldownUntil(player.getUniqueId()) - System.currentTimeMillis()) / 1000;
                if (!plugin.getConfig().getBoolean("silentMode", false)) {
                    player.sendMessage("§cYou are blocked from placing cactus/sand for §f" + secondsLeft + "s §cbecause of failed CAPTCHAs.");
                }
                event.setCancelled(true);
                return;
            }
        }
        
        Location loc = placed.getLocation();
        
        // Handle cactus placement
        if (placed.getType() == Material.CACTUS) {
            // Delay 1 tick to check for sand below
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                Block below = loc.clone().subtract(0, 1, 0).getBlock();
                if (below.getType() == Material.SAND) {
                    // Throttle: skip if last CAPTCHA <300s ago
                    if (!CaptchaManager.get().shouldChallenge(player)) {
                        if (!plugin.getConfig().getBoolean("silentMode", false)) {
                            player.sendMessage("§7[CactusCaptcha] §eSkipping cactus challenge - you recently completed a CAPTCHA (5min cooldown)");
                        }
                        return; // allow placement to proceed
                    }
                    // Remove the placed cactus and start challenge
                    placed.setType(Material.AIR);
                    CaptchaManager.get().startChallenge(player, loc);
                    PeriodicCheckTask.recordPlacement(player);
                }
            }, 1L);
        }
        
        // Handle sand placement - check if it could be used for cactus farming
        else if (placed.getType() == Material.SAND) {
            // Delay 1 tick to check for cactus above or adjacent
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                Block above = loc.clone().add(0, 1, 0).getBlock();
                boolean hasCactusAbove = above.getType() == Material.CACTUS;
                
                // Check for adjacent cactus (potential farming setup)
                boolean hasAdjacentCactus = false;
                Block[] adjacent = {
                    loc.clone().add(1, 0, 0).getBlock(),
                    loc.clone().add(-1, 0, 0).getBlock(),
                    loc.clone().add(0, 0, 1).getBlock(),
                    loc.clone().add(0, 0, -1).getBlock()
                };
                
                for (Block adj : adjacent) {
                    if (adj.getType() == Material.CACTUS) {
                        hasAdjacentCactus = true;
                        break;
                    }
                }
                
                // Trigger captcha if sand is placed in a cactus farming context
                if (hasCactusAbove || hasAdjacentCactus) {
                    // Throttle: skip if last CAPTCHA <300s ago
                    if (!CaptchaManager.get().shouldChallenge(player)) {
                        if (!plugin.getConfig().getBoolean("silentMode", false)) {
                            player.sendMessage("§7[CactusCaptcha] §eSkipping sand challenge - you recently completed a CAPTCHA (5min cooldown)");
                        }
                        return; // allow placement to proceed
                    }
                    // Remove the placed sand and start challenge
                    placed.setType(Material.AIR);
                    CaptchaManager.get().startChallenge(player, loc);
                    PeriodicCheckTask.recordPlacement(player);
                }
            }, 1L);
        }
    }
}