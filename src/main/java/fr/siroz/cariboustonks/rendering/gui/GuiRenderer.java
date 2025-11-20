package fr.siroz.cariboustonks.rendering.gui;

import com.mojang.blaze3d.systems.RenderPass;
import fr.siroz.cariboustonks.rendering.CaribouRenderPipelines;
import fr.siroz.cariboustonks.rendering.gui.state.GradientRectGuiElementRenderState;
import fr.siroz.cariboustonks.rendering.gui.state.QuadGuiElementRenderState;
import fr.siroz.cariboustonks.util.render.gui.Point;
import fr.siroz.cariboustonks.util.render.gui.Quad;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gl.ScissorState;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.TextureSetup;
import net.minecraft.client.util.Window;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3x2f;
import org.joml.Vector2f;

public final class GuiRenderer {

	private static final List<Quad> BATCH_QUADS = new ArrayList<>();
	private static final ScissorState BLUR_SCISSOR_STATE = new ScissorState();

	private GuiRenderer() {
	}

	/**
	 * Draws a rectangular border within the specified coordinates and dimensions.
	 *
	 * @param context the {@code DrawContext} used for rendering the border
	 * @param x       the x-coordinate (top-left corner)
	 * @param y       the y-coordinate (top-left corner)
	 * @param width   the width
	 * @param height  the height
	 * @param color   the color
	 */
	public static void drawBorder(@NotNull DrawContext context, int x, int y, int width, int height, int color) {
		context.fill(x, y, x + width, y + 1, color);
		context.fill(x, y + height - 1, x + width, y + height, color);
		context.fill(x, y + 1, x + 1, y + height - 1, color);
		context.fill(x + width - 1, y + 1, x + width, y + height - 1, color);
	}

	/**
	 * Draws a scrollable text within the specified coordinates and dimensions.
	 *
	 * @param context      the {@code DrawContext} used for rendering the text
	 * @param textRenderer the {@code TextRenderer} used for rendering the text
	 * @param text         the text to be rendered
	 * @param startX       the x-coordinate (top-left corner)
	 * @param startY       the y-coordinate (top-left corner)
	 * @param endX         the x-coordinate (bottom-right corner)
	 * @param endY         the y-coordinate (bottom-right corner)
	 * @param color        the color
	 */
	public static void drawScrollableText(@NotNull DrawContext context, @NotNull TextRenderer textRenderer, Text text, int startX, int startY, int endX, int endY, int color) {
		int textWidth = textRenderer.getWidth(text);
		int scrollAreaWidth = startY + endY;
		int offsetY = (scrollAreaWidth - 9) / 2 + 1;
		int textOffset = endX - startX;
		if (textWidth > textOffset) {
			int scrollOffset = textWidth - textOffset;
			double timeOffset = (double) Util.getMeasuringTimeMs() / 1_000;
			double scrollAmount = Math.max((double) scrollOffset * 0.5D, 3.0D);
			double normalizedScrollValue = Math.sin((Math.PI / 2D) * Math.cos((Math.PI * 2D) * timeOffset / scrollAmount)) / 2.0D + 0.5D;
			double textRenderOffset = MathHelper.lerp(normalizedScrollValue, 0.0F, scrollOffset);
			context.enableScissor(startX, startY, endX, endY);
			context.drawTextWithShadow(textRenderer, text, startX - (int) textRenderOffset, offsetY, color);
			context.disableScissor();
		} else {
			int centerX = (startX + endX) / 2;
			int clampedX = MathHelper.clamp(centerX, startX + textWidth / 2, endX - textWidth / 2);
			context.drawCenteredTextWithShadow(textRenderer, text, clampedX, offsetY, color);
		}
	}

	/**
	 * Enqueues a gradient rectangle GUI element for rendering using the given {@link DrawContext}.
	 * <p>
	 * This method creates a {@link GradientRectGuiElementRenderState}.
	 *
	 * @param context    the {@code DrawContext}
	 * @param depth      the depth of the element
	 * @param left       the left position of the rectangle
	 * @param top        the top position of the rectangle
	 * @param right      the right position of the rectangle
	 * @param bottom     the bottom position of the rectangle
	 * @param startColor the start color of the gradient
	 * @param endColor   the end color of the gradient
	 */
	public static void submitGradientRect(@NotNull DrawContext context, int depth, int left, int top, int right, int bottom, int startColor, int endColor) {
		GradientRectGuiElementRenderState renderState = new GradientRectGuiElementRenderState(
				CaribouRenderPipelines.GUI_QUADS, //RenderPipelines.GUI,
				TextureSetup.empty(),
				new Matrix3x2f(context.getMatrices()),
				depth,
				left,
				top,
				right,
				bottom,
				startColor,
				endColor,
				context.scissorStack.peekLast()
		);
		context.state.addSimpleElement(renderState);
	}

