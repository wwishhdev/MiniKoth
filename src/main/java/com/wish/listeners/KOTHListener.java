package com.wish.listeners;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.wish.MiniKOTH;
import com.wish.managers.KOTH;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * KOTHListener
 * Handles all KOTH-related events including player movement and region entry/exit
 *
 * @author wwishh
 * @version 0.0.1
 */
public class KOTHListener implements Listener {
    private final MiniKOTH plugin;

    /**
     * Constructor for KOTHListener
     * @param plugin Instance of the main plugin class
     */
    public KOTHListener(MiniKOTH plugin) {
        this.plugin = plugin;
    }

    /**
     * Handles player movement for KOTH capture
     * @param event PlayerMoveEvent
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerMove(PlayerMoveEvent event) {
        // Only check if there's actual movement (not just head rotation)
        if (event.getTo().getBlockX() == event.getFrom().getBlockX() &&
                event.getTo().getBlockY() == event.getFrom().getBlockY() &&
                event.getTo().getBlockZ() == event.getFrom().getBlockZ()) {
            return;
        }

        Player player = event.getPlayer();

        // Check all active KOTHs
        for (KOTH koth : plugin.getKothManager().getKOTHs().values()) {
            if (!koth.isActive()) continue;

            ProtectedRegion region = koth.getRegion();
            boolean wasInRegion = isInRegion(event.getFrom(), region);
            boolean isInRegion = isInRegion(event.getTo(), region);

            // Player entered region
            if (!wasInRegion && isInRegion) {
                handleRegionEnter(player, koth);
            }
            // Player left region
            else if (wasInRegion && !isInRegion) {
                handleRegionLeave(player, koth);
            }
        }
    }

    /**
     * Handles player quit event
     * @param event PlayerQuitEvent
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        // Check if player was capturing any KOTH
        for (KOTH koth : plugin.getKothManager().getKOTHs().values()) {
            if (koth.isActive() && player.getName().equals(koth.getCurrentCapturer())) {
                koth.setCurrentCapturer(null);
                break;
            }
        }
    }

    /**
     * Handles when a player enters a KOTH region
     * @param player Player who entered
     * @param koth KOTH that was entered
     */
    private void handleRegionEnter(Player player, KOTH koth) {
        // Check if region is already being captured
        if (koth.getCurrentCapturer() == null) {
            koth.setCurrentCapturer(player.getName());
            player.sendMessage(plugin.getConfigManager().getMessage("start-capture"));
        }
    }

    /**
     * Handles when a player leaves a KOTH region
     * @param player Player who left
     * @param koth KOTH that was left
     */
    private void handleRegionLeave(Player player, KOTH koth) {
        // Check if this player was the capturer
        if (player.getName().equals(koth.getCurrentCapturer())) {
            koth.setCurrentCapturer(null);
            player.sendMessage(plugin.getConfigManager().getMessage("leave-capture"));
        }
    }

    /**
     * Checks if a location is within a WorldGuard region
     * @param location Location to check
     * @param region Region to check against
     * @return true if location is in region
     */
    private boolean isInRegion(org.bukkit.Location location, ProtectedRegion region) {
        return region.contains(
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ()
        );
    }
}