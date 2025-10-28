package fr.siroz.cariboustonks.feature.garden.pest;

import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.manager.waypoint.Waypoint;
import fr.siroz.cariboustonks.manager.waypoint.options.TextOption;
import fr.siroz.cariboustonks.rendering.world.WorldRenderer;
import fr.siroz.cariboustonks.util.colors.Colors;
import fr.siroz.cariboustonks.util.position.Position;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

final class PestFinderRenderer {

    private static final MinecraftClient CLIENT = MinecraftClient.getInstance();

	private final PestFinderFeature pestFinder;
	private final Waypoint waypoint;

    PestFinderRenderer(PestFinderFeature pestFinder) {
        this.pestFinder = pestFinder;
		this.waypoint = Waypoint.builder(builder -> {
			builder.enabled(false);
			builder.color(Colors.GREEN);
			builder.textOption(TextOption.builder()
					.withText(Text.literal("Guess").formatted(Formatting.GREEN))
					.withDistance(false)
					.build());
		});
    }

	@EventHandler(event = "RenderEvents.WORLD_RENDER")
    public void render(WorldRenderer renderer) {
        if (!pestFinder.isEnabled()) return;
        if (CLIENT.player == null || CLIENT.world == null) return;

		if (pestFinder.getGuessPosition() != null) {
			waypoint.setEnabled(true);
			waypoint.updatePosition(Position.of(pestFinder.getGuessPosition()));
			waypoint.getRenderer().render(renderer);
		}

        for (Entity entity : CLIENT.world.getEntities()) {
            if (!(entity instanceof ArmorStandEntity armorStand)) {
				continue;
			}

            if (!armorStand.hasCustomName() || !armorStand.getName().getString().contains("ൠ")) {
				continue;
			}

            renderer.submitLineFromCursor(armorStand.getEyePos(), Colors.GREEN, 1f);
        }
    }
}
