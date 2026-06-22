package fr.siroz.cariboustonks.platform.rendering;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.core.mod.integration.IrisIntegration;
import java.util.Optional;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.client.renderer.RenderPipelines;

public final class CaribouRenderPipelines {

	private CaribouRenderPipelines() {
	}

	public static void bootstrap() {
		// Enregistre les pipelines pour Iris
		IrisIntegration.assignPipelines();
	}

	public static final RenderPipeline BEAM = RenderPipelines.register(
			RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
					.withLocation(CaribouStonks.identifier("pipeline/beam"))
					.withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR)
					.withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
					.withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, false))
					.withCull(false)
					.build()
	);

	public static final RenderPipeline BEAM_THROUGH_BLOCKS = RenderPipelines.register(
			RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
					.withLocation(CaribouStonks.identifier("pipeline/beam_through_blocks"))
					.withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR)
					.withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
					.withDepthStencilState(Optional.empty())
					.withCull(false)
					.build()
	);

	public static final RenderPipeline FILLED = RenderPipelines.register(
			RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
					.withLocation(CaribouStonks.identifier("pipeline/filled_box"))
					.withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR)
					.withPrimitiveTopology(PrimitiveTopology.TRIANGLE_STRIP)
					.build()
	);

	public static final RenderPipeline FILLED_THROUGH_BLOCKS = RenderPipelines.register(
			RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
					.withLocation(CaribouStonks.identifier("pipeline/filled_box_through_blocks"))
					.withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR)
					.withPrimitiveTopology(PrimitiveTopology.TRIANGLE_STRIP)
					.withDepthStencilState(Optional.empty())
					.build()
	);

	public static final RenderPipeline LINE_STRIP = RenderPipelines.register(
			RenderPipeline.builder()
					.withBindGroupLayout(BindGroupLayouts.MATRICES_PROJECTION)
					.withLocation(CaribouStonks.identifier("pipeline/line_strip"))
					// SIROZ-NOTE: 26.1 : LESS_THAN_OR_EQUAL | 26.2 GREATER_THAN_OR_EQUAL | pourquoi ?
					.withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, false))
					.withVertexShader("core/position_color")
					.withFragmentShader("core/position_color")
					.withCull(false)
					.withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR)
					.withPrimitiveTopology(PrimitiveTopology.DEBUG_LINE_STRIP)
					.build()
	);

	public static final RenderPipeline LINES_THROUGH_BLOCKS = RenderPipelines.register(
			RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
					.withLocation(CaribouStonks.identifier("pipeline/lines_through_blocks"))
					.withDepthStencilState(Optional.empty())
					.build()
	);

	public static final RenderPipeline CIRCLE = RenderPipelines.register(
			RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
					.withLocation(CaribouStonks.identifier("pipeline/circles"))
					.withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR)
					.withPrimitiveTopology(PrimitiveTopology.TRIANGLE_STRIP)
					.withCull(false)
					.build()
	);

	public static final RenderPipeline CIRCLE_THROUGH_BLOCKS = RenderPipelines.register(
			RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
					.withLocation(CaribouStonks.identifier("pipeline/circles_through_blocks"))
					.withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR)
					.withPrimitiveTopology(PrimitiveTopology.TRIANGLE_STRIP)
					.withDepthStencilState(Optional.empty())
					.withCull(false)
					.build()
	);

	public static final RenderPipeline QUADS_THROUGH_BLOCKS = RenderPipelines.register(
			RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
					.withLocation(CaribouStonks.identifier("pipeline/quads_through_blocks"))
					.withDepthStencilState(Optional.empty())
					.withCull(false)
					.build()
	);

	public static final RenderPipeline TEXTURE = RenderPipelines.register(
			RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
					.withLocation(CaribouStonks.identifier("pipeline/textures"))
					.withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, false))
					.withCull(false)
					.build()
	);

	public static final RenderPipeline TEXTURE_THROUGH_BLOCKS = RenderPipelines.register(
			RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
					.withLocation(CaribouStonks.identifier("pipeline/textures_through_blocks"))
					.withDepthStencilState(Optional.empty())
					.withCull(false)
					.build()
	);

	public static final RenderPipeline GUI_QUADS = RenderPipelines.register(
			RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
					.withLocation(CaribouStonks.identifier("pipeline/gui_quads"))
					.withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR_NORMAL)
					.withPrimitiveTopology(PrimitiveTopology.QUADS)
					.withDepthStencilState(Optional.empty())
					.build()
	);
}
