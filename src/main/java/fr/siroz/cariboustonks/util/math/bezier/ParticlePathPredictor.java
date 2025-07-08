/*
 * This implementation is adapted from the original code in the project
 * SkyHanni (<a href="https://github.com/hannibal002/SkyHanni">GitHub</a>)
 * a Minecraft 1.8 Mod written in Kotlin, which was licensed under LGPL-2.1
 * <p>
 * Original authors: [SkyHanni Contributors]
 * Adaptations by: [Siroz555]
 */

package fr.siroz.cariboustonks.util.math.bezier;

import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Predicts the path of a particle using Bézier curve fitting techniques.
 */
public class ParticlePathPredictor extends BezierFitter {

	/**
	 * Constructs a new instance with a specified degree for fitting the Bézier curve.
	 * This degree determines the complexity of the curve used to model the particle's path.
	 *
	 * @param degree the degree of the polynomial used in Bézier curve fitting
	 */
	public ParticlePathPredictor(int degree) {
		super(degree);
	}

	/**
	 * Solves for a point on a Bézier curve based on a calculated parameter derived
	 * from the curve's first derivative and control point distance. The method
	 * internally fits a Bézier curve to the given points, computes the start point
	 * derivative, and determines the parameter value using a weighted distance calculation.
	 * If fitting the curve fails, the result is null.
	 *
	 * @return a {@link Vec3d} representing the computed point on the Bézier curve,
	 * or null if the curve fitting process could not generate a valid curve.
	 */
	public @Nullable Vec3d solve() {
		BezierCurve curve = this.fit();
		if (curve == null) {
			return null;
		}

		Vec3d derivative = curve.getDerivative(0.0D);
		double t = 3 * computePitchWeight(derivative) / derivative.length();

		return curve.getPoint(t);
	}

	/**
	 * Siroz Brain:
	 * <p>
	 * Computes the pitch weight based on the given derivative vector. The pitch weight
	 * is calculated using a mathematical formula that involves the pitch angle derived
	 * from the vector and trigonometric operations.
	 */
	double computePitchWeight(@NotNull Vec3d derivative) {
		return Math.sqrt(24 * Math.sin(getPitchFromDerivative(derivative) - Math.PI) + 25);
	}

	/**
	 * Siroz Brain:
	 * <p>
	 * Calculates the pitch angle in radians based on the given derivative vector.
	 * The pitch is determined by analyzing the direction of the vector and performing
	 * an iterative computation using trigonometric functions to refine the result.
	 */
	double getPitchFromDerivative(@NotNull Vec3d derivative) {
		double xzAbscissa = Math.sqrt(Math.pow(derivative.x, 2) + Math.pow(derivative.z, 2));
		double pitchAngle = -Math.atan2(derivative.y, xzAbscissa);
		double guessPitch = pitchAngle;
		double resultPitch = Math.atan2(Math.sin(guessPitch) - 0.75, Math.cos(guessPitch));
		double max = Math.PI / 2;
		double min = -Math.PI / 2;

		for (int i = 0; i < 100; i++) {
			if (resultPitch < pitchAngle) {
				min = guessPitch;
			} else {
				max = guessPitch;
			}

			guessPitch = (min + max) / 2;
			resultPitch = Math.atan2(Math.sin(guessPitch) - 0.75, Math.cos(guessPitch));
			if (resultPitch == pitchAngle) {
				return guessPitch;
			}
		}

		return guessPitch;
	}
}
