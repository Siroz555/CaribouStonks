package fr.siroz.cariboustonks.feature.fishing.radar;

import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.manager.waypoint.Waypoint;
import fr.siroz.cariboustonks.manager.waypoint.options.TextOption;
import fr.siroz.cariboustonks.rendering.world.WorldRenderer;
import fr.siroz.cariboustonks.util.colors.Color;
import fr.siroz.cariboustonks.util.position.Position;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

final class HotspotRadarRenderer {

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

	@EventHandler(event = "RenderEvents.WORLD_RENDER")
	public void render(WorldRenderer renderer) {
		if (!hotspotRadar.isEnabled()) return;
		if (CLIENT.player == null || CLIENT.world == null) return;

		if (hotspotRadar.getGuessPosition() != null) {
			waypoint.setEnabled(true);
			waypoint.updatePosition(Position.of(hotspotRadar.getGuessPosition()));
			waypoint.getRenderer().render(renderer);
		}
	}
}
