package fr.siroz.cariboustonks.platform.rendering.world.renderer;

import com.mojang.blaze3d.vertex.VertexConsumer;
import fr.siroz.cariboustonks.platform.rendering.CaribouRenderPipelines;
import fr.siroz.cariboustonks.platform.rendering.world.state.LinesRenderState;
import java.util.List;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.feature.FeatureFrameContext;
import net.minecraft.client.renderer.feature.FeatureRendererType;
import net.minecraft.client.renderer.feature.submit.TranslucentSubmit;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.jspecify.annotations.NonNull;

public final class LinesFeatureRenderer extends AbstractFeatureRenderer<LinesFeatureRenderer.Submit> {
	public static final FeatureRendererType<Submit> TYPE = FeatureRendererType.create("CaribouStonks Lines");

	@Override
	protected void buildGroup(FeatureFrameContext context, List<Submit> submits) {
		for (Submit submit : submits) {
			Matrix4f positionMatrix = new Matrix4f()
					.translate((float) -submit.camera().pos.x, (float) -submit.camera().pos.y, (float) -submit.camera().pos.z);

			for (LinesRenderState state : submit.states()) {

				VertexConsumer builder = this.getVertexBuilder(submit.throughBlocks() ?
						CaribouRenderPipelines.LINES_THROUGH_BLOCKS
						: RenderPipelines.LINES
				);

				Vec3[] points = state.points();
				for (int i = 0; i < points.length; i++) {
					Vec3 nextPoint = points[i + 1 == points.length ? i - 1 : i + 1];
					Vector3f normalVec = nextPoint.toVector3f().sub((float) points[i].x(), (float) points[i].y(), (float) points[i].z()).normalize();

					if (i + 1 == points.length) normalVec.negate();

					builder.addVertex(positionMatrix, (float) points[i].x(), (float) points[i].y(), (float) points[i].z())
							.setColor(state.color().r(), state.color().g(), state.color().b(), state.color().a())
							.setNormal(normalVec.x(), normalVec.y(), normalVec.z())
							.setLineWidth(state.lineWidth());
				}
			}
		}
	}

	public record Submit(List<LinesRenderState> states, CameraRenderState camera, boolean throughBlocks) implements TranslucentSubmit {
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
