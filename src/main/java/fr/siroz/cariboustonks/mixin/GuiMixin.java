package fr.siroz.cariboustonks.mixin;

import fr.siroz.cariboustonks.config.ConfigManager;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class) // InGameHud
public abstract class GuiMixin {

	@Inject(method = "extractArmor", at = @At("HEAD"), cancellable = true)
	private static void cariboustonks$hideArmorOverlay(GuiGraphicsExtractor context, Player player, int i, int j, int k, int x, CallbackInfo ci) {
		if (ConfigManager.getConfig().vanilla.overlay.hideArmorOverlay) {
			ci.cancel();
		}
	}

	@Inject(method = "extractFood", at = @At("HEAD"), cancellable = true)
	private void cariboustonks$hideFoodOverlay(GuiGraphicsExtractor context, Player player, int top, int right, CallbackInfo ci) {
		if (ConfigManager.getConfig().vanilla.overlay.hideFoodOverlay) {
			ci.cancel();
		}
	}
}
