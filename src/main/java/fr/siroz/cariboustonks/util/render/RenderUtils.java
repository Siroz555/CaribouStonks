package fr.siroz.cariboustonks.util.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

public final class RenderUtils {

	public static final int MAX_BUILD_HEIGHT = 300;

	private RenderUtils() {
	}

	/**
	 * Creates a {@link PoseStack} from the given {@link Matrix4f}.
	 *
	 * @param matrix4f the {@link Matrix4f}
	 */
	public static @NotNull PoseStack matrixToStack(Matrix4f matrix4f) {
		PoseStack matrices = new PoseStack();
		matrices.last().pose().set(matrix4f);
		return matrices;
	}

	public static @NotNull DeltaTracker getTickCounter() {
		return Minecraft.getInstance().getDeltaTracker();
	}

	public static @NotNull Camera getCamera() {
		return Minecraft.getInstance().gameRenderer.getMainCamera();
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
}
