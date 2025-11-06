package fr.siroz.cariboustonks.rendering.world.renderer;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import fr.siroz.cariboustonks.rendering.CaribouRenderPipelines;
import fr.siroz.cariboustonks.rendering.Renderer;
import fr.siroz.cariboustonks.rendering.world.state.OutlineBoxRenderState;
import fr.siroz.cariboustonks.util.render.RenderUtils;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexRendering;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

public final class OutlineBoxRendererCommand implements RendererCommand<OutlineBoxRenderState> {

	@Override
	public void emit(@NotNull OutlineBoxRenderState state, @NotNull CameraRenderState camera) {
		RenderPipeline pipeline = state.throughBlocks()
				? CaribouRenderPipelines.LINES_THROUGH_BLOCKS
				: RenderPipelines.LINES;

		BufferBuilder buffer = Renderer.getInstance().getBuffer(pipeline, state.lineWidth());

		Matrix4f matrix4f = new Matrix4f()
				.translate((float) -camera.pos.getX(), (float) -camera.pos.getY(), (float) -camera.pos.getZ());

		MatrixStack matrices = RenderUtils.matrixToStack(matrix4f);
		float[] colorComponents = state.color().asFloatComponents();

		VertexRendering.drawBox(matrices.peek(), buffer,
				state.box(),
				colorComponents[0], colorComponents[1], colorComponents[2], state.color().getAlpha()
		);
	}
}
