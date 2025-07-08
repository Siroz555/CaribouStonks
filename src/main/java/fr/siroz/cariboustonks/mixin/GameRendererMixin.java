package fr.siroz.cariboustonks.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.feature.ui.ZoomFeature;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

	@Shadow
	@Final
	private MinecraftClient client;

	@Unique
	private boolean smoothCamera = false;

	@Unique
	private final ZoomFeature zoomFeature = CaribouStonks.features().getFeature(ZoomFeature.class);

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
}
