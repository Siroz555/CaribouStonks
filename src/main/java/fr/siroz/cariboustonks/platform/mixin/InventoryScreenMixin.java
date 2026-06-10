package fr.siroz.cariboustonks.platform.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.EffectsInInventory;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin {

	@WrapWithCondition(method = "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/EffectsInInventory;extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;II)V"))
	private boolean cariboustonks$stopRenderingStatusEffects(EffectsInInventory statusEffectsDisplay, GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
		return !(SkyBlockAPI.isOnSkyBlock() && ConfigManager.getConfig().vanilla.overlay.hideStatusEffectsOverlay);
	}

	@ModifyReturnValue(method = "showsActiveEffects", at = @At("RETURN"))
	private boolean cariboustonks$markStatusEffectsHidden(boolean original) {
		return SkyBlockAPI.isOnSkyBlock()
				? original && !ConfigManager.getConfig().vanilla.overlay.hideStatusEffectsOverlay
				: original;
	}
}
