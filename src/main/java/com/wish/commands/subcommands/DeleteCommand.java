package com.wish.commands.subcommands;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.wish.MiniKOTH;
import com.wish.commands.SubCommand;
import com.wish.managers.KOTH;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DeleteCommand
 * Handles the deletion of existing KOTH regions
 *
 * @author wwishh
 * @version 0.0.1
 */
public class DeleteCommand implements SubCommand {
    private final MiniKOTH plugin;

    public DeleteCommand(MiniKOTH plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getConfigManager().getMessage("player-only"));
            return true;
        }

        if (args.length != 2) {
            sender.sendMessage(plugin.getConfigManager().getMessage("delete-usage"));
            return true;
        }

        String kothName = args[1].toLowerCase();
        KOTH koth = plugin.getKothManager().getKOTH(kothName);

        if (koth == null) {
            sender.sendMessage(plugin.getConfigManager().getMessage("koth-not-found"));
            return true;
        }

        // Check if KOTH is active
        if (koth.isActive()) {
            sender.sendMessage(plugin.getConfigManager().getMessage("cannot-delete-active"));
            return true;
        }

        // Delete WorldGuard region
        Player player = (Player) sender;
        WorldGuardPlugin worldGuard = (WorldGuardPlugin) plugin.getServer().getPluginManager().getPlugin("WorldGuard");
        RegionManager regionManager = worldGuard.getRegionManager(player.getWorld());
        String regionId = "koth_" + kothName;

        if (regionManager.hasRegion(regionId)) {
            regionManager.removeRegion(regionId);
        }

        // Remove KOTH from config and memory
        plugin.getConfig().set("koths." + kothName, null);
        plugin.saveConfig();

        // Remove from KOTHManager
        plugin.getKothManager().removeKOTH(kothName);

        sender.sendMessage(plugin.getConfigManager().getMessage("koth-deleted", "name", kothName));
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 2) {
            String partial = args[1].toLowerCase();
            // Return list of existing KOTHs that match the partial input
            return new ArrayList<>(plugin.getKothManager().getKOTHs().keySet()).stream()
                    .filter(name -> name.toLowerCase().startsWith(partial))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    @Override
    public String getDescription() {
        return "Deletes an existing KOTH";
    }
}