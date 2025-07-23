package fr.siroz.cariboustonks.manager.waypoint;

import fr.siroz.cariboustonks.manager.waypoint.options.IconOption;
import fr.siroz.cariboustonks.manager.waypoint.options.TextOption;
import fr.siroz.cariboustonks.util.render.WorldRendererProvider;
import fr.siroz.cariboustonks.util.render.WorldRenderUtils;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * The {@code WaypointRenderer} class is responsible for rendering {@link Waypoint} instances in the game world.
 *
 * @see Waypoint
 * @see WorldRendererProvider
 * @see TextOption
 * @see IconOption
 * @see WorldRenderContext
 * @see WorldRenderUtils
 */
public final class WaypointRenderer implements WorldRendererProvider {

	private final Waypoint waypoint;

	WaypointRenderer(Waypoint waypoint) {
		this.waypoint = waypoint;
	}

	@Override
	@SuppressWarnings({"checkstyle:CyclomaticComplexity", "checkstyle:LineLength"}) // merde
	public void render(WorldRenderContext context) {
		if (!waypoint.isEnabled()) {
			return;
		}

		BlockPos pos = waypoint.getPosition().toBlockPos();

		switch (waypoint.getType()) {
			case BEAM -> WorldRenderUtils.renderBeaconBeam(context, pos, waypoint.getColor());
			case WAYPOINT -> WorldRenderUtils.renderFilledWithBeaconBeam(context, pos, waypoint.getColor(), waypoint.getAlpha(), waypoint.isBoxThroughBlocks());
			case OUTLINED_WAYPOINT -> {
				WorldRenderUtils.renderFilledWithBeaconBeam(context, pos, waypoint.getColor(), waypoint.getAlpha(), waypoint.isBoxThroughBlocks());
				WorldRenderUtils.renderOutline(context, waypoint.getBox(), waypoint.getColor(), waypoint.getBoxLineWidth(), waypoint.isBoxThroughBlocks());
			}
			case HIGHLIGHT -> WorldRenderUtils.renderFilled(context, pos, waypoint.getColor(), waypoint.getAlpha(), waypoint.isBoxThroughBlocks());
			case OUTLINED_HIGHLIGHT -> {
				WorldRenderUtils.renderFilled(context, pos, waypoint.getColor(), waypoint.getAlpha(), waypoint.isBoxThroughBlocks());
				WorldRenderUtils.renderOutline(context, waypoint.getBox(), waypoint.getColor(), waypoint.getBoxLineWidth(), waypoint.isBoxThroughBlocks());
			}
			case OUTLINE -> WorldRenderUtils.renderOutline(context, waypoint.getBox(), waypoint.getColor(), waypoint.getBoxLineWidth(), waypoint.isBoxThroughBlocks());
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
				distance = context.camera().getPos().distanceTo(centerPos);
				float scaleIcon = Math.max((float) distance / 10, 1);
				width = scaleIcon;
				height = scaleIcon;
			}

			WorldRenderUtils.renderTexture(context, centerPos, width, height,
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
				distance = context.camera().getPos().distanceTo(centerPos);
			}

			WorldRenderUtils.renderText(context,
					Text.literal(Math.round(distance) + "m").formatted(Formatting.AQUA),
					centerPos.add(0, 1, 0), Math.max((float) distance / 10, 1),
					MinecraftClient.getInstance().textRenderer.fontHeight + 1F,
					textOption.isThroughBlocks()
			);
		}

		if (textOption.getText().isPresent()) {
			if (distance == -1) {
				distance = context.camera().getPos().distanceTo(centerPos);
			}

			WorldRenderUtils.renderText(context,
					textOption.getText().get(),
					centerPos.add(0, 1, 0), Math.max((float) distance / 10, 1),
					MinecraftClient.getInstance().textRenderer.fontHeight - 12F,
					textOption.isThroughBlocks()
			);
		}
	}
}
