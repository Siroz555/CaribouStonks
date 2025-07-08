package fr.siroz.cariboustonks.util.render;

import it.unimi.dsi.fastutil.doubles.Double2ObjectMap;
import it.unimi.dsi.fastutil.doubles.Double2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayer.MultiPhase;
import net.minecraft.client.render.RenderLayer.MultiPhaseParameters;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.util.Identifier;
import net.minecraft.util.TriState;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.OptionalDouble;
import java.util.function.DoubleFunction;
import java.util.function.Function;

@ApiStatus.Internal
@SuppressWarnings("checkstyle:linelength")
public final class CustomRenderLayers {

	private static final Double2ObjectMap<MultiPhase> LINES_LAYERS = new Double2ObjectOpenHashMap<>();
	private static final Double2ObjectMap<MultiPhase> LINES_THROUGH_BLOCKS_LAYERS = new Double2ObjectOpenHashMap<>();
	private static final Object2ObjectMap<Identifier, MultiPhase> TEXTURE_LAYERS = new Object2ObjectOpenHashMap<>();
	private static final Object2ObjectMap<Identifier, MultiPhase> TEXTURE_THROUGH_BLOCKS_LAYERS = new Object2ObjectOpenHashMap<>();

	private CustomRenderLayers() {
	}

	@ApiStatus.Internal
	public static void init() {
	}

	// ---------- WORLD ----------

	public static final MultiPhase FILLED = RenderLayer.of(
			"filled",
			RenderLayer.DEFAULT_BUFFER_SIZE,
			false,
			true,
			RenderPipelines.DEBUG_FILLED_BOX,
			MultiPhaseParameters.builder()
					.layering(RenderPhase.VIEW_OFFSET_Z_LAYERING)
					.build(false));

	public static final MultiPhase FILLED_THROUGH_BLOCKS = RenderLayer.of(
			"filled_through_blocks",
			RenderLayer.DEFAULT_BUFFER_SIZE,
			false,
			true,
			CustomRenderPipelines.FILLED_THROUGH_BLOCKS,
			MultiPhaseParameters.builder()
					.layering(RenderPhase.VIEW_OFFSET_Z_LAYERING)
					.build(false));

	private static final DoubleFunction<MultiPhase> LINES = lineWidth -> RenderLayer.of(
			"lines",
			RenderLayer.DEFAULT_BUFFER_SIZE,
			false,
			false,
			RenderPipelines.LINES,
			MultiPhaseParameters.builder()
					.lineWidth(new RenderPhase.LineWidth(OptionalDouble.of(lineWidth)))
					.layering(RenderPhase.VIEW_OFFSET_Z_LAYERING)
					.build(false));

	private static final DoubleFunction<MultiPhase> LINES_THROUGH_BLOCKS = lineWidth -> RenderLayer.of(
			"lines_through_blocks",
			RenderLayer.DEFAULT_BUFFER_SIZE,
			false,
			false,
			CustomRenderPipelines.LINES_THROUGH_BLOCKS,
			MultiPhaseParameters.builder()
					.lineWidth(new RenderPhase.LineWidth(OptionalDouble.of(lineWidth)))
					.layering(RenderPhase.VIEW_OFFSET_Z_LAYERING)
					.build(false));

	public static MultiPhase getLines(double lineWidth) {
		return LINES_LAYERS.computeIfAbsent(lineWidth, LINES);
	}

	public static MultiPhase getLinesThroughBlocks(double lineWidth) {
		return LINES_THROUGH_BLOCKS_LAYERS.computeIfAbsent(lineWidth, LINES_THROUGH_BLOCKS);
	}

	// Fix : Le Through Blocks marche, mais sans, les lignes du DEBUG_TRIANGLE_FAN sont présentes
	public static final MultiPhase CIRCLES = RenderLayer.of(
			"circles",
			RenderLayer.DEFAULT_BUFFER_SIZE,
			false,
			true,
			CustomRenderPipelines.CIRCLE, // RenderPipelines.DEBUG_TRIANGLE_FAN,
			MultiPhaseParameters.builder()
					.layering(RenderPhase.VIEW_OFFSET_Z_LAYERING)
					.build(false)
	);

	public static final MultiPhase CIRCLES_THROUGH_BLOCKS = RenderLayer.of(
			"circles_through_blocks",
			RenderLayer.DEFAULT_BUFFER_SIZE,
			false,
			true,
			CustomRenderPipelines.CIRCLE_THROUGH_BLOCKS,
			MultiPhaseParameters.builder()
					.layering(RenderPhase.VIEW_OFFSET_Z_LAYERING)
					.build(false)
	);

	public static final MultiPhase QUADS = RenderLayer.of(
			"quads",
			RenderLayer.DEFAULT_BUFFER_SIZE,
			false,
			true,
			RenderPipelines.DEBUG_QUADS,
			MultiPhaseParameters.builder()
					.layering(RenderPhase.VIEW_OFFSET_Z_LAYERING)
					.build(false));

	public static final MultiPhase QUADS_THROUGH_BLOCKS = RenderLayer.of(
			"quads_through_blocks",
			RenderLayer.DEFAULT_BUFFER_SIZE,
			false,
			true,
			CustomRenderPipelines.QUADS_THROUGH_BLOCKS,
			MultiPhaseParameters.builder()
					.layering(RenderPhase.VIEW_OFFSET_Z_LAYERING)
					.build(false));

	// ---------- TEXTURE ----------

	private static final Function<Identifier, MultiPhase> TEXTURE = texture -> RenderLayer.of(
			"textures",
			RenderLayer.DEFAULT_BUFFER_SIZE,
			false,
			true,
			CustomRenderPipelines.TEXTURE,
			MultiPhaseParameters.builder()
					.texture(new RenderPhase.Texture(texture, TriState.FALSE, false))
					.layering(RenderPhase.VIEW_OFFSET_Z_LAYERING)
					.build(false));

	private static final Function<Identifier, MultiPhase> TEXTURE_THROUGH_BLOCKS = texture -> RenderLayer.of(
			"textures_through_blocks",
			RenderLayer.DEFAULT_BUFFER_SIZE,
			false,
			true,
			CustomRenderPipelines.TEXTURE_THROUGH_BLOCKS,
			MultiPhaseParameters.builder()
					.texture(new RenderPhase.Texture(texture, TriState.FALSE, false))
					.layering(RenderPhase.VIEW_OFFSET_Z_LAYERING)
					.build(false));

	public static MultiPhase getTexture(@NotNull Identifier texture) {
		return TEXTURE_LAYERS.computeIfAbsent(texture, TEXTURE);
	}

	public static MultiPhase getTextureThroughBlocks(@NotNull Identifier texture) {
		return TEXTURE_THROUGH_BLOCKS_LAYERS.computeIfAbsent(texture, TEXTURE_THROUGH_BLOCKS);
	}

	// ---------- GUI ----------

	public static final MultiPhase GUI_LINES = RenderLayer.of(
			"gui_lines",
			RenderLayer.DEFAULT_BUFFER_SIZE,
			CustomRenderPipelines.GUI_LINES,
			MultiPhaseParameters.builder()
					.build(false));

	public static final MultiPhase GUI_QUADS = RenderLayer.of(
			"gui_quads",
			RenderLayer.DEFAULT_BUFFER_SIZE,
			CustomRenderPipelines.GUI_QUADS,
			MultiPhaseParameters.builder()
					.build(false));
}
