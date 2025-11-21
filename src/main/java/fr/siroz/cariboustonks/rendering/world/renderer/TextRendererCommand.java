package fr.siroz.cariboustonks.rendering.world.renderer;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import fr.siroz.cariboustonks.rendering.CaribouRenderer;
import fr.siroz.cariboustonks.rendering.world.state.TextRenderState;
import net.minecraft.client.gui.font.TextRenderable;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.RenderPipelines;
import com.mojang.blaze3d.vertex.BufferBuilder;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.gui.render.TextureSetup;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

public final class TextRendererCommand implements RendererCommand<TextRenderState> {

	@Override
	public void emit(@NotNull TextRenderState state, @NotNull CameraRenderState camera) {
		RenderPipeline pipeline = state.throughBlocks()
				? RenderPipelines.TEXT_SEE_THROUGH
				: RenderPipelines.TEXT;

		double dx = state.pos().x() - camera.pos.x();
		double dy = state.pos().y() - camera.pos.y();
		double dz = state.pos().z() - camera.pos.z();

		Matrix4f matrix4f = new Matrix4f()
				.translate((float) dx, (float) dy, (float) dz)
				.rotate(camera.orientation)
				.scale(state.scale(), -state.scale(), state.scale());

		state.glyphs().visit(new Font.GlyphVisitor() {
			@Override
			public void acceptGlyph(TextRenderable.Styled glyph) {
				this.draw(glyph);
			}

			@Override
			public void acceptEffect(TextRenderable rect) {
				this.draw(rect);
			}

			private void draw(@NotNull TextRenderable glyph) {
				TextureSetup textureSetup = TextureSetup.singleTexture(
						glyph.textureView(),
						RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST)
				);

				BufferBuilder buffer = CaribouRenderer.getBuffer(pipeline, textureSetup);
				glyph.render(matrix4f, buffer, LightTexture.FULL_BRIGHT, false);
			}
		});
	}
}
