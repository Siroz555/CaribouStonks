package fr.siroz.cariboustonks.rendering.gui.state;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import fr.siroz.cariboustonks.util.math.MathUtils;
import fr.siroz.cariboustonks.util.render.gui.Quad;
import java.util.List;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.SimpleGuiElementRenderState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.texture.TextureSetup;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record QuadGuiElementRenderState(
		@NotNull RenderPipeline pipeline,
		@NotNull TextureSetup textureSetup,
		@NotNull List<Quad> quads,
		int color,
		@Nullable ScreenRect scissorArea,
		@Nullable ScreenRect bounds
) implements SimpleGuiElementRenderState {

	public QuadGuiElementRenderState(
			@NotNull RenderPipeline pipeline,
			@NotNull TextureSetup textureSetup,
			@NotNull List<Quad> quads,
			int color,
			@Nullable ScreenRect scissorArea
	) {
		this(pipeline, textureSetup, quads, color, scissorArea, createBounds(quads));
	}

	@Override
	public void setupVertices(VertexConsumer vertices) {
		for (Quad quad : quads) {
			vertices.vertex(quad.x1(), quad.y1(), 0).color(color);
			vertices.vertex(quad.x2(), quad.y2(), 0).color(color);
			vertices.vertex(quad.x3(), quad.y3(), 0).color(color);
			vertices.vertex(quad.x4(), quad.y4(), 0).color(color);
		}
	}

	@Contract("_ -> new")
	private static @NotNull ScreenRect createBounds(@NotNull List<Quad> quads) {
		float minX = Float.POSITIVE_INFINITY;
		float minY = Float.POSITIVE_INFINITY;
		float maxX = Float.NEGATIVE_INFINITY;
		float maxY = Float.NEGATIVE_INFINITY;

		for (Quad quad : quads) {
			// Min X
			minX = Math.min(minX, quad.x1());
			minX = Math.min(minX, quad.x2());
			minX = Math.min(minX, quad.x3());
			minX = Math.min(minX, quad.x4());
			// mIN y
			minY = Math.min(minY, quad.y1());
			minY = Math.min(minY, quad.y2());
			minY = Math.min(minY, quad.y3());
			minY = Math.min(minY, quad.y4());
			// Max X
			maxX = Math.max(maxX, quad.x1());
			maxX = Math.max(maxX, quad.x2());
			maxX = Math.max(maxX, quad.x3());
			maxX = Math.max(maxX, quad.x4());
			// Max Y
			maxY = Math.max(maxY, quad.y1());
			maxY = Math.max(maxY, quad.y2());
			maxY = Math.max(maxY, quad.y3());
			maxY = Math.max(maxY, quad.y4());
		}

		return new ScreenRect(MathUtils.floor(minX), MathUtils.floor(minY), MathUtils.ceil(maxX - minX), MathUtils.ceil(maxY - minY));
	}
}
