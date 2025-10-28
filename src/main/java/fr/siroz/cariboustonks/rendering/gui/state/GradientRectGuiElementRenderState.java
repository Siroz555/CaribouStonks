package fr.siroz.cariboustonks.rendering.gui.state;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.SimpleGuiElementRenderState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.texture.TextureSetup;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

public record GradientRectGuiElementRenderState(
		@NotNull RenderPipeline pipeline,
		@NotNull TextureSetup textureSetup,
		@NotNull Matrix3x2f matrix,
		int depth,
		int left,
		int top,
		int right,
		int bottom,
		int startColor,
		int endColor,
		@Nullable ScreenRect scissorArea,
		@Nullable ScreenRect bounds
) implements SimpleGuiElementRenderState {

	public GradientRectGuiElementRenderState(
			@NotNull RenderPipeline pipeline,
			@NotNull TextureSetup textureSetup,
			@NotNull Matrix3x2f pose,
			int depth,
			int left,
			int top,
			int right,
			int bottom,
			int startColor,
			int endColor,
			@Nullable ScreenRect scissorArea
	) {
		this(pipeline, textureSetup, pose, depth, left, top, right, bottom, startColor, endColor, scissorArea,
				createBounds(left, top, right, bottom, pose, scissorArea));
	}

	@Override
	public void setupVertices(@NotNull VertexConsumer vertices, float v) {
		float startAlpha = (float) (startColor >> 24 & 255) / 255.0F;
		float startRed = (float) (startColor >> 16 & 255) / 255.0F;
		float startGreen = (float) (startColor >> 8 & 255) / 255.0F;
		float startBlue = (float) (startColor & 255) / 255.0F;
		float endAlpha = (float) (endColor >> 24 & 255) / 255.0F;
		float endRed = (float) (endColor >> 16 & 255) / 255.0F;
		float endGreen = (float) (endColor >> 8 & 255) / 255.0F;
		float endBlue = (float) (endColor & 255) / 255.0F;

		vertices.vertex(this.matrix, right, top, depth)
				.normal(right, top, depth)
				.color(startRed, startGreen, startBlue, startAlpha);

		vertices.vertex(this.matrix, left, top, depth)
				.normal(right, top, depth)
				.color(startRed, startGreen, startBlue, startAlpha);

		vertices.vertex(this.matrix, left, bottom, depth)
				.normal(right, top, depth)
				.color(endRed, endGreen, endBlue, endAlpha);

		vertices.vertex(this.matrix, right, bottom, depth)
				.normal(right, top, depth)
				.color(endRed, endGreen, endBlue, endAlpha);
	}

	@Nullable
	private static ScreenRect createBounds(int x0, int y0, int x1, int y1, Matrix3x2f pose, @Nullable ScreenRect scissorArea) {
		ScreenRect screenRect = new ScreenRect(x0, y0, x1 - x0, y1 - y0).transformEachVertex(pose);
		return scissorArea != null ? scissorArea.intersection(screenRect) : screenRect;
	}
}
