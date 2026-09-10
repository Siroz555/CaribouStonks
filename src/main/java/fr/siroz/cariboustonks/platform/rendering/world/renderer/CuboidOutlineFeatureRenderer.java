package fr.siroz.cariboustonks.platform.rendering.world.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import fr.siroz.cariboustonks.core.annotation.Experimental;
import fr.siroz.cariboustonks.platform.rendering.CaribouRenderPipelines;
import fr.siroz.cariboustonks.platform.rendering.world.state.CuboidOutlineRenderState;
import fr.siroz.cariboustonks.util.render.RenderUtils;
import java.util.List;
import net.minecraft.client.renderer.feature.FeatureFrameContext;
import net.minecraft.client.renderer.feature.FeatureRendererType;
import net.minecraft.client.renderer.feature.submit.SubmitNode;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.joml.Matrix4f;
import org.jspecify.annotations.NonNull;

@Experimental
public final class CuboidOutlineFeatureRenderer extends AbstractFeatureRenderer<CuboidOutlineFeatureRenderer.Submit> {
	public static final FeatureRendererType<Submit> TYPE = FeatureRendererType.create("CaribouStonks Cuboid");

	// Pour le moment, c'est juste pour les Garden Plot.
	// C'est globalement le même code qu'avant dans le InfestedPlotRenderer.
	// Il faudra le rendre plus libre, notamment au sujet du "depth".

	@Override
	protected void buildGroup(FeatureFrameContext context, List<Submit> submits) {
		for (Submit submit : submits) {
			Matrix4f positionMatrix = new Matrix4f()
					.translate((float) -submit.camera().pos.x, (float) -submit.camera().pos.y, (float) -submit.camera().pos.z);

			for (CuboidOutlineRenderState state : submit.states()) {
				VertexConsumer builder = this.getVertexBuilder(CaribouRenderPipelines.LINE_STRIP);

				PoseStack matrices = RenderUtils.matrixToStack(positionMatrix);
				PoseStack.Pose entry = matrices.last();

				double chunkX = Math.floor((state.center().x + state.depth()) / state.size());
				double chunkZ = Math.floor((state.center().z + state.depth()) / state.size());

				float chunkMinX = (float) ((float) (chunkX * state.size() - state.depth()));
				float chunkMinZ = (float) ((float) (chunkZ * state.size() - state.depth()));

				float y1 = (float) (state.minY());
				float y2 = (float) (state.maxY());

				for (int i = 0; i <= state.size(); i += state.size()) {
					for (int j = 0; j <= state.size(); j += state.size()) {
						float x = chunkMinX + i;
						float z = chunkMinZ + j;

						builder.addVertex(positionMatrix, x, y1, z)
								.setColor(1.0F, 0.0F, 0.0F, 0.0F)
								.setNormal(entry, x, y1, z)
								.setLineWidth(state.lineWidth());

						builder.addVertex(entry, x, y1, z)
								.setColor(1.0F, 0.0F, 0.0F, 0.5F)
								.setNormal(entry, x, y1, z)
								.setLineWidth(state.lineWidth());

						builder.addVertex(entry, x, y2, z)
								.setColor(1.0F, 0.0F, 0.0F, 0.5F)
								.setNormal(entry, x, y2, z)
								.setLineWidth(state.lineWidth());

						builder.addVertex(entry, x, y2, z)
								.setColor(1.0F, 0.0F, 0.0F, 0.0F)
								.setNormal(entry, x, y2, z)
								.setLineWidth(state.lineWidth());
					}
				}

				for (int i = state.minY(); i <= state.maxY() + 1; i += 2) {
					float y = (float) ((double) i);
					int color = i % 8 == 0 ? state.mainColor().asInt() : state.secondColor().asInt();

					builder.addVertex(entry, chunkMinX, y, chunkMinZ)
							.setColor(1.0F, 1.0F, 0.0F, 0.0F)
							.setNormal(entry, chunkMinX, y, chunkMinZ)
							.setLineWidth(state.lineWidth());

					builder.addVertex(entry, chunkMinX, y, chunkMinZ)
							.setColor(color)
							.setNormal(entry, chunkMinX, y, chunkMinZ)
							.setLineWidth(state.lineWidth());

					builder.addVertex(entry, chunkMinX, y, chunkMinZ + state.size())
							.setColor(color).
							setNormal(entry, chunkMinX, y, chunkMinZ + state.size())
							.setLineWidth(state.lineWidth());

					builder.addVertex(entry, chunkMinX + state.size(), y, chunkMinZ + state.size())
							.setColor(color)
							.setNormal(entry, chunkMinX + state.size(), y, chunkMinZ + state.size())
							.setLineWidth(state.lineWidth());

					builder.addVertex(entry, chunkMinX + state.size(), y, chunkMinZ)
							.setColor(color)
							.setNormal(entry, chunkMinX + state.size(), y, chunkMinZ)
							.setLineWidth(state.lineWidth());

					builder.addVertex(entry, chunkMinX, y, chunkMinZ)
							.setColor(color)
							.setNormal(entry, chunkMinX, y, chunkMinZ)
							.setLineWidth(state.lineWidth());

					builder.addVertex(entry, chunkMinX, y, chunkMinZ)
							.setColor(1.0F, 1.0F, 0.0F, 0.0F)
							.setNormal(entry, chunkMinX, y, chunkMinZ)
							.setLineWidth(state.lineWidth());
				}
			}
		}
	}

	public record Submit(List<CuboidOutlineRenderState> states, CameraRenderState camera) implements SubmitNode {
		@Override
		public @NonNull FeatureRendererType<? extends SubmitNode> featureType() {
			return TYPE;
		}
	}
}
