package fr.siroz.cariboustonks.platform.mixin;

import fr.siroz.cariboustonks.rendering.CaribouRenderer;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = LevelRenderer.class, priority = 1001)
public abstract class LevelRendererMixin {

	@Shadow
	@Final
	private LevelRenderState levelRenderState;

	@Inject(method = "extractLevel", at = @At("RETURN"))
	private void cariboustonks$afterExtractLevel(DeltaTracker deltaTracker, Camera camera, float deltaPartialTick, CallbackInfo ci) {
		CaribouRenderer.startExtraction(
				this.levelRenderState,
				this.levelRenderState.cameraRenderState.cullFrustum
		);
	}

	@Inject(method = "lambda$addMainPass$0", at = @At("RETURN"))
	private void cariboustonks$drawEndMainRender(CallbackInfo ci) {
		CaribouRenderer.executeDraws(
				this.levelRenderState.cameraRenderState
		);
	}
}
