package fr.siroz.cariboustonks.rendering.world.renderer;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.BufferBuilder;
import fr.siroz.cariboustonks.rendering.CaribouRenderPipelines;
import fr.siroz.cariboustonks.rendering.CaribouRenderer;
import fr.siroz.cariboustonks.rendering.world.state.CursorLineRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.jspecify.annotations.NonNull;

public final class CursorLineRendererCommand implements RendererCommand<CursorLineRenderState> {

	@Override
	public void emit(@NonNull CursorLineRenderState state, @NonNull CameraRenderState camera) {
		RenderPipeline pipeline = CaribouRenderPipelines.LINES_THROUGH_BLOCKS;
		BufferBuilder buffer = CaribouRenderer.getBuffer(pipeline);

		Matrix4f matrix4f = new Matrix4f()
				.translate((float) -camera.pos.x(), (float) -camera.pos.y(), (float) -camera.pos.z());

		Vec3 cameraPoint = camera.pos.add(new Vec3(camera.orientation.transform(new Vector3f(0, 0, -1))));
		Vector3f normal = state.point().toVector3f()
				.sub((float) cameraPoint.x(), (float) cameraPoint.y(), (float) cameraPoint.z())
				.normalize();

		buffer.addVertex(matrix4f, (float) cameraPoint.x(), (float) cameraPoint.y(), (float) cameraPoint.z())
				.setColor(state.color().r, state.color().g, state.color().b, state.color().a)
				.setNormal(normal.x(), normal.y(), normal.z())
				.setLineWidth(state.lineWidth());

		buffer.addVertex(matrix4f, (float) state.point().x(), (float) state.point().y(), (float) state.point().z())
				.setColor(state.color().r, state.color().g, state.color().b, state.color().a)
				.setNormal(normal.x(), normal.y(), normal.z())
				.setLineWidth(state.lineWidth());
	}
}
