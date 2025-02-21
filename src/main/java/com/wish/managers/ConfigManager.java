package com.wish.managers;

import com.wish.MiniKOTH;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

/**
 * ConfigManager
 * Handles all configuration-related operations for the MiniKOTH plugin
 * including loading, saving, and accessing configuration values.
 *
 * @author wwishh
 * @version 0.0.1
 */
public class ConfigManager {
    private final MiniKOTH plugin;
    private FileConfiguration config;

    /**
     * Constructor for ConfigManager
     * @param plugin Instance of the main plugin class
     */
    public ConfigManager(MiniKOTH plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    /**
     * Loads or reloads the configuration file
     */
    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }

    /**
     * Gets a formatted message from the config
     * @param path Path to the message in config
     * @param replacements String pairs for placeholder replacement (key1, value1, key2, value2, ...)
     * @return Formatted message with color codes translated
     */
    public String getMessage(String path, String... replacements) {
        String message = config.getString("messages." + path, "Message not found: " + path);
        message = getPrefix() + " " + message;

        // Replace placeholders
        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                message = message.replace("{" + replacements[i] + "}", replacements[i + 1]);
            }
        }

        return ChatColor.translateAlternateColorCodes('&', message);
    }

    /**
     * Gets the plugin prefix from config
     * @return Formatted prefix string
     */
    public String getPrefix() {
        return ChatColor.translateAlternateColorCodes('&',
                config.getString("settings.prefix", "&8[&bMiniKOTH&8]"));
    }

    /**
     * Gets the capture time in seconds
     * @return Capture time in seconds
     */
    public int getCaptureTime() {
        return config.getInt("settings.capture-time", 300);
    }

    /**
     * Gets the message interval in seconds
     * @return Message interval in seconds
     */
    public int getMessageInterval() {
        return config.getInt("settings.message-interval", 30);
    }

    /**
     * Gets the chest despawn time in seconds
     * @return Chest despawn time in seconds
     */
    public int getChestDespawnTime() {
        return config.getInt("settings.chest-despawn-time", 300);
    }

    /**
     * Gets the list of reward commands
     * @return List of reward commands
     */
    public List<String> getRewardCommands() {
        return config.getStringList("rewards.commands");
    }

    /**
     * Gets whether schedules are enabled
     * @return true if schedules are enabled
     */
    public boolean areSchedulesEnabled() {
        return config.getBoolean("schedules.enabled", true);
    }

    /**
     * Gets the timezone for schedules
     * @return Timezone string
     */
    public String getTimezone() {
        return config.getString("schedules.timezone", "America/Argentina/Buenos_Aires");
    }

    /**
     * Gets the list of scheduled times
     * @return List of scheduled times
     */
    public List<String> getScheduledTimes() {
        return config.getStringList("schedules.times");
    }

    /**
     * Gets the KOTHs configuration section
     * @return ConfigurationSection containing KOTH data
     */
    public ConfigurationSection getKOTHsSection() {
        return config.getConfigurationSection("koths");
    }

    /**
     * Saves the current configuration to file
     */
    public void saveConfig() {
        plugin.saveConfig();
    }
}