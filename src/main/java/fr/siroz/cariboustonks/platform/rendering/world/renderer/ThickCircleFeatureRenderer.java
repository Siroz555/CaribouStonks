package fr.siroz.cariboustonks.platform.rendering.world.renderer;

import com.mojang.blaze3d.vertex.VertexConsumer;
import fr.siroz.cariboustonks.platform.rendering.CaribouRenderPipelines;
import fr.siroz.cariboustonks.platform.rendering.world.state.ThickCircleRenderState;
import java.util.List;
import net.minecraft.client.renderer.feature.FeatureFrameContext;
import net.minecraft.client.renderer.feature.FeatureRendererType;
import net.minecraft.client.renderer.feature.submit.SubmitNode;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.jspecify.annotations.NonNull;

public final class ThickCircleFeatureRenderer extends AbstractFeatureRenderer<ThickCircleFeatureRenderer.Submit> {
	public static final FeatureRendererType<Submit> TYPE = FeatureRendererType.create("CaribouStonks Thick Circle");

	@Override
	protected void buildGroup(FeatureFrameContext context, List<Submit> submits) {
		for (Submit submit : submits) {
			Matrix4f positionMatrix = new Matrix4f()
					.translate((float) -submit.camera().pos.x, (float) -submit.camera().pos.y, (float) -submit.camera().pos.z);

			for (ThickCircleRenderState state : submit.states()) {
				VertexConsumer builder = this.getVertexBuilder(submit.throughBlocks()
						? CaribouRenderPipelines.CIRCLE_THROUGH_BLOCKS
						: CaribouRenderPipelines.CIRCLE
				);

				Vec3 centerTopPos = state.center().add(0, state.thickness(), 0);

				for (int i = 0; i <= state.segments(); i++) {
					double angle = 2 * Math.PI * i / state.segments();
					float cos = (float) Math.cos(angle);
					float sin = (float) Math.sin(angle);
					double v = (state.center().x + state.radius() * cos);
					double v1 = (state.center().z + state.radius() * sin);
					// Vertex inférieur
					float xLower = (float) v;
					float zLower = (float) v1;
					// Vertex supérieur
					float xUpper = (float) v;
					float zUpper = (float) v1;

					builder.addVertex(positionMatrix, xUpper, (float) centerTopPos.y(), zUpper)
							.setColor(state.color().r(), state.color().g(), state.color().b(), state.color().a());

					builder.addVertex(positionMatrix, xLower, (float) state.center().y(), zLower)
							.setColor(state.color().r(), state.color().g(), state.color().b(), state.color().a());
				}
			}
		}
	}

	public record Submit(List<ThickCircleRenderState> states, CameraRenderState camera, boolean throughBlocks) implements SubmitNode {
		@Override
		public @NonNull FeatureRendererType<? extends SubmitNode> featureType() {
			return TYPE;
		}
	}
}
