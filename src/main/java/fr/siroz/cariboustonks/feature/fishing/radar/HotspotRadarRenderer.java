package fr.siroz.cariboustonks.feature.fishing.radar;

import fr.siroz.cariboustonks.manager.waypoint.Waypoint;
import fr.siroz.cariboustonks.manager.waypoint.options.TextOption;
import fr.siroz.cariboustonks.util.colors.Color;
import fr.siroz.cariboustonks.util.position.Position;
import fr.siroz.cariboustonks.util.render.WorldRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

final class HotspotRadarRenderer implements WorldRenderer {

	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();

	private final HotspotRadarFeature hotspotRadar;
	private final Waypoint waypoint;

	HotspotRadarRenderer(HotspotRadarFeature hotspotRadar) {
		this.hotspotRadar = hotspotRadar;
		this.waypoint = Waypoint.builder(builder -> {
			builder.enabled(false);
			builder.color(Color.fromFormatting(Formatting.LIGHT_PURPLE));
			builder.textOption(TextOption.builder()
					.withText(Text.literal("Guess").formatted(Formatting.LIGHT_PURPLE))
					.withDistance(false)
					.build());
		});
	}

	@Override
	public void render(WorldRenderContext context) {
		if (!hotspotRadar.isEnabled()) return;
		if (CLIENT.player == null || CLIENT.world == null) return;

		if (hotspotRadar.getGuessPosition() != null) {
			waypoint.setEnabled(true);
			waypoint.updatePosition(Position.of(hotspotRadar.getGuessPosition()));
			waypoint.getRenderer().render(context);
		}
	}
}
