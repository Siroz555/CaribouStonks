package fr.siroz.cariboustonks.core.dev;

import fr.siroz.cariboustonks.rendering.world.WorldRenderer;
import fr.siroz.cariboustonks.util.colors.Color;
import fr.siroz.cariboustonks.util.colors.Colors;
import fr.siroz.cariboustonks.util.render.Texture;
import fr.siroz.cariboustonks.util.render.RenderUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.SharedConstants;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

record DebugRenderer(@NotNull DeveloperManager dev) {

	public void render(WorldRenderer renderer) {
		if (!dev.getTexturedArmorStands().isEmpty()) {
			for (Object2IntMap.Entry<ArmorStandEntity> armorStand : dev.getTexturedArmorStands().object2IntEntrySet()) {
				ArmorStandEntity entity = armorStand.getKey();
				if (entity == null || entity.getEntityPos() == null) {
					continue;
				}

				Vec3d centerPos = entity.getEntityPos();
				renderer.submitText(Text.literal("#" + armorStand.getIntValue()),
						centerPos.add(0, 1, 0), 1, true);
			}
		}

		renderer.submitText(
				Text.of("CaribouStonks " + SharedConstants.getGameVersion().name()),
				new Vec3d(-1.5, 69, 25.5), 1.3f,
				true);

		renderer.submitBeaconBeam(
				new BlockPos(1, 71, 25),
				Colors.RED);

		renderer.submitBeaconBeam(
				new BlockPos(1, 71, 27),
				Colors.RAINBOW);

		renderer.submitFilled(
				new BlockPos(1, 70, 25),
				Colors.RED.withAlpha(0.25f),
				true);

		renderer.submitFilled(
				new BlockPos(1, 70, 27),
				Colors.RAINBOW,
				false);

		renderer.submitOutline(
				new Box(new BlockPos(5, 70, 25)),
				Colors.PURPLE,
				1f,
				true);

		renderer.submitOutline(
				new Box(new BlockPos(5, 70, 27)),
				Colors.PINK,
				1f,
				false);

		renderer.submitCircle(
				new Vec3d(5, 65, 18),
				5,
				16,
				.02f,
				Colors.YELLOW,
				Direction.Axis.Y,
				false);

		renderer.submitThickCircle(
				new Vec3d(5, 63, 24),
				5,
				2,
				64,
				Colors.AQUA.withAlpha(0.5f),
				false);

		renderer.submitLines(
				new Vec3d[]{
						new Vec3d(-1, 66, 16),
						new Vec3d(3, 69, 19),
						new Vec3d(3, 70, 23),
						new Vec3d(0, 73, 23)},
				Colors.MAGENTA,
				1.5f,
				true);

		renderer.submitLines(
				new Vec3d[]{
						new Vec3d(3, 70, 23),
						new Vec3d(0, 73, 23)},
				Colors.MAGENTA,
				1.5f,
				true);

		renderer.submitQuad(new Vec3d[]{
						new Vec3d(4, 66, 29.5),
						new Vec3d(4, 66, 28.5),
						new Vec3d(4, 68, 28.5),
						new Vec3d(4, 68, 29.5)},
				Colors.YELLOW.withAlpha(0.5f),
				true);

		Vec3d centerPos = new Vec3d(3, 66, 18);
		double distance = RenderUtils.getCamera().getCameraPos().distanceTo(centerPos);
		float scale = Math.max((float) distance / 10, 1);

		renderer.submitTexture(
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
