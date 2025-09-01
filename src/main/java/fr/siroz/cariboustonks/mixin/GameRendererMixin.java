package fr.siroz.cariboustonks.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.feature.ui.ZoomFeature;
import fr.siroz.cariboustonks.util.render.GuiRenderUtils;
import fr.siroz.cariboustonks.util.render.Renderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
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
	private MinecraftClient client;

	@Unique
	private boolean smoothCamera = false;

	@Unique
	private final ZoomFeature zoomFeature = CaribouStonks.features().getFeature(ZoomFeature.class);

	@Inject(method = "close", at = @At("TAIL"))
	private void cariboustonks$onGameRendererClose(CallbackInfo ci) {
		Renderer.getInstance().close();
	}

	@ModifyReturnValue(method = "getFov", at = @At("RETURN"))
	private float cariboustonks$changeFov(float fov) {
		if (zoomFeature.isZooming()) {
			if (!smoothCamera) {
				smoothCamera = true;
				client.options.smoothCameraEnabled = true;
			}

			return (float) (fov * zoomFeature.getCurrentZoomMultiplier());

		} else if (smoothCamera) {
			smoothCamera = false;
			client.options.smoothCameraEnabled = false;
			zoomFeature.resetZoomMultiplier();
		}

		return fov;
	}

	@Inject(method = "renderBlur", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gl/PostEffectProcessor;render(Lnet/minecraft/client/gl/Framebuffer;Lnet/minecraft/client/util/ObjectAllocator;)V", shift = At.Shift.AFTER))
	private void cariboustonks$onBlurRendered(CallbackInfo ci) {
		GuiRenderUtils.disableBlurScissor();
	}
}
