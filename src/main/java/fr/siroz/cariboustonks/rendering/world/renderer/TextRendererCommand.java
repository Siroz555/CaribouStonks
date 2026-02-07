package fr.siroz.cariboustonks.rendering.world.renderer;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.vertex.BufferBuilder;
import fr.siroz.cariboustonks.rendering.CaribouRenderer;
import fr.siroz.cariboustonks.rendering.world.state.TextRenderState;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.TextRenderable;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.CameraRenderState;
import org.joml.Matrix4f;
import org.jspecify.annotations.NonNull;

public final class TextRendererCommand implements RendererCommand<TextRenderState> {

	@Override
	public void emit(@NonNull TextRenderState state, @NonNull CameraRenderState camera) {
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

		state.preparedText().visit(new Font.GlyphVisitor() {
			@Override
			public void acceptGlyph(TextRenderable.@NonNull Styled style) {
				this.draw(style);
			}

			@Override
			public void acceptEffect(@NonNull TextRenderable renderer) {
				this.draw(renderer);
			}

			private void draw(@NonNull TextRenderable renderer) {
				TextureSetup textureSetup = TextureSetup.singleTexture(
						renderer.textureView(),
						RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST)
				);

				BufferBuilder buffer = CaribouRenderer.getBuffer(pipeline, textureSetup);
				renderer.render(matrix4f, buffer, LightTexture.FULL_BRIGHT, false);
			}
		});
	}
}
