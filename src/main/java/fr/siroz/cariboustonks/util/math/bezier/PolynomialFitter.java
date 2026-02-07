package fr.siroz.cariboustonks.util.math.bezier;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * The PolynomialFitter class is used to fit a polynomial of a specified degree to a set of points.
 * It uses the method of fewest squares to determine the coefficients of the polynomial.
 */
public class PolynomialFitter {

	private final int degree;
	private final List<double[]> xValues = new ArrayList<>();
	private final List<double[]> yValues = new ArrayList<>();

	public PolynomialFitter(int degree) {
		this.degree = degree;
	}

	public void addPoint(double x, double y) {
		yValues.add(new double[]{y});
		double[] xArray = IntStream.range(0, degree + 1)
				.mapToDouble(i -> Math.pow(x, i))
				.toArray();
		xValues.add(xArray);
	}

	public double[] fit() {
		Matrix xMatrix = new Matrix(xValues.toArray(new double[0][0]));
		Matrix yMatrix = new Matrix(yValues.toArray(new double[0][0]));
		Matrix xMatrixTransposed = xMatrix.transpose();

		return xMatrixTransposed.multiply(xMatrix)
				.inverse()
				.multiply(xMatrixTransposed)
				.multiply(yMatrix)
				.transpose()
				.getRow(0);
	}

	public void reset() {
		xValues.clear();
		yValues.clear();
	}
}
