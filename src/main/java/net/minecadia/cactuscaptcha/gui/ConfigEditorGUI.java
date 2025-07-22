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
import java.util.List;

/**
 * Sub-GUI for editing individual config values with increment/decrement buttons.
 */
public class ConfigEditorGUI {
    
    public enum ConfigType {
        CAPTCHA_COOLDOWN("captchaTimer.secondsBetweenCaptchas", "CAPTCHA Cooldown", "seconds", 60, 1800, 30),
        PASSIVE_TIMER("periodicCaptcha.intervalSeconds", "Passive Trigger Timer", "seconds", 300, 1800, 60),
        MAX_ATTEMPTS("captcha.maxTries", "Max Attempts", "attempts", 1, 10, 1),
        DEBUG_MODE("debug.enabled", "Debug Mode", "toggle", 0, 1, 1),
        SILENT_PROBE("periodicCaptcha.enabled", "Silent Probe", "toggle", 0, 1, 1);
        
        private final String configPath;
        private final String displayName;
        private final String unit;
        private final int minValue;
        private final int maxValue;
        private final int increment;
        
        ConfigType(String configPath, String displayName, String unit, int minValue, int maxValue, int increment) {
            this.configPath = configPath;
            this.displayName = displayName;
            this.unit = unit;
            this.minValue = minValue;
            this.maxValue = maxValue;
            this.increment = increment;
        }
        
        public String getConfigPath() { return configPath; }
        public String getDisplayName() { return displayName; }
        public String getUnit() { return unit; }
        public int getMinValue() { return minValue; }
        public int getMaxValue() { return maxValue; }
        public int getIncrement() { return increment; }
        public boolean isToggle() { return "toggle".equals(unit); }
    }
    
    /**
     * Creates a config editor sub-GUI for the specified config type.
     * 
     * @param player The player to open the GUI for
     * @param configType The type of config value to edit
     * @return The created inventory
     */
    public static Inventory createEditorGUI(Player player, ConfigType configType) {
        String title = "§9Edit: " + configType.getDisplayName();
        Inventory inv = Bukkit.createInventory(null, 9, title);
        CactusCaptcha plugin = CactusCaptcha.getInstance();
        
        if (configType.isToggle()) {
            // Toggle interface
            boolean currentValue = plugin.getConfig().getBoolean(configType.getConfigPath(), false);
            
            // Toggle button
            inv.setItem(4, buildItem(
                currentValue ? Material.EMERALD_BLOCK : Material.REDSTONE_BLOCK,
                "§e" + configType.getDisplayName(),
                Arrays.asList(
                    "§7Current: §f" + (currentValue ? "Enabled" : "Disabled"),
                    "§aClick to toggle"
                )
            ));
            
        } else {
            // Numeric interface
            int currentValue = plugin.getConfig().getInt(configType.getConfigPath(), configType.getMinValue());
            
            // Decrease buttons
            inv.setItem(0, buildItem(Material.REDSTONE_BLOCK, "§c-- (Large)", Arrays.asList("§7Decrease by " + (configType.getIncrement() * 5))));
            inv.setItem(1, buildItem(Material.GOLD_BLOCK, "§6- (Small)", Arrays.asList("§7Decrease by " + configType.getIncrement())));
            
            // Current value display
            inv.setItem(4, buildItem(Material.PAPER, "§e" + configType.getDisplayName(), Arrays.asList(
                "§7Current: §f" + currentValue + " " + configType.getUnit(),
                "§7Range: §f" + configType.getMinValue() + " - " + configType.getMaxValue()
            )));
            
            // Increase buttons
            inv.setItem(7, buildItem(Material.EMERALD_BLOCK, "§a+ (Small)", Arrays.asList("§7Increase by " + configType.getIncrement())));
            inv.setItem(8, buildItem(Material.DIAMOND_BLOCK, "§2++ (Large)", Arrays.asList("§7Increase by " + (configType.getIncrement() * 5))));
        }
        
        // Back button
        inv.setItem(configType.isToggle() ? 8 : 3, buildItem(Material.ARROW, "§7← Back", Arrays.asList("§7Return to Config & Tools")));
        
        // Save button
        inv.setItem(configType.isToggle() ? 0 : 5, buildItem(Material.EMERALD, "§aSave & Apply", Arrays.asList("§7Save changes and reload config")));
        
        return inv;
    }
    
