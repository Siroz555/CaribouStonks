package fr.siroz.cariboustonks.platform.rendering.world.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.textures.FilterMode;
import fr.siroz.cariboustonks.platform.rendering.world.state.TextRenderState;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.TextRenderable;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.feature.FeatureFrameContext;
import net.minecraft.client.renderer.feature.FeatureRendererType;
import net.minecraft.client.renderer.feature.submit.SubmitNode;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.LightCoordsUtil;
import org.joml.Matrix4f;
import org.jspecify.annotations.NonNull;

public final class TextFeatureRenderer extends AbstractFeatureRenderer<TextFeatureRenderer.Submit> {
	public static final FeatureRendererType<Submit> TYPE = FeatureRendererType.create("CaribouStonks Text");

	@Override
	protected void buildGroup(FeatureFrameContext context, List<Submit> submits) {
		for (Submit submit : submits) {
			for (TextRenderState state : submit.states()) {
				Matrix4f positionMatrix = new Matrix4f()
						.translate((float) (state.pos().x() - submit.camera().pos.x()), (float) (state.pos().y() - submit.camera().pos.y()), (float) (state.pos().z() - submit.camera().pos.z()))
						.rotate(submit.camera().orientation)
						.scale(state.scale(), -state.scale(), state.scale());

				state.preparedText().visit(new Font.GlyphVisitor() {
					@Override
					public void acceptRenderable(@NonNull TextRenderable renderable) {
						TextureSetup textureSetup = TextureSetup.singleTextureWithLightmap(
								renderable.textureView(),
								RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST)
						);

						VertexConsumer builder = TextFeatureRenderer.this.getVertexBuilder(
								getPipeline(submit.throughBlocks(), renderable.guiPipeline() == RenderPipelines.GUI_TEXT_GRAYSCALE),
								textureSetup
						);

						renderable.render(positionMatrix, builder, LightCoordsUtil.FULL_BRIGHT, false);
					}
				});
			}
		}
	}

	private static RenderPipeline getPipeline(boolean seeThrough, boolean greyscale) {
		if (seeThrough) return greyscale ? RenderPipelines.TEXT_GRAYSCALE_SEE_THROUGH : RenderPipelines.TEXT_SEE_THROUGH;
		else return greyscale ? RenderPipelines.TEXT_GRAYSCALE : RenderPipelines.TEXT;
	}

	public record Submit(List<TextRenderState> states, CameraRenderState camera, boolean throughBlocks) implements SubmitNode {
		@Override
		public @NonNull FeatureRendererType<? extends SubmitNode> featureType() {
			return TYPE;
		}
	}
}
