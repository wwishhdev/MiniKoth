package com.wish;

import com.wish.managers.ConfigManager;
import com.wish.managers.KOTHManager;
import com.wish.managers.RewardManager;
import com.wish.managers.ScheduleManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * MiniKOTH Main Class
 * This is the main class of the MiniKOTH plugin that handles all core functionality
 * including initialization of managers and event listeners.
 *
 * @author wwishh
 * @version 0.0.1
 */
public class MiniKOTH extends JavaPlugin {

    private static MiniKOTH instance;
    private ConfigManager configManager;
    private KOTHManager kothManager;
    private RewardManager rewardManager;
    private ScheduleManager scheduleManager;

    /**
     * Called when the plugin is enabled
     * Initializes all managers and registers events and commands
     */
    @Override
    public void onEnable() {
        // Set instance
        instance = this;

        // Save default config
        saveDefaultConfig();

        // Initialize managers
        this.configManager = new ConfigManager(this);
        this.kothManager = new KOTHManager(this);
        this.rewardManager = new RewardManager(this);
        this.scheduleManager = new ScheduleManager(this);

        // Register commands and events (to be implemented)
        registerCommands();
        registerEvents();

        // Log startup
        getLogger().info("MiniKOTH has been enabled!");
    }

    /**
     * Called when the plugin is disabled
     * Handles cleanup and saving of data
     */
    @Override
    public void onDisable() {
        // Save any necessary data
        if (kothManager != null) {
            kothManager.saveAll();
        }

        getLogger().info("MiniKOTH has been disabled!");
    }

    /**
     * Gets the instance of the plugin
     * @return MiniKOTH instance
     */
    public static MiniKOTH getInstance() {
        return instance;
    }

    /**
     * Registers all commands for the plugin
     */
    private void registerCommands() {
        // To be implemented
    }

    /**
     * Registers all event listeners
     */
    private void registerEvents() {
        // To be implemented
    }

    // Getter methods for managers
    public ConfigManager getConfigManager() {
        return configManager;
    }

    public KOTHManager getKothManager() {
        return kothManager;
    }

    public RewardManager getRewardManager() {
        return rewardManager;
    }

    public ScheduleManager getScheduleManager() {
        return scheduleManager;
    }
}