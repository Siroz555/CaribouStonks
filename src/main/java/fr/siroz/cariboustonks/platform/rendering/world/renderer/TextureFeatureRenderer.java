package fr.siroz.cariboustonks.platform.rendering.world.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.renderpearl.api.textures.FilterMode;
import fr.siroz.cariboustonks.platform.rendering.CaribouRenderPipelines;
import fr.siroz.cariboustonks.platform.rendering.world.state.TextureRenderState;
import java.util.List;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.feature.FeatureFrameContext;
import net.minecraft.client.renderer.feature.FeatureRendererType;
import net.minecraft.client.renderer.feature.submit.SubmitNode;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.joml.Matrix4f;
import org.jspecify.annotations.NonNull;

public final class TextureFeatureRenderer extends AbstractFeatureRenderer<TextureFeatureRenderer.Submit> {
	public static final FeatureRendererType<Submit> TYPE = FeatureRendererType.create("CaribouStonks Texture");

	@Override
	protected void buildGroup(FeatureFrameContext context, List<Submit> submits) {
		for (Submit submit : submits) {
			for (TextureRenderState state : submit.states()) {
				VertexConsumer builder = this.getVertexBuilder(
						submit.throughBlocks() ? CaribouRenderPipelines.TEXTURE_THROUGH_BLOCKS : CaribouRenderPipelines.TEXTURE,
						TextureSetup.singleTexture(
								context.textureManager().getTexture(state.texture()).getTextureView(),
								RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST)
						)
				);

				Matrix4f positionMatrix = new Matrix4f()
						.translate((float) (state.pos().x() - submit.camera().pos.x()), (float) (state.pos().y() - submit.camera().pos.y()), (float) (state.pos().z() - submit.camera().pos.z()))
						.rotate(submit.camera().orientation);

				float u0 = state.u();
				float v0 = state.v();
				float u1 = u0 + state.textureWidth();
				float v1 = v0 + state.textureHeight();

				float[] colorComponents = state.color().asFloatComponents();

				builder.addVertex(positionMatrix, (float) state.renderOffset().x(), (float) state.renderOffset().y(), (float) state.renderOffset().z())
						.setUv(u0, v1)
						.setColor(colorComponents[0], colorComponents[1], colorComponents[2], state.alpha());

				builder.addVertex(positionMatrix, (float) state.renderOffset().x(), (float) state.renderOffset().y() + state.height(), (float) state.renderOffset().z())
						.setUv(u0, v0)
						.setColor(colorComponents[0], colorComponents[1], colorComponents[2], state.alpha());

				builder.addVertex(positionMatrix, (float) state.renderOffset().x() + state.width(), (float) state.renderOffset().y() + state.height(), (float) state.renderOffset().z())
						.setUv(u1, v0)
						.setColor(colorComponents[0], colorComponents[1], colorComponents[2], state.alpha());

				builder.addVertex(positionMatrix, (float) state.renderOffset().x() + state.width(), (float) state.renderOffset().y(), (float) state.renderOffset().z())
						.setUv(u1, v1)
						.setColor(colorComponents[0], colorComponents[1], colorComponents[2], state.alpha());
			}
		}
	}

	public record Submit(List<TextureRenderState> states, CameraRenderState camera, boolean throughBlocks) implements SubmitNode {
		@Override
		public @NonNull FeatureRendererType<? extends SubmitNode> featureType() {
			return TYPE;
		}
	}
}
