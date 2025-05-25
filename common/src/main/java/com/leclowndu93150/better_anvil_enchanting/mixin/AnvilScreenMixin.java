package com.leclowndu93150.better_anvil_enchanting.mixin;

import com.leclowndu93150.better_anvil_enchanting.config.AnvilConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import net.minecraft.client.gui.screens.inventory.ItemCombinerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AnvilMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnvilScreen.class)
public abstract class AnvilScreenMixin extends ItemCombinerScreen<AnvilMenu> {

    public AnvilScreenMixin(AnvilMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle, null);
    }

    @ModifyArg(method = "renderLabels", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)I"), index = 4)
    private int modifyCostColor(int color) {
        AnvilConfig config = AnvilConfig.getInstance();
        if (config.removeAnvilLevelCap) {
            int cost = this.menu.getCost();
            if (cost > 0 && this.menu.getSlot(2).hasItem()) {
                return this.minecraft.player.experienceLevel >= cost ? 8453920 : 16736352;
            }
        }
        return color;
    }

    @Inject(method = "renderLabels", at = @At("HEAD"), cancellable = true)
    private void renderCustomLabels(GuiGraphics guiGraphics, int mouseX, int mouseY, CallbackInfo ci) {
        AnvilConfig config = AnvilConfig.getInstance();
        if (!config.removeAnvilLevelCap) {
            return;
        }

        super.renderLabels(guiGraphics, mouseX, mouseY);

        int cost = this.menu.getCost();
        if (cost > 0) {
            int textColor = 8453920;
            Component component = null;

            if (!this.menu.getSlot(2).hasItem()) {
                component = null;
            } else {
                component = Component.translatable("container.repair.cost", cost);

                if (!this.minecraft.player.getAbilities().instabuild &&
                        this.minecraft.player.experienceLevel < cost) {
                    textColor = 16736352; // Red
                }

                if (config.showDetailedCosts && this.menu.getSlot(0).hasItem()) {
                    component = Component.empty()
                            .append(component)
                            .append(getDetailedCostBreakdown());
                }
            }

            if (component != null) {
                int k = this.imageWidth - 8 - this.font.width(component) - 2;
                int l = 69;
                guiGraphics.fill(k - 2, 67, this.imageWidth - 8, 79, 1325400064);
                guiGraphics.drawString(this.font, component, k, 69, textColor);
            }
        }

        ci.cancel();
    }

    private Component getDetailedCostBreakdown() {
        AnvilConfig config = AnvilConfig.getInstance();
        if (!config.showDetailedCosts) {
            return Component.empty();
        }

        return Component.literal(" (Modified)").withStyle(style -> style.withItalic(true));
    }
}