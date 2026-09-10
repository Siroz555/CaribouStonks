package fr.siroz.cariboustonks.platform.rendering.world.renderer;

import com.mojang.blaze3d.vertex.VertexConsumer;
import fr.siroz.cariboustonks.platform.rendering.CaribouRenderPipelines;
import fr.siroz.cariboustonks.platform.rendering.world.state.BeamRenderState;
import java.util.List;
import net.minecraft.client.renderer.feature.FeatureFrameContext;
import net.minecraft.client.renderer.feature.FeatureRendererType;
import net.minecraft.client.renderer.feature.submit.SubmitNode;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.joml.Matrix4f;
import org.jspecify.annotations.NonNull;

public final class BeamFeatureRenderer extends AbstractFeatureRenderer<BeamFeatureRenderer.Submit> {
	public static final FeatureRendererType<Submit> TYPE = FeatureRendererType.create("CaribouStonks Beam");
	/**
	 * Extérieur → intérieur : {demi-largeur en blocks, facteur alpha}.
	 */
	private static final float[][] LAYERS = {
			{0.15f, 0.12f}, // halo externe — large, transparent
			{0.07f, 0.30f}, // glow intermédiaire
			{0.025f, 0.75f}, // couche interne
			{0.010f, 1.00f}, // cœur central — étroit, opaque
	};

	@Override
	protected void buildGroup(FeatureFrameContext context, List<Submit> submits) {
		for (Submit submit : submits) {
			Matrix4f positionMatrix = new Matrix4f()
					.translate((float) -submit.camera().pos.x, (float) -submit.camera().pos.y, (float) -submit.camera().pos.z);

			for (BeamRenderState state : submit.states()) {
				VertexConsumer builder = this.getVertexBuilder(submit.throughBlocks()
						? CaribouRenderPipelines.BEAM_THROUGH_BLOCKS
						: CaribouRenderPipelines.BEAM
				);

				float[] colorComponents = state.color().asFloatComponents();
				float r = colorComponents[0];
				float g = colorComponents[1];
				float b = colorComponents[2];
				float baseAlpha = state.color().getAlpha();

				double bx = state.pos().x();
				double by = state.pos().y();
				double bz = state.pos().z();
				double topY = by + state.height();

				for (float[] layer : LAYERS) {
					float hw = layer[0] * state.widthScale(); // half-width
					float a = layer[1] * baseAlpha;
					// Quad selon l'axe X (varie en X, fixe en Z)
					buildGradientQuad(builder, positionMatrix,
							bx - hw, by, bz, // BL
							bx + hw, by, bz, // BR
							bx - hw, topY, bz, // TL
							bx + hw, topY, bz, // TR
							r, g, b, a
					);
					// Quad selon l'axe Z — croisé avec le précédent pour l'effet 3D
					buildGradientQuad(builder, positionMatrix,
							bx, by, bz - hw, // BL
							bx, by, bz + hw, // BR
							bx, topY, bz - hw, // TL
							bx, topY, bz + hw, // TR
							r, g, b, a
					);
				}
			}
		}
	}

	private void buildGradientQuad(
			@NonNull VertexConsumer builder, @NonNull Matrix4f matrix,
			double blX, double blY, double blZ,
			double brX, double brY, double brZ,
			double tlX, double tlY, double tlZ,
			double trX, double trY, double trZ,
			float r, float g, float b, float a
	) {
		// Triangle 1 : bas-gauche (BL), bas-droite (BR), haut-gauche (TL)
		builder.addVertex(matrix, (float) blX, (float) blY, (float) blZ).setColor(r, g, b, a);
		builder.addVertex(matrix, (float) brX, (float) brY, (float) brZ).setColor(r, g, b, a);
		builder.addVertex(matrix, (float) tlX, (float) tlY, (float) tlZ).setColor(r, g, b, 0.0f);
		// Triangle 2 : bas-droite (BR), haut-droite (TR), haut-gauche (TL)
		builder.addVertex(matrix, (float) brX, (float) brY, (float) brZ).setColor(r, g, b, a);
		builder.addVertex(matrix, (float) trX, (float) trY, (float) trZ).setColor(r, g, b, 0.0f);
		builder.addVertex(matrix, (float) tlX, (float) tlY, (float) tlZ).setColor(r, g, b, 0.0f);
	}

	public record Submit(List<BeamRenderState> states, CameraRenderState camera, boolean throughBlocks) implements SubmitNode {
		@Override
		public @NonNull FeatureRendererType<? extends SubmitNode> featureType() {
			return TYPE;
		}
	}
}
