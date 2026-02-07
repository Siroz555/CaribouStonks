package fr.siroz.cariboustonks.rendering.world.renderer;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.BufferBuilder;
import fr.siroz.cariboustonks.rendering.CaribouRenderPipelines;
import fr.siroz.cariboustonks.rendering.CaribouRenderer;
import fr.siroz.cariboustonks.rendering.world.state.CircleRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import org.joml.Matrix4f;
import org.jspecify.annotations.NonNull;

public final class CircleRendererCommand implements RendererCommand<CircleRenderState> {

	@Override
	public void emit(@NonNull CircleRenderState state, @NonNull CameraRenderState camera) {
		RenderPipeline pipeline = state.throughBlocks()
				? CaribouRenderPipelines.CIRCLE_THROUGH_BLOCKS
				: CaribouRenderPipelines.CIRCLE;

		BufferBuilder buffer = CaribouRenderer.getBuffer(pipeline);

		Matrix4f matrix4f = new Matrix4f()
				.translate((float) -camera.pos.x(), (float) -camera.pos.y(), (float) -camera.pos.z());

		// 5% du rayon (0.05) | min : 0.01f (trop faible) | max : 0.95 (la quasi-totalité du cercle)
		float thicknessPercent = state.thicknessPercent();
		thicknessPercent = Math.max(0.01f, Math.min(thicknessPercent, 0.95f));
		float thickness = (float) (state.radius() * thicknessPercent);

		for (int i = 0; i <= state.segments(); i++) {
			double angle = 2.0 * Math.PI * i / state.segments();
			float sin = (float) Math.sin(angle);
			float cos = (float) Math.cos(angle);
			// Vertex extérieur
			float outerX = (float) state.center().x;
			float outerY = (float) state.center().y;
			float outerZ = (float) state.center().z;
			// Vertex intérieur
			float innerX = (float) state.center().x;
			float innerY = (float) state.center().y;
			float innerZ = (float) state.center().z;

			switch (state.axis()) {
				case X -> {
					outerY += (float) ((state.radius() + thickness) * cos);
					outerZ += (float) ((state.radius() + thickness) * sin);
					innerY += (float) ((state.radius() - thickness) * cos);
					innerZ += (float) ((state.radius() - thickness) * sin);
				}
				case Y -> {
					outerX += (float) ((state.radius() + thickness) * cos);
					outerZ += (float) ((state.radius() + thickness) * sin);
					innerX += (float) ((state.radius() - thickness) * cos);
					innerZ += (float) ((state.radius() - thickness) * sin);
				}
				case Z -> {
					outerX += (float) ((state.radius() + thickness) * cos);
					outerY += (float) ((state.radius() + thickness) * sin);
					innerX += (float) ((state.radius() - thickness) * cos);
					innerY += (float) ((state.radius() - thickness) * sin);
				}
				default -> {
				}
			}

			buffer.addVertex(matrix4f, outerX, outerY, outerZ)
					.setColor(state.color().r, state.color().g, state.color().b, state.color().a);

			buffer.addVertex(matrix4f, innerX, innerY, innerZ)
					.setColor(state.color().r, state.color().g, state.color().b, state.color().a);
		}
	}
}
