package fr.siroz.cariboustonks.platform.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.features.ui.overlay.EtherWarpOverlayFeature;
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
	private ZoomFeature zoomFeature;

	@Unique
	private EtherWarpOverlayFeature etherWarpOverlayFeature;

	@Unique
	private ZoomFeature zoomFeature() {
		if (zoomFeature == null) zoomFeature = CaribouStonks.features().getFeature(ZoomFeature.class);
		return zoomFeature;
	}

	@Unique
	private EtherWarpOverlayFeature etherWarpOverlayFeature() {
		if (etherWarpOverlayFeature == null) etherWarpOverlayFeature = CaribouStonks.features().getFeature(EtherWarpOverlayFeature.class);
		return etherWarpOverlayFeature;
	}

	@ModifyReturnValue(method = "calculateFov", at = @At("TAIL"))
	private float cariboustonks$changeFov(float original) {
		boolean isEtherwarpZoom = etherWarpOverlayFeature().canZoom();
		if (zoomFeature().isZooming() || isEtherwarpZoom) {
			if (!smoothCamera) {
				smoothCamera = true;
				minecraft.options.smoothCamera = true;
			}
			double multiplier = isEtherwarpZoom
					? etherWarpOverlayFeature().getZoomMultiplier()
					: zoomFeature().getCurrentZoomMultiplier();
			return (float) (original * multiplier);
		} else if (smoothCamera) {
			smoothCamera = false;
			minecraft.options.smoothCamera = false;
			zoomFeature().resetZoomMultiplier();
			etherWarpOverlayFeature().resetTarget();
		}

		return original;
	}
}
