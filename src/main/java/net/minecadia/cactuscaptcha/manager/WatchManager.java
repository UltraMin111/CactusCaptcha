package net.minecadia.cactuscaptcha.manager;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Manages the CAPTCHA watchlist system for monitoring specific players.
 * Allows staff to add/remove players from watchlist and receive notifications
 * when watched players fail CAPTCHAs or trigger suspicious behavior.
 */
public class WatchManager {
    
    private static final Set<UUID> watched = new HashSet<>();
    
    /**
     * Toggles a player's watch status on the CAPTCHA watchlist.
     * If the player is already being watched, removes them from the list.
     * If not being watched, adds them to the list.
     * 
     * @param target The player to toggle watch status for
     * @param sender The command sender who initiated the toggle
     */
    public static void toggleWatch(Player target, CommandSender sender) {
        if (watched.contains(target.getUniqueId())) {
            watched.remove(target.getUniqueId());
            sender.sendMessage("§aRemoved " + target.getName() + " from CAPTCHA watchlist.");
        } else {
            watched.add(target.getUniqueId());
            sender.sendMessage("§eWatching " + target.getName() + " for CAPTCHA activity.");
        }
    }
    
    /**
     * Checks if a player is currently being watched.
     * 
     * @param uuid The UUID of the player to check
     * @return true if the player is being watched, false otherwise
     */
    public static boolean isWatched(UUID uuid) {
        return watched.contains(uuid);
    }
    
    /**
     * Sends a notification to all online staff members if the specified player
     * is being watched. This should be called when a watched player triggers
     * CAPTCHA-related events.
     * 
     * @param player The player who triggered the event
     * @param reason The reason for the notification (e.g., "Failed CAPTCHA", "Triggered honeytoken")
     */
    public static void notifyIfWatched(Player player, String reason) {
        if (!isWatched(player.getUniqueId())) {
            return;
        }
        
        String msg = "§6[CaptchaWatch] §e" + player.getName() + " §7→ " + reason;
        for (Player staff : Bukkit.getOnlinePlayers()) {
            if (staff.hasPermission("cactuscaptcha.watch")) {
                staff.sendMessage(msg);
            }
        }
    }
    
    /**
     * Gets the current number of players being watched.
     * 
     * @return The number of players on the watchlist
     */
    public static int getWatchedCount() {
        return watched.size();
    }
    
    /**
     * Removes a player from the watchlist by UUID.
     * This can be used when a player logs off or is no longer of interest.
     * 
     * @param uuid The UUID of the player to remove
     * @return true if the player was removed, false if they weren't being watched
     */
    public static boolean removeWatch(UUID uuid) {
        return watched.remove(uuid);
    }
    
    /**
     * Clears all players from the watchlist.
     * This might be useful for administrative purposes.
     */
    public static void clearWatchlist() {
        watched.clear();
    }
}