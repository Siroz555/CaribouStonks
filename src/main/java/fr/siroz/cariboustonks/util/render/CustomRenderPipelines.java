package fr.siroz.cariboustonks.util.render;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat;
import fr.siroz.cariboustonks.CaribouStonks;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.VertexFormats;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class CustomRenderPipelines {

	private CustomRenderPipelines() {
	}

	@ApiStatus.Internal
	public static void init() {
		Renderer.getInstance().excludePipelineFromBatching(CIRCLE);
		Renderer.getInstance().excludePipelineFromBatching(CIRCLE_THROUGH_BLOCKS);
	}

	// ---------- WORLD ----------

	public static final RenderPipeline FILLED_THROUGH_BLOCKS = RenderPipelines.register(
			RenderPipeline.builder(RenderPipelines.POSITION_COLOR_SNIPPET)
					.withLocation(CaribouStonks.identifier("pipeline/filled_box_through_blocks"))
					.withVertexFormat(VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.TRIANGLE_STRIP)
					.withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
					.build());

	public static final RenderPipeline LINES_THROUGH_BLOCKS = RenderPipelines.register(
			RenderPipeline.builder(RenderPipelines.RENDERTYPE_LINES_SNIPPET)
					.withLocation(CaribouStonks.identifier("pipeline/lines_through_blocks"))
					.withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
					.build());

	// Fix : Le Through Blocks marche, mais sans, les lignes du DEBUG_TRIANGLE_FAN sont présentes
	public static final RenderPipeline CIRCLE = RenderPipelines.register(
			RenderPipeline.builder(RenderPipelines.POSITION_COLOR_SNIPPET)
					.withLocation(CaribouStonks.identifier("pipeline/circles"))
					.withVertexFormat(VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.TRIANGLE_STRIP)
					.withCull(false)
					.build());

	public static final RenderPipeline CIRCLE_THROUGH_BLOCKS = RenderPipelines.register(
			RenderPipeline.builder(RenderPipelines.POSITION_COLOR_SNIPPET)
					.withLocation(CaribouStonks.identifier("pipeline/circles_through_blocks"))
					.withVertexFormat(VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.TRIANGLE_STRIP)
					.withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
					.withCull(false)
					.build());

	public static final RenderPipeline QUADS_THROUGH_BLOCKS = RenderPipelines.register(
			RenderPipeline.builder(RenderPipelines.POSITION_COLOR_SNIPPET)
					.withLocation(CaribouStonks.identifier("pipeline/quads_through_blocks"))
					.withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
					.withCull(false)
					.build());

	public static final RenderPipeline TEXTURE = RenderPipelines.register(
			RenderPipeline.builder(RenderPipelines.POSITION_TEX_COLOR_SNIPPET)
					.withLocation(CaribouStonks.identifier("pipeline/textures"))
					.withCull(false)
					.build());

	public static final RenderPipeline TEXTURE_THROUGH_BLOCKS = RenderPipelines.register(
			RenderPipeline.builder(RenderPipelines.POSITION_TEX_COLOR_SNIPPET)
					.withLocation(CaribouStonks.identifier("pipeline/textures_through_blocks"))
					.withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
					.withCull(false)
					.build());

	// ---------- GUI ----------

	public static final RenderPipeline GUI_LINES = RenderPipelines.register(
			RenderPipeline.builder(RenderPipelines.RENDERTYPE_LINES_SNIPPET)
					.withLocation(CaribouStonks.identifier("pipeline/gui_lines"))
					.withVertexFormat(VertexFormats.POSITION_COLOR_NORMAL, VertexFormat.DrawMode.LINE_STRIP)
					.withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
					.withCull(false)
					.build());

	public static final RenderPipeline GUI_QUADS = RenderPipelines.register(
			RenderPipeline.builder(RenderPipelines.POSITION_COLOR_SNIPPET)
					.withLocation(CaribouStonks.identifier("pipeline/gui_quads"))
					.withVertexFormat(VertexFormats.POSITION_COLOR_NORMAL, VertexFormat.DrawMode.QUADS)
					.withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
					.build());
}
