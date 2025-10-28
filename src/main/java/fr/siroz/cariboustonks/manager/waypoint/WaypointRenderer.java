package fr.siroz.cariboustonks.manager.waypoint;

import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.manager.waypoint.options.IconOption;
import fr.siroz.cariboustonks.manager.waypoint.options.TextOption;
import fr.siroz.cariboustonks.rendering.world.WorldRenderer;
import fr.siroz.cariboustonks.util.render.RenderUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * The {@code WaypointRenderer} class is responsible for rendering {@link Waypoint} instances in the game world.
 *
 * @see Waypoint
 * @see TextOption
 * @see IconOption
 * @see RenderUtils
 */
public final class WaypointRenderer {

	private final Waypoint waypoint;

	WaypointRenderer(Waypoint waypoint) {
		this.waypoint = waypoint;
	}

	@EventHandler(event = "RenderEvents.WORLD_RENDER")
	@SuppressWarnings({"checkstyle:CyclomaticComplexity", "checkstyle:LineLength"}) // merde
	public void render(WorldRenderer renderer) {
		if (!waypoint.isEnabled()) {
			return;
		}

		BlockPos pos = waypoint.getPosition().toBlockPos();

		switch (waypoint.getType()) {
			case BEAM -> renderer.submitBeaconBeam(pos, waypoint.getColor());
			case WAYPOINT -> {
				renderer.submitFilled(pos, waypoint.getColor().withAlpha(waypoint.getAlpha()), waypoint.isBoxThroughBlocks());
				renderer.submitBeaconBeam(pos, waypoint.getColor());
			}
			case OUTLINED_WAYPOINT -> {
				renderer.submitFilled(pos, waypoint.getColor().withAlpha(waypoint.getAlpha()), waypoint.isBoxThroughBlocks());
				renderer.submitBeaconBeam(pos, waypoint.getColor());
				renderer.submitOutline(waypoint.getBox(), waypoint.getColor(), waypoint.getBoxLineWidth(), waypoint.isBoxThroughBlocks());
			}
			case HIGHLIGHT -> renderer.submitFilled(pos, waypoint.getColor().withAlpha(waypoint.getAlpha()), waypoint.isBoxThroughBlocks());
			case OUTLINED_HIGHLIGHT -> {
				renderer.submitFilled(pos, waypoint.getColor().withAlpha(waypoint.getAlpha()), waypoint.isBoxThroughBlocks());
				renderer.submitOutline(waypoint.getBox(), waypoint.getColor(), waypoint.getBoxLineWidth(), waypoint.isBoxThroughBlocks());
			}
			case OUTLINE -> renderer.submitOutline(waypoint.getBox(), waypoint.getColor(), waypoint.getBoxLineWidth(), waypoint.isBoxThroughBlocks());
			default -> {
			}
		}

		Vec3d centerPos = pos.toCenterPos();
		double distance = -1;

		IconOption iconOption = waypoint.getIconOption();
		if (iconOption.getIcon().isPresent()) {

			float width = iconOption.getWidth();
			float height = iconOption.getHeight();
			if (iconOption.isScaleWithDistance()) {
				distance = RenderUtils.getCamera().getPos().distanceTo(centerPos);
				float scaleIcon = Math.max((float) distance / 10, 1);
				width = scaleIcon;
				height = scaleIcon;
			}

			renderer.submitTexture(centerPos, width, height,
					iconOption.getTextureWidth(), iconOption.getTextureHeight(),
					iconOption.getRenderOffset(), iconOption.getIcon().get(),
					iconOption.getColor(), iconOption.getAlpha(), iconOption.isThroughBlocks()
			);
		}

		TextOption textOption = waypoint.getTextOption();
		if (textOption.getText().isEmpty() && !textOption.isWithDistance()) {
			return;
		}

		if (textOption.getOffsetY() != -1) {
			centerPos = centerPos.add(0, textOption.getOffsetY(), 0);
		}

		if (textOption.isWithDistance()) {
			if (distance == -1) {
				distance = RenderUtils.getCamera().getPos().distanceTo(centerPos);
			}

			renderer.submitText(
					Text.literal(Math.round(distance) + "m").formatted(Formatting.AQUA),
					centerPos.add(0, 1, 0), Math.max((float) distance / 10, 1),
					MinecraftClient.getInstance().textRenderer.fontHeight + 1F,
					textOption.isThroughBlocks()
			);
		}

		if (textOption.getText().isPresent()) {
			if (distance == -1) {
				distance = RenderUtils.getCamera().getPos().distanceTo(centerPos);
			}

			renderer.submitText(
					textOption.getText().get(),
					centerPos.add(0, 1, 0), Math.max((float) distance / 10, 1),
					MinecraftClient.getInstance().textRenderer.fontHeight - 12F,
					textOption.isThroughBlocks()
			);
		}
	}
}
