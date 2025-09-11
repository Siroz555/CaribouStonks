package fr.siroz.cariboustonks.util.render;

import com.mojang.blaze3d.systems.RenderPass;
import fr.siroz.cariboustonks.util.render.gui.Point;
import fr.siroz.cariboustonks.util.render.gui.Quad;
import fr.siroz.cariboustonks.util.render.gui.state.GradientRectGuiElementRenderState;
import fr.siroz.cariboustonks.util.render.gui.state.QuadGuiElementRenderState;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gl.ScissorState;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.TextureSetup;
import net.minecraft.client.util.Window;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3x2f;
import org.joml.Vector2f;

/**
 * <b>2D</b> rendering in GUIs.
 */
public final class GuiRenderUtils {

	private static final List<Quad> BATCH_QUADS = new ArrayList<>();
	private static final ScissorState BLUR_SCISSOR_STATE = new ScissorState();

	private GuiRenderUtils() {
	}

	/**
	 * Determines whether a point is within a rectangular area defined by two corners.
	 *
	 * @param x  the x-coordinate of the point to check
	 * @param y  the y-coordinate of the point to check
	 * @param x1 the x-coordinate of the first corner
	 * @param y1 the y-coordinate of the first corner
	 * @param x2 the x-coordinate of the opposite corner
	 * @param y2 the y-coordinate of the opposite corner
	 * @return {@code true} if the point (x, y) is within the area defined by (x1, y1) and (x2, y2), inclusive
	 */
	public static boolean pointIsInArea(double x, double y, double x1, double y1, double x2, double y2) {
		return x >= x1 && x <= x2 && y >= y1 && y <= y2;
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
	public static void drawGradientRect(@NotNull DrawContext context, int depth, int left, int top, int right, int bottom, int startColor, int endColor) {
		GradientRectGuiElementRenderState renderState = new GradientRectGuiElementRenderState(
				CustomRenderPipelines.GUI_QUADS, //RenderPipelines.GUI,
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
	 * Enqueues a polyline as a series of quads GUI element for rendering using the given {@link DrawContext}.
	 * <p>
	 * This method creates a {@link QuadGuiElementRenderState}.
	 *
	 * @param context   the {@code DrawContext}
	 * @param points    an array of Points describing the polyline
	 * @param color     the color
	 * @param thickness the thickness of the line in pixels
	 */
	public static void renderLinesFromPoints(
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
		List<Quad> batchCopy = List.copyOf(BATCH_QUADS);
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

	@ApiStatus.Internal
	public static void disableBlurScissor() {
		BLUR_SCISSOR_STATE.disable();
	}

	@ApiStatus.Internal
	public static void applyBlurScissorToRenderPass(RenderPass renderPass) {
		if (BLUR_SCISSOR_STATE.method_72091()) {
			Window window = MinecraftClient.getInstance().getWindow();
			int framebufferHeight = window.getFramebufferHeight();
			double scaleFactor = window.getScaleFactor();

			double x = BLUR_SCISSOR_STATE.method_72092() * scaleFactor;
			double y = framebufferHeight - (BLUR_SCISSOR_STATE.method_72093() + BLUR_SCISSOR_STATE.method_72095()) * scaleFactor;
			double width = BLUR_SCISSOR_STATE.method_72094() * scaleFactor;
			double height = BLUR_SCISSOR_STATE.method_72095() * scaleFactor;

			renderPass.enableScissor((int) x, (int) y, Math.max(0, (int) width), Math.max(0, (int) height));
		}
	}
}
