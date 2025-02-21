package com.wish.commands.subcommands;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.wish.MiniKOTH;
import com.wish.commands.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * CreateCommand
 * Handles the creation of new KOTH regions
 *
 * @author wwishh
 * @version 0.0.1
 */
public class CreateCommand implements SubCommand {
    private final MiniKOTH plugin;

    public CreateCommand(MiniKOTH plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getConfigManager().getMessage("player-only"));
            return true;
        }

        if (args.length != 2) {
            sender.sendMessage(plugin.getConfigManager().getMessage("create-usage"));
            return true;
        }

        Player player = (Player) sender;
        String kothName = args[1].toLowerCase();

        // Check if KOTH already exists
        if (plugin.getKothManager().getKOTH(kothName) != null) {
            sender.sendMessage(plugin.getConfigManager().getMessage("koth-exists"));
            return true;
        }

        // Get WorldEdit selection
        WorldEditPlugin worldEdit = (WorldEditPlugin) plugin.getServer().getPluginManager().getPlugin("WorldEdit");
        Selection selection = worldEdit.getSelection(player);

        if (selection == null) {
            sender.sendMessage(plugin.getConfigManager().getMessage("no-selection"));
            return true;
        }

        // Create WorldGuard region
        WorldGuardPlugin worldGuard = (WorldGuardPlugin) plugin.getServer().getPluginManager().getPlugin("WorldGuard");
        RegionManager regionManager = worldGuard.getRegionManager(player.getWorld());

        String regionId = "koth_" + kothName;
        ProtectedRegion region = new ProtectedCuboidRegion(
                regionId,
                new BlockVector(selection.getNativeMinimumPoint()),
                new BlockVector(selection.getNativeMaximumPoint())
        );

        // Add region to WorldGuard
        regionManager.addRegion(region);

        // Create KOTH with player's location as chest spawn
        boolean created = plugin.getKothManager().createKOTH(
                kothName,
                region,
                player.getLocation()
        );

        if (created) {
            sender.sendMessage(plugin.getConfigManager().getMessage("koth-created",
                    "name", kothName));
            sender.sendMessage(plugin.getConfigManager().getMessage("set-chest-reminder",
                    "name", kothName));
        } else {
            sender.sendMessage(plugin.getConfigManager().getMessage("creation-failed"));
        }

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        // No tab completion for create command as it only takes a new name
        return new ArrayList<>();
    }

    @Override
    public String getDescription() {
        return "Creates a new KOTH area from WorldEdit selection";
    }
}