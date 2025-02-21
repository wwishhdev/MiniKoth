package com.wish.managers;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.wish.MiniKOTH;
import com.wish.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

/**
 * KOTHManager
 * Manages all KOTH-related operations including creation, deletion,
 * and runtime management of KOTHs
 *
 * @author wwishh
 * @version 0.0.1
 */
public class KOTHManager {
    private final MiniKOTH plugin;
    private final Map<String, KOTH> koths;
    private final Map<String, BukkitRunnable> activeTimers;

    /**
     * Constructor for KOTHManager
     *
     * @param plugin Instance of the main plugin class
     */
    public KOTHManager(MiniKOTH plugin) {
        this.plugin = plugin;
        this.koths = new HashMap<>();
        this.activeTimers = new HashMap<>();
        loadKOTHs();
    }

    /**
     * Creates a new KOTH
     *
     * @param name KOTH name
     * @param region WorldGuard region
     * @param chestSpawnLocation Location for chest spawn
     * @return true if creation was successful
     */
    public boolean createKOTH(String name, ProtectedRegion region, Location chestSpawnLocation) {
        if (koths.containsKey(name)) {
            return false;
        }

        KOTH koth = new KOTH(name, region, chestSpawnLocation);
        koths.put(name, koth);
        saveKOTH(koth);
        return true;
    }

    /**
     * Starts a KOTH event
     *
     * @param name KOTH name
     * @return true if KOTH was started successfully
     */
    public boolean startKOTH(String name) {
        KOTH koth = koths.get(name);
        if (koth == null || koth.isActive()) {
            return false;
        }

        koth.setActive(true);
        koth.setRemainingTime(plugin.getConfigManager().getCaptureTime());

        // Start capture timer
        BukkitRunnable timer = new BukkitRunnable() {
            @Override
            public void run() {
                if (koth.getRemainingTime() <= 0) {
                    endKOTH(name);
                    return;
                }

                // Check for capturer
                checkCapture(koth);

                // Update time
                koth.setRemainingTime(koth.getRemainingTime() - 1);

                // Send time messages
                if (koth.getRemainingTime() % plugin.getConfigManager().getMessageInterval() == 0) {
                    broadcastTimeRemaining(koth);
                }
            }
        };

        timer.runTaskTimer(plugin, 20L, 20L);
        activeTimers.put(name, timer);

        // Broadcast start message
        Bukkit.broadcastMessage(plugin.getConfigManager().getMessage("koth-started", "name", name));
        return true;
    }

    /**
     * Ends a KOTH event
     *
     * @param name KOTH name
     */
    public void endKOTH(String name) {
        KOTH koth = koths.get(name);
        if (koth == null || !koth.isActive()) {
            return;
        }

        // Cancel timer
        BukkitRunnable timer = activeTimers.remove(name);
        if (timer != null) {
            timer.cancel();
        }

        // Reset KOTH state
        koth.setActive(false);
        koth.setCurrentCapturer(null);
        koth.setRemainingTime(0);

        // Handle rewards if there was a capturer
        if (koth.getCurrentCapturer() != null) {
            plugin.getRewardManager().giveRewards(koth.getCurrentCapturer());
        }

        // Broadcast end message
        Bukkit.broadcastMessage(plugin.getConfigManager().getMessage("koth-ended", "name", name));
    }

    /**
     * Checks if a player is capturing a KOTH
     *
     * @param koth KOTH to check
     */
    private void checkCapture(KOTH koth) {
        Player capturer = null;
        int playersInRegion = 0;

        // Check all players in region
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (isInRegion(player, koth.getRegion())) {
                playersInRegion++;
                capturer = player;
            }
        }

