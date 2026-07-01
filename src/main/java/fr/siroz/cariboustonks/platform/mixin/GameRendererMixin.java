package fr.siroz.cariboustonks.platform.mixin;

import fr.siroz.cariboustonks.platform.rendering.world.CaribouWorldRenderer;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

	@Inject(method = "close", at = @At("TAIL"))
	private void cariboustonks$onGameRendererClose(CallbackInfo ci) {
		CaribouWorldRenderer.close();
	}
}
