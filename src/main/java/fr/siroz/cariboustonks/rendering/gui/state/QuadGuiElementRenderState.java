package fr.siroz.cariboustonks.rendering.gui.state;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import fr.siroz.cariboustonks.util.math.MathUtils;
import fr.siroz.cariboustonks.util.render.gui.Quad;
import java.util.List;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.state.GuiElementRenderState;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public record QuadGuiElementRenderState(
		@NonNull RenderPipeline pipeline,
		@NonNull TextureSetup textureSetup,
		@NonNull List<Quad> quads,
		int color,
		@Nullable ScreenRectangle scissorArea,
		@Nullable ScreenRectangle bounds
) implements GuiElementRenderState {

	public QuadGuiElementRenderState(
			@NonNull RenderPipeline pipeline,
			@NonNull TextureSetup textureSetup,
			@NonNull List<Quad> quads,
			int color,
			@Nullable ScreenRectangle scissorArea
	) {
		this(pipeline, textureSetup, quads, color, scissorArea, createBounds(quads));
	}

	@Override
	public void buildVertices(@NonNull VertexConsumer vertices) {
		for (Quad quad : quads) {
			vertices.addVertex(quad.x1(), quad.y1(), 0).setColor(color);
			vertices.addVertex(quad.x2(), quad.y2(), 0).setColor(color);
			vertices.addVertex(quad.x3(), quad.y3(), 0).setColor(color);
			vertices.addVertex(quad.x4(), quad.y4(), 0).setColor(color);
		}
	}

	private static @NonNull ScreenRectangle createBounds(@NonNull List<Quad> quads) {
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

		return new ScreenRectangle(MathUtils.floor(minX), MathUtils.floor(minY), MathUtils.ceil(maxX - minX), MathUtils.ceil(maxY - minY));
	}
}
