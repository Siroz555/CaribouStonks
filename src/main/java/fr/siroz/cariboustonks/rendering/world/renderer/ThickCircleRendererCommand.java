package fr.siroz.cariboustonks.rendering.world.renderer;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.BufferBuilder;
import fr.siroz.cariboustonks.rendering.CaribouRenderPipelines;
import fr.siroz.cariboustonks.rendering.CaribouRenderer;
import fr.siroz.cariboustonks.rendering.world.state.ThickCircleRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.jspecify.annotations.NonNull;

public final class ThickCircleRendererCommand implements RendererCommand<ThickCircleRenderState> {

	@Override
	public void emit(@NonNull ThickCircleRenderState state, @NonNull CameraRenderState camera) {
		RenderPipeline pipeline = state.throughBlocks()
				? CaribouRenderPipelines.CIRCLE_THROUGH_BLOCKS
				: CaribouRenderPipelines.CIRCLE;

		BufferBuilder buffer = CaribouRenderer.getBuffer(pipeline);

		Matrix4f matrix4f = new Matrix4f()
				.translate((float) -camera.pos.x(), (float) -camera.pos.y(), (float) -camera.pos.z());

		Vec3 centerTopPos = state.center().add(0, state.thickness(), 0);

		for (int i = 0; i <= state.segments(); i++) {
			double angle = 2 * Math.PI * i / state.segments();
			float cos = (float) Math.cos(angle);
			float sin = (float) Math.sin(angle);
			double v = (state.center().x + state.radius() * cos);
			double v1 = (state.center().z + state.radius() * sin);
			// Vertex inférieur
			float xLower = (float) v;
			float zLower = (float) v1;
			// Vertex supérieur
			float xUpper = (float) v;
			float zUpper = (float) v1;

			buffer.addVertex(matrix4f, xUpper, (float) centerTopPos.y(), zUpper)
					.setColor(state.color().r, state.color().g, state.color().b, state.color().a);

			buffer.addVertex(matrix4f, xLower, (float) state.center().y(), zLower)
					.setColor(state.color().r, state.color().g, state.color().b, state.color().a);
		}
	}
}
