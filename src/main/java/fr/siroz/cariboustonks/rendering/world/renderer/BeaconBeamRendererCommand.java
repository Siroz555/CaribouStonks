package fr.siroz.cariboustonks.rendering.world.renderer;

import fr.siroz.cariboustonks.rendering.world.state.BeaconBeamRenderState;
import fr.siroz.cariboustonks.util.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.CameraRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

public final class BeaconBeamRendererCommand implements RendererCommand<BeaconBeamRenderState> {

	@Override
	public void emit(@NotNull BeaconBeamRenderState state, @NotNull CameraRenderState camera) {
		double dx = state.pos().getX() - camera.pos.x();
		double dy = state.pos().getY() - camera.pos.y();
		double dz = state.pos().getZ() - camera.pos.z();

		Matrix4f matrix4f = new Matrix4f()
				.translate((float) dx, (float) dy, (float) dz);

		PoseStack matrices = RenderUtils.matrixToStack(matrix4f);
		SubmitNodeCollector commandQueue = Minecraft.getInstance().gameRenderer.getSubmitNodeStorage();

		// SIROZ-NOTE: Virer ce Renderer et call directement avec le levelRenderer.blockRenderStates.add
		BeaconRenderer.submitBeaconBeam(
				matrices,
				commandQueue,
				BeaconRenderer.BEAM_LOCATION,
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
