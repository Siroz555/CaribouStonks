package fr.siroz.cariboustonks.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.features.vanilla.ZoomFeature;
import fr.siroz.cariboustonks.rendering.CaribouRenderer;
import fr.siroz.cariboustonks.rendering.gui.GuiRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

	@Shadow
	@Final
	private Minecraft minecraft;

	@Unique
	private boolean smoothCamera = false;

	@Unique
	private final ZoomFeature zoomFeature = CaribouStonks.features().getFeature(ZoomFeature.class);

	@Inject(method = "close", at = @At("TAIL"))
	private void cariboustonks$onGameRendererClose(CallbackInfo ci) {
		CaribouRenderer.close();
	}

	@ModifyReturnValue(method = "getFov", at = @At("RETURN"))
	private float cariboustonks$changeFov(float fov) {
		if (zoomFeature.isZooming()) {
			if (!smoothCamera) {
				smoothCamera = true;
				minecraft.options.smoothCamera = true;
			}

			return (float) (fov * zoomFeature.getCurrentZoomMultiplier());

		} else if (smoothCamera) {
			smoothCamera = false;
			minecraft.options.smoothCamera = false;
			zoomFeature.resetZoomMultiplier();
		}

		return fov;
	}

	@Inject(method = "processBlurEffect", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/PostChain;process(Lcom/mojang/blaze3d/pipeline/RenderTarget;Lcom/mojang/blaze3d/resource/GraphicsResourceAllocator;)V", shift = At.Shift.AFTER))
	private void cariboustonks$onBlurRendered(CallbackInfo ci) {
		GuiRenderer.disableBlurScissor();
	}
}
