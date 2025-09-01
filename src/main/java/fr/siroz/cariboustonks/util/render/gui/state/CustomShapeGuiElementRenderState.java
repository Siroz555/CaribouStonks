package fr.siroz.cariboustonks.util.render.gui.state;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.SimpleGuiElementRenderState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.texture.TextureSetup;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;
import org.joml.Vector2f;

public record CustomShapeGuiElementRenderState(
		@NotNull RenderPipeline pipeline,
		@NotNull TextureSetup textureSetup,
		@NotNull Matrix3x2f matrix,
		@NotNull List<Vector2f> vertices,
		int color,
		@Nullable ScreenRect scissorArea,
		@Nullable ScreenRect bounds
) implements SimpleGuiElementRenderState {

	public CustomShapeGuiElementRenderState(
			@NotNull RenderPipeline pipeline,
			@NotNull TextureSetup textureSetup,
			@NotNull Matrix3x2f pose,
			@NotNull List<Vector2f> vertices,
			int color,
			@Nullable ScreenRect scissorArea
	) {
		this(pipeline, textureSetup, pose, vertices, color, scissorArea, createBounds(vertices, pose, scissorArea));
	}

	@Override
	public void setupVertices(VertexConsumer vertices, float depth) {
		for (Vector2f vector : this.vertices) {
			vertices.vertex(this.matrix(), vector.x(), vector.y(), depth).color(color);
		}
	}

	@Nullable
	private static ScreenRect createBounds(@NotNull List<Vector2f> vertices, Matrix3x2f pose, @Nullable ScreenRect scissorArea) {
		int x0 = Collections.max(vertices.stream().map(vertex -> (int) vertex.x).toList());
		int x1 = Collections.min(vertices.stream().map(vertex -> (int) vertex.x).toList());
		int y0 = Collections.max(vertices.stream().map(vertex -> (int) vertex.y).toList());
		int y1 = Collections.min(vertices.stream().map(vertex -> (int) vertex.y).toList());
		ScreenRect screenRect = new ScreenRect(x0, y0, x1 - x0, y1 - y0).transformEachVertex(pose);
		return scissorArea != null ? scissorArea.intersection(screenRect) : screenRect;
	}
}
