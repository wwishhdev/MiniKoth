package com.wish.managers;

import com.wish.MiniKOTH;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * ScheduleManager
 * Handles automatic scheduling of KOTH events based on configured times
 *
 * @author wwishh
 * @version 0.0.1
 */
public class ScheduleManager {
    private final MiniKOTH plugin;
    private BukkitRunnable scheduleChecker;
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * Constructor for ScheduleManager
     * @param plugin Instance of the main plugin class
     */
    public ScheduleManager(MiniKOTH plugin) {
        this.plugin = plugin;
        if (plugin.getConfigManager().areSchedulesEnabled()) {
            startScheduler();
        }
    }

    /**
     * Starts the schedule checker
     */
    public void startScheduler() {
        if (scheduleChecker != null) {
            scheduleChecker.cancel();
        }

        scheduleChecker = new BukkitRunnable() {
            @Override
            public void run() {
                checkSchedule();
            }
        };

        // Run every minute
        scheduleChecker.runTaskTimer(plugin, 20L, 20L * 60);
    }

    /**
     * Stops the schedule checker
     */
    public void stopScheduler() {
        if (scheduleChecker != null) {
            scheduleChecker.cancel();
            scheduleChecker = null;
        }
    }

    /**
     * Checks if any KOTH should start based on current time
     */
    private void checkSchedule() {
        if (!plugin.getConfigManager().areSchedulesEnabled()) {
            return;
        }

        // Get current time in configured timezone
        LocalTime currentTime = LocalTime.now(ZoneId.of(plugin.getConfigManager().getTimezone()));
        String currentTimeStr = currentTime.format(timeFormatter);

        // Check if current time matches any scheduled time
        List<String> scheduledTimes = plugin.getConfigManager().getScheduledTimes();
        if (scheduledTimes.contains(currentTimeStr)) {
            startRandomKOTH();
        }
    }

    /**
     * Starts a random KOTH event
     */
    private void startRandomKOTH() {
        // Get all available KOTHs
        java.util.Map<String, KOTH> koths = plugin.getKothManager().getKOTHs();
        if (koths.isEmpty()) {
            return;
        }

        // Filter out active KOTHs
        List<String> availableKOTHs = koths.values().stream()
                .filter(koth -> !koth.isActive())
                .map(KOTH::getName)
                .collect(java.util.stream.Collectors.toList());

        if (!availableKOTHs.isEmpty()) {
            // Select random KOTH
            String randomKOTH = availableKOTHs.get(new java.util.Random().nextInt(availableKOTHs.size()));
            plugin.getKothManager().startKOTH(randomKOTH);
        }
    }

    /**
     * Adds a new scheduled time
     * @param time Time in HH:mm format
     * @return true if time was added successfully
     */
    public boolean addScheduledTime(String time) {
        try {
            // Validate time format
            LocalTime.parse(time, timeFormatter);

            List<String> times = plugin.getConfigManager().getScheduledTimes();
            if (!times.contains(time)) {
                times.add(time);
                plugin.getConfig().set("schedules.times", times);
                plugin.saveConfig();
                return true;
            }
        } catch (DateTimeParseException e) {
            return false;
        }
        return false;
    }

    /**
     * Removes a scheduled time
     * @param time Time to remove
     * @return true if time was removed successfully
     */
    public boolean removeScheduledTime(String time) {
        List<String> times = plugin.getConfigManager().getScheduledTimes();
        if (times.remove(time)) {
            plugin.getConfig().set("schedules.times", times);
            plugin.saveConfig();
            return true;
        }
        return false;
    }

    /**
     * Lists all scheduled times
     * @return List of scheduled times
     */
    public List<String> listScheduledTimes() {
        return plugin.getConfigManager().getScheduledTimes();
    }

    /**
     * Reloads the scheduler
     */
    public void reload() {
        stopScheduler();
        if (plugin.getConfigManager().areSchedulesEnabled()) {
            startScheduler();
        }
    }
}