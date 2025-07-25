package fr.siroz.cariboustonks.mixin;

import fr.siroz.cariboustonks.config.ConfigManager;
import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameOverlayRenderer.class)
public abstract class InGameOverlayRendererMixin {

	@Inject(method = "renderFireOverlay", at = @At("HEAD"), cancellable = true)
	private static void cariboustonks$hideFireOverlay(CallbackInfo ci) {
		if (ConfigManager.getConfig().vanilla.overlay.hideFireOverlay) {
			ci.cancel();
		}
	}
}
