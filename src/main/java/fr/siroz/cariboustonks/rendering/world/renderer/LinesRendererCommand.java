package fr.siroz.cariboustonks.rendering.world.renderer;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import fr.siroz.cariboustonks.rendering.CaribouRenderPipelines;
import fr.siroz.cariboustonks.rendering.CaribouRenderer;
import fr.siroz.cariboustonks.rendering.world.state.LinesRenderState;
import net.minecraft.client.renderer.RenderPipelines;
import com.mojang.blaze3d.vertex.BufferBuilder;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class LinesRendererCommand implements RendererCommand<LinesRenderState> {

	@Override
	public void emit(@NotNull LinesRenderState state, @NotNull CameraRenderState camera) {
		RenderPipeline pipeline = state.throughBlocks()
				? CaribouRenderPipelines.LINES_THROUGH_BLOCKS
				: RenderPipelines.LINES;

		BufferBuilder buffer = CaribouRenderer.getBuffer(pipeline);

		Matrix4f matrix4f = new Matrix4f()
				.translate((float) -camera.pos.x(), (float) -camera.pos.y(), (float) -camera.pos.z());

		Vec3[] points = state.points();
		for (int i = 0; i < points.length; i++) {
			Vec3 nextPoint = points[i + 1 == points.length ? i - 1 : i + 1];
			Vector3f normalVec = nextPoint.toVector3f()
					.sub((float) points[i].x(), (float) points[i].y(), (float) points[i].z())
					.normalize();

			if (i + 1 == points.length) {
				normalVec.negate();
			}

			buffer.addVertex(matrix4f, (float) points[i].x(), (float) points[i].y(), (float) points[i].z())
					.setColor(state.color().r, state.color().g, state.color().b, state.color().a)
					.setNormal(normalVec.x(), normalVec.y(), normalVec.z())
					.setLineWidth(state.lineWidth());
		}
	}
}
