package fr.siroz.cariboustonks.util.render;

import com.mojang.blaze3d.systems.RenderPass;
import fr.siroz.cariboustonks.util.render.gui.Point;
import fr.siroz.cariboustonks.util.render.gui.state.CustomShapeGuiElementRenderState;
import fr.siroz.cariboustonks.util.render.gui.state.GradientRectGuiElementRenderState;
import java.awt.Color;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gl.ScissorState;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.TextureSetup;
import net.minecraft.client.util.Window;
import net.minecraft.text.Text;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3x2f;
import org.joml.Vector2f;

/**
 * <b>2D</b> rendering in GUIs.
 */
public final class GuiRenderUtils {

	// TODO - Utiliser le CustomShapeGuiElementRenderState pour fix le graphique des prix
	//  en utilisant un autre moyen d'affichage que des lignes?

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
	 * Enqueues a custom-shaped GUI element for rendering using the given {@link DrawContext}.
	 * <p>
	 * This method creates a {@link CustomShapeGuiElementRenderState}.
	 *
	 * @param context  the {@code DrawContext}
	 * @param vertices an ordered list of 2D vertices describing the custom shape.
	 * @param color    the shape color.
	 */
	public static void drawCustomShape(@NotNull DrawContext context, @NotNull List<Vector2f> vertices, int color) {
		CustomShapeGuiElementRenderState renderState = new CustomShapeGuiElementRenderState(
				RenderPipelines.GUI,
				TextureSetup.empty(),
				new Matrix3x2f(context.getMatrices()),
				vertices,
				color,
				context.scissorStack.peekLast()
		);
		context.state.addSimpleElement(renderState);
	}

	public static void renderLinesFromPoints(
			@NotNull DrawContext context,
			@NotNull Point[] points,
			@NotNull Color color,
			float lineWidth
	) {

		if (points.length < 2) return;

		context.drawTextWithShadow(
				MinecraftClient.getInstance().textRenderer,
				Text.literal("Graph is not yet implemented in this version :'(").withColor(color.getRGB()),
				points[0].x() + 5,
				points[0].y() + 50,
				color.getRGB()
		);

//		GL11.glEnable(GL11.GL_LINE_SMOOTH);
//		GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
//		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
//		RenderSystem.lineWidth(lineWidth);
//
//		context.draw(consumerProvider -> {
//			VertexConsumer buffer = consumerProvider.getBuffer(CustomRenderLayers.GUI_LINES);
//
//			MatrixStack matrices = context.getMatrices();
//			for (int i = 0; i < points.length; i++) {
//				Point nextPoint = points[i + 1 == points.length ? i - 1 : i + 1];
//				MatrixStack.Entry entry = matrices.peek();
//				Matrix4f matrix4f = entry.getPositionMatrix();
//				Vec3d normalized = new Vec3d(nextPoint.x() - points[i].x(), nextPoint.y() - points[i].y(), 0).normalize();
//
//				buffer.vertex(matrix4f, (float) points[i].x(), (float) points[i].y(), -1)
//						.color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha())
//						.normal(entry, (float) normalized.x, (float) normalized.y, (float) normalized.z);
//
//				buffer.vertex(matrix4f, (float) nextPoint.x(), (float) nextPoint.y(), -1)
//						.color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha())
//						.normal(entry, (float) normalized.x, (float) normalized.y, (float) normalized.z);
//			}
//		});
//
//		GL11.glDisable(GL11.GL_LINE_SMOOTH);
//		RenderSystem.lineWidth(1f);
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
