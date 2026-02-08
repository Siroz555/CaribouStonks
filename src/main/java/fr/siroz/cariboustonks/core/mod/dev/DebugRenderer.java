package fr.siroz.cariboustonks.core.mod.dev;

import fr.siroz.cariboustonks.core.module.color.Color;
import fr.siroz.cariboustonks.core.module.color.Colors;
import fr.siroz.cariboustonks.rendering.world.WorldRenderer;
import fr.siroz.cariboustonks.util.render.RenderUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

record DebugRenderer(DeveloperManager dev) {

	public void render(WorldRenderer renderer) {
		if (!dev.getTexturedArmorStands().isEmpty()) {
			for (Object2IntMap.Entry<ArmorStand> armorStand : dev.getTexturedArmorStands().object2IntEntrySet()) {
				ArmorStand entity = armorStand.getKey();
				if (entity == null) {
					continue;
				}

				Vec3 centerPos = entity.position();
				renderer.submitText(Component.literal("#" + armorStand.getIntValue()),
						centerPos.add(0, 1, 0), 1, true);
			}
		}

		renderer.submitText(
				Component.nullToEmpty("CaribouStonks " + SharedConstants.getCurrentVersion().name()),
				new Vec3(-1.5, 69, 25.5), 1.3f,
				true);

		renderer.submitBeaconBeam(
				new BlockPos(1, 71, 25),
				Colors.RED);

		renderer.submitFilled(
				new BlockPos(1, 70, 25),
				Colors.RED.withAlpha(0.25f),
				true);

		renderer.submitBeaconBeam(
				new BlockPos(1, 71, 27),
				Colors.RAINBOW);

		renderer.submitFilled(
				new BlockPos(1, 70, 27),
				Colors.RAINBOW,
				false); // Le true marche a coup sur, mais 1.21.11 j'utilise ma propre Pipline pour

		renderer.submitOutline(
				new AABB(new BlockPos(5, 70, 25)),
				Colors.PURPLE,
				1f,
				true);

		renderer.submitOutline(
				new AABB(new BlockPos(5, 70, 27)),
				Colors.PINK,
				1f,
				false);

		renderer.submitCircle(
				new Vec3(5, 65, 18),
				5,
				16,
				.02f,
				Colors.YELLOW,
				Direction.Axis.Y,
				false);

		renderer.submitThickCircle(
				new Vec3(5, 63, 24),
				5,
				2,
				64,
				Colors.AQUA.withAlpha(0.5f),
				false);

		renderer.submitLines(
				new Vec3[]{
						new Vec3(-1, 66, 16),
						new Vec3(3, 69, 19),
						new Vec3(3, 70, 23),
						new Vec3(0, 73, 23)},
				Colors.MAGENTA,
				1.5f,
				true);

		renderer.submitLines(
				new Vec3[]{
						new Vec3(3, 70, 23),
						new Vec3(0, 73, 23)},
				Colors.MAGENTA,
				1.5f,
				true);

		renderer.submitQuad(new Vec3[]{
						new Vec3(4, 66, 29.5),
						new Vec3(4, 66, 28.5),
						new Vec3(4, 68, 28.5),
						new Vec3(4, 68, 29.5)},
				Colors.YELLOW.withAlpha(0.5f),
				true);

		Vec3 centerPos = new Vec3(3, 66, 18);
		double distance = RenderUtils.getCamera().position().distanceTo(centerPos);
		float scale = Math.max((float) distance / 10, 1);

		renderer.submitTexture(
				centerPos,
				scale,
				scale,
				1f,
				1f,
				new Vec3(0, 0, 0),
				Identifier.withDefaultNamespace("textures/item/netherite_sword.png"),
				new Color(255, 255, 255),
				1f,
				true
		);
	}
}
