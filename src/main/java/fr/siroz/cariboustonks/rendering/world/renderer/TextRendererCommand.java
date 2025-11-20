package fr.siroz.cariboustonks.rendering.world.renderer;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import fr.siroz.cariboustonks.rendering.CaribouRenderer;
import fr.siroz.cariboustonks.rendering.world.state.TextRenderState;
import net.minecraft.client.font.TextDrawable;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.texture.TextureSetup;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

public final class TextRendererCommand implements RendererCommand<TextRenderState> {

	private static final int MAX_LIGHT_COORDINATE = LightmapTextureManager.MAX_LIGHT_COORDINATE;

	@Override
	public void emit(@NotNull TextRenderState state, @NotNull CameraRenderState camera) {
		RenderPipeline pipeline = state.throughBlocks()
				? RenderPipelines.RENDERTYPE_TEXT_SEETHROUGH
				: RenderPipelines.RENDERTYPE_TEXT;

		double dx = state.pos().getX() - camera.pos.getX();
		double dy = state.pos().getY() - camera.pos.getY();
		double dz = state.pos().getZ() - camera.pos.getZ();

		Matrix4f matrix4f = new Matrix4f()
				.translate((float) dx, (float) dy, (float) dz)
				.rotate(camera.orientation)
				.scale(state.scale(), -state.scale(), state.scale());

		state.glyphs().draw(new TextRenderer.GlyphDrawer() {
			@Override
			public void drawGlyph(TextDrawable glyph) {
				TextureSetup textureSetup = TextureSetup.of(glyph.textureView());
				BufferBuilder buffer = CaribouRenderer.getBuffer(pipeline, textureSetup);
				glyph.render(matrix4f, buffer, MAX_LIGHT_COORDINATE, false);
			}

			@Override
			public void drawRectangle(TextDrawable bakedGlyph) {
				TextureSetup textureSetup = TextureSetup.of(bakedGlyph.textureView());
				BufferBuilder buffer = CaribouRenderer.getBuffer(pipeline, textureSetup);
				bakedGlyph.render(matrix4f, buffer, MAX_LIGHT_COORDINATE, false);
			}
		});
	}
}
