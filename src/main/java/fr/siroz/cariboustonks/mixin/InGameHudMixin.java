package fr.siroz.cariboustonks.mixin;

import fr.siroz.cariboustonks.config.ConfigManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {

	@Shadow
	public abstract void render(DrawContext context, RenderTickCounter tickCounter);

	@Inject(method = "renderArmor", at = @At("HEAD"), cancellable = true)
	private static void cariboustonks$hideArmorOverlay(DrawContext context, PlayerEntity player, int i, int j, int k, int x, CallbackInfo ci) {
		if (ConfigManager.getConfig().vanilla.overlay.hideArmorOverlay) {
			ci.cancel();
		}
	}

	@Inject(method = "renderFood", at = @At("HEAD"), cancellable = true)
	private void cariboustonks$hideFoodOverlay(DrawContext context, PlayerEntity player, int top, int right, CallbackInfo ci) {
		if (ConfigManager.getConfig().vanilla.overlay.hideFoodOverlay) {
			ci.cancel();
		}
	}
}
