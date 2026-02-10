package fr.siroz.cariboustonks.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import fr.siroz.cariboustonks.rendering.CaribouRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.WorldBorderRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.state.LevelRenderState;
import net.minecraft.client.renderer.state.WorldBorderRenderState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = LevelRenderer.class, priority = 1001) // YARN-MIXIN: WorldRenderer
public abstract class LevelRendererMixin {

	@Shadow
	@Final
	private LevelRenderState levelRenderState;

	@Unique
	private Frustum extractedFrustum;

	// 1.21.11 & 26.1
	//
	// Set Frustum - From Fabric: #onSetupFrustum > extractionContext.setFrustum()
	//
	@ModifyExpressionValue(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;prepareCullFrustum(Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/client/renderer/culling/Frustum;"))
	private Frustum cariboustonks$setUpFrustum(Frustum frustum) {
		this.extractedFrustum = frustum;
		return frustum;
	}

	// 1.21.11 & 26.1
	//
	// Extract - From Fabric: WorldRenderEvents.END_EXTRACTION
	//
	@WrapOperation(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/WorldBorderRenderer;extract(Lnet/minecraft/world/level/border/WorldBorder;FLnet/minecraft/world/phys/Vec3;DLnet/minecraft/client/renderer/state/WorldBorderRenderState;)V"))
	private void cariboustonks$extractWorldRendering(WorldBorderRenderer instance, WorldBorder worldBorder, float tickProgress, Vec3 vec3d, double viewDistanceBlocks, WorldBorderRenderState worldBorderRenderState, Operation<Void> original) {
		original.call(instance, worldBorder, tickProgress, vec3d, viewDistanceBlocks, worldBorderRenderState);
		CaribouRenderer.startExtraction(this.extractedFrustum);
	}

	//
	// 1.21.11 & 26.1
	// Draw - From Fabric: LevelRenderEvents.END_MAIN
	//
	// 26.1 > @Inject(method = "lambda$addMainPass$0", at = @At("RETURN"))
	// 26.1 (snapshots) > @Inject(method = "lambda$addMainPass$0", at = @At(value = "INVOKE:LAST", target = "Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;endBatch()V"))
	//
	@Inject(method = "method_62214", at = @At(value = "INVOKE:LAST", target = "Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;endBatch()V"))
	private void cariboustonks$drawEndMainRender(CallbackInfo ci) {
		CaribouRenderer.executeDraws(this.levelRenderState);
	}
}
