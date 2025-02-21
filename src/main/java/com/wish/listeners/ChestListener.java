package com.wish.listeners;

import com.wish.MiniKOTH;
import com.wish.utils.ChestReward;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * ChestListener
 * Handles all chest-related events for the KOTH reward chests
 * including protection and access control.
 *
 * @author wwishh
 * @version 0.0.1
 */
public class ChestListener implements Listener {
    private final MiniKOTH plugin;
    private static final String KOTH_CHEST_META = "koth_chest";
    private static final String KOTH_WINNER_META = "koth_winner";

    /**
     * Constructor for ChestListener
     * @param plugin Instance of the main plugin class
     */
    public ChestListener(MiniKOTH plugin) {
        this.plugin = plugin;
    }

    /**
     * Handles chest interaction events
     * @param event PlayerInteractEvent
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onChestInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Block block = event.getClickedBlock();
        if (block == null || block.getType() != Material.CHEST) return;

        // Check if it's a KOTH chest
        if (!block.hasMetadata(KOTH_CHEST_META)) return;

        Player player = event.getPlayer();
        String winner = block.getMetadata(KOTH_WINNER_META).get(0).asString();

        // Only allow the winner to open the chest
        if (!player.getName().equals(winner)) {
            event.setCancelled(true);
            player.sendMessage(plugin.getConfigManager().getMessage("cannot-open"));
        }
    }

    /**
     * Prevents breaking of KOTH chests
     * @param event BlockBreakEvent
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onChestBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getType() != Material.CHEST) return;

        // Prevent breaking KOTH chests
        if (block.hasMetadata(KOTH_CHEST_META)) {
            event.setCancelled(true);
        }
    }

    /**
     * Creates a KOTH reward chest
     * @param block Block to make into a KOTH chest
     * @param winner Name of the player who won
     */
    public void createKOTHChest(Block block, String winner) {
        block.setType(Material.CHEST);
        block.setMetadata(KOTH_CHEST_META, new FixedMetadataValue(plugin, true));
        block.setMetadata(KOTH_WINNER_META, new FixedMetadataValue(plugin, winner));

        // Schedule chest removal
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (block.getType() == Material.CHEST && block.hasMetadata(KOTH_CHEST_META)) {
                block.setType(Material.AIR);
                block.removeMetadata(KOTH_CHEST_META, plugin);
                block.removeMetadata(KOTH_WINNER_META, plugin);
            }
        }, plugin.getConfigManager().getChestDespawnTime() * 20L);

        // Fill chest with rewards (if implemented)
        if (block.getState() instanceof Chest) {
            fillChestWithRewards((Chest) block.getState());
        }
    }

    /**
     * Fills a chest with reward items
     * @param chest Chest to fill with rewards
     */
    private void fillChestWithRewards(Chest chest) {
        ConfigurationSection rewardsSection = plugin.getConfig().getConfigurationSection("rewards.chest.items");
        if (rewardsSection == null || !plugin.getConfig().getBoolean("rewards.chest.enabled", true)) {
            return;
        }

        // Cargar todas las recompensas posibles
        List<ChestReward> rewards = new ArrayList<>();
        for (String tier : rewardsSection.getKeys(false)) {
            ConfigurationSection tierSection = rewardsSection.getConfigurationSection(tier);
            if (tierSection != null) {
                rewards.add(ChestReward.fromConfig(tier, tierSection));
            }
        }

        // Generar recompensas aleatorias
        Random random = new Random();
        Inventory inv = chest.getInventory();

        // Llenar el cofre con 5-7 items aleatorios
        int itemCount = random.nextInt(3) + 5;
        for (int i = 0; i < itemCount; i++) {
            int slot = random.nextInt(27); // Cofre simple tiene 27 slots
            if (inv.getItem(slot) == null) { // Solo si el slot está vacío
                ItemStack reward = ChestReward.getRandomReward(rewards);
                inv.setItem(slot, reward);
            }
        }
    }


    /**
     * Checks if a block is a KOTH chest
     * @param block Block to check
     * @return true if the block is a KOTH chest
     */
    public boolean isKOTHChest(Block block) {
        return block.hasMetadata(KOTH_CHEST_META);
    }

    /**
     * Gets the winner of a KOTH chest
     * @param block Chest block to check
     * @return Name of the winner, or null if not a KOTH chest
     */
    public String getChestWinner(Block block) {
        if (!isKOTHChest(block)) return null;
        return block.getMetadata(KOTH_WINNER_META).get(0).asString();
    }
}