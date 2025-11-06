package fr.siroz.cariboustonks.rendering.world.renderer;

import fr.siroz.cariboustonks.rendering.world.state.BeaconBeamRenderState;
import fr.siroz.cariboustonks.util.render.RenderUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.block.entity.BeaconBlockEntityRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

public final class BeaconBeamRendererCommand implements RendererCommand<BeaconBeamRenderState> {

	@Override
	public void emit(@NotNull BeaconBeamRenderState state, @NotNull CameraRenderState camera) {
		double dx = state.pos().getX() - camera.pos.getX();
		double dy = state.pos().getY() - camera.pos.getY();
		double dz = state.pos().getZ() - camera.pos.getZ();

		Matrix4f matrix4f = new Matrix4f()
				.translate((float) dx, (float) dy, (float) dz);

		MatrixStack matrices = RenderUtils.matrixToStack(matrix4f);
		OrderedRenderCommandQueue commandQueue = MinecraftClient.getInstance().gameRenderer.getEntityRenderCommandQueue();

		BeaconBlockEntityRenderer.renderBeam(
				matrices,
				commandQueue,
				BeaconBlockEntityRenderer.BEAM_TEXTURE,
				state.scale(), // 1.0f
				state.beamRotationDegrees(),
				0,
				RenderUtils.MAX_BUILD_HEIGHT,
				state.colorInt(),
				0.2f, // 0.166f
				0.25f // 0.33f
		);
	}
}
