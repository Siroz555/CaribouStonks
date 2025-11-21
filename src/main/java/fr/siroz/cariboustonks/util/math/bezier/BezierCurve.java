/*
 * This implementation is adapted from the original code in the project
 * SkyHanni (<a href="https://github.com/hannibal002/SkyHanni">GitHub</a>)
 * a Minecraft 1.8 Mod written in Kotlin, which was licensed under LGPL-2.1
 * <p>
 * Original authors: [SkyHanni Contributors]
 * Adaptations by: [Siroz555]
 */

package fr.siroz.cariboustonks.util.math.bezier;

import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.IntStream;

/**
 * Represents a 3D Bézier curve with associated coefficients for each dimension.
 * This class provides methods to compute a point on the curve and the derivative
 * at a given parameter value.
 */
public class BezierCurve {

	private final List<double[]> coefficients;

	public BezierCurve(@NotNull List<double[]> coefficients) {
		if (coefficients.size() != 3) {
			throw new IllegalArgumentException();
		}

		this.coefficients = coefficients;
	}

	/**
	 * Computes the derivative of the 3-dimensional Bézier curve at a given parameter value.
	 *
	 * @param t the parameter value at which to evaluate the derivative
	 * @return a {@link Vec3}
	 */
	public Vec3 getDerivative(double t) {
		double x = 0.0D;
		double y = 0.0D;
		double z = 0.0D;

		for (int dim = 0; dim < 3; dim++) {
			double result = 0.0D;
			double[] coefficient = coefficients.get(dim);
			double[] reversed = reverseArray(coefficient);

			for (int i = 0; i < reversed.length - 1; i++) {
				result = result * t + reversed[i] * (reversed.length - 1 - i);
			}

			if (dim == 0) {
				x = result;
			} else if (dim == 1) {
				y = result;
			} else {
				z = result;
			}
		}

		return new Vec3(x, y, z);
	}

	/**
	 * Computes a point on the 3-dimensional Bézier curve at the specified parameter value.
	 *
	 * @param t the parameter value at which to evaluate the curve
	 * @return a {@link Vec3}
	 */
	public Vec3 getPoint(double t) {
		double x = 0.0D;
		double y = 0.0D;
		double z = 0.0D;

		for (int dim = 0; dim < 3; dim++) {
			double result = 0.0D;
			double[] coefficient = coefficients.get(dim);
			double[] reversed = reverseArray(coefficient);

			for (double value : reversed) {
				result = result * t + value;
			}

			if (dim == 0) {
				x = result;
			} else if (dim == 1) {
				y = result;
			} else {
				z = result;
			}
		}

		return new Vec3(x, y, z);
	}

	private double[] reverseArray(double @NotNull [] array) {
		return IntStream.range(0, array.length)
				.mapToDouble(i -> array[array.length - 1 - i])
				.toArray();
	}
}
