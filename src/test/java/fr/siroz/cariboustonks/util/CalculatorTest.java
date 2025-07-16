package fr.siroz.cariboustonks.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CalculatorTest {

	@Test
	@DisplayName("Simple addition")
	public void testSimpleAddition() {
		assertEquals(5.0, Calculator.calculate("2 + 3"), 1e-9);
	}

	@Test
	@DisplayName("Simple subtraction")
	public void testSimpleSubtraction() {
		assertEquals(1.0, Calculator.calculate("4 - 3"), 1e-9);
	}

	@Test
	@DisplayName("Simple multiplication")
	public void testSimpleMultiplication() {
		assertEquals(12.0, Calculator.calculate("4 * 3"), 1e-9);
	}

	@Test
	@DisplayName("Multiplication using 'x' character")
	public void testMultiplicationUsingX() {
		assertEquals(12.0, Calculator.calculate("4 x 3"), 1e-9);
	}

	@Test
	@DisplayName("Simple division")
	public void testSimpleDivision() {
		assertEquals(2.0, Calculator.calculate("6 / 3"), 1e-9);
	}

	@Test
	@DisplayName("Exponentiation with caret")
	public void testExponentiation() {
		assertEquals(8.0, Calculator.calculate("2 ^ 3"), 1e-9);
	}

	@Test
	@DisplayName("Precedence with parentheses")
	public void testParenthesesPrecedence() {
		assertEquals(14.0, Calculator.calculate("2 + (3 * 4)"), 1e-9);
	}

	@Test
	@DisplayName("Nested parentheses calculation")
	public void testNestedParentheses() {
		assertEquals(20.0, Calculator.calculate("2 + (3 * (4 + 2))"), 1e-9);
	}

	@Test
	@DisplayName("Implicit multiplication with parentheses")
	public void testImplicitMultiplication() {
		assertEquals(6.0, Calculator.calculate("2(3)"), 1e-9);
		assertEquals(8.0, Calculator.calculate("2 + 2(2+1)"), 1e-9);
	}

	@Test
	@DisplayName("Division by zero should throw exception")
	public void testDivisionByZero() {
		Exception exception = assertThrows(UnsupportedOperationException.class, () -> Calculator.calculate("10 / 0"));
		assertEquals("Division by Zero", exception.getMessage());
	}

	@Test
	@DisplayName("Invalid operator sequence should throw exception")
	public void testInvalidOperatorSequence() {
		assertThrows(UnsupportedOperationException.class, () -> Calculator.calculate("2 ++ 2"));
	}

	@Test
	@DisplayName("Invalid characters should throw exception")
	public void testInvalidCharacters() {
		assertThrows(UnsupportedOperationException.class, () -> Calculator.calculate("2 + a"));
	}

	@Test
	@DisplayName("Magnitude values should be parsed correctly")
	public void testMagnitudeValues() {
		assertEquals(2000.0, Calculator.calculate("2k"), 1e-9);
		assertEquals(3_000_000.0, Calculator.calculate("3m"), 1e-9);
	}

	@Test
	@DisplayName("Addition with millions")
	public void testMillionsAddition() {
		assertEquals(21_000_000.0, Calculator.calculate("10m + 11m"), 1e-6);
	}

	@Test
	@DisplayName("Addition with billions and millions")
	public void testBillionsAndMillionsAddition() {
		assertEquals(1_485_000_000.0, Calculator.calculate("1b + 485m"), 1e-6);
	}

	@Test
	@DisplayName("Multiplication between kilounit and billion")
	public void testMixedMagnitudesMultiplication() {
		assertEquals(2_000_000_000_000.0, Calculator.calculate("2k * 1b"), 1e-3);
	}

	@Test
	@DisplayName("Parentheses after magnitude value")
	public void testMagnitudeParenthesis() {
		assertEquals(6_000_000.0, Calculator.calculate("2m(1 + 2)"), 1e-3);
	}

	@Test
	@DisplayName("Subtraction involving magnitude values")
	public void testMagnitudeSubtraction() {
		assertEquals(500_000.0, Calculator.calculate("1m - 500k"), 1e-3);
	}

	@Test
	@DisplayName("Unknown magnitude should throw exception")
	public void testUnknownMagnitude() {
		Throwable exception = assertThrows(UnsupportedOperationException.class, () -> Calculator.calculate("2z"));
		assertEquals("Character is invalid", exception.getMessage());
	}

	@Test
	@DisplayName("Case-insensitive input parsing")
	public void testCaseInsensitivity() {
		assertEquals(4.0, Calculator.calculate(" 2 + 2 "), 1e-9);
		assertEquals(6.0, Calculator.calculate("2 X 3"), 1e-9);
		assertEquals(2000.0, Calculator.calculate("2K"), 1e-9);
	}
}
