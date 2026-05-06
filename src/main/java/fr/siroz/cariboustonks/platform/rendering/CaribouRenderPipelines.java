package fr.siroz.cariboustonks.platform.rendering;

import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.core.mod.integration.IrisIntegration;
import fr.siroz.cariboustonks.platform.rendering.world.WorldRenderer;
import java.util.Optional;
import net.minecraft.client.renderer.RenderPipelines;

public final class CaribouRenderPipelines {

	private CaribouRenderPipelines() {
	}

	public static void bootstrap() {
		// Enregistre les pipelines pour Iris
		IrisIntegration.assignPipelines();
		// Les cercles sont exclus.
		WorldRenderer.excludePipelineFromBatching(CIRCLE);
		WorldRenderer.excludePipelineFromBatching(CIRCLE_THROUGH_BLOCKS);
	}

	public static final RenderPipeline FILLED = RenderPipelines.register(
			RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
					.withLocation(CaribouStonks.identifier("pipeline/filled_box"))
					.withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLE_STRIP)
					.build()
	);

	public static final RenderPipeline FILLED_THROUGH_BLOCKS = RenderPipelines.register(
			RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
					.withLocation(CaribouStonks.identifier("pipeline/filled_box_through_blocks"))
					.withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLE_STRIP)
					.withDepthStencilState(Optional.empty())
					.build()
	);

	public static final RenderPipeline LINE_STRIP = RenderPipelines.register(
			RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET)
					.withLocation(CaribouStonks.identifier("pipeline/line_strip"))
					.withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, false))
					.withVertexShader("core/position_color")
					.withFragmentShader("core/position_color")
					.withCull(false)
					.withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.DEBUG_LINE_STRIP)
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
					.withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLE_STRIP)
					.withCull(false)
					.build()
	);

	public static final RenderPipeline CIRCLE_THROUGH_BLOCKS = RenderPipelines.register(
			RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
					.withLocation(CaribouStonks.identifier("pipeline/circles_through_blocks"))
					.withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLE_STRIP)
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

	@Deprecated
	public static final RenderPipeline GUI_LINES = RenderPipelines.register(
			RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
					.withLocation(CaribouStonks.identifier("pipeline/gui_lines"))
					.withVertexFormat(DefaultVertexFormat.POSITION_COLOR_NORMAL, VertexFormat.Mode.DEBUG_LINE_STRIP)
					.withDepthStencilState(Optional.empty())
					.withCull(false)
					.build()
	);

	public static final RenderPipeline GUI_QUADS = RenderPipelines.register(
			RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
					.withLocation(CaribouStonks.identifier("pipeline/gui_quads"))
					.withVertexFormat(DefaultVertexFormat.POSITION_COLOR_NORMAL, VertexFormat.Mode.QUADS)
					.withDepthStencilState(Optional.empty())
					.build()
	);
}
