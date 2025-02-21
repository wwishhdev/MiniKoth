package com.wish.commands;

import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * SubCommand
 * Base interface for all MiniKOTH subcommands
 *
 * @author wwishh
 * @version 0.0.1
 */
public interface SubCommand {
    /**
     * Executes the subcommand
     * @param sender Command sender
     * @param args Command arguments
     * @return true if command was executed successfully
     */
    boolean execute(CommandSender sender, String[] args);

    /**
     * Provides tab completion for the subcommand
     * @param sender Command sender
     * @param args Command arguments
     * @return List of possible completions
     */
    List<String> tabComplete(CommandSender sender, String[] args);

    /**
     * Gets the description of the subcommand
     * @return Command description
     */
    String getDescription();
}