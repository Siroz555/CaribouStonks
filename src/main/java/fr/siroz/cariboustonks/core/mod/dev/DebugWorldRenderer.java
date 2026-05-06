package fr.siroz.cariboustonks.core.mod.dev;

import fr.siroz.cariboustonks.core.module.color.Color;
import fr.siroz.cariboustonks.core.module.color.Colors;
import fr.siroz.cariboustonks.platform.api.render.WorldRenderer;
import fr.siroz.cariboustonks.util.render.RenderUtils;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

class DebugWorldRenderer {

	public void render(WorldRenderer renderer) {
		renderer.submitText(
				Component.nullToEmpty("CaribouStonks " + SharedConstants.getCurrentVersion().name()),
				new Vec3(64, 135, 213), 1.3f,
				true
		);
		renderer.submitBeaconBeam(
				new BlockPos(63, 129, 219),
				Colors.RED
		);
		renderer.submitFilled(
				new BlockPos(57, 129, 218),
				Colors.RED.withAlpha(0.25f),
				true
		);
		renderer.submitBeaconBeam(
				new BlockPos(65, 129, 219),
				Colors.RAINBOW
		);
		renderer.submitFilled(
				new BlockPos(59, 129, 218),
				Colors.RAINBOW,
				false
		); // Le true marche a coup sur, mais 1.21.11 j'utilise ma propre Pipeline pour
		renderer.submitOutline(
				new AABB(new BlockPos(57, 129, 211)),
				Colors.PURPLE,
				1f,
				true
		);
		renderer.submitOutline(
				new AABB(new BlockPos(59, 129, 211)),
				Colors.PINK,
				1f,
				false
		);
		renderer.submitCircle(
				new Vec3(74, 129, 206),
				5, 16, .02f,
				Colors.YELLOW,
				Direction.Axis.Y,
				false
		);
		renderer.submitThickCircle(
				new Vec3(74, 129, 218),
				5, 2, 64,
				Colors.AQUA.withAlpha(0.5f),
				false
		);
		renderer.submitLines(
				new Vec3[]{
						new Vec3(63, 130, 207),
						new Vec3(66, 135, 210)},
				Colors.MAGENTA,
				1.5f,
				true
		);
		renderer.submitQuad(new Vec3[]{
						new Vec3(63, 129, 225),
						new Vec3(61, 129, 225),
						new Vec3(61, 135, 225),
						new Vec3(63, 135, 225)},
				Colors.YELLOW.withAlpha(0.5f),
				true
		);

		Vec3 centerPos = new Vec3(70, 128, 214);
		double distance = RenderUtils.getCamera().position().distanceTo(centerPos);
		float scale = Math.max((float) distance / 10, 1);

		renderer.submitTexture(
				centerPos,
				scale, scale,
				1f, 1f,
				new Vec3(0, 0, 0),
				Identifier.withDefaultNamespace("textures/item/netherite_sword.png"),
				new Color(255, 255, 255), 1f,
				true
		);
	}
}
