package com.wish.utils;

import org.bukkit.entity.Player;

/**
 * MessageUtils
 * Utility class for handling message-related operations including
 * action bar messages and progress bar creation.
 *
 * @author wwishh
 * @version 0.0.1
 */
public class MessageUtils {

    /**
     * Sends an action bar message to a player using reflection
     * This method maintains compatibility with version 1.8.8
     * by using reflection to access NMS classes
     *
     * @param player The player to send the action bar to
     * @param message The message to display in the action bar
     */
    public static void sendActionBar(Player player, String message) {
        try {
            // Get the CraftPlayer class
            Class<?> craftPlayerClass = player.getClass();
            Object craftPlayer = craftPlayerClass.cast(player);

            // Get NMS classes through reflection
            Class<?> packetClass = Class.forName("net.minecraft.server.v1_8_R3.PacketPlayOutChat");
            Class<?> chatComponentClass = Class.forName("net.minecraft.server.v1_8_R3.ChatComponentText");
            Class<?> iChatBaseComponentClass = Class.forName("net.minecraft.server.v1_8_R3.IChatBaseComponent");

            // Create the chat component and packet
            Object chatComponent = chatComponentClass.getConstructor(String.class).newInstance(message);
            Object packet = packetClass.getConstructor(iChatBaseComponentClass, byte.class)
                    .newInstance(chatComponent, (byte) 2);

            // Send the packet to the player
            Object handle = craftPlayerClass.getMethod("getHandle").invoke(craftPlayer);
            Object playerConnection = handle.getClass().getField("playerConnection").get(handle);
            playerConnection.getClass().getMethod("sendPacket",
                            Class.forName("net.minecraft.server.v1_8_R3.Packet"))
                    .invoke(playerConnection, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a visual progress bar using unicode blocks
     * The bar consists of filled (green) and empty (gray) blocks
     *
     * @param progress The progress value between 0.0 and 1.0
     * @param length The total length of the progress bar in characters
     * @return A string representing the progress bar with color codes
     */
    public static String createProgressBar(double progress, int length) {
        int filled = (int) (progress * length);
        StringBuilder bar = new StringBuilder("§a");

        for (int i = 0; i < length; i++) {
            if (i < filled) {
                bar.append("■"); // Filled block (green)
            } else {
                bar.append("§7■"); // Empty block (gray)
            }
        }

        return bar.toString();
    }
}