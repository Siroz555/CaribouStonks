package fr.siroz.cariboustonks.feature.garden.pest;

import fr.siroz.cariboustonks.core.module.waypoint.Waypoint;
import fr.siroz.cariboustonks.core.module.waypoint.options.TextOption;
import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.rendering.world.WorldRenderer;
import fr.siroz.cariboustonks.util.colors.Colors;
import fr.siroz.cariboustonks.util.position.Position;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;

final class PestFinderRenderer {

    private static final Minecraft CLIENT = Minecraft.getInstance();

	private final PestFinderFeature pestFinder;
	private final Waypoint waypoint;

    PestFinderRenderer(PestFinderFeature pestFinder) {
        this.pestFinder = pestFinder;
		this.waypoint = Waypoint.builder(builder -> {
			builder.enabled(false);
			builder.color(Colors.GREEN);
			builder.textOption(TextOption.builder()
					.withText(Component.literal("Guess").withStyle(ChatFormatting.GREEN))
					.withDistance(false)
					.build());
		});
    }

	@EventHandler(event = "RenderEvents.WORLD_RENDER")
    public void render(WorldRenderer renderer) {
        if (!pestFinder.isEnabled()) return;
        if (CLIENT.player == null || CLIENT.level == null) return;

		if (pestFinder.getGuessPosition() != null) {
			waypoint.setEnabled(true);
			waypoint.updatePosition(Position.of(pestFinder.getGuessPosition()));
			waypoint.getRenderer().render(renderer);
		}

        for (Entity entity : CLIENT.level.entitiesForRendering()) {
            if (!(entity instanceof ArmorStand armorStand)) {
				continue;
			}

            if (!armorStand.hasCustomName() || !armorStand.getName().getString().startsWith("ൠ")) {
				continue;
			}

            renderer.submitLineFromCursor(armorStand.getEyePosition(), Colors.GREEN, 1f);
        }
    }
}
