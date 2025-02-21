package com.wish.managers;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

/**
 * KOTH Class
 * Represents a single KOTH instance with all its properties and state
 *
 * @author wwishh
 * @version 0.0.1
 */
public class KOTH {
    private final String name;
    private final ProtectedRegion region;
    private Location chestSpawnLocation;
    private boolean active;
    private String currentCapturer;
    private int captureTime;
    private int remainingTime;

    /**
     * Constructor for KOTH
     *
     * @param name The name of the KOTH
     * @param region The WorldGuard region for this KOTH
     * @param chestSpawnLocation Location where reward chest will spawn
     */
    public KOTH(String name, ProtectedRegion region, Location chestSpawnLocation) {
        this.name = name;
        this.region = region;
        this.chestSpawnLocation = chestSpawnLocation;
        this.active = false;
        this.currentCapturer = null;
        this.captureTime = 300; // Default 5 minutes
        this.remainingTime = 0;
    }

    /**
     * Loads a KOTH from configuration
     *
     * @param section ConfigurationSection containing KOTH data
     * @param region WorldGuard region for this KOTH
     * @return KOTH instance
     */
    public static KOTH fromConfig(ConfigurationSection section, ProtectedRegion region) {
        String name = section.getName();
        Location chestSpawn = Location.deserialize(section.getConfigurationSection("chest-spawn").getValues(false));
        return new KOTH(name, region, chestSpawn);
    }

    /**
     * Saves KOTH data to configuration
     *
     * @param section ConfigurationSection to save to
     */
    public void saveToConfig(ConfigurationSection section) {
        section.set("region-name", region.getId());
        section.set("chest-spawn", chestSpawnLocation.serialize());
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public ProtectedRegion getRegion() {
        return region;
    }

    public Location getChestSpawnLocation() {
        return chestSpawnLocation;
    }

    public void setChestSpawnLocation(Location chestSpawnLocation) {
        this.chestSpawnLocation = chestSpawnLocation;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getCurrentCapturer() {
        return currentCapturer;
    }

    public void setCurrentCapturer(String currentCapturer) {
        this.currentCapturer = currentCapturer;
    }

    public int getCaptureTime() {
        return captureTime;
    }

    public void setCaptureTime(int captureTime) {
        this.captureTime = captureTime;
    }

    public int getRemainingTime() {
        return remainingTime;
    }

    public void setRemainingTime(int remainingTime) {
        this.remainingTime = remainingTime;
    }
}