package fr.siroz.cariboustonks.platform.rendering.world.renderer;

import com.mojang.blaze3d.vertex.VertexConsumer;
import fr.siroz.cariboustonks.platform.rendering.CaribouRenderPipelines;
import fr.siroz.cariboustonks.platform.rendering.world.state.FilledBoxRenderState;
import java.util.List;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.feature.FeatureFrameContext;
import net.minecraft.client.renderer.feature.FeatureRendererType;
import net.minecraft.client.renderer.feature.submit.TranslucentSubmit;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.joml.Matrix4f;
import org.jspecify.annotations.NonNull;

public final class FilledBoxFeatureRenderer extends AbstractFeatureRenderer<FilledBoxFeatureRenderer.Submit> {
	public static final FeatureRendererType<Submit> TYPE = FeatureRendererType.create("CaribouStonks Filled Box");

	@Override
	protected void buildGroup(FeatureFrameContext context, List<Submit> submits) {
		for (Submit submit : submits) {
			Matrix4f positionMatrix = new Matrix4f()
					.translate((float) -submit.camera().pos.x, (float) -submit.camera().pos.y, (float) -submit.camera().pos.z);

			for (FilledBoxRenderState state : submit.states()) {
				// SIROZ-NOTE:: Encore des pt1 de triangle
				VertexConsumer builder = this.getVertexBuilder(submit.throughBlocks()
						? CaribouRenderPipelines.FILLED_THROUGH_BLOCKS
						: RenderPipelines.DEBUG_FILLED_BOX // CaribouRenderPipelines.FILLED
				);

				float minX = (float) state.minX();
				float minY = (float) state.minY();
				float minZ = (float) state.minZ();
				float maxX = (float) state.maxX();
				float maxY = (float) state.maxY();
				float maxZ = (float) state.maxZ();

				float[] colorComponents = state.color().asFloatComponents();
				float red = colorComponents[0];
				float green = colorComponents[1];
				float blue = colorComponents[2];
				float alpha = state.color().getAlpha();

				// Front
				builder.addVertex(positionMatrix, minX, minY, maxZ).setColor(red, green, blue, alpha);
				builder.addVertex(positionMatrix, maxX, minY, maxZ).setColor(red, green, blue, alpha);
				builder.addVertex(positionMatrix, maxX, maxY, maxZ).setColor(red, green, blue, alpha);
				builder.addVertex(positionMatrix, minX, maxY, maxZ).setColor(red, green, blue, alpha);
				// Back
				builder.addVertex(positionMatrix, maxX, minY, minZ).setColor(red, green, blue, alpha);
				builder.addVertex(positionMatrix, minX, minY, minZ).setColor(red, green, blue, alpha);
				builder.addVertex(positionMatrix, minX, maxY, minZ).setColor(red, green, blue, alpha);
				builder.addVertex(positionMatrix, maxX, maxY, minZ).setColor(red, green, blue, alpha);
				// Left
				builder.addVertex(positionMatrix, minX, minY, minZ).setColor(red, green, blue, alpha);
				builder.addVertex(positionMatrix, minX, minY, maxZ).setColor(red, green, blue, alpha);
				builder.addVertex(positionMatrix, minX, maxY, maxZ).setColor(red, green, blue, alpha);
				builder.addVertex(positionMatrix, minX, maxY, minZ).setColor(red, green, blue, alpha);
				// Right
				builder.addVertex(positionMatrix, maxX, minY, maxZ).setColor(red, green, blue, alpha);
				builder.addVertex(positionMatrix, maxX, minY, minZ).setColor(red, green, blue, alpha);
				builder.addVertex(positionMatrix, maxX, maxY, minZ).setColor(red, green, blue, alpha);
				builder.addVertex(positionMatrix, maxX, maxY, maxZ).setColor(red, green, blue, alpha);
				// Top
				builder.addVertex(positionMatrix, minX, maxY, maxZ).setColor(red, green, blue, alpha);
				builder.addVertex(positionMatrix, maxX, maxY, maxZ).setColor(red, green, blue, alpha);
				builder.addVertex(positionMatrix, maxX, maxY, minZ).setColor(red, green, blue, alpha);
				builder.addVertex(positionMatrix, minX, maxY, minZ).setColor(red, green, blue, alpha);
				// Bottom
				builder.addVertex(positionMatrix, minX, minY, minZ).setColor(red, green, blue, alpha);
				builder.addVertex(positionMatrix, maxX, minY, minZ).setColor(red, green, blue, alpha);
				builder.addVertex(positionMatrix, maxX, minY, maxZ).setColor(red, green, blue, alpha);
				builder.addVertex(positionMatrix, minX, minY, maxZ).setColor(red, green, blue, alpha);
			}
		}
	}

	public record Submit(List<FilledBoxRenderState> states, CameraRenderState camera, boolean throughBlocks) implements TranslucentSubmit {
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
