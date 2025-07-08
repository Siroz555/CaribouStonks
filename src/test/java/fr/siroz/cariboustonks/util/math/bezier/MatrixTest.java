package fr.siroz.cariboustonks.util.math.bezier;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MatrixTest {

	@Test
	@DisplayName("Matrix creation should work with valid dimensions")
	public void testMatrixCreation() {
		double[][] data = {{1, 2, 3}, {4, 5, 6}};
		Matrix matrix = new Matrix(data);

		// Vérifier que les valeurs sont correctement stockées
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 3; j++) {
				assertEquals(data[i][j], matrix.get(i, j), 0.0001);
			}
		}
	}

	@Test
	@DisplayName("Matrix addition should work correctly")
	public void testMatrixAddition() {
		double[][] data1 = {{1, 2}, {3, 4}};
		double[][] data2 = {{5, 6}, {7, 8}};
		Matrix m1 = new Matrix(data1);
		Matrix m2 = new Matrix(data2);

		Matrix result = m1.add(m2);

		assertEquals(6, result.get(0, 0), 0.0001);
		assertEquals(8, result.get(0, 1), 0.0001);
		assertEquals(10, result.get(1, 0), 0.0001);
		assertEquals(12, result.get(1, 1), 0.0001);
	}

	@Test
	@DisplayName("Matrix subtraction should work correctly")
	public void testMatrixSubtraction() {
		double[][] data1 = {{9, 8}, {7, 6}};
		double[][] data2 = {{1, 2}, {3, 4}};
		Matrix m1 = new Matrix(data1);
		Matrix m2 = new Matrix(data2);

		Matrix result = m1.subtract(m2);

		assertEquals(8, result.get(0, 0), 0.0001);
		assertEquals(6, result.get(0, 1), 0.0001);
		assertEquals(4, result.get(1, 0), 0.0001);
		assertEquals(2, result.get(1, 1), 0.0001);
	}

	@Test
	@DisplayName("The scalar product should work correctly")
	public void testScalarMultiplication() {
		double[][] data = {{1, 2}, {3, 4}};
		Matrix matrix = new Matrix(data);

		Matrix result = matrix.multiply(2.5);

		assertEquals(2.5, result.get(0, 0), 0.0001);
		assertEquals(5.0, result.get(0, 1), 0.0001);
		assertEquals(7.5, result.get(1, 0), 0.0001);
		assertEquals(10.0, result.get(1, 1), 0.0001);
	}

	@Test
	@DisplayName("Matrix multiplication should work correctly")
	public void testMatrixMultiplication() {
		double[][] data1 = {{1, 2}, {3, 4}};
		double[][] data2 = {{5, 6}, {7, 8}};
		Matrix m1 = new Matrix(data1);
		Matrix m2 = new Matrix(data2);

		Matrix result = m1.multiply(m2);

		assertEquals(19, result.get(0, 0), 0.0001);
		assertEquals(22, result.get(0, 1), 0.0001);
		assertEquals(43, result.get(1, 0), 0.0001);
		assertEquals(50, result.get(1, 1), 0.0001);
	}

	@Test
	@DisplayName("Matrix transposition should work correctly")
	public void testTranspose() {
		double[][] data = {{1, 2, 3}, {4, 5, 6}};
		Matrix matrix = new Matrix(data);

		Matrix transposed = matrix.transpose();

		assertEquals(1, transposed.get(0, 0), 0.0001);
		assertEquals(4, transposed.get(0, 1), 0.0001);
		assertEquals(2, transposed.get(1, 0), 0.0001);
		assertEquals(5, transposed.get(1, 1), 0.0001);
		assertEquals(3, transposed.get(2, 0), 0.0001);
		assertEquals(6, transposed.get(2, 1), 0.0001);
	}

	@Test
	@DisplayName("Matrix inversion should work correctly")
	public void testInversion() {
		// Matrice 2x2 simple
		double[][] data = {{4, 7}, {2, 6}};
		Matrix matrix = new Matrix(data);

		Matrix inverse = matrix.inverse();

		// Déterminant = 4*6 - 7*2 = 24 - 14 = 10
		// Inverse = 1/10 * [6, -7; -2, 4]
		assertEquals(0.6, inverse.get(0, 0), 0.0001);
		assertEquals(-0.7, inverse.get(0, 1), 0.0001);
		assertEquals(-0.2, inverse.get(1, 0), 0.0001);
		assertEquals(0.4, inverse.get(1, 1), 0.0001);

		// Vérifier que A * A(-1) = I
		Matrix identity = matrix.multiply(inverse);
		assertEquals(1.0, identity.get(0, 0), 0.0001);
		assertEquals(0.0, identity.get(0, 1), 0.0001);
		assertEquals(0.0, identity.get(1, 0), 0.0001);
		assertEquals(1.0, identity.get(1, 1), 0.0001);
	}
}
