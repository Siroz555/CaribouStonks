package fr.siroz.cariboustonks.rendering.world.renderer;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import fr.siroz.cariboustonks.rendering.CaribouRenderPipelines;
import fr.siroz.cariboustonks.rendering.CaribouRenderer;
import fr.siroz.cariboustonks.rendering.world.state.QuadRenderState;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

public final class QuadRendererCommand implements RendererCommand<QuadRenderState> {

	@Override
	public void emit(@NotNull QuadRenderState state, @NotNull CameraRenderState camera) {
		RenderPipeline pipeline = state.throughBlocks()
				? CaribouRenderPipelines.QUADS_THROUGH_BLOCKS
				: RenderPipelines.DEBUG_QUADS;

		BufferBuilder buffer = CaribouRenderer.getBuffer(pipeline);

		Matrix4f matrix4f = new Matrix4f()
				.translate((float) -camera.pos.getX(), (float) -camera.pos.getY(), (float) -camera.pos.getZ());

		float[] colorComponents = state.color().asFloatComponents();

		Vec3d[] points = state.points();
		for (int i = 0; i < 4; i++) {
			buffer.vertex(matrix4f, (float) points[i].getX(), (float) points[i].getY(), (float) points[i].getZ())
					.color(colorComponents[0], colorComponents[1], colorComponents[2], state.color().getAlpha());
		}
	}
}
