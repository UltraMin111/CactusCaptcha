package net.minecadia.cactuscaptcha.gui;

import net.minecadia.cactuscaptcha.CactusCaptcha;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

/**
 * GUI for viewing reported CAPTCHA bugs from players.
 * Displays bug reports with player name, timestamp, and message in a scrollable interface.
 */
public class BugReportGUI {
    
    private static final String GUI_TITLE = "§cBug Reports";
    
    /**
     * Opens the bug report GUI for the specified admin player.
     * Reads bug reports from bugreports.txt and displays them as paper items.
     * 
     * @param admin The admin player to open the GUI for
     */
    public static void open(Player admin) {
        List<String> lines;
        try {
            File file = new File(CactusCaptcha.getInstance().getDataFolder(), "bugreports.txt");
            if (!file.exists()) {
                admin.sendMessage("§cNo bug reports file found. No reports have been submitted yet.");
                return;
            }
            lines = Files.readAllLines(file.toPath());
        } catch (IOException e) {
            admin.sendMessage("§cCould not load bug reports: " + e.getMessage());
            return;
        }
        
        if (lines.isEmpty()) {
            admin.sendMessage("§eNo bug reports found.");
            return;
        }
        
        // Calculate inventory size (multiple of 9, max 54)
        int size = Math.min(((lines.size() / 9) + 1) * 9, 54);
        Inventory inv = Bukkit.createInventory(null, size, GUI_TITLE);
        
        int slot = 0;
        for (String line : lines) {
            if (slot >= 54) break; // Prevent overflow
            
            if (!line.contains("]") || !line.contains(":")) {
                continue; // Skip malformed lines
            }
            
            try {
                // Parse the line format: [timestamp] player: message
                String timestamp = line.substring(1, line.indexOf("]"));
                String rest = line.substring(line.indexOf("]") + 2);
                
                if (!rest.contains(":")) {
                    continue; // Skip if no colon found
                }
                
                String player = rest.split(":")[0].trim();
                String msg = rest.substring(rest.indexOf(":") + 1).trim();
                
                ItemStack paper = new ItemStack(Material.PAPER);
                ItemMeta meta = paper.getItemMeta();
                meta.setDisplayName("§f" + player);
                meta.setLore(Arrays.asList(
                    "§7Date: §e" + timestamp,
                    "§7Message:",
                    "§f" + wrap(msg, 30)
                ));
                paper.setItemMeta(meta);
                inv.setItem(slot++, paper);
                
            } catch (Exception e) {
                // Skip malformed lines and continue
                continue;
            }
        }
        
        // Fill empty slots with glass panes for better appearance
        fillEmpty(inv);
        
        admin.openInventory(inv);
    }
    
    /**
     * Wraps text to fit within the specified width and adds proper color formatting.
     * 
     * @param text The text to wrap
     * @param width The maximum width per line
     * @return The wrapped text with color formatting
     */
    private static String wrap(String text, int width) {
        if (text.length() <= width) {
            return text;
        }
        
        StringBuilder wrapped = new StringBuilder();
        String[] words = text.split(" ");
        int currentLineLength = 0;
        
        for (String word : words) {
            if (currentLineLength + word.length() + 1 > width) {
                wrapped.append("\n§f");
                currentLineLength = 0;
            }
            
            if (currentLineLength > 0) {
                wrapped.append(" ");
                currentLineLength++;
            }
            
            wrapped.append(word);
            currentLineLength += word.length();
        }
        
        return wrapped.toString();
    }
    
    /**
     * Fills empty slots in the inventory with gray glass panes for better visual appearance.
     * 
     * @param inv The inventory to fill
     */
    private static void fillEmpty(Inventory inv) {
        ItemStack filler = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7); // Gray glass pane
        ItemMeta meta = filler.getItemMeta();
        meta.setDisplayName(" ");
        filler.setItemMeta(meta);
        
        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getItem(i) == null) {
                inv.setItem(i, filler);
            }
        }
    }
    
    /**
     * Checks if the given inventory is the Bug Report GUI.
     * 
     * @param inventory The inventory to check
     * @return true if it's the Bug Report GUI, false otherwise
     */
    public static boolean isBugReportGUI(Inventory inventory) {
        return inventory != null && GUI_TITLE.equals(inventory.getTitle());
    }
}