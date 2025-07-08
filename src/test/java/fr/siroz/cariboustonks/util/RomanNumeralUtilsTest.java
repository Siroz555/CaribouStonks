package fr.siroz.cariboustonks.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RomanNumeralUtilsTest {

    @Test
    public void testIsRomanNumeral_Valid() {
        assertTrue(RomanNumeralUtils.isRomanNumeral("I"), "I must be a valid Roman numeral");
        assertTrue(RomanNumeralUtils.isRomanNumeral("IX"), "IX must be a valid Roman numeral");
        assertTrue(RomanNumeralUtils.isRomanNumeral("MMXXV"), "MMXXV must be a valid Roman numeral");
        assertTrue(RomanNumeralUtils.isRomanNumeral("XLII"), "XLII must be a valid Roman numeral");
    }

    @Test
    public void testIsRomanNumeral_Invalid() {
        assertFalse(RomanNumeralUtils.isRomanNumeral("IIII"), "IIII must not be a valid Roman numeral");
        assertFalse(RomanNumeralUtils.isRomanNumeral("IL"), "IL must not be a valid Roman numeral");
        assertFalse(RomanNumeralUtils.isRomanNumeral("123"), "123 must not be a valid Roman numeral");
        assertFalse(RomanNumeralUtils.isRomanNumeral("MMMXCIVIV"), "MMMXCIVIV must not be a valid Roman numeral");
    }

    @Test
    public void testParse_Valid() {
        assertEquals(1, RomanNumeralUtils.parse("I"), "I should be equal to 1");
        assertEquals(4, RomanNumeralUtils.parse("IV"), "IV should be equal to 4");
        assertEquals(9, RomanNumeralUtils.parse("IX"), "IX should be equal to 9");
        assertEquals(44, RomanNumeralUtils.parse("XLIV"), "XLIV should be equal to 44");
        assertEquals(3999, RomanNumeralUtils.parse("MMMCMXCIX"), "MMMCMXCIX should be equal to 3999");
    }

    @Test
    public void testParse_Invalid() {
        assertEquals(-1, RomanNumeralUtils.parse("IIII"), "IIII is invalid and should return -1");
        assertEquals(-1, RomanNumeralUtils.parse("IL"), "IL is invalid and should return -1");
        assertEquals(-1, RomanNumeralUtils.parse("123"), "123 is invalid and should return -1");
        assertEquals(-1, RomanNumeralUtils.parse("MMMXCIVIV"), "MMMXCIVIV is invalid and should return -1");
    }

    @Test
    public void testGenerate_Valid() {
        assertEquals("I", RomanNumeralUtils.generate(1), "1 should be generated as 'I'");
        assertEquals("IV", RomanNumeralUtils.generate(4), "4 should be generated as 'IV'");
        assertEquals("IX", RomanNumeralUtils.generate(9), "9 should be generated as 'IX'");
        assertEquals("XLIV", RomanNumeralUtils.generate(44), "44 should be generated as 'XLIV'");
        assertEquals("MMMCMXCIX", RomanNumeralUtils.generate(3999), "3999 should be generated as 'MMMCMXCIX'");
    }

    @Test
    public void testGenerate_OutOfRange() {
        assertThrows(IllegalArgumentException.class, () -> RomanNumeralUtils.generate(0), "The number 0 must not be accepted");
        assertThrows(IllegalArgumentException.class, () -> RomanNumeralUtils.generate(4000), "The number 4000 must not be accepted");
    }
}