    /**
     * Creates a special editor for fail cooldowns (list of integers).
     * 
     * @param player The player to open the GUI for
     * @return The created inventory
     */
    public static Inventory createFailCooldownsGUI(Player player) {
        String title = "§9Edit: Fail Cooldowns";
        Inventory inv = Bukkit.createInventory(null, 27, title);
        CactusCaptcha plugin = CactusCaptcha.getInstance();
        
        List<Integer> cooldowns = plugin.getConfig().getIntegerList("captcha.punishmentCooldownsMinutes");
        
        // Display current cooldowns
        inv.setItem(4, buildItem(Material.PAPER, "§eCurrent Cooldowns", Arrays.asList(
            "§7Tier 1: §f" + (cooldowns.size() > 0 ? cooldowns.get(0) : 3) + " minutes",
            "§7Tier 2: §f" + (cooldowns.size() > 1 ? cooldowns.get(1) : 10) + " minutes", 
            "§7Tier 3: §f" + (cooldowns.size() > 2 ? cooldowns.get(2) : 60) + " minutes",
            "§7Tier 4: §f" + (cooldowns.size() > 3 ? cooldowns.get(3) : 120) + " minutes"
        )));
        
        // Preset buttons
        inv.setItem(10, buildItem(Material.WOOL, "§aLight Penalties", Arrays.asList("§7Set to: [2, 5, 30, 60]")));
        inv.setItem(12, buildItem(Material.WOOL, "§eDefault Penalties", Arrays.asList("§7Set to: [3, 10, 60, 120]")));
        inv.setItem(14, buildItem(Material.WOOL, "§cHeavy Penalties", Arrays.asList("§7Set to: [5, 15, 120, 300]")));
        inv.setItem(16, buildItem(Material.WOOL, "§4Extreme Penalties", Arrays.asList("§7Set to: [10, 30, 300, 600]")));
        
        // Control buttons
        inv.setItem(18, buildItem(Material.ARROW, "§7← Back", Arrays.asList("§7Return to Config & Tools")));
        inv.setItem(26, buildItem(Material.EMERALD, "§aSave & Apply", Arrays.asList("§7Save changes and reload config")));
        
        return inv;
    }
    
    /**
     * Helper method to create ItemStack with display name and lore.
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
     * Checks if the given inventory is a config editor GUI.
     */
    public static boolean isConfigEditorGUI(Inventory inventory) {
        return inventory != null && inventory.getTitle().startsWith("§9Edit:");
    }
    
    /**
     * Gets the config type from the inventory title.
     */
    public static ConfigType getConfigTypeFromTitle(String title) {
        if (title.contains("CAPTCHA Cooldown")) return ConfigType.CAPTCHA_COOLDOWN;
        if (title.contains("Passive Trigger Timer")) return ConfigType.PASSIVE_TIMER;
        if (title.contains("Max Attempts")) return ConfigType.MAX_ATTEMPTS;
        if (title.contains("Debug Mode")) return ConfigType.DEBUG_MODE;
        if (title.contains("Silent Probe")) return ConfigType.SILENT_PROBE;
        return null;
    }
    
    /**
     * Handles value updates for config options.
     */
    public static void updateConfigValue(Player player, ConfigType configType, int change) {
        CactusCaptcha plugin = CactusCaptcha.getInstance();
        
        if (configType.isToggle()) {
            // Toggle boolean value
            boolean currentValue = plugin.getConfig().getBoolean(configType.getConfigPath(), false);
            plugin.getConfig().set(configType.getConfigPath(), !currentValue);
        } else {
            // Update numeric value
            int currentValue = plugin.getConfig().getInt(configType.getConfigPath(), configType.getMinValue());
            int newValue = Math.max(configType.getMinValue(), 
                          Math.min(configType.getMaxValue(), currentValue + change));
            plugin.getConfig().set(configType.getConfigPath(), newValue);
        }
        
        // Save and reload config
        plugin.saveConfig();
        plugin.reloadConfig();
        
        // Show success message
        player.sendMessage(ChatColor.GREEN + "§aSaved successfully!");
        
        // Refresh the GUI
        player.openInventory(createEditorGUI(player, configType));
    }
    
    /**
     * Updates fail cooldowns with preset values.
     */
    public static void updateFailCooldowns(Player player, List<Integer> newCooldowns) {
        CactusCaptcha plugin = CactusCaptcha.getInstance();
        plugin.getConfig().set("captcha.punishmentCooldownsMinutes", newCooldowns);
        
        // Save and reload config
        plugin.saveConfig();
        plugin.reloadConfig();
        
        // Show success message
        player.sendMessage(ChatColor.GREEN + "§aSaved successfully!");
        
        // Refresh the GUI
        player.openInventory(createFailCooldownsGUI(player));
    }
}