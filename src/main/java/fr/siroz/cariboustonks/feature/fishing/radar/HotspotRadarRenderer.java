package fr.siroz.cariboustonks.feature.fishing.radar;

import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.system.waypoint.Waypoint;
import fr.siroz.cariboustonks.system.waypoint.options.TextOption;
import fr.siroz.cariboustonks.rendering.world.WorldRenderer;
import fr.siroz.cariboustonks.util.colors.Color;
import fr.siroz.cariboustonks.util.position.Position;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

final class HotspotRadarRenderer {

	private static final Minecraft CLIENT = Minecraft.getInstance();

	private final HotspotRadarFeature hotspotRadar;
	private final Waypoint waypoint;

	HotspotRadarRenderer(HotspotRadarFeature hotspotRadar) {
		this.hotspotRadar = hotspotRadar;
		this.waypoint = Waypoint.builder(builder -> {
			builder.enabled(false);
			builder.color(Color.fromFormatting(ChatFormatting.LIGHT_PURPLE));
			builder.textOption(TextOption.builder()
					.withText(Component.literal("Guess").withStyle(ChatFormatting.LIGHT_PURPLE))
					.withDistance(false)
					.build());
		});
	}

	@EventHandler(event = "RenderEvents.WORLD_RENDER")
	public void render(WorldRenderer renderer) {
		if (!hotspotRadar.isEnabled()) return;
		if (CLIENT.player == null || CLIENT.level == null) return;

		if (hotspotRadar.getGuessPosition() != null) {
			waypoint.setEnabled(true);
			waypoint.updatePosition(Position.of(hotspotRadar.getGuessPosition()));
			waypoint.getRenderer().render(renderer);
		}
	}
}
