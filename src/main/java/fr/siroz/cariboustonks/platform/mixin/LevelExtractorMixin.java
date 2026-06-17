package fr.siroz.cariboustonks.platform.mixin;

import fr.siroz.cariboustonks.platform.rendering.world.CaribouWorldRenderer;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.extract.LevelExtractor;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelExtractor.class)
public abstract class LevelExtractorMixin {
	@Shadow
	@Final
	private LevelRenderState levelRenderState;

	@Inject(method = "extract", at = @At("RETURN"))
	private void cariboustonks$afterExtractLevel(DeltaTracker deltaTracker, Camera camera, float deltaPartialTick, CallbackInfo ci) {
		// Fabric >>> LevelExtractorEvents.END_EXTRACTION
		CaribouWorldRenderer.extract(this.levelRenderState, this.levelRenderState.cameraRenderState.cullFrustum);
	}
}
