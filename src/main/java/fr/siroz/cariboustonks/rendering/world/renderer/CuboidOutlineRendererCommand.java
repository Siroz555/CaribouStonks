package fr.siroz.cariboustonks.rendering.world.renderer;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import fr.siroz.cariboustonks.core.annotation.Experimental;
import fr.siroz.cariboustonks.rendering.CaribouRenderPipelines;
import fr.siroz.cariboustonks.rendering.CaribouRenderer;
import fr.siroz.cariboustonks.rendering.world.state.CuboidOutlineRenderState;
import fr.siroz.cariboustonks.util.render.RenderUtils;
import net.minecraft.client.renderer.state.CameraRenderState;
import org.joml.Matrix4f;
import org.jspecify.annotations.NonNull;

@Experimental
public final class CuboidOutlineRendererCommand implements RendererCommand<CuboidOutlineRenderState> {

	// Pour le moment, c'est juste pour les Garden Plot.
	// C'est globalement le même code qu'avant dans le InfestedPlotRenderer.
	// Il faudra le rendre plus libre, notamment au sujet du "depth".

	@Override
	public void emit(@NonNull CuboidOutlineRenderState state, @NonNull CameraRenderState camera) {
		BufferBuilder buffer = CaribouRenderer.getBuffer(CaribouRenderPipelines.LINE_STRIP);

		Matrix4f matrix4f = new Matrix4f()
				.translate((float) -camera.pos.x(), (float) -camera.pos.y(), (float) -camera.pos.z());

		PoseStack matrices = RenderUtils.matrixToStack(matrix4f);
		PoseStack.Pose entry = matrices.last();

		double chunkX = Math.floor((state.center().x + state.depth()) / state.size());
		double chunkZ = Math.floor((state.center().z + state.depth()) / state.size());

		float chunkMinX = (float) ((float) (chunkX * state.size() - state.depth()));
		float chunkMinZ = (float) ((float) (chunkZ * state.size() - state.depth()));

		float y1 = (float) (state.minY());
		float y2 = (float) (state.maxY());

		for (int i = 0; i <= state.size(); i += state.size()) {
			for (int j = 0; j <= state.size(); j += state.size()) {
				float x = chunkMinX + i;
				float z = chunkMinZ + j;
				buffer.addVertex(entry, x, y1, z)
						.setColor(1.0F, 0.0F, 0.0F, 0.0F)
						.setNormal(entry, x, y1, z)
						.setLineWidth(state.lineWidth());

				buffer.addVertex(entry, x, y1, z)
						.setColor(1.0F, 0.0F, 0.0F, 0.5F)
						.setNormal(entry, x, y1, z)
						.setLineWidth(state.lineWidth());

				buffer.addVertex(entry, x, y2, z)
						.setColor(1.0F, 0.0F, 0.0F, 0.5F)
						.setNormal(entry, x, y2, z)
						.setLineWidth(state.lineWidth());

				buffer.addVertex(entry, x, y2, z)
						.setColor(1.0F, 0.0F, 0.0F, 0.0F)
						.setNormal(entry, x, y2, z)
						.setLineWidth(state.lineWidth());
			}
		}

		for (int i = state.minY(); i <= state.maxY() + 1; i += 2) {
			float y = (float) ((double) i);
			int color = i % 8 == 0 ? state.mainColor().asInt() : state.secondColor().asInt();
			buffer.addVertex(entry, chunkMinX, y, chunkMinZ)
					.setColor(1.0F, 1.0F, 0.0F, 0.0F)
					.setNormal(entry, chunkMinX, y, chunkMinZ)
					.setLineWidth(state.lineWidth());

			buffer.addVertex(entry, chunkMinX, y, chunkMinZ)
					.setColor(color)
					.setNormal(entry, chunkMinX, y, chunkMinZ)
					.setLineWidth(state.lineWidth());

			buffer.addVertex(entry, chunkMinX, y, chunkMinZ + state.size())
					.setColor(color).
                    setNormal(entry, chunkMinX, y, chunkMinZ + state.size())
					.setLineWidth(state.lineWidth());

			buffer.addVertex(entry, chunkMinX + state.size(), y, chunkMinZ + state.size())
					.setColor(color)
					.setNormal(entry, chunkMinX + state.size(), y, chunkMinZ + state.size())
					.setLineWidth(state.lineWidth());

			buffer.addVertex(entry, chunkMinX + state.size(), y, chunkMinZ)
					.setColor(color)
					.setNormal(entry, chunkMinX + state.size(), y, chunkMinZ)
					.setLineWidth(state.lineWidth());

			buffer.addVertex(entry, chunkMinX, y, chunkMinZ)
					.setColor(color)
					.setNormal(entry, chunkMinX, y, chunkMinZ)
					.setLineWidth(state.lineWidth());

			buffer.addVertex(entry, chunkMinX, y, chunkMinZ)
					.setColor(1.0F, 1.0F, 0.0F, 0.0F)
					.setNormal(entry, chunkMinX, y, chunkMinZ)
					.setLineWidth(state.lineWidth());
		}
	}
}
