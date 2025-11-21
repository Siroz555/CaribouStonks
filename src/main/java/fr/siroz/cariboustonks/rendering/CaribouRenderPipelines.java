package fr.siroz.cariboustonks.rendering;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat;
import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.core.integration.IrisIntegration;
import net.minecraft.client.renderer.RenderPipelines;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import org.jetbrains.annotations.ApiStatus;

public final class CaribouRenderPipelines {

	private CaribouRenderPipelines() {
	}

	@ApiStatus.Internal
	public static void init() {
		CaribouRenderer.excludePipelineFromBatching(CIRCLE);
		CaribouRenderer.excludePipelineFromBatching(CIRCLE_THROUGH_BLOCKS);
		IrisIntegration.assignPipelines();
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
					.withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
					.build()
	);

	public static final RenderPipeline LINE_STRIP = RenderPipelines.register(
			RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET)
					.withLocation(CaribouStonks.identifier("pipeline/line_strip"))
					.withVertexShader("core/position_color")
					.withFragmentShader("core/position_color")
					.withCull(false)
					.withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.DEBUG_LINE_STRIP)
					.build()
	);

	public static final RenderPipeline LINES_THROUGH_BLOCKS = RenderPipelines.register(
			RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
					.withLocation(CaribouStonks.identifier("pipeline/lines_through_blocks"))
					.withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
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
					.withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
					.withCull(false)
					.build()
	);

	public static final RenderPipeline QUADS_THROUGH_BLOCKS = RenderPipelines.register(
			RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
					.withLocation(CaribouStonks.identifier("pipeline/quads_through_blocks"))
					.withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
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
					.withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
					.withCull(false)
					.build()
	);

	@Deprecated
	public static final RenderPipeline GUI_LINES = RenderPipelines.register(
			RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
					.withLocation(CaribouStonks.identifier("pipeline/gui_lines"))
					.withVertexFormat(DefaultVertexFormat.POSITION_COLOR_NORMAL, VertexFormat.Mode.DEBUG_LINE_STRIP)
					.withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
					.withCull(false)
					.build()
	);

	public static final RenderPipeline GUI_QUADS = RenderPipelines.register(
			RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
					.withLocation(CaribouStonks.identifier("pipeline/gui_quads"))
					.withVertexFormat(DefaultVertexFormat.POSITION_COLOR_NORMAL, VertexFormat.Mode.QUADS)
					.withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
					.build()
	);
}
