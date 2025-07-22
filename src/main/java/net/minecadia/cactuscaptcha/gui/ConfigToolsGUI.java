package net.minecadia.cactuscaptcha.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

/**
 * GUI for configuration tools and testing features.
 * Provides access to config editing, theme preview, reloading, and fake bot testing.
 */
public class ConfigToolsGUI {
    
    private static final String GUI_TITLE = "§9🧪 Config & Tools";
    private static final int GUI_SIZE = 27; // 3 rows
    
    /**
     * Creates and returns the Config & Tools GUI inventory.
     * 
     * @param player The player to build the GUI for
     * @return The created inventory
     */
    public static Inventory build(Player player) {
        Inventory inv = Bukkit.createInventory(null, GUI_SIZE, GUI_TITLE);

        // Toggle Features - slot 10
        inv.setItem(10, buildItem(Material.LEVER, "§aToggle Features", Arrays.asList(
            "§7Enable/disable modules like:", 
            "§e• Movement Detection", 
            "§e• Periodic Captcha", 
            "§e• Retry Cooldown"
        )));

        // Adjust Values - slot 12
        inv.setItem(12, buildItem(Material.REPEATER, "§bAdjust Values", Arrays.asList(
            "§7Click to open value sliders", 
            "§7(e.g., tries, cooldown minutes)"
        )));

        // Theme Preview - slot 14
        inv.setItem(14, buildItem(Material.BOOK_AND_QUILL, "§6Theme Preview", Arrays.asList(
            "§7Preview and test GUI themes"
        )));

        // Reload Config - slot 16
        inv.setItem(16, buildItem(Material.REDSTONE_COMPARATOR, "§cReload Config", Arrays.asList(
            "§7Reloads plugin.yml, config.yml", 
            "§7without restart"
        )));

        // Fake Bot Tester - slot 22
        inv.setItem(22, buildItem(Material.VILLAGER_EGG, "§dFake Bot Tester", Arrays.asList(
            "§7Spawns dummy bot that fails CAPTCHA", 
            "§7Simulates scripted cactus farms"
        )));

        return inv;
    }

    /**
     * Helper method to create ItemStack with display name and lore.
     * 
     * @param mat The material for the item
     * @param name The display name
     * @param lore The lore list
     * @return The created ItemStack
     */
    private static ItemStack buildItem(Material mat, String name, List<String> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * Checks if the given inventory is the Config & Tools GUI.
     * 
     * @param inventory The inventory to check
     * @return true if it's the Config & Tools GUI, false otherwise
     */
    public static boolean isConfigToolsGUI(Inventory inventory) {
        return inventory != null && 
               inventory.getSize() == GUI_SIZE && 
               GUI_TITLE.equals(inventory.getTitle());
    }
}