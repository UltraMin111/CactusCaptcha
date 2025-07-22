package net.minecadia.cactuscaptcha.gui;

import net.minecadia.cactuscaptcha.CactusCaptcha;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

/**
 * GUI for displaying CactusCaptcha changelog with version history.
 */
public class ChangelogGui {
    
    private final CactusCaptcha plugin;
    
    public ChangelogGui(CactusCaptcha plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Opens the changelog GUI for a player.
     * 
     * @param player The player to open the GUI for
     */
    public void openGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 36, "§bCactusCaptcha Changelog");
        
        // Create changelog items in reverse order (latest first)
        int slot = 0;
        
        // v1.2.20 - Revamped Config & Tools tab with GUI-based config editor and cleanup
        gui.setItem(slot++, createVersionItem("v1.2.20", Arrays.asList(
            ChatColor.GRAY + "Removed fake bot tester and",
            ChatColor.GRAY + "old config commands",
            ChatColor.GRAY + "Implemented live GUI-based",
            ChatColor.GRAY + "config editor",
            ChatColor.GRAY + "Updated Admin GUI tools tab layout"
        )));
        
        // v1.2.18 - Watchlist system, bug report GUI, GUI lockouts, better admin feedback
        gui.setItem(slot++, createVersionItem("v1.2.18", Arrays.asList(
            ChatColor.GRAY + "Watchlist system, bug report",
            ChatColor.GRAY + "GUI, GUI lockouts, better",
            ChatColor.GRAY + "admin feedback"
        )));
        
        // v1.2.16 - Full behavioral detection: movement % repeat, honeytokens, solve speed, fail pattern
        gui.setItem(slot++, createVersionItem("v1.2.16", Arrays.asList(
            ChatColor.GRAY + "Full behavioral detection:",
            ChatColor.GRAY + "movement % repeat,",
            ChatColor.GRAY + "honeytokens, solve speed,",
            ChatColor.GRAY + "fail pattern"
        )));
        
        // v1.1.7 - /cc viewbugs GUI showing submitted bug reports
        gui.setItem(slot++, createVersionItem("v1.1.7", Arrays.asList(
            ChatColor.GRAY + "/cc viewbugs GUI showing",
            ChatColor.GRAY + "submitted bug reports"
        )));
        
        // v1.1.6 - /cc reportbug <message> command for players
        gui.setItem(slot++, createVersionItem("v1.1.6", Arrays.asList(
            ChatColor.GRAY + "/cc reportbug <message>",
            ChatColor.GRAY + "command for players"
        )));
        
        // v1.1.5 - /cc watch <player> system for staff alerts
        gui.setItem(slot++, createVersionItem("v1.1.5", Arrays.asList(
            ChatColor.GRAY + "/cc watch <player> system",
            ChatColor.GRAY + "for staff alerts"
        )));
        
        // v1.1.4 - Solve time tracking (under 1s detection)
        gui.setItem(slot++, createVersionItem("v1.1.4", Arrays.asList(
            ChatColor.GRAY + "Solve time tracking (under",
            ChatColor.GRAY + "1s detection)"
        )));
        
        // v1.1.3 - Fail pattern detection (repeated slot clicks)
        gui.setItem(slot++, createVersionItem("v1.1.3", Arrays.asList(
            ChatColor.GRAY + "Fail pattern detection",
            ChatColor.GRAY + "(repeated slot clicks)"
        )));
        
        // v1.1.2 - Silent CAPTCHA probe every 10 min, no GUI shown
        gui.setItem(slot++, createVersionItem("v1.1.2", Arrays.asList(
            ChatColor.GRAY + "Silent CAPTCHA probe every",
            ChatColor.GRAY + "10 min, no GUI shown"
        )));
        
        // v1.1.1 - Honeytoken decoy slot in CAPTCHA (clicking it flags player)
        gui.setItem(slot++, createVersionItem("v1.1.1", Arrays.asList(
            ChatColor.GRAY + "Honeytoken decoy slot in",
            ChatColor.GRAY + "CAPTCHA (clicking it flags",
            ChatColor.GRAY + "player)"
        )));
        
