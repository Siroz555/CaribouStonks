package fr.siroz.cariboustonks.rendering.world.renderer;

import fr.siroz.cariboustonks.rendering.Renderer;
import fr.siroz.cariboustonks.rendering.world.state.CameraRenderState;
import fr.siroz.cariboustonks.rendering.world.state.CuboidOutlineRenderState;
import fr.siroz.cariboustonks.util.render.RenderUtils;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.util.math.MatrixStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

@ApiStatus.Experimental
public final class CuboidOutlineRendererCommand implements RendererCommand<CuboidOutlineRenderState> {

	// TODO - Pour le moment c'est juste pour les Garden Plot.
	//  C'est globalement le même code qu'avant dans le InfestedPlotRenderer.
	//  Il faudra le rendre plus libre, notamment au sujet du "depth"

	@Override
	public void emit(@NotNull CuboidOutlineRenderState state, @NotNull CameraRenderState camera) {
		BufferBuilder buffer = Renderer.getInstance().getBuffer(RenderPipelines.DEBUG_LINE_STRIP, state.lineWidth());

		Matrix4f matrix4f = new Matrix4f()
				.translate((float) -camera.pos().getX(), (float) -camera.pos().getY(), (float) -camera.pos().getZ());

		MatrixStack matrices = RenderUtils.matrixToStack(matrix4f);
		MatrixStack.Entry entry = matrices.peek();

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
				buffer.vertex(entry, x, y1, z).color(1.0F, 0.0F, 0.0F, 0.0F).normal(entry, x, y1, z);
				buffer.vertex(entry, x, y1, z).color(1.0F, 0.0F, 0.0F, 0.5F).normal(entry, x, y1, z);
				buffer.vertex(entry, x, y2, z).color(1.0F, 0.0F, 0.0F, 0.5F).normal(entry, x, y2, z);
				buffer.vertex(entry, x, y2, z).color(1.0F, 0.0F, 0.0F, 0.0F).normal(entry, x, y2, z);
			}
		}

		for (int i = state.minY(); i <= state.maxY() + 1; i += 2) {
			float y = (float) ((double) i);
			int color = i % 8 == 0 ? state.mainColor().asInt() : state.secondColor().asInt();
			buffer.vertex(entry, chunkMinX, y, chunkMinZ).color(1.0F, 1.0F, 0.0F, 0.0F).normal(entry, chunkMinX, y, chunkMinZ);
			buffer.vertex(entry, chunkMinX, y, chunkMinZ).color(color).normal(entry, chunkMinX, y, chunkMinZ);
			buffer.vertex(entry, chunkMinX, y, chunkMinZ + state.size()).color(color).normal(entry, chunkMinX, y, chunkMinZ + state.size());
			buffer.vertex(entry, chunkMinX + state.size(), y, chunkMinZ + state.size()).color(color).normal(entry, chunkMinX + state.size(), y, chunkMinZ + state.size());
			buffer.vertex(entry, chunkMinX + state.size(), y, chunkMinZ).color(color).normal(entry, chunkMinX + state.size(), y, chunkMinZ);
			buffer.vertex(entry, chunkMinX, y, chunkMinZ).color(color).normal(entry, chunkMinX, y, chunkMinZ);
			buffer.vertex(entry, chunkMinX, y, chunkMinZ).color(1.0F, 1.0F, 0.0F, 0.0F).normal(entry, chunkMinX, y, chunkMinZ);
		}
	}
}
