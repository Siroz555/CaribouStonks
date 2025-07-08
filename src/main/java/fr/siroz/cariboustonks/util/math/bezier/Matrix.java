/*
 * This implementation is adapted from the original code in the project
 * SkyHanni (<a href="https://github.com/hannibal002/SkyHanni">GitHub</a>)
 * a Minecraft 1.8 Mod written in Kotlin, which was licensed under LGPL-2.1
 * <p>
 * Original authors: [SkyHanni Contributors]
 * Adaptations by: [Siroz555]
 */

package fr.siroz.cariboustonks.util.math.bezier;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.ToDoubleBiFunction;
import java.util.stream.Collectors;

/**
 * A Matrix implementation optimized for {@code Bézier curve} calculations.
 * <a href="https://fr.wikipedia.org/wiki/Courbe_de_B%C3%A9zier">Wikipedia Bézier Curve</a>
 * <p>
 * Provides a comprehensive set of matrix operations with a row-major storage format,
 * with matrix inversion algorithm using {@code Gauss-Jordan} elimination.
 *
 * @see BezierCurve
 * @see BezierFitter
 */
public class Matrix {

	private final double[][] data;
	private final int width;
	private final int height;

	/**
	 * Constructs a new matrix from a 2D array of doubles.
	 *
	 * @param data The 2D array of doubles to initialize the matrix with
	 * @throws IllegalArgumentException if the data array is empty or if rows have inconsistent lengths
	 */
	public Matrix(double[] @NotNull [] data) {
		if (data.length == 0) {
			throw new IllegalArgumentException("Matrix can't be empty");
		}

		this.width = data[0].length;
		this.height = data.length;

		for (double[] row : data) {
			if (row.length != this.width) {
				throw new IllegalArgumentException("All rows must have the same length");
			}
		}

		this.data = deepCopy(data);
	}

	/**
	 * Returns a copy of the specified row.
	 *
	 * @param index the row index
	 * @return a copy of the specified row
	 */
	public double[] getRow(int index) {
		return data[index].clone();
	}

	/**
	 * Gets the value at the specified position.
	 *
	 * @param row the row index
	 * @param col the column index
	 * @return the value at the specified position
	 */
	public double get(int row, int col) {
		return data[row][col];
	}

	/**
	 * Creates a deep copy of this matrix.
	 *
	 * @return a new matrix with the same values
	 */
	public Matrix copy() {
		return new Matrix(deepCopy(data));
	}

	/**
	 * Computes the inverse of this matrix using Gauss-Jordan elimination with pivoting.
	 * <p>
	 * This implementation uses partial pivoting for improved numerical stability compared
	 * to standard Gaussian elimination. The algorithm selects the largest absolute value
	 * in each column as the pivot, reducing the accumulation of round-off errors.
	 *
	 * @return the inverse of this matrix
	 * @throws IllegalArgumentException if the matrix is not square or is singular
	 */
	public Matrix inverse() {
		if (!(width == height)) {
			throw new IllegalArgumentException("Matrix must be square");
		}

		double[][] a = deepCopy(data);
		double[][] b = identity(width).data;

		for (int c = 0; c < width; c++) {
			// Trouver la ligne avec la valeur absolue maximale dans la colonne 'C'
			int rBig = c;
			double maxVal = Math.abs(a[c][c]);

			for (int r = c + 1; r < height; r++) {
				double absVal = Math.abs(a[r][c]);
				if (absVal > maxVal) {
					maxVal = absVal;
					rBig = r;
				}
			}

			if (maxVal == 0.0D) {
				throw new IllegalArgumentException("Cannot invert matrix");
			}

			// Échanger les lignes si nécessaire
			if (rBig != c) {
				swapRows(a, c, rBig);
				swapRows(b, c, rBig);
			}

			// "Normalise" une ligne pivot 'C' en divisant tous ses éléments par la valeur du pivot 'a[c][c]'.
			// L'objectif est d'obtenir un 1 à la position du pivot.
			normalizePivotRow(a, b, c);

			// "Élimine" les valeurs dans toutes les autres lignes de la colonne 'C'.
			// L'objectif est d'obtenir des '0' partout sauf au pivot (qui vaut 1).
			eliminateOtherRows(a, b, c);
		}

		return new Matrix(b);
	}

	// this::L#134+1
	private void swapRows(double[] @NotNull [] array, int i, int j) {
		double[] temp = array[i];
		array[i] = array[j];
		array[j] = temp;
	}

