package fr.siroz.cariboustonks.platform.rendering.world.renderer;

import com.mojang.blaze3d.vertex.VertexConsumer;
import fr.siroz.cariboustonks.platform.rendering.CaribouRenderPipelines;
import fr.siroz.cariboustonks.platform.rendering.world.state.CursorLineRenderState;
import java.util.List;
import net.minecraft.client.renderer.feature.FeatureFrameContext;
import net.minecraft.client.renderer.feature.FeatureRendererType;
import net.minecraft.client.renderer.feature.submit.SubmitNode;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.jspecify.annotations.NonNull;

public final class CursorLineFeatureRenderer extends AbstractFeatureRenderer<CursorLineFeatureRenderer.Submit> {
	public static final FeatureRendererType<Submit> TYPE = FeatureRendererType.create("CaribouStonks Cursor Line");

	@Override
	protected void buildGroup(FeatureFrameContext context, List<Submit> submits) {
		for (Submit submit : submits) {
			Matrix4f positionMatrix = new Matrix4f()
					.translate((float) -submit.camera().pos.x, (float) -submit.camera().pos.y, (float) -submit.camera().pos.z);

			for (CursorLineRenderState state : submit.states()) {
				VertexConsumer builder = this.getVertexBuilder(CaribouRenderPipelines.LINES_THROUGH_BLOCKS);

				Vec3 point = state.point();
				Vec3 cameraPoint = submit.camera().pos.add(new Vec3(submit.camera().orientation.transform(new Vector3f(0, 0, -1))));
				Vector3f normal = point.toVector3f().sub((float) cameraPoint.x, (float) cameraPoint.y, (float) cameraPoint.z).normalize();

				builder.addVertex(positionMatrix, (float) cameraPoint.x, (float) cameraPoint.y, (float) cameraPoint.z)
						.setColor(state.color().r(), state.color().g(), state.color().b(), state.color().a())
						.setNormal(normal.x(), normal.y(), normal.z())
						.setLineWidth(state.lineWidth());

				builder.addVertex(positionMatrix, (float) point.x(), (float) point.y(), (float) point.z())
						.setColor(state.color().r(), state.color().g(), state.color().b(), state.color().a())
						.setNormal(normal.x(), normal.y(), normal.z())
						.setLineWidth(state.lineWidth());
			}
		}
	}

	public record Submit(List<CursorLineRenderState> states, CameraRenderState camera) implements SubmitNode {
		@Override
		public @NonNull FeatureRendererType<? extends SubmitNode> featureType() {
			return TYPE;
		}
	}
}
