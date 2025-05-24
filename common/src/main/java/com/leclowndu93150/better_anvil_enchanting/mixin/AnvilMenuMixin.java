package com.leclowndu93150.better_anvil_enchanting.mixin;

import com.leclowndu93150.better_anvil_enchanting.config.AnvilConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AnvilMenu.class)
public class AnvilMenuMixin {

    @Final
    @Shadow
    private DataSlot cost;

    // Debug logging at start of operations
    @Inject(method = "createResult", at = @At("HEAD"))
    private void debugLogStart(CallbackInfo ci) {
        AnvilConfig config = AnvilConfig.getInstance();
        if (config.enableDebugLogging) {
            ItemStack input = ((AnvilMenu)(Object)this).getSlot(0).getItem();
            ItemStack addition = ((AnvilMenu)(Object)this).getSlot(1).getItem();
            System.out.println("[Better Anvil] Starting anvil operation:");
            System.out.println("  Input: " + (input.isEmpty() ? "Empty" : input.getDescriptionId()));
            System.out.println("  Addition: " + (addition.isEmpty() ? "Empty" : addition.getDescriptionId()));
        }
    }

    // Allow enchantments above vanilla max level
    @Redirect(method = "createResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/Enchantment;getMaxLevel()I"))
    private int allowHigherEnchantmentLevels(Enchantment enchantment) {
        AnvilConfig config = AnvilConfig.getInstance();
        if (config.removeEnchantmentLevelCap) {
            return config.maxEnchantmentLevel;
        }
        return enchantment.getMaxLevel();
    }

    // Allow combining incompatible enchantments
    @Redirect(method = "createResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/Enchantment;areCompatible(Lnet/minecraft/core/Holder;Lnet/minecraft/core/Holder;)Z"))
    private boolean allowIncompatibleEnchantments(Holder<Enchantment> first, Holder<Enchantment> second) {
        AnvilConfig config = AnvilConfig.getInstance();
        if (config.allowIncompatibleEnchantments) {
            return true;
        }
        return Enchantment.areCompatible(first, second);
    }

    // Allow creative-only enchantments in survival
    @Redirect(method = "createResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/Enchantment;canEnchant(Lnet/minecraft/world/item/ItemStack;)Z"))
    private boolean allowCreativeEnchantments(Enchantment enchantment, ItemStack stack) {
        AnvilConfig config = AnvilConfig.getInstance();
        if (config.allowCreativeOnlyEnchantments) {
            return true;
        }
        return enchantment.canEnchant(stack);
    }

    // Custom repair material cost
    @ModifyVariable(method = "createResult", at = @At("STORE"), ordinal = 0)
    private int modifyRepairMaterialCost(int value) {
        AnvilConfig config = AnvilConfig.getInstance();
        if (config.customRepairCosts && value == 1) {
            return config.repairMaterialCost;
        }
        return value;
    }

    // Custom sacrifice item cost
    @ModifyVariable(method = "createResult", at = @At("STORE"), ordinal = 1)
    private int modifyRepairSacrificeCost(int value) {
        AnvilConfig config = AnvilConfig.getInstance();
        if (config.customRepairCosts && value == 2) {
            return config.repairSacrificeCost;
        }
        return value;
    }

    // Custom incompatible enchantment penalty
    @ModifyVariable(method = "createResult", at = @At("STORE"), ordinal = 2)
    private int modifyIncompatiblePenalty(int value) {
        AnvilConfig config = AnvilConfig.getInstance();
        if (config.customRepairCosts && value == 1) {
            return config.incompatiblePenalty;
        }
        return value;
    }

    // Custom rename cost
    @ModifyVariable(method = "createResult", at = @At("STORE"), ordinal = 3)
    private int modifyRenameCost(int value) {
        AnvilConfig config = AnvilConfig.getInstance();
        if (config.customRepairCosts && value == 1) {
            return config.renameCost;
        }
        return value;
    }

    // Apply repair cost multiplier to total cost
    @ModifyVariable(method = "createResult", at = @At(value = "STORE", ordinal = 0), ordinal = 0)
    private int applyRepairCostMultiplier(int cost) {
        AnvilConfig config = AnvilConfig.getInstance();
        if (config.customRepairCosts) {
            return (int)(cost * config.repairCostMultiplier);
        }
        return cost;
    }

    // Allow over-repair and improved repair logic
    @Redirect(method = "createResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;setDamageValue(I)V", ordinal = 0))
    private void improvedRepairWithOverRepair(ItemStack stack, int damage) {
        AnvilConfig config = AnvilConfig.getInstance();

        if (config.allowOverRepair) {
            stack.setDamageValue(damage);
        } else {
            stack.setDamageValue(Math.max(0, damage));
        }
    }

    // Apply repair efficiency bonus when combining items
    @ModifyVariable(method = "createResult", at = @At(value = "STORE"), name = "j1")
    private int applyRepairEfficiencyBonus(int repairAmount) {
        AnvilConfig config = AnvilConfig.getInstance();
        if (config.improvedRepairLogic) {
            return repairAmount + (int)(((AnvilMenu)(Object)this).getSlot(0).getItem().getMaxDamage() * config.repairEfficiencyBonus);
        }
        return repairAmount;
    }

    // Apply custom XP costs and level cap
    @Inject(method = "createResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/DataSlot;set(I)V", shift = At.Shift.AFTER))
    private void applyCustomCostModifications(CallbackInfo ci) {
        AnvilConfig config = AnvilConfig.getInstance();
        int currentCost = this.cost.get();

        if (config.customXpCosts) {
            currentCost = (int)(currentCost * config.xpCostMultiplier);
            currentCost = Math.max(currentCost, config.minimumXpCost);
        }

        if (!config.removeAnvilLevelCap && currentCost > config.maxAnvilCost) {
            currentCost = config.maxAnvilCost;
        }

        this.cost.set(currentCost);
    }

    // Handle free repairs and remove level cap for pickup
    @Inject(method = "mayPickup", at = @At("HEAD"), cancellable = true)
    private void customAnvilCostLogic(Player player, boolean hasStack, CallbackInfoReturnable<Boolean> cir) {
        AnvilConfig config = AnvilConfig.getInstance();

        if (config.freeRepairs) {
            cir.setReturnValue(hasStack && this.cost.get() > 0);
            return;
        }

        int currentCost = this.cost.get();
        boolean canAfford = player.hasInfiniteMaterials() || player.experienceLevel >= currentCost;

        if (config.removeAnvilLevelCap) {
            cir.setReturnValue(canAfford && currentCost > 0);
        }
    }

    // Apply custom XP deduction
    @Redirect(method = "onTake", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;giveExperienceLevels(I)V"))
    private void applyCustomXpCost(Player player, int originalCost) {
        AnvilConfig config = AnvilConfig.getInstance();

        if (config.freeRepairs || player.getAbilities().instabuild) {
            return;
        }

        int xpCost = Math.abs(originalCost);
        if (config.customXpCosts) {
            xpCost = (int)(xpCost * config.xpCostMultiplier);
            xpCost = Math.max(xpCost, config.minimumXpCost);
        }

        player.giveExperienceLevels(-xpCost);
    }

    // Control anvil damage chance
    @Redirect(method = "lambda$onTake$2", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/RandomSource;nextFloat()F"))
    private static float modifyAnvilDamageChance(RandomSource random) {
        AnvilConfig config = AnvilConfig.getInstance();
        if (config.anvilNeverBreaks) {
            return 1.0f;
        }
        return random.nextFloat();
    }

    // Set configurable damage chance threshold
    @ModifyConstant(method = "lambda$onTake$2", constant = @Constant(floatValue = 0.12F))
    private static float getConfiguredDamageChance(float original) {
        AnvilConfig config = AnvilConfig.getInstance();
        return (float)config.anvilDamageChance;
    }

    // Control anvil sound effects
    @Redirect(method = "lambda$onTake$2", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;levelEvent(ILnet/minecraft/core/BlockPos;I)V"))
    private static void controlAnvilSounds(Level level, int eventId, BlockPos pos, int data) {
        AnvilConfig config = AnvilConfig.getInstance();
        if (config.playAnvilSounds) {
            level.levelEvent(eventId, pos, data);
        }
    }

    // Prevent anvil damage in lambda (for NeoForge compatibility)
    @Redirect(method = "lambda$onTake$2", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;hasInfiniteMaterials()Z"))
    private static boolean preventAnvilDamage(Player player) {
        AnvilConfig config = AnvilConfig.getInstance();
        if (config.anvilNeverBreaks) {
            return true;
        }
        return player.hasInfiniteMaterials();
    }

    // Custom prior work penalty multiplier
    @Redirect(method = "createResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/AnvilMenu;calculateIncreasedRepairCost(I)I"))
    private int modifyPriorWorkPenalty(int oldRepairCost) {
        AnvilConfig config = AnvilConfig.getInstance();
        return (int)Math.min((long)oldRepairCost * config.priorWorkPenaltyMultiplier + 1L, 2147483647L);
    }
}