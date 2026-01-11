package fr.siroz.cariboustonks.feature.fishing.hotspot;

import fr.siroz.cariboustonks.rendering.world.WorldRenderer;
import fr.siroz.cariboustonks.util.colors.Color;
import fr.siroz.cariboustonks.util.colors.Colors;

record HotspotRenderer(HotspotFeature hotspotFeature) {

	private static final Color BOBBER_IN = Colors.GREEN.withAlpha(0.5F);
	private static final Color BOBBER_OUT = Colors.RED.withAlpha(0.5F);

	public void render(WorldRenderer renderer) {
		if (!hotspotFeature.isEnabled() || hotspotFeature.getCurrentHotspot() == null) {
			return;
		}

		Double hotspotRadius = hotspotFeature.getHotspotRadius();
		if (hotspotRadius == null || hotspotRadius <= 0D || hotspotRadius > 16D) {
			return;
		}

		renderer.submitThickCircle(
				hotspotFeature.getCurrentHotspot().centerPos().subtract(0D, 2.5D, 0D), // 2
				hotspotRadius,
				1,
				32,
				hotspotFeature.isBobberInHotspot() ? BOBBER_IN : BOBBER_OUT,
				true);
	}
}
