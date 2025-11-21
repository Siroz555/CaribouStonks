package fr.siroz.cariboustonks.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import fr.siroz.cariboustonks.rendering.CaribouRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.state.LevelRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = LevelRenderer.class, priority = 1001) // YARN-MIXIN: WorldRenderer
public abstract class LevelRendererMixin {

	@Shadow
	@Final
	private LevelRenderState levelRenderState;

	/**
	 * Extract - From Fabric: WorldRenderEvents.END_EXTRACTION
	 * <p>
	 * <a href="https://github.com/FabricMC/fabric/blob/850c318777d99bf5ca96d29f96ba15e58d08060f/fabric-rendering-v1/src/client/java/net/fabricmc/fabric/mixin/client/rendering/WorldRendererMixin.java#L98">GitHub FabricMC Mixin</a>
	 */
	@Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/WorldBorderRenderer;extract(Lnet/minecraft/world/level/border/WorldBorder;FLnet/minecraft/world/phys/Vec3;DLnet/minecraft/client/renderer/state/WorldBorderRenderState;)V", shift = At.Shift.AFTER))
	private void cariboustonks$extractWorldRendering(CallbackInfo ci, @Local Frustum frustum) {
		CaribouRenderer.startExtraction(frustum);
	}

	// TODO(Ravel): @At.args is not supported
	/**
	 * Draw - From Fabric: WorldRenderEvents.BEFORE_TRANSLUCENT
	 * <p>
	 * <a href="https://github.com/FabricMC/fabric/blob/850c318777d99bf5ca96d29f96ba15e58d08060f/fabric-rendering-v1/src/client/java/net/fabricmc/fabric/mixin/client/rendering/WorldRendererMixin.java#L143">GitHub FabricMC Mixin</a>
	 */
	@Inject(method = "method_62214", at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiling/ProfilerFiller;push(Ljava/lang/String;)V", args = "ldc=translucent"))
	private void cariboustonks$drawBeforeTranslucent(CallbackInfo ci) {
		CaribouRenderer.executeDraws(this.levelRenderState);
	}
}
