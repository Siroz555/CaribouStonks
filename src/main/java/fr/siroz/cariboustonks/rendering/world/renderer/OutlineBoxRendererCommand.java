package fr.siroz.cariboustonks.rendering.world.renderer;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import fr.siroz.cariboustonks.rendering.CaribouRenderPipelines;
import fr.siroz.cariboustonks.rendering.CaribouRenderer;
import fr.siroz.cariboustonks.rendering.world.state.OutlineBoxRenderState;
import fr.siroz.cariboustonks.util.render.RenderUtils;
import net.minecraft.client.renderer.RenderPipelines;
import com.mojang.blaze3d.vertex.BufferBuilder;
import net.minecraft.client.renderer.state.CameraRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

public final class OutlineBoxRendererCommand implements RendererCommand<OutlineBoxRenderState> {

	@Override
	public void emit(@NotNull OutlineBoxRenderState state, @NotNull CameraRenderState camera) {
		RenderPipeline pipeline = state.throughBlocks()
				? CaribouRenderPipelines.LINES_THROUGH_BLOCKS
				: RenderPipelines.LINES;

		BufferBuilder buffer = CaribouRenderer.getBuffer(pipeline);

		Matrix4f matrix4f = new Matrix4f()
				.translate((float) -camera.pos.x(), (float) -camera.pos.y(), (float) -camera.pos.z());

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

		PoseStack.Pose entry = RenderUtils.matrixToStack(matrix4f).last();

		buffer.addVertex(entry, minX, minY, minZ).setColor(red, green, blue, alpha).setNormal(entry, 1.0F, 0.0F, 0.0F).setLineWidth(state.lineWidth());
		buffer.addVertex(entry, maxX, minY, minZ).setColor(red, green, blue, alpha).setNormal(entry, 1.0F, 0.0F, 0.0F).setLineWidth(state.lineWidth());
		buffer.addVertex(entry, minX, minY, minZ).setColor(red, green, blue, alpha).setNormal(entry, 0.0F, 1.0F, 0.0F).setLineWidth(state.lineWidth());
		buffer.addVertex(entry, minX, maxY, minZ).setColor(red, green, blue, alpha).setNormal(entry, 0.0F, 1.0F, 0.0F).setLineWidth(state.lineWidth());
		buffer.addVertex(entry, minX, minY, minZ).setColor(red, green, blue, alpha).setNormal(entry, 0.0F, 0.0F, 1.0F).setLineWidth(state.lineWidth());
		buffer.addVertex(entry, minX, minY, maxZ).setColor(red, green, blue, alpha).setNormal(entry, 0.0F, 0.0F, 1.0F).setLineWidth(state.lineWidth());
		buffer.addVertex(entry, maxX, minY, minZ).setColor(red, green, blue, alpha).setNormal(entry, 0.0F, 1.0F, 0.0F).setLineWidth(state.lineWidth());
		buffer.addVertex(entry, maxX, maxY, minZ).setColor(red, green, blue, alpha).setNormal(entry, 0.0F, 1.0F, 0.0F).setLineWidth(state.lineWidth());
		buffer.addVertex(entry, maxX, maxY, minZ).setColor(red, green, blue, alpha).setNormal(entry, -1.0F, 0.0F, 0.0F).setLineWidth(state.lineWidth());
		buffer.addVertex(entry, minX, maxY, minZ).setColor(red, green, blue, alpha).setNormal(entry, -1.0F, 0.0F, 0.0F).setLineWidth(state.lineWidth());
		buffer.addVertex(entry, minX, maxY, minZ).setColor(red, green, blue, alpha).setNormal(entry, 0.0F, 0.0F, 1.0F).setLineWidth(state.lineWidth());
		buffer.addVertex(entry, minX, maxY, maxZ).setColor(red, green, blue, alpha).setNormal(entry, 0.0F, 0.0F, 1.0F).setLineWidth(state.lineWidth());
		buffer.addVertex(entry, minX, maxY, maxZ).setColor(red, green, blue, alpha).setNormal(entry, 0.0F, -1.0F, 0.0F).setLineWidth(state.lineWidth());
		buffer.addVertex(entry, minX, minY, maxZ).setColor(red, green, blue, alpha).setNormal(entry, 0.0F, -1.0F, 0.0F).setLineWidth(state.lineWidth());
		buffer.addVertex(entry, minX, minY, maxZ).setColor(red, green, blue, alpha).setNormal(entry, 1.0F, 0.0F, 0.0F).setLineWidth(state.lineWidth());
		buffer.addVertex(entry, maxX, minY, maxZ).setColor(red, green, blue, alpha).setNormal(entry, 1.0F, 0.0F, 0.0F).setLineWidth(state.lineWidth());
		buffer.addVertex(entry, maxX, minY, maxZ).setColor(red, green, blue, alpha).setNormal(entry, 0.0F, 0.0F, -1.0F).setLineWidth(state.lineWidth());
		buffer.addVertex(entry, maxX, minY, minZ).setColor(red, green, blue, alpha).setNormal(entry, 0.0F, 0.0F, -1.0F).setLineWidth(state.lineWidth());
		buffer.addVertex(entry, minX, maxY, maxZ).setColor(red, green, blue, alpha).setNormal(entry, 1.0F, 0.0F, 0.0F).setLineWidth(state.lineWidth());
		buffer.addVertex(entry, maxX, maxY, maxZ).setColor(red, green, blue, alpha).setNormal(entry, 1.0F, 0.0F, 0.0F).setLineWidth(state.lineWidth());
		buffer.addVertex(entry, maxX, minY, maxZ).setColor(red, green, blue, alpha).setNormal(entry, 0.0F, 1.0F, 0.0F).setLineWidth(state.lineWidth());
		buffer.addVertex(entry, maxX, maxY, maxZ).setColor(red, green, blue, alpha).setNormal(entry, 0.0F, 1.0F, 0.0F).setLineWidth(state.lineWidth());
		buffer.addVertex(entry, maxX, maxY, minZ).setColor(red, green, blue, alpha).setNormal(entry, 0.0F, 0.0F, 1.0F).setLineWidth(state.lineWidth());
		buffer.addVertex(entry, maxX, maxY, maxZ).setColor(red, green, blue, alpha).setNormal(entry, 0.0F, 0.0F, 1.0F).setLineWidth(state.lineWidth());
	}
}
