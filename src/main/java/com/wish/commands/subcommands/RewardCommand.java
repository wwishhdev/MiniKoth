package com.wish.commands.subcommands;

import com.wish.MiniKOTH;
import com.wish.commands.SubCommand;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * RewardCommand
 * Handles the management of KOTH rewards
 *
 * @author wwishh
 * @version 0.0.1
 */
public class RewardCommand implements SubCommand {
    private final MiniKOTH plugin;
    private final List<String> subCommands = Arrays.asList("add", "remove", "list");

    public RewardCommand(MiniKOTH plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("minikoth.reward")) {
            sender.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(plugin.getConfigManager().getMessage("reward-usage"));
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
            default:
                sender.sendMessage(plugin.getConfigManager().getMessage("reward-usage"));
                return true;
        }
    }

    /**
     * Handles the add subcommand
     */
    private boolean handleAdd(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(plugin.getConfigManager().getMessage("reward-add-usage"));
            return true;
        }

        // Combine all arguments after "add" into the command
        StringBuilder command = new StringBuilder();
        for (int i = 2; i < args.length; i++) {
            command.append(args[i]).append(" ");
        }
        String rewardCommand = command.toString().trim();

        if (plugin.getRewardManager().addRewardCommand(rewardCommand)) {
            sender.sendMessage(plugin.getConfigManager().getMessage("reward-added",
                    "command", rewardCommand));
        } else {
            sender.sendMessage(plugin.getConfigManager().getMessage("reward-exists"));
        }
        return true;
    }

    /**
     * Handles the remove subcommand
     */
    private boolean handleRemove(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(plugin.getConfigManager().getMessage("reward-remove-usage"));
            return true;
        }

        // Combine all arguments after "remove" into the command
        StringBuilder command = new StringBuilder();
        for (int i = 2; i < args.length; i++) {
            command.append(args[i]).append(" ");
        }
        String rewardCommand = command.toString().trim();

        if (plugin.getRewardManager().removeRewardCommand(rewardCommand)) {
            sender.sendMessage(plugin.getConfigManager().getMessage("reward-removed",
                    "command", rewardCommand));
        } else {
            sender.sendMessage(plugin.getConfigManager().getMessage("reward-not-found"));
        }
        return true;
    }

    /**
     * Handles the list subcommand
     */
    private boolean handleList(CommandSender sender) {
        List<String> rewards = plugin.getRewardManager().listRewardCommands();

        if (rewards.isEmpty()) {
            sender.sendMessage(plugin.getConfigManager().getMessage("no-rewards"));
            return true;
        }

        sender.sendMessage(plugin.getConfigManager().getMessage("reward-list-header"));
        for (int i = 0; i < rewards.size(); i++) {
            sender.sendMessage(plugin.getConfigManager().getMessage("reward-list-format",
                    "number", String.valueOf(i + 1),
                    "command", rewards.get(i)));
        }
        sender.sendMessage(plugin.getConfigManager().getMessage("reward-list-footer"));
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
        if (args.length >= 3 && args[1].equalsIgnoreCase("remove")) {
            String partial = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
            return plugin.getRewardManager().listRewardCommands().stream()
                    .filter(cmd -> cmd.startsWith(partial))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    @Override
    public String getDescription() {
        return "Manages KOTH rewards";
    }
}