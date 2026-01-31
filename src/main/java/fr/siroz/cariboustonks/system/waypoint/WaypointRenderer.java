package fr.siroz.cariboustonks.system.waypoint;

import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.system.waypoint.options.IconOption;
import fr.siroz.cariboustonks.system.waypoint.options.TextOption;
import fr.siroz.cariboustonks.rendering.world.WorldRenderer;
import fr.siroz.cariboustonks.util.colors.Color;
import fr.siroz.cariboustonks.util.colors.Colors;
import fr.siroz.cariboustonks.util.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

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
		Color color = waypoint.getColor() == Colors.RAINBOW
				? waypoint.getColor()
				: waypoint.getColor().withAlpha(waypoint.getAlpha());

		switch (waypoint.getType()) {
			case BEAM -> renderer.submitBeaconBeam(pos, color);
			case WAYPOINT -> {
				renderer.submitFilled(pos, color, waypoint.isBoxThroughBlocks());
				renderer.submitBeaconBeam(pos.offset(0, 1, 0), color);
			}
			case OUTLINED_WAYPOINT -> {
				renderer.submitFilled(pos, color, waypoint.isBoxThroughBlocks());
				renderer.submitBeaconBeam(pos.offset(0, 1, 0), color);
				renderer.submitOutline(waypoint.getBox(), color, waypoint.getBoxLineWidth(), waypoint.isBoxThroughBlocks());
			}
			case HIGHLIGHT -> renderer.submitFilled(pos, color, waypoint.isBoxThroughBlocks());
			case OUTLINED_HIGHLIGHT -> {
				renderer.submitFilled(pos, color, waypoint.isBoxThroughBlocks());
				renderer.submitOutline(waypoint.getBox(),color, waypoint.getBoxLineWidth(), waypoint.isBoxThroughBlocks());
			}
			case OUTLINE -> renderer.submitOutline(waypoint.getBox(), color, waypoint.getBoxLineWidth(), waypoint.isBoxThroughBlocks());
			default -> {
			}
		}

		Vec3 centerPos = pos.getCenter();
		double distance = -1;

		IconOption iconOption = waypoint.getIconOption();
		if (iconOption.getIcon().isPresent()) {

			float width = iconOption.getWidth();
			float height = iconOption.getHeight();
			if (iconOption.isScaleWithDistance()) {
				distance = RenderUtils.getCamera().position().distanceTo(centerPos);
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
				distance = RenderUtils.getCamera().position().distanceTo(centerPos);
			}

			renderer.submitText(
					Component.literal(Math.round(distance) + "m").withStyle(ChatFormatting.AQUA),
					centerPos.add(0, 1, 0), Math.max((float) distance / 10, 1),
					Minecraft.getInstance().font.lineHeight + 1F,
					textOption.isThroughBlocks()
			);
		}

		if (textOption.getText().isPresent()) {
			if (distance == -1) {
				distance = RenderUtils.getCamera().position().distanceTo(centerPos);
			}

			renderer.submitText(
					textOption.getText().get(),
					centerPos.add(0, 1, 0), Math.max((float) distance / 10, 1),
					Minecraft.getInstance().font.lineHeight - 12F,
					textOption.isThroughBlocks()
			);
		}
	}
}
