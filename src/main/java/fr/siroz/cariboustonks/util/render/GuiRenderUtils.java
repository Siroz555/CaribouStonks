package fr.siroz.cariboustonks.util.render;

import com.mojang.blaze3d.systems.RenderSystem;
import fr.siroz.cariboustonks.util.render.gui.Point;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.awt.Color;

// TODO - cleanup & removes
public final class GuiRenderUtils {

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
	 * Determines whether the mouse cursor is within a rectangular area on the screen.
	 *
	 * @param mouseX the x-coordinate of the mouse cursor
	 * @param mouseY the y-coordinate of the mouse cursor
	 * @param x      the x-coordinate of the top-left corner
	 * @param y      the y-coordinate of the top-left corner
	 * @param width  the width of the rectangle
	 * @param height the height of the rectangle
	 * @return {@code true} if the mouse cursor is within the specified rectangle
	 */
	public static boolean mouseIsInArea(double mouseX, double mouseY, int x, int y, int width, int height) {
		return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
	}

	public static void renderLinesFromPoints(
			@NotNull DrawContext context,
			@NotNull Point[] points,
			@NotNull Color color,
			float lineWidth
	) {
		GL11.glEnable(GL11.GL_LINE_SMOOTH);
		GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		RenderSystem.lineWidth(lineWidth);

		context.draw(consumerProvider -> {
			VertexConsumer buffer = consumerProvider.getBuffer(CustomRenderLayers.GUI_LINES);

			MatrixStack matrices = context.getMatrices();
			for (int i = 0; i < points.length; i++) {
				Point nextPoint = points[i + 1 == points.length ? i - 1 : i + 1];
				MatrixStack.Entry entry = matrices.peek();
				Matrix4f matrix4f = entry.getPositionMatrix();
				Vec3d normalized = new Vec3d(nextPoint.x() - points[i].x(), nextPoint.y() - points[i].y(), 0).normalize();

				buffer.vertex(matrix4f, (float) points[i].x(), (float) points[i].y(), -1)
						.color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha())
						.normal(entry, (float) normalized.x, (float) normalized.y, (float) normalized.z);

				buffer.vertex(matrix4f, (float) nextPoint.x(), (float) nextPoint.y(), -1)
						.color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha())
						.normal(entry, (float) normalized.x, (float) normalized.y, (float) normalized.z);
			}
		});

		GL11.glDisable(GL11.GL_LINE_SMOOTH);
		RenderSystem.lineWidth(1f);
	}

	public static void drawGradientRect(
			@NotNull DrawContext context,
			@NotNull Matrix4f matrix4f,
			int zLevel,
			int left,
			int top,
			int right,
			int bottom,
			int startColor,
			int endColor
	) {
		float startAlpha = (float) (startColor >> 24 & 255) / 255.0F;
		float startRed = (float) (startColor >> 16 & 255) / 255.0F;
		float startGreen = (float) (startColor >> 8 & 255) / 255.0F;
		float startBlue = (float) (startColor & 255) / 255.0F;
		float endAlpha = (float) (endColor >> 24 & 255) / 255.0F;
		float endRed = (float) (endColor >> 16 & 255) / 255.0F;
		float endGreen = (float) (endColor >> 8 & 255) / 255.0F;
		float endBlue = (float) (endColor & 255) / 255.0F;

		context.draw(consumerProvider -> {
			VertexConsumer buffer = consumerProvider.getBuffer(CustomRenderLayers.GUI_QUADS);
			buffer.vertex(matrix4f, right, top, zLevel)
					.normal(right, top, zLevel)
					.color(startRed, startGreen, startBlue, startAlpha);

			buffer.vertex(matrix4f, left, top, zLevel)
					.normal(right, top, zLevel)
					.color(startRed, startGreen, startBlue, startAlpha);

			buffer.vertex(matrix4f, left, bottom, zLevel)
					.normal(right, top, zLevel)
					.color(endRed, endGreen, endBlue, endAlpha);

			buffer.vertex(matrix4f, right, bottom, zLevel)
					.normal(right, top, zLevel)
					.color(endRed, endGreen, endBlue, endAlpha);
		});
	}
}
