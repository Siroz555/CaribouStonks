package fr.siroz.cariboustonks.rendering.world.renderer;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import fr.siroz.cariboustonks.rendering.CaribouRenderPipelines;
import fr.siroz.cariboustonks.rendering.CaribouRenderer;
import fr.siroz.cariboustonks.rendering.world.state.FilledBoxRenderState;
import fr.siroz.cariboustonks.util.render.RenderUtils;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

public final class FilledBoxRendererCommand implements RendererCommand<FilledBoxRenderState> {

	@Override
	public void emit(@NotNull FilledBoxRenderState state, @NotNull CameraRenderState camera) {
		RenderPipeline pipeline = state.throughBlocks()
				? CaribouRenderPipelines.FILLED_THROUGH_BLOCKS
				// Il y a certains cotés qui ont des triangles vide.
				// Pipeline de MC est remplacé par une nouvelle.
				: CaribouRenderPipelines.FILLED; // RenderPipelines.DEBUG_FILLED_BOX;

		BufferBuilder buffer = CaribouRenderer.getBuffer(pipeline);

		Matrix4f matrix4f = new Matrix4f()
				.translate((float) -camera.pos.getX(), (float) -camera.pos.getY(), (float) -camera.pos.getZ());

		MatrixStack matrices = RenderUtils.matrixToStack(matrix4f);

//		VertexRendering.drawFilledBox(matrices, buffer,
//				state.minX(), state.minY(), state.minZ(), state.maxX(), state.maxY(), state.maxZ(),
//				colorComponents[0], colorComponents[1], colorComponents[2], state.color().getAlpha()
//		);

		Matrix4f positionMatrix = matrices.peek().getPositionMatrix();

		float minX = (float) state.minX();
		float minY = (float) state.minY();
		float minZ = (float) state.minZ();
		float maxX = (float) state.maxX();
		float maxY = (float) state.maxY();
		float maxZ = (float) state.maxZ();

		float[] colorComponents = state.color().asFloatComponents();
		float red = colorComponents[0];
		float green = colorComponents[1];
		float blue = colorComponents[2];
		float alpha = state.color().getAlpha();

		buffer.vertex(positionMatrix, minX, minY, minZ).color(red, green, blue, alpha);
		buffer.vertex(positionMatrix, minX, minY, minZ).color(red, green, blue, alpha);
		buffer.vertex(positionMatrix, minX, minY, minZ).color(red, green, blue, alpha);
		buffer.vertex(positionMatrix, minX, minY, maxZ).color(red, green, blue, alpha);
		buffer.vertex(positionMatrix, minX, maxY, minZ).color(red, green, blue, alpha);
		buffer.vertex(positionMatrix, minX, maxY, maxZ).color(red, green, blue, alpha);
		buffer.vertex(positionMatrix, minX, maxY, maxZ).color(red, green, blue, alpha);
		buffer.vertex(positionMatrix, minX, minY, maxZ).color(red, green, blue, alpha);
		buffer.vertex(positionMatrix, maxX, maxY, maxZ).color(red, green, blue, alpha);
		buffer.vertex(positionMatrix, maxX, minY, maxZ).color(red, green, blue, alpha);
		buffer.vertex(positionMatrix, maxX, minY, maxZ).color(red, green, blue, alpha);
		buffer.vertex(positionMatrix, maxX, minY, minZ).color(red, green, blue, alpha);
		buffer.vertex(positionMatrix, maxX, maxY, maxZ).color(red, green, blue, alpha);
		buffer.vertex(positionMatrix, maxX, maxY, minZ).color(red, green, blue, alpha);
		buffer.vertex(positionMatrix, maxX, maxY, minZ).color(red, green, blue, alpha);
		buffer.vertex(positionMatrix, maxX, minY, minZ).color(red, green, blue, alpha);
		buffer.vertex(positionMatrix, minX, maxY, minZ).color(red, green, blue, alpha);
		buffer.vertex(positionMatrix, minX, minY, minZ).color(red, green, blue, alpha);
		buffer.vertex(positionMatrix, minX, minY, minZ).color(red, green, blue, alpha);
		buffer.vertex(positionMatrix, maxX, minY, minZ).color(red, green, blue, alpha);
		buffer.vertex(positionMatrix, minX, minY, maxZ).color(red, green, blue, alpha);
		buffer.vertex(positionMatrix, maxX, minY, maxZ).color(red, green, blue, alpha);
		buffer.vertex(positionMatrix, maxX, minY, maxZ).color(red, green, blue, alpha);
		buffer.vertex(positionMatrix, minX, maxY, minZ).color(red, green, blue, alpha);
		buffer.vertex(positionMatrix, minX, maxY, minZ).color(red, green, blue, alpha);
		buffer.vertex(positionMatrix, minX, maxY, maxZ).color(red, green, blue, alpha);
		buffer.vertex(positionMatrix, maxX, maxY, minZ).color(red, green, blue, alpha);
		buffer.vertex(positionMatrix, maxX, maxY, maxZ).color(red, green, blue, alpha);
		buffer.vertex(positionMatrix, maxX, maxY, maxZ).color(red, green, blue, alpha);
		buffer.vertex(positionMatrix, maxX, maxY, maxZ).color(red, green, blue, alpha);
	}
}
