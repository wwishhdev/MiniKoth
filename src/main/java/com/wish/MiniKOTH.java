package com.wish;

import com.wish.commands.MiniKOTHCommand;
import com.wish.listeners.ChestListener;
import com.wish.listeners.KOTHListener;
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
        try {
            // Set instance
            instance = this;

            // Check dependencies
            if (!checkDependencies()) {
                getServer().getPluginManager().disablePlugin(this);
                return;
            }

            // Save default config
            saveDefaultConfig();

            // Initialize managers in correct order
            this.configManager = new ConfigManager(this);
            this.kothManager = new KOTHManager(this);
            this.rewardManager = new RewardManager(this);
            this.scheduleManager = new ScheduleManager(this);

            // Register commands
            registerCommands();

            // Register events last
            registerEvents();

            // Log startup
            getLogger().info("MiniKOTH has been enabled!");
        } catch (Exception e) {
            getLogger().severe("Error enabling MiniKOTH: " + e.getMessage());
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
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
        MiniKOTHCommand commandExecutor = new MiniKOTHCommand(this);
        getCommand("minikoth").setExecutor(commandExecutor);
        getCommand("minikoth").setTabCompleter(commandExecutor);
    }

    /**
     * Registers all event listeners
     */
    private void registerEvents() {
        try {
            getServer().getPluginManager().registerEvents(new KOTHListener(this), this);
            getServer().getPluginManager().registerEvents(rewardManager.getChestListener(), this);
        } catch (Exception e) {
            getLogger().severe("Error registering events: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Verifies that all required dependencies are present
     * @return true if all dependencies are loaded
     */
    private boolean checkDependencies() {
        try {
            // Check WorldGuard
            if (getServer().getPluginManager().getPlugin("WorldGuard") == null) {
                getLogger().severe("WorldGuard not found! Disabling plugin...");
                return false;
            }

            // Check WorldEdit
            if (getServer().getPluginManager().getPlugin("WorldEdit") == null) {
                getLogger().severe("WorldEdit not found! Disabling plugin...");
                return false;
            }

            return true;
        } catch (Exception e) {
            getLogger().severe("Error checking dependencies: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
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