package net.minecadia.cactuscaptcha.listener;

import net.minecadia.cactuscaptcha.CactusCaptcha;
import net.minecadia.cactuscaptcha.gui.ConfigToolsGUI;
import net.minecadia.cactuscaptcha.gui.ConfigEditorGUI;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

/**
 * Listener for handling ConfigToolsGUI and ConfigEditorGUI interactions.
 * Handles clicks on config editor buttons and sub-GUI interactions.
 */
public class ConfigToolsGUIListener implements Listener {
    
    private final CactusCaptcha plugin;
    
    public ConfigToolsGUIListener(CactusCaptcha plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player player = (Player) event.getWhoClicked();
        
        // Handle ConfigToolsGUI
        if (ConfigToolsGUI.isConfigToolsGUI(event.getInventory())) {
            handleConfigToolsClick(event, player);
        }
        // Handle ConfigEditorGUI
        else if (ConfigEditorGUI.isConfigEditorGUI(event.getInventory())) {
            handleConfigEditorClick(event, player);
        }
    }
    
    /**
     * Handles clicks in the main Config & Tools GUI.
     */
    private void handleConfigToolsClick(InventoryClickEvent event, Player player) {
        event.setCancelled(true); // Prevent item movement
        
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;
        
        String displayName = clicked.hasItemMeta() ? clicked.getItemMeta().getDisplayName() : "";
        
        // Handle different config option buttons
        if (displayName.contains("CAPTCHA Cooldown")) {
            player.openInventory(ConfigEditorGUI.createEditorGUI(player, ConfigEditorGUI.ConfigType.CAPTCHA_COOLDOWN));
        } else if (displayName.contains("Passive Trigger Timer")) {
            player.openInventory(ConfigEditorGUI.createEditorGUI(player, ConfigEditorGUI.ConfigType.PASSIVE_TIMER));
        } else if (displayName.contains("Max Attempts")) {
            player.openInventory(ConfigEditorGUI.createEditorGUI(player, ConfigEditorGUI.ConfigType.MAX_ATTEMPTS));
        } else if (displayName.contains("Fail Cooldowns")) {
            player.openInventory(ConfigEditorGUI.createFailCooldownsGUI(player));
        } else if (displayName.contains("Debug Mode")) {
            player.openInventory(ConfigEditorGUI.createEditorGUI(player, ConfigEditorGUI.ConfigType.DEBUG_MODE));
        } else if (displayName.contains("Silent Probe")) {
            player.openInventory(ConfigEditorGUI.createEditorGUI(player, ConfigEditorGUI.ConfigType.SILENT_PROBE));
        } else if (displayName.contains("Reload Config")) {
            handleReloadConfig(player);
        } else if (displayName.contains("Save Config")) {
            handleConfigSave(player);
        }
    }
    
    /**
     * Handles clicks in the Config Editor sub-GUIs.
     */
    private void handleConfigEditorClick(InventoryClickEvent event, Player player) {
        event.setCancelled(true); // Prevent item movement
        
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;
        
        String displayName = clicked.hasItemMeta() ? clicked.getItemMeta().getDisplayName() : "";
        String title = event.getInventory().getTitle();
        
        // Handle back button
        if (displayName.contains("Back")) {
            player.openInventory(ConfigToolsGUI.build(player));
            return;
        }
        
        // Handle fail cooldowns GUI
        if (title.contains("Fail Cooldowns")) {
            handleFailCooldownsClick(event, player, displayName);
            return;
        }
        
        // Handle regular config editor
        ConfigEditorGUI.ConfigType configType = ConfigEditorGUI.getConfigTypeFromTitle(title);
        if (configType != null) {
            handleConfigValueClick(event, player, configType, displayName);
        }
    }
    
    /**
     * Handles clicks in the fail cooldowns editor GUI.
     */
    private void handleFailCooldownsClick(InventoryClickEvent event, Player player, String displayName) {
        if (displayName.contains("Light Penalties")) {
            ConfigEditorGUI.updateFailCooldowns(player, Arrays.asList(2, 5, 30, 60));
        } else if (displayName.contains("Default Penalties")) {
            ConfigEditorGUI.updateFailCooldowns(player, Arrays.asList(3, 10, 60, 120));
        } else if (displayName.contains("Heavy Penalties")) {
            ConfigEditorGUI.updateFailCooldowns(player, Arrays.asList(5, 15, 120, 300));
        } else if (displayName.contains("Extreme Penalties")) {
            ConfigEditorGUI.updateFailCooldowns(player, Arrays.asList(10, 30, 300, 600));
        } else if (displayName.contains("Save & Apply")) {
            // Already saved by the preset buttons, just show message
            player.sendMessage(ChatColor.GREEN + "§aSaved successfully!");
        }
    }
    
    /**
     * Handles clicks for config value adjustment.
     */
    private void handleConfigValueClick(InventoryClickEvent event, Player player, ConfigEditorGUI.ConfigType configType, String displayName) {
        if (displayName.contains("Save & Apply")) {
            // Config is already saved by the update methods
            player.sendMessage(ChatColor.GREEN + "§aSaved successfully!");
            return;
        }
        
        if (configType.isToggle()) {
            // Handle toggle
            if (displayName.contains(configType.getDisplayName())) {
                ConfigEditorGUI.updateConfigValue(player, configType, 0); // Toggle
            }
        } else {
            // Handle numeric changes
            if (displayName.contains("-- (Large)")) {
                ConfigEditorGUI.updateConfigValue(player, configType, -(configType.getIncrement() * 5));
            } else if (displayName.contains("- (Small)")) {
                ConfigEditorGUI.updateConfigValue(player, configType, -configType.getIncrement());
            } else if (displayName.contains("+ (Small)")) {
                ConfigEditorGUI.updateConfigValue(player, configType, configType.getIncrement());
            } else if (displayName.contains("++ (Large)")) {
                ConfigEditorGUI.updateConfigValue(player, configType, configType.getIncrement() * 5);
            }
        }
    }
    
    /**
     * Handles the Reload Config button click.
     * Reloads the plugin configuration without restart.
     */
    private void handleReloadConfig(Player player) {
        try {
            plugin.reloadConfig();
            player.sendMessage(ChatColor.GREEN + "✓ Configuration reloaded successfully!");
            player.sendMessage(ChatColor.GRAY + "All settings have been refreshed from config.yml");
        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "✗ Failed to reload configuration: " + e.getMessage());
            plugin.getLogger().warning("Config reload failed: " + e.getMessage());
        }
    }
    
    /**
     * Handles the Config Save Tool button click.
     * Saves the current configuration to config.yml file.
     */
    private void handleConfigSave(Player player) {
        try {
            plugin.saveConfig();
            plugin.reloadConfig();
            player.sendMessage(ChatColor.GREEN + "✓ Config saved and reloaded successfully!");
            player.sendMessage(ChatColor.GRAY + "All configuration changes have been saved to config.yml");
        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "✗ Failed to save config: " + e.getMessage());
            plugin.getLogger().warning("Config save failed: " + e.getMessage());
        }
    }
}