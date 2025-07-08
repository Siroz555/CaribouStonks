package fr.siroz.cariboustonks.util;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StonksUtilsTest {

	@Test
	void stripColor_noColorCodes_returnsSameString() {
		String input = "Hello World";
		assertEquals(input, StonksUtils.stripColor(input));
	}

	@Test
	void stripColor_withColorCodes_removesAllColorCodes() {
		String input = "§aHello §bWorld";
		assertEquals("Hello World", StonksUtils.stripColor(input));
	}

	@Test
	void stripColor_allPossibleColorCodes_removesAllColorCodes() {
		String input = "§0§1§2§3§4§5§6§7§8§9§a§b§c§d§e§f§k§l§m§n§o§rText";
		assertEquals("Text", StonksUtils.stripColor(input));
	}

	@Test
	void stripColor_invalidColorCodes_keepInvalidCodes() {
		String input = "§xHello§World§";
		assertEquals("§xHello§World§", StonksUtils.stripColor(input));
	}

	@Test
	void stripColor_mixedValidAndInvalidCodes_onlyRemovesValidCodes() {
		String input = "§aHello§xWorld§b!";
		assertEquals("Hello§xWorld!", StonksUtils.stripColor(input));
	}

	@Test
	void calculateMedian_emptyList_returnsMinusOne() {
		assertEquals(-1, StonksUtils.calculateMedian(Collections.emptyList()));
	}

	@Test
	void calculateMedian_singleValue_returnsThatValue() {
		List<Double> values = List.of(5.0);
		assertEquals(5.0, StonksUtils.calculateMedian(values));
	}

	@Test
	void calculateMedian_oddNumberOfValues_returnsMiddleValue() {
		List<Double> values = Arrays.asList(1.0, 3.0, 2.0, 5.0, 4.0);
		assertEquals(3.0, StonksUtils.calculateMedian(values));
	}

	@Test
	void calculateMedian_evenNumberOfValues_returnsAverageOfMiddleValues() {
		List<Double> values = Arrays.asList(1.0, 2.0, 3.0, 4.0);
		assertEquals(2.5, StonksUtils.calculateMedian(values));
	}

	@Test
	void calculateMedian_unsortedList_returnsCorrectMedian() {
		List<Double> values = Arrays.asList(4.0, 1.0, 3.0, 2.0);
		assertEquals(2.5, StonksUtils.calculateMedian(values));
	}

	@Test
	void calculateMedian_negativeNumbers_returnsCorrectMedian() {
		List<Double> values = Arrays.asList(-5.0, -2.0, -1.0, -4.0, -3.0);
		assertEquals(-3.0, StonksUtils.calculateMedian(values));
	}

	@Test
	void calculateMedian_mixedPositiveAndNegative_returnsCorrectMedian() {
		List<Double> values = Arrays.asList(-2.0, 0.0, 1.0, -1.0, 2.0);
		assertEquals(0.0, StonksUtils.calculateMedian(values));
	}

	@Test
	void calculateMedian_duplicateValues_returnsCorrectMedian() {
		List<Double> values = Arrays.asList(1.0, 2.0, 2.0, 3.0, 3.0);
		assertEquals(2.0, StonksUtils.calculateMedian(values));
	}

	@Test
	void calculateMedian_decimalValues_returnsCorrectMedian() {
		List<Double> values = Arrays.asList(1.5, 2.5, 3.5, 4.5);
		assertEquals(3.0, StonksUtils.calculateMedian(values));
	}

	@Test
	public void testFormatNumbers_integer() {
		assertEquals("1,000", StonksUtils.INTEGER_NUMBERS.format(1000));
		assertEquals("10,000", StonksUtils.INTEGER_NUMBERS.format(10_000));
		assertEquals("100,000", StonksUtils.INTEGER_NUMBERS.format(100_000));
		assertEquals("99,999,999", StonksUtils.INTEGER_NUMBERS.format(99_999_999.4));
		assertEquals("100,000,000", StonksUtils.INTEGER_NUMBERS.format(100_000_000));
	}

	@Test
	public void testFormatNumbers_double() {
		assertEquals("1,000.15", StonksUtils.DOUBLE_NUMBERS.format(1000.15));
		assertEquals("10,000.99", StonksUtils.DOUBLE_NUMBERS.format(10_000.987));
		assertEquals("100,000.69", StonksUtils.DOUBLE_NUMBERS.format(100_000.69));
		assertEquals("99,999,999.98", StonksUtils.DOUBLE_NUMBERS.format(99_999_999.978));
	}

	@Test
	public void testFormatNumbers_float() {
		assertEquals("1,000.2", StonksUtils.FLOAT_NUMBERS.format(1000.23));
		assertEquals("10,000.9", StonksUtils.FLOAT_NUMBERS.format(10_000.94));
		assertEquals("100,000.7", StonksUtils.FLOAT_NUMBERS.format(100_000.69));
		assertEquals("99,999,999.8", StonksUtils.FLOAT_NUMBERS.format(99_999_999.84));
	}

	@Test
	public void testFormatNumbers_shortInteger() {
		assertEquals("1K", StonksUtils.SHORT_INTEGER_NUMBERS.format(1000));
		assertEquals("10K", StonksUtils.SHORT_INTEGER_NUMBERS.format(10_000));
		assertEquals("1M", StonksUtils.SHORT_INTEGER_NUMBERS.format(1_000_000));
		assertEquals("1B", StonksUtils.SHORT_INTEGER_NUMBERS.format(1_000_000_000));
		assertEquals("9B", StonksUtils.SHORT_INTEGER_NUMBERS.format(9_000_000_000L));
		assertEquals("10B", StonksUtils.SHORT_INTEGER_NUMBERS.format(9_500_000_000L));
		assertEquals("15B", StonksUtils.SHORT_INTEGER_NUMBERS.format(15_000_000_000L));
		assertEquals("16B", StonksUtils.SHORT_INTEGER_NUMBERS.format(15_500_000_000L));
	}

	@Test
	public void testFormatNumbers_shortFloat() {
		assertEquals("1.0K", StonksUtils.SHORT_FLOAT_NUMBERS.format(1000.23));
		assertEquals("10.5K", StonksUtils.SHORT_FLOAT_NUMBERS.format(10_500));
		assertEquals("1.2M", StonksUtils.SHORT_FLOAT_NUMBERS.format(1_200_000));
		assertEquals("1.3B", StonksUtils.SHORT_FLOAT_NUMBERS.format(1_300_000_000));
		assertEquals("9.9B", StonksUtils.SHORT_FLOAT_NUMBERS.format(9_900_000_000L));
		assertEquals("10.1B", StonksUtils.SHORT_FLOAT_NUMBERS.format(10_100_000_000L));
		assertEquals("14.5B", StonksUtils.SHORT_FLOAT_NUMBERS.format(14_500_000_000L));
		assertEquals("15.2B", StonksUtils.SHORT_FLOAT_NUMBERS.format(15_200_000_000L));
	}
}
