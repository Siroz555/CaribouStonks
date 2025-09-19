package fr.siroz.cariboustonks.core.dev;

import fr.siroz.cariboustonks.util.colors.Color;
import fr.siroz.cariboustonks.util.colors.Colors;
import fr.siroz.cariboustonks.util.render.Texture;
import fr.siroz.cariboustonks.util.render.WorldRenderUtils;
import fr.siroz.cariboustonks.util.render.WorldRendererProvider;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.SharedConstants;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

record DebugRenderer(@NotNull DeveloperManager dev) implements WorldRendererProvider {

	@Override
	public void render(WorldRenderContext context) {
		if (!dev.getTexturedArmorStands().isEmpty()) {
			for (Object2IntMap.Entry<ArmorStandEntity> armorStand : dev.getTexturedArmorStands().object2IntEntrySet()) {
				ArmorStandEntity entity = armorStand.getKey();
				if (entity == null || entity.getPos() == null) {
					continue;
				}

				Vec3d centerPos = entity.getPos();
				WorldRenderUtils.renderText(context, Text.literal("#" + armorStand.getIntValue()),
						centerPos.add(0, 1, 0), 1, true);
			}
		}

		WorldRenderUtils.renderText(context,
				Text.of("CaribouStonks " + SharedConstants.getGameVersion().name()),
				new Vec3d(-1.5, 69, 25.5), 1.3f,
				true);

		WorldRenderUtils.renderFilledWithBeaconBeam(context,
				new BlockPos(1, 70, 25),
				Colors.RED,
				.5f,
				true);

		WorldRenderUtils.renderCircle(context,
				new Vec3d(5, 65, 18),
				5,
				16,
				.02f,
				Colors.YELLOW,
				Direction.Axis.Y,
				false);

		WorldRenderUtils.renderThickCircle(context,
				new Vec3d(5, 63, 24),
				5,
				2,
				64,
				Colors.AQUA.withAlpha(0.5f),
				false);

		WorldRenderUtils.renderLinesFromPoints(context,
				new Vec3d[]{
						new Vec3d(0, 68, 17),
						new Vec3d(4, 70, 20),
						new Vec3d(5, 71, 17)},
				Colors.MAGENTA,
				5f,
				true);

		WorldRenderUtils.renderQuad(context, new Vec3d[]{
						new Vec3d(4, 66, 29.5),
						new Vec3d(4, 66, 28.5),
						new Vec3d(4, 68, 28.5),
						new Vec3d(4, 68, 29.5)},
				Colors.YELLOW,
				.5f,
				true);

		Vec3d centerPos = new Vec3d(3, 66, 18);
		double distance = context.camera().getPos().distanceTo(centerPos);
		float scale = Math.max((float) distance / 10, 1);

		WorldRenderUtils.renderTexture(
				context,
				centerPos,
				scale,
				scale,
				1f,
				1f,
				new Vec3d(0, 0, 0),
				Texture.NETHERITE_SWORD,
				new Color(255, 255, 255),
				1f,
				true
		);
	}
}
