package com.leclowndu93150.better_anvil_enchanting.mixin;

import com.leclowndu93150.better_anvil_enchanting.config.AnvilConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.BiConsumer;

@Mixin(AnvilMenu.class)
public class AnvilMenuMixin {

    @Final
    @Shadow
    private DataSlot cost;

    @Shadow
    private int repairItemCountCost;

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
//    @Redirect(method = "createResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/Enchantment;canEnchant(Lnet/minecraft/world/item/ItemStack;)Z"))
//    private boolean allowCreativeEnchantments(Enchantment enchantment, ItemStack stack) {
//        AnvilConfig config = AnvilConfig.getInstance();
//        if (config.allowCreativeOnlyEnchantments) {
//            return true;
//        }
//        return enchantment.canEnchant(stack);
//    }

    // Modify repair material cost increment (++i)
    @ModifyConstant(method = "createResult", constant = @Constant(intValue = 1, ordinal = 0))
    private int modifyRepairMaterialCost(int value) {
        AnvilConfig config = AnvilConfig.getInstance();
        if (config.customRepairCosts) {
            return config.repairMaterialCost;
        }
        return value;
    }

    // Modify repair sacrifice cost (i += 2)
    @ModifyConstant(method = "createResult", constant = @Constant(intValue = 2, ordinal = 0))
    private int modifyRepairSacrificeCost(int value) {
        AnvilConfig config = AnvilConfig.getInstance();
        if (config.customRepairCosts) {
            return config.repairSacrificeCost;
        }
        return value;
    }

    // Modify incompatible enchantment penalty (++i)
    @ModifyConstant(method = "createResult", constant = @Constant(intValue = 1, ordinal = 4))
    private int modifyIncompatiblePenalty(int value) {
        AnvilConfig config = AnvilConfig.getInstance();
        if (config.customRepairCosts) {
            return config.incompatiblePenalty;
        }
        return value;
    }

    // Modify rename cost (k = 1)
    @ModifyConstant(method = "createResult", constant = @Constant(intValue = 1, ordinal = 5))
    private int modifyRenameCost(int value) {
        AnvilConfig config = AnvilConfig.getInstance();
        if (config.customRepairCosts) {
            return config.renameCost;
        }
        return value;
    }

    // Apply ALL cost modifiers BEFORE setting to DataSlot
    @ModifyVariable(method = "createResult", at = @At(value = "STORE", ordinal = 0), index = 9)
    private int applyAllCostModifiers(int k2) {
        AnvilConfig config = AnvilConfig.getInstance();

        // Apply repair cost multiplier
        if (config.customRepairCosts) {
            k2 = (int)(k2 * config.repairCostMultiplier);
        }

        // Apply XP cost multiplier
        if (config.customXpCosts) {
            k2 = (int)(k2 * config.xpCostMultiplier);
            k2 = Math.max(k2, config.minimumXpCost);
        }

        return k2;
    }

    // Modify repair efficiency bonus (12%)
    @ModifyConstant(method = "createResult", constant = @Constant(intValue = 12))
    private int modifyRepairEfficiencyBonus(int value) {
        AnvilConfig config = AnvilConfig.getInstance();
        if (config.improvedRepairLogic) {
            return (int)(100 * config.repairEfficiencyBonus);
        }
        return value;
    }

    // Remove the 40 level cap check (first occurrence)
    @ModifyConstant(method = "createResult", constant = @Constant(intValue = 40, ordinal = 0))
    private int removeFirstLevelCap(int value) {
        AnvilConfig config = AnvilConfig.getInstance();
        if (config.removeAnvilLevelCap) {
            return Integer.MAX_VALUE;
        }
        return config.maxAnvilCost;
    }

    // Remove the 40 level cap check (second occurrence)
    @ModifyConstant(method = "createResult", constant = @Constant(intValue = 40, ordinal = 1))
    private int removeSecondLevelCap(int value) {
        AnvilConfig config = AnvilConfig.getInstance();
        if (config.removeAnvilLevelCap) {
            return Integer.MAX_VALUE;
        }
        return config.maxAnvilCost;
    }

    // Modify the 39 level adjustment
    @ModifyConstant(method = "createResult", constant = @Constant(intValue = 39))
    private int modifyMaxAnvilCostMinusOne(int value) {
        AnvilConfig config = AnvilConfig.getInstance();
        if (config.removeAnvilLevelCap) {
            return Integer.MAX_VALUE - 1;
        }
        return config.maxAnvilCost - 1;
    }

    // Allow over-repair by modifying the damage value calculation (variable l1)
    @ModifyVariable(method = "createResult", at = @At(value = "STORE", ordinal = 0), index = 14)
    private int allowOverRepair(int l1) {
        AnvilConfig config = AnvilConfig.getInstance();
        if (!config.allowOverRepair && l1 < 0) {
            return 0;
        }
        return l1; // Allow negative values if over-repair is enabled
    }

