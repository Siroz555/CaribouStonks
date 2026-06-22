package fr.siroz.cariboustonks.core.mod.dev;

import fr.siroz.cariboustonks.core.module.color.Color;
import fr.siroz.cariboustonks.core.module.color.Colors;
import fr.siroz.cariboustonks.platform.context.ClientContext;
import fr.siroz.cariboustonks.platform.rendering.world.WorldRenderer;
import fr.siroz.cariboustonks.util.MinecraftUtils;
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
		renderer.submitVanillaBeaconBeam(
				new BlockPos(63, 129, 219),
				Colors.RED
		);
		renderer.submitVanillaBeaconBeam(
				new BlockPos(65, 129, 219),
				Colors.RAINBOW
		);
		renderer.submitBeam(new Vec3(54, 129, 218), Colors.RED, true);
		renderer.submitBeam(new Vec3(54, 129, 216), Colors.AQUA, 10f, 2f, true);
		renderer.submitBeam(new Vec3(54, 129, 214), Colors.GOLD, 2f, 1.5f, false);
		renderer.submitBeam(new Vec3(54, 129, 212), Colors.RAINBOW, 2.5f, 1.25f, false);
		renderer.submitFilled(
				new BlockPos(57, 129, 218),
				Colors.RED.withAlpha(0.25f),
				true
		);
		renderer.submitFilled(
				new BlockPos(59, 129, 218),
				Colors.RAINBOW,
				false
		);
		renderer.submitOutline(
				new AABB(new BlockPos(57, 129, 211)),
				Colors.LIGHT_PURPLE,
				1f,
				true
		);
		renderer.submitOutline(
				new AABB(new BlockPos(59, 129, 211)),
				Colors.DARK_PURPLE,
				1f,
				false
		);
		renderer.submitCircle(
				new Vec3(74, 129, 206),
				5, 16, .02f,
				Colors.YELLOW,
				Direction.Axis.Y,
				true
		);
		renderer.submitCircle(
				new Vec3(74, 129, 206),
				3, 16, .02f,
				Colors.YELLOW.withAlpha(0.5f),
				Direction.Axis.Y,
				false
		);
		renderer.submitThickCircle(
				new Vec3(74, 129, 218),
				5, 2, 64,
				Colors.AQUA,
				true
		);
		renderer.submitThickCircle(
				new Vec3(74, 129, 218),
				3, 2, 64,
				Colors.AQUA.withAlpha(0.5f),
				false
		);
		renderer.submitLines(
				new Vec3[]{
						new Vec3(63, 130, 207),
						new Vec3(66, 135, 210)},
				Colors.DARK_PURPLE.withAlpha(0.75f),
				1.5f,
				false
		);
		renderer.submitLines(
				new Vec3[]{
						new Vec3(63, 130, 210),
						new Vec3(66, 135, 213)},
				Colors.LIGHT_PURPLE,
				1.5f,
				true
		);
		renderer.submitQuad(new Vec3[]{
						new Vec3(63, 129, 225),
						new Vec3(61, 129, 225),
						new Vec3(61, 135, 225),
						new Vec3(63, 135, 225)},
				Colors.YELLOW.withAlpha(0.5f),
				false
		);
		renderer.submitQuad(new Vec3[]{
						new Vec3(58, 129, 225),
						new Vec3(56, 129, 225),
						new Vec3(56, 135, 225),
						new Vec3(58, 135, 225)},
				Colors.YELLOW,
				true
		);

		Vec3 centerPos = new Vec3(70, 128, 214);
		double distance = RenderUtils.getCamera().position().distanceTo(centerPos);
		float scale = Math.max((float) distance / 10, 1);

		renderer.submitTexture(
				centerPos,
				scale, scale,
				RenderUtils.TEXTURE_FULL_UV, RenderUtils.TEXTURE_FULL_UV,
				1f, 1f,
				new Vec3(0, 0, 0),
				Identifier.withDefaultNamespace("textures/item/netherite_sword.png"),
				new Color(255, 255, 255), 1f,
				true
		);
		renderer.submitTexture(
				centerPos.add(0, 10, 0),
				scale, scale,
				RenderUtils.TEXTURE_HEAD_UV, RenderUtils.TEXTURE_HEAD_UV,
				RenderUtils.TEXTURE_HEAD_UV, RenderUtils.TEXTURE_HEAD_UV,
				new Vec3(0, 0, 0),
				MinecraftUtils.getPlayerHeadTexture(ClientContext.getPlayerName()),
				new Color(255, 255, 255), 1f,
				true
		);
	}
}
