package com.leclowndu93150.better_anvil_enchanting.config;

import com.leclowndu93150.better_anvil_enchanting.platform.Services;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class AnvilConfig {
    private static final String CONFIG_FILE_NAME = "better_anvil_enchanting.toml";
    private static AnvilConfig INSTANCE;

    // Enchantment Settings
    public boolean removeEnchantmentLevelCap = true;
    public int maxEnchantmentLevel = Integer.MAX_VALUE;
    public boolean allowIncompatibleEnchantments = false;
    public boolean removeAnvilLevelCap = true;
    public int maxAnvilCost = 40;

    // Cost Settings
    public boolean customRepairCosts = false;
    public int repairMaterialCost = 1;
    public int repairSacrificeCost = 2;
    public int incompatiblePenalty = 1;
    public int renameCost = 1;
    public double repairCostMultiplier = 1.0;

    // Durability Settings
    public boolean allowOverRepair = false;
    public boolean improvedRepairLogic = true;
    public double repairEfficiencyBonus = 0.12;

    // XP Settings
    public boolean customXpCosts = false;
    public boolean freeRepairs = false;
    public double xpCostMultiplier = 1.0;
    public int minimumXpCost = 1;

    // Anvil Durability
    public boolean anvilNeverBreaks = false;
    public double anvilDamageChance = 0.12;
    public boolean playAnvilSounds = true;

    // Advanced Features
    public boolean allowCreativeOnlyEnchantments = false;
    public boolean showDetailedCosts = true;
    public boolean enableDebugLogging = false;
    public int priorWorkPenaltyMultiplier = 2;

    private AnvilConfig() {}

    public static AnvilConfig getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AnvilConfig();
            INSTANCE.load();
        }
        return INSTANCE;
    }

    public void load() {
        Path configPath = Services.PLATFORM.getConfigDirectory().resolve(CONFIG_FILE_NAME);

        if (!Files.exists(configPath)) {
            save();
            return;
        }

        try (BufferedReader reader = Files.newBufferedReader(configPath)) {
            Properties props = new Properties();
            props.load(reader);

            // Enchantment Settings
            removeEnchantmentLevelCap = Boolean.parseBoolean(props.getProperty("enchantment.removeEnchantmentLevelCap", "true"));
            maxEnchantmentLevel = Integer.parseInt(props.getProperty("enchantment.maxEnchantmentLevel", String.valueOf(Integer.MAX_VALUE)));
            allowIncompatibleEnchantments = Boolean.parseBoolean(props.getProperty("enchantment.allowIncompatibleEnchantments", "false"));
            removeAnvilLevelCap = Boolean.parseBoolean(props.getProperty("enchantment.removeAnvilLevelCap", "true"));
            maxAnvilCost = Integer.parseInt(props.getProperty("enchantment.maxAnvilCost", "40"));

            // Cost Settings
            customRepairCosts = Boolean.parseBoolean(props.getProperty("costs.customRepairCosts", "false"));
            repairMaterialCost = Integer.parseInt(props.getProperty("costs.repairMaterialCost", "1"));
            repairSacrificeCost = Integer.parseInt(props.getProperty("costs.repairSacrificeCost", "2"));
            incompatiblePenalty = Integer.parseInt(props.getProperty("costs.incompatiblePenalty", "1"));
            renameCost = Integer.parseInt(props.getProperty("costs.renameCost", "1"));
            repairCostMultiplier = Double.parseDouble(props.getProperty("costs.repairCostMultiplier", "1.0"));

            // Durability Settings
            allowOverRepair = Boolean.parseBoolean(props.getProperty("durability.allowOverRepair", "false"));
            improvedRepairLogic = Boolean.parseBoolean(props.getProperty("durability.improvedRepairLogic", "true"));
            repairEfficiencyBonus = Double.parseDouble(props.getProperty("durability.repairEfficiencyBonus", "0.12"));

            // XP Settings
            customXpCosts = Boolean.parseBoolean(props.getProperty("xp.customXpCosts", "false"));
            freeRepairs = Boolean.parseBoolean(props.getProperty("xp.freeRepairs", "false"));
            xpCostMultiplier = Double.parseDouble(props.getProperty("xp.xpCostMultiplier", "1.0"));
            minimumXpCost = Integer.parseInt(props.getProperty("xp.minimumXpCost", "1"));

            // Anvil Durability
            anvilNeverBreaks = Boolean.parseBoolean(props.getProperty("anvil.anvilNeverBreaks", "false"));
            anvilDamageChance = Double.parseDouble(props.getProperty("anvil.anvilDamageChance", "0.12"));
            playAnvilSounds = Boolean.parseBoolean(props.getProperty("anvil.playAnvilSounds", "true"));

            // Advanced Features
            allowCreativeOnlyEnchantments = Boolean.parseBoolean(props.getProperty("advanced.allowCreativeOnlyEnchantments", "false"));
            showDetailedCosts = Boolean.parseBoolean(props.getProperty("advanced.showDetailedCosts", "true"));
            enableDebugLogging = Boolean.parseBoolean(props.getProperty("advanced.enableDebugLogging", "false"));
            priorWorkPenaltyMultiplier = Integer.parseInt(props.getProperty("advanced.priorWorkPenaltyMultiplier", "2"));

        } catch (Exception e) {
            System.err.println("Failed to load Better Anvil Enchanting config, using defaults: " + e.getMessage());
        }
    }

    public void save() {
        Path configPath = Services.PLATFORM.getConfigDirectory().resolve(CONFIG_FILE_NAME);

        try {
            Files.createDirectories(configPath.getParent());

            try (BufferedWriter writer = Files.newBufferedWriter(configPath)) {
                writer.write("# Better Anvil Enchanting Configuration\n");
                writer.write("# This file controls all aspects of anvil behavior\n\n");

                writer.write("# ========== Enchantment Settings ==========\n");
                writer.write("# Remove the vanilla enchantment level cap (allows levels above normal max)\n");
                writer.write("enchantment.removeEnchantmentLevelCap=" + removeEnchantmentLevelCap + "\n");
                writer.write("# Maximum enchantment level allowed (only used if removeEnchantmentLevelCap is false)\n");
                writer.write("enchantment.maxEnchantmentLevel=" + maxEnchantmentLevel + "\n");
                writer.write("# Allow combining incompatible enchantments (e.g., Sharpness + Smite)\n");
                writer.write("enchantment.allowIncompatibleEnchantments=" + allowIncompatibleEnchantments + "\n");
                writer.write("# Remove the 40 level anvil cost cap\n");
                writer.write("enchantment.removeAnvilLevelCap=" + removeAnvilLevelCap + "\n");
                writer.write("# Maximum anvil cost (only used if removeAnvilLevelCap is false)\n");
                writer.write("enchantment.maxAnvilCost=" + maxAnvilCost + "\n\n");

                writer.write("# ========== Cost Settings ==========\n");
                writer.write("# Enable custom repair costs\n");
                writer.write("costs.customRepairCosts=" + customRepairCosts + "\n");
                writer.write("# Cost per repair material used\n");
                writer.write("costs.repairMaterialCost=" + repairMaterialCost + "\n");
                writer.write("# Cost when sacrificing an item for repair\n");
                writer.write("costs.repairSacrificeCost=" + repairSacrificeCost + "\n");
                writer.write("# Penalty for incompatible enchantments\n");
                writer.write("costs.incompatiblePenalty=" + incompatiblePenalty + "\n");
                writer.write("# Cost for renaming items\n");
                writer.write("costs.renameCost=" + renameCost + "\n");
                writer.write("# Multiplier for all repair costs\n");
                writer.write("costs.repairCostMultiplier=" + repairCostMultiplier + "\n\n");

                writer.write("# ========== Durability Settings ==========\n");
                writer.write("# Allow repairing items beyond their max durability\n");
                writer.write("durability.allowOverRepair=" + allowOverRepair + "\n");
                writer.write("# Use improved repair logic for better efficiency\n");
                writer.write("durability.improvedRepairLogic=" + improvedRepairLogic + "\n");
                writer.write("# Bonus durability percentage when combining items\n");
                writer.write("durability.repairEfficiencyBonus=" + repairEfficiencyBonus + "\n\n");

                writer.write("# ========== XP Settings ==========\n");
                writer.write("# Enable custom XP costs\n");
                writer.write("xp.customXpCosts=" + customXpCosts + "\n");
                writer.write("# Make all repairs free (no XP cost)\n");
                writer.write("xp.freeRepairs=" + freeRepairs + "\n");
                writer.write("# Multiplier for XP costs\n");
                writer.write("xp.xpCostMultiplier=" + xpCostMultiplier + "\n");
                writer.write("# Minimum XP cost for any operation\n");
                writer.write("xp.minimumXpCost=" + minimumXpCost + "\n\n");

                writer.write("# ========== Anvil Durability ==========\n");
                writer.write("# Prevent anvils from breaking\n");
                writer.write("anvil.anvilNeverBreaks=" + anvilNeverBreaks + "\n");
                writer.write("# Chance for anvil to take damage (0.0 = never, 1.0 = always)\n");
                writer.write("anvil.anvilDamageChance=" + anvilDamageChance + "\n");
                writer.write("# Play anvil sounds when using\n");
                writer.write("anvil.playAnvilSounds=" + playAnvilSounds + "\n\n");

                writer.write("# ========== Advanced Features ==========\n");
                writer.write("# Allow enchantments normally only available in creative mode\n");
                writer.write("advanced.allowCreativeOnlyEnchantments=" + allowCreativeOnlyEnchantments + "\n");
                writer.write("# Show detailed cost breakdown in tooltips\n");
                writer.write("advanced.showDetailedCosts=" + showDetailedCosts + "\n");
                writer.write("# Enable debug logging for troubleshooting\n");
                writer.write("advanced.enableDebugLogging=" + enableDebugLogging + "\n");
                writer.write("# Multiplier for prior work penalty\n");
                writer.write("advanced.priorWorkPenaltyMultiplier=" + priorWorkPenaltyMultiplier + "\n");
            }
        } catch (Exception e) {
            System.err.println("Failed to save Better Anvil Enchanting config: " + e.getMessage());
        }
    }

    public void reload() {
        load();
    }
}