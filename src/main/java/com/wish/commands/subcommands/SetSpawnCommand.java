package com.wish.commands.subcommands;

import com.wish.MiniKOTH;
import com.wish.commands.SubCommand;
import com.wish.managers.KOTH;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * SetSpawnCommand
 * Handles setting the chest spawn location for KOTHs
 *
 * @author wwishh
 * @version 0.0.1
 */
public class SetSpawnCommand implements SubCommand {
    private final MiniKOTH plugin;

    public SetSpawnCommand(MiniKOTH plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getConfigManager().getMessage("player-only"));
            return true;
        }

        if (args.length != 2) {
            sender.sendMessage(plugin.getConfigManager().getMessage("setspawn-usage"));
            return true;
        }

        Player player = (Player) sender;
        String kothName = args[1].toLowerCase();
        KOTH koth = plugin.getKothManager().getKOTH(kothName);

        if (koth == null) {
            sender.sendMessage(plugin.getConfigManager().getMessage("koth-not-found"));
            return true;
        }

        // Check if KOTH is active
        if (koth.isActive()) {
            sender.sendMessage(plugin.getConfigManager().getMessage("cannot-modify-active"));
            return true;
        }

        // Update chest spawn location
        koth.setChestSpawnLocation(player.getLocation());

        // Save the updated KOTH
        plugin.getKothManager().saveKOTH(koth);

        sender.sendMessage(plugin.getConfigManager().getMessage("chest-spawn-set",
                "name", kothName));
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
        return "Sets the chest spawn location for a KOTH";
    }
}