	// this::L#140
	private void normalizePivotRow(double[] @NotNull [] a, double[] @NotNull [] b, int c) {
		double pivot = a[c][c];
		for (int s = c; s < width; s++) {
			a[c][s] /= pivot;
		}

		for (int s = 0; s < width; s++) {
			b[c][s] /= pivot;
		}
	}

	// this::L#144
	private void eliminateOtherRows(double[][] a, double[][] b, int c) {
		for (int r2 = 0; r2 < height; r2++) {
			if (r2 == c) {
				continue;
			}

			double factor = -a[r2][c];
			for (int s = c; s < width; s++) {
				a[r2][s] += factor * a[c][s];
			}

			for (int s = 0; s < width; s++) {
				b[r2][s] += factor * b[c][s];
			}
		}
	}

	/**
	 * Transposes this matrix.
	 * <p>
	 * The (i,j) element of the original matrix becomes the (j,i) element
	 * of the transposed matrix.
	 *
	 * @return a new matrix that is the transpose of this matrix
	 */
	public Matrix transpose() {
		double[][] transposed = createArray(width, height, (row, col) ->
				data[col][row]
		);

		return new Matrix(transposed);
	}

	/**
	 * Multiplies this matrix by another matrix.
	 * <p>
	 * The width of this matrix must equal the height of the other matrix.
	 *
	 * @param other the matrix to multiply by
	 * @return the resulting matrix from the multiplication
	 */
	public Matrix multiply(@NotNull Matrix other) {
		if (width != other.height) {
			throw new IllegalArgumentException("Invalid Matrix sizes");
		}

		double[][] result = createArray(height, other.width, (row, col) -> {
			double sum = 0.0;
			for (int k = 0; k < width; k++) {
				sum += data[row][k] * other.data[k][col];
			}
			return sum;
		});

		return new Matrix(result);
	}

	/**
	 * Multiplies all elements of this matrix by a scalar value.
	 *
	 * @param scalar the scalar value to multiply by
	 * @return a new matrix with all elements multiplied by the given value
	 */
	public Matrix multiply(double scalar) {
		double[][] result = createArray(height, width, (row, col) ->
				data[row][col] * scalar
		);

		return new Matrix(result);
	}

	/**
	 * Adds another matrix to this matrix.
	 * <p>
	 * Both matrices must have the same dimensions.
	 *
	 * @param other the matrix to add
	 * @return a new matrix resulting from the addition
	 * @throws IllegalArgumentException if the matrices have different dimensions
	 */
	public Matrix add(@NotNull Matrix other) {
		if (width != other.width || height != other.height) {
			throw new IllegalArgumentException("Invalid Dimensions");
		}

		double[][] result = createArray(height, width, (row, col) ->
				data[row][col] + other.data[row][col]
		);

		return new Matrix(result);
	}

	/**
	 * Subtracts another matrix from this matrix.
	 * <p>
	 * Both matrices must have the same dimensions.
	 *
	 * @param other the matrix to subtract
	 * @return a new matrix resulting from the subtraction
	 * @throws IllegalArgumentException if the matrices have different dimensions
	 */
	public Matrix subtract(@NotNull Matrix other) {
		if (width != other.width || height != other.height) {
			throw new IllegalArgumentException("Invalid Dimensions");
		}

		double[][] result = createArray(height, width, (row, col) ->
				data[row][col] - other.data[row][col]
		);

		return new Matrix(result);
	}

	@Override
	public String toString() {
		return Arrays.stream(data)
				.map(row -> Arrays.stream(row)
						.mapToObj(cell -> " " + cell)
						.collect(Collectors.joining()))
				.collect(Collectors.joining("\n"));
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof Matrix matrix
				&& this.height == matrix.height
				&& this.width == matrix.width
				&& Arrays.deepEquals(data, matrix.data);
	}

	@Override
	public int hashCode() {
		return Arrays.deepHashCode(data);
	}

	private double[] @NotNull [] deepCopy(double[] @NotNull [] array) {
		double[][] copy = new double[array.length][];
		for (int i = 0; i < array.length; i++) {
			copy[i] = array[i].clone();
		}

		return copy;
	}

	private @NotNull Matrix identity(int size) {
		double[][] result = createArray(size, size, (row, col) ->
				(Objects.equals(row, col) ? 1.0 : 0.0)
		);

		return new Matrix(result);
	}

	private double[] @NotNull [] createArray(int rows, int cols, ToDoubleBiFunction<Integer, Integer> function) {
		double[][] array = new double[rows][cols];
		for (int row = 0; row < rows; row++) {
			for (int col = 0; col < cols; col++) {
				array[row][col] = function.applyAsDouble(row, col);
			}
		}

		return array;
	}
}
