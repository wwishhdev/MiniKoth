package com.wish.commands;

import com.wish.MiniKOTH;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MiniKOTHCommand
 * Main command handler for the MiniKOTH plugin
 * Manages all subcommands and their execution
 *
 * @author wwishh
 * @version 0.0.1
 */
public class MiniKOTHCommand implements CommandExecutor, TabCompleter {
    private final MiniKOTH plugin;
    private final Map<String, SubCommand> subCommands;

    /**
     * Constructor for MiniKOTHCommand
     * @param plugin Instance of the main plugin class
     */
    public MiniKOTHCommand(MiniKOTH plugin) {
        this.plugin = plugin;
        this.subCommands = new HashMap<>();
        registerSubCommands();
    }

    /**
     * Registers all subcommands
     */
    private void registerSubCommands() {
        // Register all subcommands here
        subCommands.put("create", new CreateCommand(plugin));
        subCommands.put("delete", new DeleteCommand(plugin));
        subCommands.put("setspawn", new SetSpawnCommand(plugin));
        subCommands.put("reward", new RewardCommand(plugin));
        subCommands.put("schedule", new ScheduleCommand(plugin));
        subCommands.put("start", new StartCommand(plugin));
        subCommands.put("stop", new StopCommand(plugin));
        subCommands.put("reload", new ReloadCommand(plugin));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();
        if (!subCommands.containsKey(subCommand)) {
            sendHelp(sender);
            return true;
        }

        // Check permission
        if (!sender.hasPermission("minikoth." + subCommand)) {
            sender.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
            return true;
        }

        // Execute subcommand
        try {
            return subCommands.get(subCommand).execute(sender, args);
        } catch (Exception e) {
            sender.sendMessage(plugin.getConfigManager().getMessage("command-error"));
            e.printStackTrace();
            return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // Show available subcommands based on permissions
            for (Map.Entry<String, SubCommand> entry : subCommands.entrySet()) {
                if (sender.hasPermission("minikoth." + entry.getKey())) {
                    if (entry.getKey().startsWith(args[0].toLowerCase())) {
                        completions.add(entry.getKey());
                    }
                }
            }
        } else if (args.length > 1) {
            // Delegate tab completion to subcommand
            String subCommand = args[0].toLowerCase();
            if (subCommands.containsKey(subCommand)) {
                return subCommands.get(subCommand).tabComplete(sender, args);
            }
        }

        return completions;
    }

    /**
     * Sends help message to sender
     * @param sender Command sender
     */
    private void sendHelp(CommandSender sender) {
        sender.sendMessage(plugin.getConfigManager().getMessage("help-header"));
        for (Map.Entry<String, SubCommand> entry : subCommands.entrySet()) {
            if (sender.hasPermission("minikoth." + entry.getKey())) {
                sender.sendMessage(plugin.getConfigManager().getMessage("help-format",
                        "command", entry.getKey(),
                        "description", entry.getValue().getDescription()));
            }
        }
        sender.sendMessage(plugin.getConfigManager().getMessage("help-footer"));
    }
}