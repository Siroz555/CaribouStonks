package fr.siroz.cariboustonks.rendering.world.renderer;

import fr.siroz.cariboustonks.rendering.world.state.BeaconBeamRenderState;
import fr.siroz.cariboustonks.rendering.world.state.CameraRenderState;
import fr.siroz.cariboustonks.util.render.RenderUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BeaconBlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

public final class BeaconBeamRendererCommand implements RendererCommand<BeaconBeamRenderState> {

	@Override
	public void emit(@NotNull BeaconBeamRenderState state, @NotNull CameraRenderState camera) {
		double dx = state.pos().getX() - camera.pos().getX();
		double dy = state.pos().getY() - camera.pos().getY();
		double dz = state.pos().getZ() - camera.pos().getZ();

		Matrix4f matrix4f = new Matrix4f()
				.translate((float) dx, (float) dy, (float) dz);

		MatrixStack matrices = RenderUtils.matrixToStack(matrix4f);
		VertexConsumerProvider.Immediate consumers = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();

		BeaconBlockEntityRenderer.renderBeam(
				matrices,
				consumers,
				BeaconBlockEntityRenderer.BEAM_TEXTURE,
				state.tickProgress(),
				state.scale(), // 1.0f
				state.worldTime(), // auto-closable
				0,
				RenderUtils.MAX_BUILD_HEIGHT,
				state.colorInt(),
				0.2f, // 0.166f
				0.25f // 0.33f
		);
	}
}
