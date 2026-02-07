package fr.siroz.cariboustonks.rendering.world.renderer;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.BufferBuilder;
import fr.siroz.cariboustonks.rendering.CaribouRenderPipelines;
import fr.siroz.cariboustonks.rendering.CaribouRenderer;
import fr.siroz.cariboustonks.rendering.world.state.QuadRenderState;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.jspecify.annotations.NonNull;

public final class QuadRendererCommand implements RendererCommand<QuadRenderState> {

	@Override
	public void emit(@NonNull QuadRenderState state, @NonNull CameraRenderState camera) {
		RenderPipeline pipeline = state.throughBlocks()
				? CaribouRenderPipelines.QUADS_THROUGH_BLOCKS
				: RenderPipelines.DEBUG_QUADS;

		BufferBuilder buffer = CaribouRenderer.getBuffer(pipeline);

		Matrix4f matrix4f = new Matrix4f()
				.translate((float) -camera.pos.x(), (float) -camera.pos.y(), (float) -camera.pos.z());

		float[] colorComponents = state.color().asFloatComponents();

		Vec3[] points = state.points();
		for (int i = 0; i < 4; i++) {
			buffer.addVertex(matrix4f, (float) points[i].x(), (float) points[i].y(), (float) points[i].z())
					.setColor(colorComponents[0], colorComponents[1], colorComponents[2], state.color().getAlpha());
		}
	}
}
