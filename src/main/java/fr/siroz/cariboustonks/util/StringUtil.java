package fr.siroz.cariboustonks.util;

import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import java.util.Locale;
import java.util.regex.Pattern;
import org.jspecify.annotations.NonNull;

public final class StringUtil {
	public static final String ALPHANUMERIC = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
	public static final Pattern ROMAN_PATTERN = Pattern.compile("M{0,3}(CM|CD|D?C{0,3})(XC|XL|L?X{0,3})(IX|IV|V?I{0,3})");
	public static final Int2ObjectMap<String> VALUE_TO_ROMAN = Int2ObjectMaps.unmodifiable(new Int2ObjectLinkedOpenHashMap<>(
			new int[]{1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1},
			new String[]{"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"}
	));
	public static final Object2IntMap<String> ROMAN_TO_VALUE = Object2IntMaps.unmodifiable(new Object2IntLinkedOpenHashMap<>(
			new String[]{"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"},
			new int[]{1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1}
	));

	private StringUtil() {
	}

	/**
	 * Generate a random String ID with a capacity of 10 (4c2Pm42j69)
	 *
	 * @return the generate random ID as String
	 */
	public static @NonNull String generateRandomId() {
		StringBuilder id = new StringBuilder(10);
		for (int i = 0; i < 10; i++) id.append(ALPHANUMERIC.charAt(StonksUtil.RANDOM.nextInt(ALPHANUMERIC.length())));
		return id.toString();
	}

	/**
	 * Capitalize the given input String.
	 *
	 * <li>blessed -> Blessed</li>
	 * <li>BLESSED -> Blessed</li>
	 * <li>blood_soaked -> Blood Soaked</li>
	 * <li>BLOOD_SOAKED -> Blood Soaked</li>
	 *
	 * @param input     the input String
	 * @param delimiter the delimiter
	 * @return the capitalized input String
	 */
	public static @NonNull String capitalize(@NonNull String input, char delimiter) {
		boolean newWord = true;
		StringBuilder sb = new StringBuilder(input.length());
		for (int i = 0; i < input.length(); i++) {
			char c = input.charAt(i);
			if (c == delimiter) {
				sb.append(' ');
				newWord = true;
			} else {
				if (newWord) {
					sb.append(Character.toUpperCase(c));
					newWord = false;
				} else {
					sb.append(Character.toLowerCase(c));
				}
			}
		}
		return sb.toString();
	}

	public static boolean isRomanNumeral(@NonNull String number) {
		if (number.isEmpty()) return false;
		return ROMAN_PATTERN.matcher(number.toUpperCase(Locale.ENGLISH)).matches();
	}

	public static int parseRomanNumeral(@NonNull String roman) {
		if (roman.isEmpty() || !isRomanNumeral(roman)) return -1;

		int result = 0;
		int i = 0;
		while (i < roman.length()) {
			// deux caractères
			if (i + 1 < roman.length()) {
				String twoChar = roman.substring(i, i + 2);
				if (ROMAN_TO_VALUE.containsKey(twoChar)) {
					result += ROMAN_TO_VALUE.getInt(twoChar);
					i += 2;
					continue;
				}
			}
			// un caractère
			String oneChar = roman.substring(i, i + 1);
			result += ROMAN_TO_VALUE.getInt(oneChar);
			i++;
		}
		return result;
	}

	public static @NonNull String generateRomanNumeral(int number) {
		if (number < 1 || number > 3999) throw new IllegalArgumentException("Number out of range for Roman numeral conversion.");

		StringBuilder roman = new StringBuilder();
		for (Int2ObjectMap.Entry<String> entry : Int2ObjectMaps.fastIterable(VALUE_TO_ROMAN)) {
			while (number >= entry.getIntKey()) {
				roman.append(entry.getValue());
				number -= entry.getIntKey();
			}
		}
		return roman.toString();
	}
}
