package com.wish.managers;

import com.wish.MiniKOTH;
import com.wish.listeners.ChestListener;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 * RewardManager
 * Handles all reward-related operations for the MiniKOTH plugin
 * including executing reward commands and managing reward chests.
 *
 * @author wwishh
 * @version 0.0.1
 */
public class RewardManager {
    private final MiniKOTH plugin;
    private final ChestListener chestListener;

    /**
     * Constructor for RewardManager
     * @param plugin Instance of the main plugin class
     */
    public RewardManager(MiniKOTH plugin) {
        this.plugin = plugin;
        this.chestListener = new ChestListener(plugin);
        plugin.getServer().getPluginManager().registerEvents(chestListener, plugin);
    }

    /**
     * Gives rewards to a player
     * @param playerName Name of the player to receive rewards
     */
    public void giveRewards(String playerName) {
        Player player = Bukkit.getPlayer(playerName);
        if (player == null || !player.isOnline()) {
            return;
        }

        // Execute reward commands
        for (String command : plugin.getConfigManager().getRewardCommands()) {
            String processedCommand = command.replace("{player}", playerName);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), processedCommand);
        }

        // Spawn reward chest if enabled (to be implemented in ChestManager)
        spawnRewardChest(player);
    }

    /**
     * Spawns a reward chest for the winner
     * @param player Player who won the KOTH
     */
    private void spawnRewardChest(Player player) {
        KOTH koth = null;

        // Buscar el KOTH que el jugador capturó
        for (KOTH k : plugin.getKothManager().getKOTHs().values()) {
            if (player.getName().equals(k.getCurrentCapturer())) {
                koth = k;
                break;
            }
        }

        if (koth == null) return;

        // Obtener la ubicación del cofre
        Location chestLoc = koth.getChestSpawnLocation();
        if (chestLoc == null) return;

        // Crear el cofre
        Block block = chestLoc.getBlock();
        chestListener.createKOTHChest(block, player.getName());

        // Notificar al jugador
        player.sendMessage(plugin.getConfigManager().getMessage("chest-spawned"));
    }

    /**
     * Adds a new reward command
     * @param command Command to add
     * @return true if command was added successfully
     */
    public boolean addRewardCommand(String command) {
        if (!command.contains("{player}")) {
            command = command.trim();
            if (!command.startsWith("/")) {
                command = "/" + command;
            }
        }

        java.util.List<String> commands = plugin.getConfigManager().getRewardCommands();
        if (!commands.contains(command)) {
            commands.add(command);
            plugin.getConfig().set("rewards.commands", commands);
            plugin.saveConfig();
            return true;
        }
        return false;
    }

    /**
     * Removes a reward command
     * @param command Command to remove
     * @return true if command was removed successfully
     */
    public boolean removeRewardCommand(String command) {
        java.util.List<String> commands = plugin.getConfigManager().getRewardCommands();
        if (commands.remove(command)) {
            plugin.getConfig().set("rewards.commands", commands);
            plugin.saveConfig();
            return true;
        }
        return false;
    }

    /**
     * Lists all reward commands
     * @return List of reward commands
     */
    public java.util.List<String> listRewardCommands() {
        return plugin.getConfigManager().getRewardCommands();
    }
}