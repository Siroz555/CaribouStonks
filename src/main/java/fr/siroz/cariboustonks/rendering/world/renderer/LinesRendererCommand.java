package fr.siroz.cariboustonks.rendering.world.renderer;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import fr.siroz.cariboustonks.rendering.CaribouRenderPipelines;
import fr.siroz.cariboustonks.rendering.CaribouRenderer;
import fr.siroz.cariboustonks.rendering.world.state.LinesRenderState;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class LinesRendererCommand implements RendererCommand<LinesRenderState> {

	@Override
	public void emit(@NotNull LinesRenderState state, @NotNull CameraRenderState camera) {
		RenderPipeline pipeline = state.throughBlocks()
				? CaribouRenderPipelines.LINES_THROUGH_BLOCKS
				: RenderPipelines.LINES;

		BufferBuilder buffer = CaribouRenderer.getBuffer(pipeline, state.lineWidth());

		Matrix4f matrix4f = new Matrix4f()
				.translate((float) -camera.pos.getX(), (float) -camera.pos.getY(), (float) -camera.pos.getZ());

		Vec3d[] points = state.points();
		for (int i = 0; i < points.length; i++) {
			Vec3d nextPoint = points[i + 1 == points.length ? i - 1 : i + 1];
			Vector3f normalVec = nextPoint.toVector3f()
					.sub((float) points[i].getX(), (float) points[i].getY(), (float) points[i].getZ())
					.normalize();

			if (i + 1 == points.length) {
				normalVec.negate();
			}

			buffer.vertex(matrix4f, (float) points[i].getX(), (float) points[i].getY(), (float) points[i].getZ())
					.color(state.color().r, state.color().g, state.color().b, state.color().a)
					.normal(normalVec.x(), normalVec.y(), normalVec.z())
					.lineWidth(state.lineWidth());
		}
	}
}