        // v1.1.0 - Hook monitor GUI for checking which detection systems are active
        gui.setItem(slot++, createVersionItem("v1.1.0", Arrays.asList(
            ChatColor.GRAY + "Hook monitor GUI for",
            ChatColor.GRAY + "checking which detection",
            ChatColor.GRAY + "systems are active"
        )));
        
        // v1.0.9 - Tools tab in admin GUI: reload, live config, theme preview, bot tester
        gui.setItem(slot++, createVersionItem("v1.0.9", Arrays.asList(
            ChatColor.GRAY + "Tools tab in admin GUI:",
            ChatColor.GRAY + "reload, live config, theme",
            ChatColor.GRAY + "preview, bot tester"
        )));
        
        // v1.0.8 - /cc changelog GUI added
        gui.setItem(slot++, createVersionItem("v1.0.8", Arrays.asList(
            ChatColor.GRAY + "/cc changelog GUI added"
        )));
        
        // v1.0.7 - Passive 10-minute CAPTCHA if actively placing cactus/sand
        gui.setItem(slot++, createVersionItem("v1.0.7", Arrays.asList(
            ChatColor.GRAY + "Passive 10-minute CAPTCHA",
            ChatColor.GRAY + "if actively placing",
            ChatColor.GRAY + "cactus/sand"
        )));
        
        // v1.0.6 - Switched to number memory CAPTCHA with randomized answers
        gui.setItem(slot++, createVersionItem("v1.0.6", Arrays.asList(
            ChatColor.GRAY + "Switched to number memory",
            ChatColor.GRAY + "CAPTCHA with randomized",
            ChatColor.GRAY + "answers"
        )));
        
        // v1.0.5 - Identical movement detection and % tracker
        gui.setItem(slot++, createVersionItem("v1.0.5", Arrays.asList(
            ChatColor.GRAY + "Identical movement detection",
            ChatColor.GRAY + "and % tracker"
        )));
        
        // v1.0.4 - Admin GUI base with player head stats (passes, fails, cooldowns)
        gui.setItem(slot++, createVersionItem("v1.0.4", Arrays.asList(
            ChatColor.GRAY + "Admin GUI base with player",
            ChatColor.GRAY + "head stats (passes, fails,",
            ChatColor.GRAY + "cooldowns)"
        )));
        
        // v1.0.3 - Fail logic with escalating lockout (3m → 10m → 60m → 120m)
        gui.setItem(slot++, createVersionItem("v1.0.3", Arrays.asList(
            ChatColor.GRAY + "Fail logic with escalating",
            ChatColor.GRAY + "lockout (3m → 10m → 60m →",
            ChatColor.GRAY + "120m)"
        )));
        
        // v1.0.2 - Added manual trigger commands: /cc force, /cc reset
        gui.setItem(slot++, createVersionItem("v1.0.2", Arrays.asList(
            ChatColor.GRAY + "Added manual trigger",
            ChatColor.GRAY + "commands: /cc force,",
            ChatColor.GRAY + "/cc reset"
        )));
        
        // v1.0.1 - Fixes for detection trigger logic and cooldown timing
        gui.setItem(slot++, createVersionItem("v1.0.1", Arrays.asList(
            ChatColor.GRAY + "Fixes for detection trigger",
            ChatColor.GRAY + "logic and cooldown timing"
        )));
        
        // v1.0.0 - Initial core system: cactus/sand placement detection, CAPTCHA GUI, basic block lockout
        gui.setItem(slot++, createVersionItem("v1.0.0", Arrays.asList(
            ChatColor.GRAY + "Initial core system:",
            ChatColor.GRAY + "cactus/sand placement",
            ChatColor.GRAY + "detection, CAPTCHA GUI,",
            ChatColor.GRAY + "basic block lockout"
        )));
        
        player.openInventory(gui);
    }
    
    /**
     * Creates a version item for the changelog.
     * 
     * @param version The version string
     * @param lore The lore list for the version
     * @return The created ItemStack
     */
    private ItemStack createVersionItem(String version, java.util.List<String> lore) {
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§eVersion " + version);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * Handles clicks in the changelog GUI.
     * All interactions are disabled as per requirements.
     * 
     * @param player The player who clicked
     * @param slot The slot that was clicked
     * @return false - all interactions are disabled
     */
    public boolean handleClick(Player player, int slot) {
        // All interactions are disabled
        return false;
    }
}