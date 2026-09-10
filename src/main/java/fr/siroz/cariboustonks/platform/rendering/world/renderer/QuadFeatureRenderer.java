package fr.siroz.cariboustonks.platform.rendering.world.renderer;

import com.mojang.blaze3d.vertex.VertexConsumer;
import fr.siroz.cariboustonks.platform.rendering.CaribouRenderPipelines;
import fr.siroz.cariboustonks.platform.rendering.world.state.QuadRenderState;
import java.util.List;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.feature.FeatureFrameContext;
import net.minecraft.client.renderer.feature.FeatureRendererType;
import net.minecraft.client.renderer.feature.submit.SubmitNode;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.jspecify.annotations.NonNull;

public final class QuadFeatureRenderer extends AbstractFeatureRenderer<QuadFeatureRenderer.Submit> {
	public static final FeatureRendererType<Submit> TYPE = FeatureRendererType.create("CaribouStonks Quad");

	@Override
	protected void buildGroup(FeatureFrameContext context, List<Submit> submits) {
		for (Submit submit : submits) {
			Matrix4f positionMatrix = new Matrix4f()
					.translate((float) -submit.camera().pos.x, (float) -submit.camera().pos.y, (float) -submit.camera().pos.z);

			for (QuadRenderState state : submit.states()) {
				VertexConsumer builder = this.getVertexBuilder(submit.throughBlocks()
						? CaribouRenderPipelines.QUADS_THROUGH_BLOCKS
						: RenderPipelines.DEBUG_QUADS
				);

				Vec3[] points = state.points();
				float[] colorComponents = state.color().asFloatComponents();

				for (int i = 0; i < 4; i++) {
					builder.addVertex(positionMatrix, (float) points[i].x(), (float) points[i].y(), (float) points[i].z())
							.setColor(colorComponents[0], colorComponents[1], colorComponents[2], state.color().getAlpha());
				}
			}
		}
	}

	public record Submit(List<QuadRenderState> states, CameraRenderState camera, boolean throughBlocks) implements SubmitNode {
		@Override
		public @NonNull FeatureRendererType<? extends SubmitNode> featureType() {
			return TYPE;
		}
	}
}
