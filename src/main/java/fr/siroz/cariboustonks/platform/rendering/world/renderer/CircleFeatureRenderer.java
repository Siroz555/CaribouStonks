package fr.siroz.cariboustonks.platform.rendering.world.renderer;

import com.mojang.blaze3d.vertex.VertexConsumer;
import fr.siroz.cariboustonks.platform.rendering.CaribouRenderPipelines;
import fr.siroz.cariboustonks.platform.rendering.world.state.CircleRenderState;
import java.util.List;
import net.minecraft.client.renderer.feature.FeatureFrameContext;
import net.minecraft.client.renderer.feature.FeatureRendererType;
import net.minecraft.client.renderer.feature.submit.TranslucentSubmit;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.joml.Matrix4f;
import org.jspecify.annotations.NonNull;

public final class CircleFeatureRenderer extends AbstractFeatureRenderer<CircleFeatureRenderer.Submit> {
	public static final FeatureRendererType<Submit> TYPE = FeatureRendererType.create("CaribouStonks Circle");

	@Override
	protected void buildGroup(FeatureFrameContext context, List<Submit> submits) {
		for (Submit submit : submits) {
			Matrix4f positionMatrix = new Matrix4f()
					.translate((float) -submit.camera().pos.x, (float) -submit.camera().pos.y, (float) -submit.camera().pos.z);

			for (CircleRenderState state : submit.states()) {
				VertexConsumer builder = this.getVertexBuilder(submit.throughBlocks()
						? CaribouRenderPipelines.CIRCLE_THROUGH_BLOCKS
						: CaribouRenderPipelines.CIRCLE
				);

				// 5% du rayon (0.05) | min : 0.01f (trop faible) | max : 0.95 (la quasi-totalité du cercle)
				float thicknessPercent = state.thicknessPercent();
				thicknessPercent = Math.clamp(thicknessPercent, 0.01f, 0.95f);
				float thickness = (float) (state.radius() * thicknessPercent);

				for (int i = 0; i <= state.segments(); i++) {
					double angle = 2.0 * Math.PI * i / state.segments();
					float sin = (float) Math.sin(angle);
					float cos = (float) Math.cos(angle);
					// Vertex extérieur
					float outerX = (float) state.center().x;
					float outerY = (float) state.center().y;
					float outerZ = (float) state.center().z;
					// Vertex intérieur
					float innerX = (float) state.center().x;
					float innerY = (float) state.center().y;
					float innerZ = (float) state.center().z;

					switch (state.axis()) {
						case X -> {
							outerY += (float) ((state.radius() + thickness) * cos);
							outerZ += (float) ((state.radius() + thickness) * sin);
							innerY += (float) ((state.radius() - thickness) * cos);
							innerZ += (float) ((state.radius() - thickness) * sin);
						}
						case Y -> {
							outerX += (float) ((state.radius() + thickness) * cos);
							outerZ += (float) ((state.radius() + thickness) * sin);
							innerX += (float) ((state.radius() - thickness) * cos);
							innerZ += (float) ((state.radius() - thickness) * sin);
						}
						case Z -> {
							outerX += (float) ((state.radius() + thickness) * cos);
							outerY += (float) ((state.radius() + thickness) * sin);
							innerX += (float) ((state.radius() - thickness) * cos);
							innerY += (float) ((state.radius() - thickness) * sin);
						}
						default -> {
						}
					}

					builder.addVertex(positionMatrix, outerX, outerY, outerZ)
							.setColor(state.color().r(), state.color().g(), state.color().b(), state.color().a());

					builder.addVertex(positionMatrix, innerX, innerY, innerZ)
							.setColor(state.color().r(), state.color().g(), state.color().b(), state.color().a());
				}
			}
		}
	}

	public record Submit(List<CircleRenderState> states, CameraRenderState camera, boolean throughBlocks) implements TranslucentSubmit {
		@Override
		public float distanceToCameraSq() {
			return 0;
		}

		@Override
		public @NonNull FeatureRendererType<? extends TranslucentSubmit> featureType() {
			return TYPE;
		}
	}
}
