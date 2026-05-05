package fr.siroz.cariboustonks.platform.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.features.vanilla.ZoomFeature;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Camera.class)
public abstract class CameraMixin {
	@Shadow
	@Final
	private Minecraft minecraft;
	@Unique
	private boolean smoothCamera = false;
	@Unique
	private final ZoomFeature zoomFeature = CaribouStonks.features().getFeature(ZoomFeature.class);

	@ModifyReturnValue(method = "calculateFov", at = @At("TAIL"))
	private float cariboustonks$changeFov(float original) {
		if (zoomFeature.isZooming()) {
			if (!smoothCamera) {
				smoothCamera = true;
				minecraft.options.smoothCamera = true;
			}
			return (float) (original * zoomFeature.getCurrentZoomMultiplier());
		} else if (smoothCamera) {
			smoothCamera = false;
			minecraft.options.smoothCamera = false;
			zoomFeature.resetZoomMultiplier();
		}

		return original;
	}
}