	/**
	 * Enqueues a polyline as a series of a quad GUI element for rendering using the given {@link DrawContext}.
	 * <p>
	 * This method creates a {@link QuadGuiElementRenderState}.
	 *
	 * @param context   the {@code DrawContext}
	 * @param points    an array of Points describing the polyline
	 * @param color     the color
	 * @param thickness the thickness of the line in pixels
	 */
	public static void submitLinesFromPoints(
			@NotNull DrawContext context,
			@NotNull Point @NotNull [] points,
			@NotNull Color color,
			int thickness
	) {
		if (points.length < 2) return;

		Matrix3x2f matrix = context.getMatrices();

		for (int i = 0; i < points.length; i++) {
			Point currentPoint = points[i];
			Point nextPoint = points[i + 1 == points.length ? i - 1 : i + 1];
			int startX = currentPoint.x();
			int startY = currentPoint.y();
			int endX = nextPoint.x();
			int endY = nextPoint.y();

			// Calcul du vecteur perpendiculaire (side) à la direction du segment,
			// normalisé et multiplié par (thickness / 2) pour obtenir l'offset des bords.
			Vector2f side = new Vector2f(endX, endY)
					.sub(startX, startY)
					.normalize()
					.perpendicular()
					.mul(thickness / 2f);

			// Coins du quad (avant transformation)
			float x1 = startX + side.x();
			float y1 = startY + side.y();
			float x2 = startX - side.x();
			float y2 = startY - side.y();
			float x3 = endX - side.x();
			float y3 = endY - side.y();
			float x4 = endX + side.x();
			float y4 = endY + side.y();

			Vector2f v1 = matrix.transformPosition(new Vector2f(x1, y1));
			Vector2f v2 = matrix.transformPosition(new Vector2f(x2, y2));
			Vector2f v3 = matrix.transformPosition(new Vector2f(x3, y3));
			Vector2f v4 = matrix.transformPosition(new Vector2f(x4, y4));
			BATCH_QUADS.add(new Quad(v1.x(), v1.y(), v2.x(), v2.y(), v3.x(), v3.y(), v4.x(), v4.y()));
		}

		// Le jeu crash si j'utilise directement les quads,
		// avec un "IllegalStateException : BufferBuilder was empty"
		// Je récupère donc une copy et je clear le batch juste après.
		java.util.List<Quad> batchCopy = List.copyOf(BATCH_QUADS);
		BATCH_QUADS.clear();

		QuadGuiElementRenderState renderState = new QuadGuiElementRenderState(
				RenderPipelines.GUI,
				TextureSetup.empty(),
				batchCopy,
				color.getRGB(),
				context.scissorStack.peekLast()
		);
		context.state.addSimpleElement(renderState);
	}

	public static void enableBlurScissor(int x, int y, int width, int height) {
		BLUR_SCISSOR_STATE.enable(x, y, width, height);
	}

	public static void disableBlurScissor() {
		BLUR_SCISSOR_STATE.disable();
	}

	public static void applyBlurScissorToRenderPass(RenderPass renderPass) {
		if (BLUR_SCISSOR_STATE.isEnabled()) {
			Window window = MinecraftClient.getInstance().getWindow();
			int framebufferHeight = window.getFramebufferHeight();
			double scaleFactor = window.getScaleFactor();

			double x = BLUR_SCISSOR_STATE.getX() * scaleFactor;
			double y = framebufferHeight - (BLUR_SCISSOR_STATE.getY() + BLUR_SCISSOR_STATE.getHeight()) * scaleFactor;
			double width = BLUR_SCISSOR_STATE.getWidth() * scaleFactor;
			double height = BLUR_SCISSOR_STATE.getHeight() * scaleFactor;

			renderPass.enableScissor((int) x, (int) y, Math.max(0, (int) width), Math.max(0, (int) height));
		}
	}
}
