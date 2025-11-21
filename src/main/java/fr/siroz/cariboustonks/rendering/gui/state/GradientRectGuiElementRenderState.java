package fr.siroz.cariboustonks.rendering.gui.state;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.state.GuiElementRenderState;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.render.TextureSetup;
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
		@Nullable ScreenRectangle scissorArea,
		@Nullable ScreenRectangle bounds
) implements GuiElementRenderState {

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
			@Nullable ScreenRectangle scissorArea
	) {
		this(pipeline, textureSetup, pose, depth, left, top, right, bottom, startColor, endColor, scissorArea,
				createBounds(left, top, right, bottom, pose, scissorArea));
	}

	@Override
	public void buildVertices(@NotNull VertexConsumer vertices) {
		float startAlpha = (float) (startColor >> 24 & 255) / 255.0F;
		float startRed = (float) (startColor >> 16 & 255) / 255.0F;
		float startGreen = (float) (startColor >> 8 & 255) / 255.0F;
		float startBlue = (float) (startColor & 255) / 255.0F;
		float endAlpha = (float) (endColor >> 24 & 255) / 255.0F;
		float endRed = (float) (endColor >> 16 & 255) / 255.0F;
		float endGreen = (float) (endColor >> 8 & 255) / 255.0F;
		float endBlue = (float) (endColor & 255) / 255.0F;

		vertices.addVertexWith2DPose(this.matrix, right, top)
				.setNormal(right, top, depth)
				.setColor(startRed, startGreen, startBlue, startAlpha);

		vertices.addVertexWith2DPose(this.matrix, left, top)
				.setNormal(right, top, depth)
				.setColor(startRed, startGreen, startBlue, startAlpha);

		vertices.addVertexWith2DPose(this.matrix, left, bottom)
				.setNormal(right, top, depth)
				.setColor(endRed, endGreen, endBlue, endAlpha);

		vertices.addVertexWith2DPose(this.matrix, right, bottom)
				.setNormal(right, top, depth)
				.setColor(endRed, endGreen, endBlue, endAlpha);
	}

	@Nullable
	private static ScreenRectangle createBounds(int x0, int y0, int x1, int y1, Matrix3x2f pose, @Nullable ScreenRectangle scissorArea) {
		ScreenRectangle screenRect = new ScreenRectangle(x0, y0, x1 - x0, y1 - y0).transformMaxBounds(pose);
		return scissorArea != null ? scissorArea.intersection(screenRect) : screenRect;
	}
}
