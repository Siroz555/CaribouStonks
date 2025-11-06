package fr.siroz.cariboustonks.rendering.world.renderer;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import fr.siroz.cariboustonks.rendering.CaribouRenderPipelines;
import fr.siroz.cariboustonks.rendering.Renderer;
import fr.siroz.cariboustonks.rendering.world.state.CursorLineRenderState;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class CursorLineRendererCommand implements RendererCommand<CursorLineRenderState> {

	@Override
	public void emit(@NotNull CursorLineRenderState state, @NotNull CameraRenderState camera) {
		RenderPipeline pipeline = CaribouRenderPipelines.LINES_THROUGH_BLOCKS;
		BufferBuilder buffer = Renderer.getInstance().getBuffer(pipeline, state.lineWidth());

		Matrix4f matrix4f = new Matrix4f()
				.translate((float) -camera.pos.getX(), (float) -camera.pos.getY(), (float) -camera.pos.getZ());

		Vec3d cameraPoint = camera.pos.add(new Vec3d(camera.orientation.transform(new Vector3f(0, 0, -1))));
		Vector3f normal = state.point().toVector3f()
				.sub((float) cameraPoint.getX(), (float) cameraPoint.getY(), (float) cameraPoint.getZ())
				.normalize();

		buffer.vertex(matrix4f, (float) cameraPoint.getX(), (float) cameraPoint.getY(), (float) cameraPoint.getZ())
				.color(state.color().r, state.color().g, state.color().b, state.color().a)
				.normal(normal.x(), normal.y(), normal.z());

		buffer.vertex(matrix4f, (float) state.point().getX(), (float) state.point().getY(), (float) state.point().getZ())
				.color(state.color().r, state.color().g, state.color().b, state.color().a)
				.normal(normal.x(), normal.y(), normal.z());
	}
}
