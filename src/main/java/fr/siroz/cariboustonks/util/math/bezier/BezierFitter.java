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
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Provides functionality for fitting a 3D Bézier curve to a collection of points.
 * This class uses polynomial fitting for each of the three spatial dimensions (X, Y, Z) independently.
 */
public class BezierFitter {

	private final int degree;
	protected final List<Vec3d> points = new ArrayList<>();
	private final PolynomialFitter[] fitters;
	private BezierCurve lastBezierCurve = null;

	public BezierFitter(int degree) {
		this.degree = degree;
		this.fitters = new PolynomialFitter[]{
				new PolynomialFitter(degree),
				new PolynomialFitter(degree),
				new PolynomialFitter(degree)
		};
	}

	public void addPoint(@NotNull Vec3d point) {
		if (!Double.isFinite(point.x) || !Double.isFinite(point.y) || !Double.isFinite(point.z)) {
			throw new IllegalArgumentException();
		}

		double[] array = toDoubleArray(point);
		IntStream.range(0, fitters.length)
				.forEach(i -> fitters[i].addPoint(points.size(), array[i]));

		points.add(point);
		lastBezierCurve = null;
	}

	public @Nullable Vec3d getLastPoint() {
		return points.isEmpty() ? null : points.getLast();
	}

	public boolean isEmpty() {
		return points.isEmpty();
	}

	public @Nullable BezierCurve fit() {
		if (points.size() <= degree) {
			return null;
		}

		if (lastBezierCurve != null) {
			return lastBezierCurve;
		}

		List<double[]> coefficients = Arrays.stream(fitters)
				.map(PolynomialFitter::fit)
				.toList();

		lastBezierCurve = new BezierCurve(coefficients);
		return lastBezierCurve;
	}

	public void reset() {
		points.clear();
		Arrays.stream(fitters).forEach(PolynomialFitter::reset);
		lastBezierCurve = null;
	}

	@Contract(value = "_ -> new", pure = true)
	private double @NotNull [] toDoubleArray(@NotNull Vec3d vec) {
		return new double[]{vec.x, vec.y, vec.z};
	}
}
