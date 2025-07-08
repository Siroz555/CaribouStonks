package fr.siroz.cariboustonks.feature.fishing;

import fr.siroz.cariboustonks.util.colors.Color;
import fr.siroz.cariboustonks.util.colors.Colors;
import fr.siroz.cariboustonks.util.render.WorldRenderUtils;
import fr.siroz.cariboustonks.util.render.WorldRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;

class HotspotRenderer implements WorldRenderer {

	private static final Color BOBBER_IN = Colors.GREEN.withAlpha(0.25F);
	private static final Color BOBBER_OUT = Colors.RED.withAlpha(0.25F);

	private final HotspotFeature hotspotFeature;

	HotspotRenderer(HotspotFeature hotspotFeature) {
		this.hotspotFeature = hotspotFeature;
	}

	@Override
	public void render(WorldRenderContext context) {
		if (!hotspotFeature.isEnabled() || hotspotFeature.getCurrentHotspot() == null) {
			return;
		}

		Double hotspotRadius = hotspotFeature.getHotspotRadius();
		if (hotspotRadius == null || hotspotRadius <= 0D || hotspotRadius > 16D) {
			return;
		}

		WorldRenderUtils.renderThickCircle(context,
				hotspotFeature.getCurrentHotspot().centerPos().subtract(0D, 2.5D, 0D), // 2
				hotspotRadius,
				1,
				32,
				hotspotFeature.isBobberInHotspot() ? BOBBER_IN : BOBBER_OUT,
				true);
	}
}
