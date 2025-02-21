package com.wish.commands.subcommands;

import com.wish.MiniKOTH;
import com.wish.commands.SubCommand;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ScheduleCommand
 * Handles the management of KOTH schedules
 *
 * @author wwishh
 * @version 0.0.1
 */
public class ScheduleCommand implements SubCommand {
    private final MiniKOTH plugin;
    private final List<String> subCommands = Arrays.asList("add", "remove", "list", "enable", "disable");

    public ScheduleCommand(MiniKOTH plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("minikoth.schedule")) {
            sender.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(plugin.getConfigManager().getMessage("schedule-usage"));
            return true;
        }

        String subCommand = args[1].toLowerCase();

        switch (subCommand) {
            case "add":
                return handleAdd(sender, args);
            case "remove":
                return handleRemove(sender, args);
            case "list":
                return handleList(sender);
            case "enable":
                return handleEnable(sender);
            case "disable":
                return handleDisable(sender);
            default:
                sender.sendMessage(plugin.getConfigManager().getMessage("schedule-usage"));
                return true;
        }
    }

    /**
     * Handles the add subcommand
     */
    private boolean handleAdd(CommandSender sender, String[] args) {
        if (args.length != 3) {
            sender.sendMessage(plugin.getConfigManager().getMessage("schedule-add-usage"));
            return true;
        }

        String time = args[2];
        if (plugin.getScheduleManager().addScheduledTime(time)) {
            sender.sendMessage(plugin.getConfigManager().getMessage("schedule-added",
                    "time", time));
        } else {
            sender.sendMessage(plugin.getConfigManager().getMessage("schedule-invalid-time"));
        }
        return true;
    }

    /**
     * Handles the remove subcommand
     */
    private boolean handleRemove(CommandSender sender, String[] args) {
        if (args.length != 3) {
            sender.sendMessage(plugin.getConfigManager().getMessage("schedule-remove-usage"));
            return true;
        }

        String time = args[2];
        if (plugin.getScheduleManager().removeScheduledTime(time)) {
            sender.sendMessage(plugin.getConfigManager().getMessage("schedule-removed",
                    "time", time));
        } else {
            sender.sendMessage(plugin.getConfigManager().getMessage("schedule-not-found"));
        }
        return true;
    }

    /**
     * Handles the list subcommand
     */
    private boolean handleList(CommandSender sender) {
        List<String> times = plugin.getScheduleManager().listScheduledTimes();

        sender.sendMessage(plugin.getConfigManager().getMessage("schedule-list-header"));
        if (times.isEmpty()) {
            sender.sendMessage(plugin.getConfigManager().getMessage("no-schedules"));
        } else {
            for (int i = 0; i < times.size(); i++) {
                sender.sendMessage(plugin.getConfigManager().getMessage("schedule-list-format",
                        "number", String.valueOf(i + 1),
                        "time", times.get(i)));
            }
        }

        // Show current status
        String status = plugin.getConfigManager().areSchedulesEnabled() ?
                "enabled" : "disabled";
        sender.sendMessage(plugin.getConfigManager().getMessage("schedule-status",
                "status", status));

        sender.sendMessage(plugin.getConfigManager().getMessage("schedule-list-footer"));
        return true;
    }

    /**
     * Handles the enable subcommand
     */
    private boolean handleEnable(CommandSender sender) {
        plugin.getConfig().set("schedules.enabled", true);
        plugin.saveConfig();
        plugin.getScheduleManager().reload();
        sender.sendMessage(plugin.getConfigManager().getMessage("schedule-enabled"));
        return true;
    }

    /**
     * Handles the disable subcommand
     */
    private boolean handleDisable(CommandSender sender) {
        plugin.getConfig().set("schedules.enabled", false);
        plugin.saveConfig();
        plugin.getScheduleManager().reload();
        sender.sendMessage(plugin.getConfigManager().getMessage("schedule-disabled"));
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 2) {
            String partial = args[1].toLowerCase();
            return subCommands.stream()
                    .filter(cmd -> cmd.startsWith(partial))
                    .collect(Collectors.toList());
        }
        if (args.length == 3 && args[1].equalsIgnoreCase("remove")) {
            String partial = args[2].toLowerCase();
            return plugin.getScheduleManager().listScheduledTimes().stream()
                    .filter(time -> time.startsWith(partial))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    @Override
    public String getDescription() {
        return "Manages KOTH schedules";
    }
}