package fr.siroz.cariboustonks.platform.rendering.world.renderer;

import com.mojang.blaze3d.vertex.VertexConsumer;
import fr.siroz.cariboustonks.platform.rendering.CaribouRenderPipelines;
import fr.siroz.cariboustonks.platform.rendering.world.state.OutlineBoxRenderState;
import java.util.List;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.feature.FeatureFrameContext;
import net.minecraft.client.renderer.feature.FeatureRendererType;
import net.minecraft.client.renderer.feature.submit.TranslucentSubmit;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.joml.Matrix4f;
import org.jspecify.annotations.NonNull;

public final class OutlineBoxFeatureRenderer extends AbstractFeatureRenderer<OutlineBoxFeatureRenderer.Submit> {
	public static final FeatureRendererType<Submit> TYPE = FeatureRendererType.create("CaribouStonks Outline Box");

	@Override
	protected void buildGroup(FeatureFrameContext context, List<Submit> submits) {
		for (Submit submit : submits) {
			Matrix4f positionMatrix = new Matrix4f()
					.translate((float) -submit.camera().pos.x, (float) -submit.camera().pos.y, (float) -submit.camera().pos.z);

			for (OutlineBoxRenderState state : submit.states()) {
				VertexConsumer builder = this.getVertexBuilder(submit.throughBlocks()
						? CaribouRenderPipelines.LINES_THROUGH_BLOCKS
						: RenderPipelines.LINES
				);

				float minX = (float) state.box().minX;
				float minY = (float) state.box().minY;
				float minZ = (float) state.box().minZ;
				float maxX = (float) state.box().maxX;
				float maxY = (float) state.box().maxY;
				float maxZ = (float) state.box().maxZ;

				float[] colorComponents = state.color().asFloatComponents();
				float red = colorComponents[0];
				float green = colorComponents[1];
				float blue = colorComponents[2];
				float alpha = state.color().getAlpha();

				builder.addVertex(positionMatrix, minX, minY, minZ).setColor(red, green, blue, alpha).setNormal(1.0f, 0.0f, 0.0f).setLineWidth(state.lineWidth());
				builder.addVertex(positionMatrix, maxX, minY, minZ).setColor(red, green, blue, alpha).setNormal(1.0f, 0.0f, 0.0f).setLineWidth(state.lineWidth());
				builder.addVertex(positionMatrix, minX, minY, minZ).setColor(red, green, blue, alpha).setNormal(0.0f, 1.0f, 0.0f).setLineWidth(state.lineWidth());
				builder.addVertex(positionMatrix, minX, maxY, minZ).setColor(red, green, blue, alpha).setNormal(0.0f, 1.0f, 0.0f).setLineWidth(state.lineWidth());
				builder.addVertex(positionMatrix, minX, minY, minZ).setColor(red, green, blue, alpha).setNormal(0.0f, 0.0f, 1.0f).setLineWidth(state.lineWidth());
				builder.addVertex(positionMatrix, minX, minY, maxZ).setColor(red, green, blue, alpha).setNormal(0.0f, 0.0f, 1.0f).setLineWidth(state.lineWidth());
				builder.addVertex(positionMatrix, maxX, minY, minZ).setColor(red, green, blue, alpha).setNormal(0.0f, 1.0f, 0.0f).setLineWidth(state.lineWidth());
				builder.addVertex(positionMatrix, maxX, maxY, minZ).setColor(red, green, blue, alpha).setNormal(0.0f, 1.0f, 0.0f).setLineWidth(state.lineWidth());
				builder.addVertex(positionMatrix, maxX, maxY, minZ).setColor(red, green, blue, alpha).setNormal(-1.0f, 0.0f, 0.0f).setLineWidth(state.lineWidth());
				builder.addVertex(positionMatrix, minX, maxY, minZ).setColor(red, green, blue, alpha).setNormal(-1.0f, 0.0f, 0.0f).setLineWidth(state.lineWidth());
				builder.addVertex(positionMatrix, minX, maxY, minZ).setColor(red, green, blue, alpha).setNormal(0.0f, 0.0f, 1.0f).setLineWidth(state.lineWidth());
				builder.addVertex(positionMatrix, minX, maxY, maxZ).setColor(red, green, blue, alpha).setNormal(0.0f, 0.0f, 1.0f).setLineWidth(state.lineWidth());
				builder.addVertex(positionMatrix, minX, maxY, maxZ).setColor(red, green, blue, alpha).setNormal(0.0f, -1.0f, 0.0f).setLineWidth(state.lineWidth());
				builder.addVertex(positionMatrix, minX, minY, maxZ).setColor(red, green, blue, alpha).setNormal(0.0f, -1.0f, 0.0f).setLineWidth(state.lineWidth());
				builder.addVertex(positionMatrix, minX, minY, maxZ).setColor(red, green, blue, alpha).setNormal(1.0f, 0.0f, 0.0f).setLineWidth(state.lineWidth());
				builder.addVertex(positionMatrix, maxX, minY, maxZ).setColor(red, green, blue, alpha).setNormal(1.0f, 0.0f, 0.0f).setLineWidth(state.lineWidth());
				builder.addVertex(positionMatrix, maxX, minY, maxZ).setColor(red, green, blue, alpha).setNormal(0.0f, 0.0f, -1.0f).setLineWidth(state.lineWidth());
				builder.addVertex(positionMatrix, maxX, minY, minZ).setColor(red, green, blue, alpha).setNormal(0.0f, 0.0f, -1.0f).setLineWidth(state.lineWidth());
				builder.addVertex(positionMatrix, minX, maxY, maxZ).setColor(red, green, blue, alpha).setNormal(1.0f, 0.0f, 0.0f).setLineWidth(state.lineWidth());
				builder.addVertex(positionMatrix, maxX, maxY, maxZ).setColor(red, green, blue, alpha).setNormal(1.0f, 0.0f, 0.0f).setLineWidth(state.lineWidth());
				builder.addVertex(positionMatrix, maxX, minY, maxZ).setColor(red, green, blue, alpha).setNormal(0.0f, 1.0f, 0.0f).setLineWidth(state.lineWidth());
				builder.addVertex(positionMatrix, maxX, maxY, maxZ).setColor(red, green, blue, alpha).setNormal(0.0f, 1.0f, 0.0f).setLineWidth(state.lineWidth());
				builder.addVertex(positionMatrix, maxX, maxY, minZ).setColor(red, green, blue, alpha).setNormal(0.0f, 0.0f, 1.0f).setLineWidth(state.lineWidth());
				builder.addVertex(positionMatrix, maxX, maxY, maxZ).setColor(red, green, blue, alpha).setNormal(0.0f, 0.0f, 1.0f).setLineWidth(state.lineWidth());
			}
		}
	}

	public record Submit(List<OutlineBoxRenderState> states, CameraRenderState camera, boolean throughBlocks) implements TranslucentSubmit {
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