        // Only one player can capture
        if (playersInRegion == 1) {
            String newCapturer = capturer.getName();
            if (!newCapturer.equals(koth.getCurrentCapturer())) {
                koth.setCurrentCapturer(newCapturer);
                capturer.sendMessage(plugin.getConfigManager().getMessage("start-capture"));
            }

            // Agregar barra de progreso en ActionBar
            double progress = 1 - ((double) koth.getRemainingTime() / koth.getCaptureTime());
            String progressBar = MessageUtils.createProgressBar(progress, 20);
            String timeLeft = String.format("%d:%02d",
                    koth.getRemainingTime() / 60,
                    koth.getRemainingTime() % 60);
            MessageUtils.sendActionBar(capturer,
                    "§eCaptured: " + progressBar + " §e" + timeLeft);
        } else {
            // Reset capturer if multiple players or no players
            if (koth.getCurrentCapturer() != null) {
                Player oldCapturer = Bukkit.getPlayer(koth.getCurrentCapturer());
                if (oldCapturer != null) {
                    oldCapturer.sendMessage(plugin.getConfigManager().getMessage("leave-capture"));
                }
                koth.setCurrentCapturer(null);
            }
        }
    }

    /**
     * Removes a KOTH from the manager
     * @param name Name of the KOTH to remove
     * @return true if KOTH was removed successfully
     */
    public boolean removeKOTH(String name) {
        KOTH koth = koths.remove(name);
        if (koth != null) {
            // Cancel any active timers
            BukkitRunnable timer = activeTimers.remove(name);
            if (timer != null) {
                timer.cancel();
            }
            return true;
        }
        return false;
    }

    /**
     * Checks if a player is in a WorldGuard region
     *
     * @param player Player to check
     * @param region Region to check
     * @return true if player is in region
     */
    private boolean isInRegion(Player player, ProtectedRegion region) {
        WorldGuardPlugin worldGuard = getWorldGuard();
        if (worldGuard == null) return false;

        RegionManager regionManager = worldGuard.getRegionManager(player.getWorld());
        return regionManager != null && region.contains(
                player.getLocation().getBlockX(),
                player.getLocation().getBlockY(),
                player.getLocation().getBlockZ()
        );
    }

    /**
     * Gets WorldGuard plugin instance
     *
     * @return WorldGuardPlugin instance
     */
    private WorldGuardPlugin getWorldGuard() {
        return (WorldGuardPlugin) plugin.getServer().getPluginManager().getPlugin("WorldGuard");
    }

    /**
     * Broadcasts remaining time message
     *
     * @param koth KOTH to broadcast for
     */
    private void broadcastTimeRemaining(KOTH koth) {
        String timeStr = String.format("%d:%02d",
                koth.getRemainingTime() / 60,
                koth.getRemainingTime() % 60);
        Bukkit.broadcastMessage(plugin.getConfigManager().getMessage("time-remaining", "time", timeStr));
    }

    /**
     * Loads all KOTHs from configuration
     */
    private void loadKOTHs() {
        ConfigurationSection kothsSection = plugin.getConfigManager().getKOTHsSection();
        if (kothsSection == null) return;

        WorldGuardPlugin worldGuard = getWorldGuard();
        if (worldGuard == null) return;

        for (String key : kothsSection.getKeys(false)) {
            ConfigurationSection kothSection = kothsSection.getConfigurationSection(key);
            if (kothSection == null) continue;

            String regionName = kothSection.getString("region-name");
            if (regionName == null) continue;

            // Get region from WorldGuard
            RegionManager regionManager = worldGuard.getRegionManager(
                    Bukkit.getWorlds().get(0)); // You might want to store world name in config
            if (regionManager == null) continue;

            ProtectedRegion region = regionManager.getRegion(regionName);
            if (region == null) continue;

            // Create KOTH instance
            KOTH koth = KOTH.fromConfig(kothSection, region);
            koths.put(koth.getName(), koth);
        }
    }

    /**
     * Saves a KOTH to configuration
     *
     * @param koth KOTH to save
     */
    public void saveKOTH(KOTH koth) {
        ConfigurationSection kothsSection = plugin.getConfig().getConfigurationSection("koths");
        if (kothsSection == null) {
            kothsSection = plugin.getConfig().createSection("koths");
        }

        ConfigurationSection kothSection = kothsSection.createSection(koth.getName());
        koth.saveToConfig(kothSection);
        plugin.saveConfig();
    }

    /**
     * Saves all KOTHs to configuration
     */
    public void saveAll() {
        for (KOTH koth : koths.values()) {
            saveKOTH(koth);
        }
    }

    /**
     * Gets a KOTH by name
     *
     * @param name KOTH name
     * @return KOTH instance or null if not found
     */
    public KOTH getKOTH(String name) {
        return koths.get(name);
    }

    /**
     * Gets all KOTHs
     *
     * @return Map of KOTH names to KOTH instances
     */
    public Map<String, KOTH> getKOTHs() {
        return new HashMap<>(koths);
    }
}