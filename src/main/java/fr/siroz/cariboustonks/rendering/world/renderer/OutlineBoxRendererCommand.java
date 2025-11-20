package fr.siroz.cariboustonks.rendering.world.renderer;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import fr.siroz.cariboustonks.rendering.CaribouRenderPipelines;
import fr.siroz.cariboustonks.rendering.CaribouRenderer;
import fr.siroz.cariboustonks.rendering.world.state.OutlineBoxRenderState;
import fr.siroz.cariboustonks.util.render.RenderUtils;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.BufferBuilder;
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

		BufferBuilder buffer = CaribouRenderer.getBuffer(pipeline, state.lineWidth());

		Matrix4f matrix4f = new Matrix4f()
				.translate((float) -camera.pos.getX(), (float) -camera.pos.getY(), (float) -camera.pos.getZ());

//		VertexRendering.drawBox(matrices.peek(), buffer,
//				state.box(),
//				colorComponents[0], colorComponents[1], colorComponents[2], state.color().getAlpha()
//		);

		float minX = (float) state.box().minX;
		float minY = (float) state.box().minY;
		float minZ = (float) state.box().minZ;
		float maxX = (float) state.box().maxX;
		float maxY = (float) state.box().maxY;
		float maxZ = (float) state.box().maxZ;

		float[] colorComponents = state.color().asFloatComponents();
		float red = colorComponents[0];
		float green = colorComponents[1];
		float blue = colorComponents[2];
		float alpha = state.color().getAlpha();

		MatrixStack.Entry entry = RenderUtils.matrixToStack(matrix4f).peek();

		buffer.vertex(entry, minX, minY, minZ).color(red, green, blue, alpha).normal(entry, 1.0F, 0.0F, 0.0F).lineWidth(state.lineWidth());
		buffer.vertex(entry, maxX, minY, minZ).color(red, green, blue, alpha).normal(entry, 1.0F, 0.0F, 0.0F).lineWidth(state.lineWidth());
		buffer.vertex(entry, minX, minY, minZ).color(red, green, blue, alpha).normal(entry, 0.0F, 1.0F, 0.0F).lineWidth(state.lineWidth());
		buffer.vertex(entry, minX, maxY, minZ).color(red, green, blue, alpha).normal(entry, 0.0F, 1.0F, 0.0F).lineWidth(state.lineWidth());
		buffer.vertex(entry, minX, minY, minZ).color(red, green, blue, alpha).normal(entry, 0.0F, 0.0F, 1.0F).lineWidth(state.lineWidth());
		buffer.vertex(entry, minX, minY, maxZ).color(red, green, blue, alpha).normal(entry, 0.0F, 0.0F, 1.0F).lineWidth(state.lineWidth());
		buffer.vertex(entry, maxX, minY, minZ).color(red, green, blue, alpha).normal(entry, 0.0F, 1.0F, 0.0F).lineWidth(state.lineWidth());
		buffer.vertex(entry, maxX, maxY, minZ).color(red, green, blue, alpha).normal(entry, 0.0F, 1.0F, 0.0F).lineWidth(state.lineWidth());
		buffer.vertex(entry, maxX, maxY, minZ).color(red, green, blue, alpha).normal(entry, -1.0F, 0.0F, 0.0F).lineWidth(state.lineWidth());
		buffer.vertex(entry, minX, maxY, minZ).color(red, green, blue, alpha).normal(entry, -1.0F, 0.0F, 0.0F).lineWidth(state.lineWidth());
		buffer.vertex(entry, minX, maxY, minZ).color(red, green, blue, alpha).normal(entry, 0.0F, 0.0F, 1.0F).lineWidth(state.lineWidth());
		buffer.vertex(entry, minX, maxY, maxZ).color(red, green, blue, alpha).normal(entry, 0.0F, 0.0F, 1.0F).lineWidth(state.lineWidth());
		buffer.vertex(entry, minX, maxY, maxZ).color(red, green, blue, alpha).normal(entry, 0.0F, -1.0F, 0.0F).lineWidth(state.lineWidth());
		buffer.vertex(entry, minX, minY, maxZ).color(red, green, blue, alpha).normal(entry, 0.0F, -1.0F, 0.0F).lineWidth(state.lineWidth());
		buffer.vertex(entry, minX, minY, maxZ).color(red, green, blue, alpha).normal(entry, 1.0F, 0.0F, 0.0F).lineWidth(state.lineWidth());
		buffer.vertex(entry, maxX, minY, maxZ).color(red, green, blue, alpha).normal(entry, 1.0F, 0.0F, 0.0F).lineWidth(state.lineWidth());
		buffer.vertex(entry, maxX, minY, maxZ).color(red, green, blue, alpha).normal(entry, 0.0F, 0.0F, -1.0F).lineWidth(state.lineWidth());
		buffer.vertex(entry, maxX, minY, minZ).color(red, green, blue, alpha).normal(entry, 0.0F, 0.0F, -1.0F).lineWidth(state.lineWidth());
		buffer.vertex(entry, minX, maxY, maxZ).color(red, green, blue, alpha).normal(entry, 1.0F, 0.0F, 0.0F).lineWidth(state.lineWidth());
		buffer.vertex(entry, maxX, maxY, maxZ).color(red, green, blue, alpha).normal(entry, 1.0F, 0.0F, 0.0F).lineWidth(state.lineWidth());
		buffer.vertex(entry, maxX, minY, maxZ).color(red, green, blue, alpha).normal(entry, 0.0F, 1.0F, 0.0F).lineWidth(state.lineWidth());
		buffer.vertex(entry, maxX, maxY, maxZ).color(red, green, blue, alpha).normal(entry, 0.0F, 1.0F, 0.0F).lineWidth(state.lineWidth());
		buffer.vertex(entry, maxX, maxY, minZ).color(red, green, blue, alpha).normal(entry, 0.0F, 0.0F, 1.0F).lineWidth(state.lineWidth());
		buffer.vertex(entry, maxX, maxY, maxZ).color(red, green, blue, alpha).normal(entry, 0.0F, 0.0F, 1.0F).lineWidth(state.lineWidth());
	}
}
