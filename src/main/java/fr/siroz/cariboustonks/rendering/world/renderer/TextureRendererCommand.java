package fr.siroz.cariboustonks.rendering.world.renderer;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import fr.siroz.cariboustonks.rendering.CaribouRenderPipelines;
import fr.siroz.cariboustonks.rendering.Renderer;
import fr.siroz.cariboustonks.rendering.world.state.CameraRenderState;
import fr.siroz.cariboustonks.rendering.world.state.TextureRenderState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.texture.TextureSetup;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

public final class TextureRendererCommand implements RendererCommand<TextureRenderState> {

	@Override
	public void emit(@NotNull TextureRenderState state, @NotNull CameraRenderState camera) {
		RenderPipeline pipeline = state.throughBlocks()
				? CaribouRenderPipelines.TEXTURE_THROUGH_BLOCKS
				: CaribouRenderPipelines.TEXTURE;

		TextureSetup textureSetup = TextureSetup.of(
				MinecraftClient.getInstance().getTextureManager().getTexture(state.texture()).getGlTextureView()
		);
		BufferBuilder buffer = Renderer.getInstance().getBuffer(pipeline, textureSetup);

		double dx = state.pos().getX() - camera.pos().getX();
		double dy = state.pos().getY() - camera.pos().getY();
		double dz = state.pos().getZ() - camera.pos().getZ();

		Matrix4f matrix4f = new Matrix4f()
				.translate((float) dx, (float) dy, (float) dz)
				.rotate(camera.rotation());

		float[] colorComponents = state.color().asFloatComponents();

		buffer.vertex(matrix4f, (float) state.renderOffset().getX(), (float) state.renderOffset().getY(), (float) state.renderOffset().getZ())
				.texture(1, 1 + state.textureHeight())
				.color(colorComponents[0], colorComponents[1], colorComponents[2], state.alpha());

		buffer.vertex(matrix4f, (float) state.renderOffset().getX(), (float) state.renderOffset().getY() + state.height(), (float) state.renderOffset().getZ())
				.texture(1, 1)
				.color(colorComponents[0], colorComponents[1], colorComponents[2], state.alpha());

		buffer.vertex(matrix4f, (float) state.renderOffset().getX() + state.width(), (float) state.renderOffset().getY() + state.height(), (float) state.renderOffset().getZ())
				.texture(1 + state.textureWidth(), 1)
				.color(colorComponents[0], colorComponents[1], colorComponents[2], state.alpha());

		buffer.vertex(matrix4f, (float) state.renderOffset().getX() + state.width(), (float) state.renderOffset().getY(), (float) state.renderOffset().getZ())
				.texture(1 + state.textureWidth(), 1 + state.textureHeight())
				.color(colorComponents[0], colorComponents[1], colorComponents[2], state.alpha());
	}
}
