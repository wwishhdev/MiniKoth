package com.wish.commands.subcommands;

import com.wish.MiniKOTH;
import com.wish.commands.SubCommand;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

/**
 * ReloadCommand
 * Handles reloading of plugin configuration
 *
 * @author wwishh
 * @version 0.0.1
 */
public class ReloadCommand implements SubCommand {
    private final MiniKOTH plugin;

    public ReloadCommand(MiniKOTH plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("minikoth.reload")) {
            sender.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
            return true;
        }

        // Stop all active KOTHs
        for (String kothName : new ArrayList<>(plugin.getKothManager().getKOTHs().keySet())) {
            if (plugin.getKothManager().getKOTH(kothName).isActive()) {
                plugin.getKothManager().endKOTH(kothName);
            }
        }

        // Reload configuration
        plugin.getConfigManager().loadConfig();

        // Reload schedule manager
        plugin.getScheduleManager().reload();

        sender.sendMessage(plugin.getConfigManager().getMessage("reload-success"));
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        // No tab completion needed for reload command
        return new ArrayList<>();
    }

    @Override
    public String getDescription() {
        return "Reloads the plugin configuration";
    }
}