    // Redirect all setDamageValue calls to allow negative values
    @Redirect(method = "createResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;setDamageValue(I)V"))
    private void setDamageValueWithOverRepair(ItemStack stack, int damage) {
        AnvilConfig config = AnvilConfig.getInstance();
        if (config.allowOverRepair && damage < 0) {
            // Bypass the normal clamping in setDamageValue
            stack.set(DataComponents.DAMAGE, damage);
        } else {
            stack.setDamageValue(damage);
        }
    }

    // Handle free repairs and custom level cap for pickup
    @Inject(method = "mayPickup", at = @At("HEAD"), cancellable = true)
    private void customAnvilCostLogic(Player player, boolean hasStack, CallbackInfoReturnable<Boolean> cir) {
        AnvilConfig config = AnvilConfig.getInstance();

        if (config.freeRepairs) {
            cir.setReturnValue(hasStack && this.cost.get() > 0);
            return;
        }

        int currentCost = this.cost.get();
        boolean canAfford = player.hasInfiniteMaterials() || player.experienceLevel >= currentCost;

        if (config.removeAnvilLevelCap || currentCost <= config.maxAnvilCost) {
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

        // The XP cost should already be modified in the DataSlot, so just use it
        player.giveExperienceLevels(originalCost);
    }

    // Modify anvil damage behavior
    @Redirect(method = "onTake", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/ContainerLevelAccess;execute(Ljava/util/function/BiConsumer;)V"))
    private void modifyAnvilDamage(ContainerLevelAccess access, BiConsumer<Level, BlockPos> consumer, Player player, ItemStack stack) {
        AnvilConfig config = AnvilConfig.getInstance();

        access.execute((level, pos) -> {
            BlockState blockstate = level.getBlockState(pos);

            if (player.getAbilities().instabuild || config.anvilNeverBreaks) {
                if (config.playAnvilSounds) {
                    level.levelEvent(1030, pos, 0);
                }
                return;
            }

            if (blockstate.is(BlockTags.ANVIL)) {
                float damageChance = player.getRandom().nextFloat();
                if (damageChance < config.anvilDamageChance) {
                    BlockState blockstate1 = AnvilBlock.damage(blockstate);
                    if (blockstate1 == null) {
                        level.removeBlock(pos, false);
                        if (config.playAnvilSounds) {
                            level.levelEvent(1029, pos, 0);
                        }
                    } else {
                        level.setBlock(pos, blockstate1, 2);
                        if (config.playAnvilSounds) {
                            level.levelEvent(1030, pos, 0);
                        }
                    }
                } else {
                    if (config.playAnvilSounds) {
                        level.levelEvent(1030, pos, 0);
                    }
                }
            }
        });
    }

    // Custom prior work penalty multiplier
    @Redirect(method = "createResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/AnvilMenu;calculateIncreasedRepairCost(I)I"))
    private int modifyPriorWorkPenalty(int oldRepairCost) {
        AnvilConfig config = AnvilConfig.getInstance();
        return (int)Math.min((long)oldRepairCost * config.priorWorkPenaltyMultiplier + 1L, Integer.MAX_VALUE);
    }

    // Modify the anvil damage chance
//    @ModifyConstant(method = "onTake", constant = @Constant(floatValue = 0.12F))
//    private float modifyAnvilDamageChance(float value) {
//        AnvilConfig config = AnvilConfig.getInstance();
//        return (float)config.anvilDamageChance;
//    }

    // Debug logging
    @Inject(method = "createResult", at = @At("HEAD"))
    private void debugLogStart(CallbackInfo ci) {
        AnvilConfig config = AnvilConfig.getInstance();
        if (config.enableDebugLogging) {
            AnvilMenu menu = (AnvilMenu)(Object)this;
            ItemStack input = menu.getSlot(0).getItem();
            ItemStack addition = menu.getSlot(1).getItem();
            System.out.println("[Better Anvil] Starting anvil operation:");
            System.out.println("  Input: " + (input.isEmpty() ? "Empty" : input.getDescriptionId()));
            System.out.println("  Addition: " + (addition.isEmpty() ? "Empty" : addition.getDescriptionId()));
        }
    }

    @Inject(method = "createResult", at = @At("TAIL"))
    private void debugLogEnd(CallbackInfo ci) {
        AnvilConfig config = AnvilConfig.getInstance();
        if (config.enableDebugLogging) {
            AnvilMenu menu = (AnvilMenu)(Object)this;
            ItemStack result = menu.getSlot(2).getItem();
            System.out.println("  Result: " + (result.isEmpty() ? "Empty" : result.getDescriptionId()));
            System.out.println("  Final Cost: " + this.cost.get());
            System.out.println("  Repair item count: " + this.repairItemCountCost);
        }
    }
}