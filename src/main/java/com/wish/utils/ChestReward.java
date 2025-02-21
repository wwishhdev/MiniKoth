package com.wish.utils;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * ChestReward
 * Handles the generation and management of chest reward items
 *
 * @author wwishh
 * @version 0.0.1
 */
public class ChestReward {
    private final String tier;
    private final int chance;
    private final List<RewardItem> items;

    public ChestReward(String tier, int chance) {
        this.tier = tier;
        this.chance = chance;
        this.items = new ArrayList<>();
    }

    /**
     * Adds a reward item to this tier
     */
    public void addItem(Material material, int amount, int chance) {
        items.add(new RewardItem(material, amount, chance));
    }

    /**
     * Gets a random item from this tier
     */
    public ItemStack getRandomItem() {
        int totalChance = items.stream().mapToInt(RewardItem::getChance).sum();
        int random = new Random().nextInt(totalChance);
        int currentSum = 0;

        for (RewardItem item : items) {
            currentSum += item.getChance();
            if (random < currentSum) {
                return item.createItemStack();
            }
        }

        return items.get(0).createItemStack(); // Fallback to first item
    }

    /**
     * Gets a random reward based on tier chances and then item chances within that tier
     */
    public static ItemStack getRandomReward(List<ChestReward> rewards) {
        // First, select a tier based on tier chances
        int totalTierChance = rewards.stream().mapToInt(ChestReward::getChance).sum();
        int random = new Random().nextInt(totalTierChance);
        int currentSum = 0;

        // Find selected tier
        ChestReward selectedTier = null;
        for (ChestReward tier : rewards) {
            currentSum += tier.getChance();
            if (random < currentSum) {
                selectedTier = tier;
                break;
            }
        }

        // If no tier was selected (shouldn't happen), use first tier
        if (selectedTier == null) {
            selectedTier = rewards.get(0);
        }

        // Now get a random item from the selected tier
        return selectedTier.getRandomItem();
    }

    /**
     * Loads a chest reward from configuration
     */
    public static ChestReward fromConfig(String tier, ConfigurationSection section) {
        ChestReward reward = new ChestReward(tier, section.getInt("chance", 100));

        ConfigurationSection itemsSection = section.getConfigurationSection("items");
        if (itemsSection != null) {
            for (String key : itemsSection.getKeys(false)) {
                ConfigurationSection itemSection = itemsSection.getConfigurationSection(key);
                Material material = Material.valueOf(itemSection.getString("material"));
                int amount = itemSection.getInt("amount", 1);
                int chance = itemSection.getInt("chance", 100);
                reward.addItem(material, amount, chance);
            }
        }

        return reward;
    }

    // Getters
    public String getTier() {
        return tier;
    }

    public int getChance() {
        return chance;
    }

    /**
     * Inner class to represent a single reward item
     */
    private static class RewardItem {
        private final Material material;
        private final int amount;
        private final int chance;

        public RewardItem(Material material, int amount, int chance) {
            this.material = material;
            this.amount = amount;
            this.chance = chance;
        }

        public ItemStack createItemStack() {
            return new ItemStack(material, amount);
        }

        public int getChance() {
            return chance;
        }
    }
}