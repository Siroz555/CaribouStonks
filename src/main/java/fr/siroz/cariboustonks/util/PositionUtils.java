package fr.siroz.cariboustonks.util;

import net.minecraft.util.math.Position;
import org.jetbrains.annotations.NotNull;

/**
 * Utility class for handling position-based calculations.
 */
public final class PositionUtils {

	private PositionUtils() {
	}

	/**
	 * Calculates the 2D distance between two positions, ignoring the Y coordinate.
	 * <p>
	 * {@code Note à moi-même} : Math.pow() est généralement moins efficace que
	 * la multiplication directe pour élever au carré.
	 *
	 * @param a the first position
	 * @param b the second position
	 * @return the 2D distance between the two positions
	 * @see #distanceToIgnoringY(Position, Position)
	 * @see #squaredDistanceToIgnoringY(Position, Position)
	 */
	public static double distanceIgnoringY(@NotNull Position a, @NotNull Position b) {
		return Math.sqrt(Math.pow(a.getX() - b.getX(), 2) + Math.pow(a.getZ() - b.getZ(), 2));
	}

	/**
	 * Calculates the distance between two positions, ignoring the Y coordinate.
	 *
	 * @param from the starting position
	 * @param to   the destination position
	 * @return the calculated 2D distance between the two positions
	 */
	public static double distanceToIgnoringY(@NotNull Position from, @NotNull Position to) {
		double dx = to.getX() - from.getX();
		double dz = to.getZ() - from.getZ();
		return Math.sqrt(dx * dx + dz * dz);
	}

	/**
	 * Calculates the squared distance between two positions, ignoring their Y coordinates.
	 *
	 * @param from the starting position
	 * @param to   the destination position
	 * @return the squared distance between the two positions
	 */
	public static double squaredDistanceToIgnoringY(@NotNull Position from, @NotNull Position to) {
		double dx = from.getX() - to.getX();
		double dz = from.getZ() - to.getZ();
		return dx * dx + dz * dz;
	}
}
