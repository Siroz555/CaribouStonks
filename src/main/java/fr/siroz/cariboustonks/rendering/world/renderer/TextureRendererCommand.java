package fr.siroz.cariboustonks.rendering.world.renderer;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.BufferBuilder;
import fr.siroz.cariboustonks.rendering.CaribouRenderPipelines;
import fr.siroz.cariboustonks.rendering.CaribouRenderer;
import fr.siroz.cariboustonks.rendering.world.state.TextureRenderState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.state.CameraRenderState;
import org.joml.Matrix4f;
import org.jspecify.annotations.NonNull;

public final class TextureRendererCommand implements RendererCommand<TextureRenderState> {

	@Override
	public void emit(@NonNull TextureRenderState state, @NonNull CameraRenderState camera) {
		RenderPipeline pipeline = state.throughBlocks()
				? CaribouRenderPipelines.TEXTURE_THROUGH_BLOCKS
				: CaribouRenderPipelines.TEXTURE;

		TextureSetup textureSetup = TextureSetup.singleTexture(
				Minecraft.getInstance().getTextureManager().getTexture(state.texture()).getTextureView(),
				Minecraft.getInstance().getTextureManager().getTexture(state.texture()).getSampler()
		);
		BufferBuilder buffer = CaribouRenderer.getBuffer(pipeline, textureSetup);

		double dx = state.pos().x() - camera.pos.x();
		double dy = state.pos().y() - camera.pos.y();
		double dz = state.pos().z() - camera.pos.z();

		Matrix4f matrix4f = new Matrix4f()
				.translate((float) dx, (float) dy, (float) dz)
				.rotate(camera.orientation);

		float[] colorComponents = state.color().asFloatComponents();

		buffer.addVertex(matrix4f, (float) state.renderOffset().x(), (float) state.renderOffset().y(), (float) state.renderOffset().z())
				.setUv(1, 1 + state.textureHeight())
				.setColor(colorComponents[0], colorComponents[1], colorComponents[2], state.alpha());

		buffer.addVertex(matrix4f, (float) state.renderOffset().x(), (float) state.renderOffset().y() + state.height(), (float) state.renderOffset().z())
				.setUv(1, 1)
				.setColor(colorComponents[0], colorComponents[1], colorComponents[2], state.alpha());

		buffer.addVertex(matrix4f, (float) state.renderOffset().x() + state.width(), (float) state.renderOffset().y() + state.height(), (float) state.renderOffset().z())
				.setUv(1 + state.textureWidth(), 1)
				.setColor(colorComponents[0], colorComponents[1], colorComponents[2], state.alpha());

		buffer.addVertex(matrix4f, (float) state.renderOffset().x() + state.width(), (float) state.renderOffset().y(), (float) state.renderOffset().z())
				.setUv(1 + state.textureWidth(), 1 + state.textureHeight())
				.setColor(colorComponents[0], colorComponents[1], colorComponents[2], state.alpha());
	}
}
