package com.leclowndu93150.better_anvil_enchanting.mixin;

import com.leclowndu93150.better_anvil_enchanting.config.AnvilConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ItemStack.class)
public class ItemStackMixin {

    @Inject(method = "getTooltipLines", at = @At("RETURN"))
    private void addAnvilTooltip(Item.TooltipContext context, Player player, TooltipFlag flag, CallbackInfoReturnable<List<Component>> cir) {
        AnvilConfig config = AnvilConfig.getInstance();

        if (!config.showDetailedCosts || player == null) {
            return;
        }

        if (!(player.containerMenu instanceof AnvilMenu)) {
            return;
        }

        ItemStack stack = (ItemStack)(Object)this;
        List<Component> tooltip = cir.getReturnValue();

        Integer repairCost = stack.get(DataComponents.REPAIR_COST);
        if (repairCost != null && repairCost > 0) {
            tooltip.add(Component.empty());
            tooltip.add(Component.translatable("tooltip.better_anvil.repair_cost", repairCost)
                    .withStyle(ChatFormatting.GRAY));

            int nextCost = calculateNextRepairCost(repairCost, config);
            tooltip.add(Component.translatable("tooltip.better_anvil.next_repair_cost", nextCost)
                    .withStyle(ChatFormatting.DARK_GRAY));
        }

        if (stack.isDamageableItem() && config.allowOverRepair) {
            int damage = stack.getDamageValue();
            int maxDamage = stack.getMaxDamage();

            if (damage < 0) {
                tooltip.add(Component.translatable("tooltip.better_anvil.over_repaired", -damage)
                        .withStyle(ChatFormatting.AQUA));
            }
        }

        if (config.removeEnchantmentLevelCap && stack.isEnchantable()) {
            tooltip.add(Component.empty());
            tooltip.add(Component.translatable("tooltip.better_anvil.unlimited_enchant")
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
        }
    }

    @Inject(method = "getDamageValue", at = @At("HEAD"), cancellable = true)
    private void getAllowNegativeDamage(CallbackInfoReturnable<Integer> cir) {
        AnvilConfig config = AnvilConfig.getInstance();
        if (config.allowOverRepair) {
            ItemStack stack = (ItemStack)(Object)this;
            Integer damage = stack.get(DataComponents.DAMAGE);
            if (damage != null) {
                cir.setReturnValue(Mth.clamp(damage, Integer.MIN_VALUE, stack.getMaxDamage()));
            }
        }
    }

    @Inject(method = "isDamaged", at = @At("HEAD"), cancellable = true)
    private void checkOverRepaired(CallbackInfoReturnable<Boolean> cir) {
        AnvilConfig config = AnvilConfig.getInstance();
        if (config.allowOverRepair) {
            ItemStack stack = (ItemStack)(Object)this;
            if (stack.isDamageableItem()) {
                cir.setReturnValue(stack.getDamageValue() > 0);
            }
        }
    }

    private int calculateNextRepairCost(int currentCost, AnvilConfig config) {
        return (int)Math.min((long)currentCost * config.priorWorkPenaltyMultiplier + 1L, Integer.MAX_VALUE);
    }
}