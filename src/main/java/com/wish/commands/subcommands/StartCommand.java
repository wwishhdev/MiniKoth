package com.wish.commands.subcommands;

import com.wish.MiniKOTH;
import com.wish.commands.SubCommand;
import com.wish.managers.KOTH;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * StartCommand
 * Handles manually starting KOTH events
 *
 * @author wwishh
 * @version 0.0.1
 */
public class StartCommand implements SubCommand {
    private final MiniKOTH plugin;

    public StartCommand(MiniKOTH plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("minikoth.start")) {
            sender.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
            return true;
        }

        if (args.length != 2) {
            sender.sendMessage(plugin.getConfigManager().getMessage("start-usage"));
            return true;
        }

        String kothName = args[1].toLowerCase();
        KOTH koth = plugin.getKothManager().getKOTH(kothName);

        if (koth == null) {
            sender.sendMessage(plugin.getConfigManager().getMessage("koth-not-found"));
            return true;
        }

        if (koth.isActive()) {
            sender.sendMessage(plugin.getConfigManager().getMessage("koth-already-active"));
            return true;
        }

        if (plugin.getKothManager().startKOTH(kothName)) {
            sender.sendMessage(plugin.getConfigManager().getMessage("koth-started-manual",
                    "name", kothName));
        } else {
            sender.sendMessage(plugin.getConfigManager().getMessage("start-failed"));
        }

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 2) {
            String partial = args[1].toLowerCase();
            // Return list of inactive KOTHs that match the partial input
            return new ArrayList<>(plugin.getKothManager().getKOTHs().values()).stream()
                    .filter(koth -> !koth.isActive())
                    .map(KOTH::getName)
                    .filter(name -> name.toLowerCase().startsWith(partial))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    @Override
    public String getDescription() {
        return "Manually starts a KOTH event";
    }
}