package fr.siroz.cariboustonks.rendering.world.renderer;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import fr.siroz.cariboustonks.rendering.Renderer;
import fr.siroz.cariboustonks.rendering.world.state.CameraRenderState;
import fr.siroz.cariboustonks.rendering.world.state.TextRenderState;
import net.minecraft.client.font.BakedGlyph;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.LightmapTextureManager;
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

		double dx = state.pos().getX() - camera.pos().getX();
		double dy = state.pos().getY() - camera.pos().getY();
		double dz = state.pos().getZ() - camera.pos().getZ();

		Matrix4f matrix4f = new Matrix4f()
				.translate((float) dx, (float) dy, (float) dz)
				.rotate(camera.rotation())
				.scale(state.scale(), -state.scale(), state.scale());

		state.glyphs().draw(new net.minecraft.client.font.TextRenderer.GlyphDrawer() {
			@Override
			public void drawGlyph(BakedGlyph.DrawnGlyph glyph) {
				BakedGlyph bakedGlyph = glyph.glyph();
				TextureSetup textureSetup = TextureSetup.of(bakedGlyph.getTexture());
				BufferBuilder buffer = Renderer.getInstance().getBuffer(pipeline, textureSetup);
				bakedGlyph.draw(glyph, matrix4f, buffer, MAX_LIGHT_COORDINATE, false);
			}

			@Override
			public void drawRectangle(BakedGlyph bakedGlyph, BakedGlyph.Rectangle rect) {
				TextureSetup textureSetup = TextureSetup.of(bakedGlyph.getTexture());
				BufferBuilder buffer = Renderer.getInstance().getBuffer(pipeline, textureSetup);
				bakedGlyph.drawRectangle(rect, matrix4f, buffer, MAX_LIGHT_COORDINATE, false);
			}
		});
	}
}
