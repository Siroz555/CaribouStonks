package fr.siroz.cariboustonks.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import org.apache.commons.codec.digest.DigestUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Stonks utilities
 */
public final class StonksUtil {

	/**
	 * => {@code 100,000,000}
	 */
	public static final NumberFormat INTEGER_NUMBERS = NumberFormat.getIntegerInstance(Locale.US);

	/**
	 * => {@code 100,000.15}
	 */
	public static final NumberFormat DOUBLE_NUMBERS = StonksUtil.make(
			NumberFormat.getInstance(Locale.US),
			nf -> nf.setMaximumFractionDigits(2));

	/**
	 * => {@code 100,000.1}
	 */
	public static final NumberFormat FLOAT_NUMBERS = StonksUtil.make(
			NumberFormat.getInstance(Locale.US),
			nf -> nf.setMaximumFractionDigits(1));

	/**
	 * => {@code 10B} / {@code 10M} / {@code 5k}
	 */
	@SuppressWarnings("unused")
	public static final NumberFormat SHORT_INTEGER_NUMBERS = new CompactSuffixFormat(0);

	/**
	 * => {@code 42.6B} / {@code 69.5M} / {@code 10.2k}
	 */
	public static final NumberFormat SHORT_FLOAT_NUMBERS = new CompactSuffixFormat(1);

	public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#0.00");

	public static final SecureRandom RANDOM = new SecureRandom();

	private StonksUtil() {
	}

	public static long parseAmount(@Nullable String value) {
		if (value == null || value.isEmpty()) return 0;

		value = value.replace(",", "").trim();
		char suffix = Character.toLowerCase(value.charAt(value.length() - 1));

		long multiplier = switch (suffix) {
			case 'k' -> 1_000L;
			case 'm' -> 1_000_000L;
			case 'b' -> 1_000_000_000L;
			default -> 1L;
		};

		String digits = multiplier != 1 ? value.substring(0, value.length() - 1) : value;
		return (long) (Double.parseDouble(digits) * multiplier);
	}

	/**
	 * Calculate the SHA-256 Digest of the given {@code file}
	 *
	 * @param filePath the file path
	 * @return the hexadecimal String of the SHA-256 Digest
	 * @throws IOException if an I/O error occurs
	 */
	public static @NonNull String calculateSHA256(@NonNull Path filePath) throws IOException {
		try (InputStream is = Files.newInputStream(filePath)) {
			return DigestUtils.sha256Hex(is);
		}
	}

	/**
	 * Split a list into sublists of a maximum size with the {@code maxSize}
	 *
	 * @param list    the list
	 * @param maxSize the max size
	 * @param <T>     the list type instance
	 * @return a list of sublists
	 */
	public static <T> @NonNull List<List<T>> partitionList(@NonNull List<T> list, int maxSize) {
		List<List<T>> partitions = new ArrayList<>();
		for (int i = 0; i < list.size(); i += maxSize) partitions.add(list.subList(i, Math.min(i + maxSize, list.size())));
		return partitions;
	}

	public static <T> T make(@NonNull T object, @NonNull Consumer<? super T> initializer) {
		initializer.accept(object);
		return object;
	}

	/**
	 * Converts a {@link String} into a {@code int} or return the default value if the conversion fails.
	 *
	 * @param s            the string to convert
	 * @param defaultValue the default value
	 * @return the int represented by the string or the default value
	 */
	public static int toInt(@Nullable String s, int defaultValue) {
		if (s == null) return defaultValue;

		try {
			return Integer.parseInt(s);
		} catch (Exception _) {
			return defaultValue;
		}
	}

	/**
	 * The median is the central value of a sorted list. It is less sensitive to extreme values than the means.
	 *
	 * @param values the values
	 * @return the Median
	 */
	public static double calculateMedian(@NonNull List<Double> values) {
		if (values.isEmpty()) return -1;

		List<Double> sorted = values.stream().sorted().toList();

		int n = sorted.size();
		if (n % 2 == 1) {
			return sorted.get(n / 2);
		} else {
			return (sorted.get(n / 2 - 1) + sorted.get(n / 2)) / 2.0;
		}
	}

	/**
	 * Réduit la taille de la liste fournie pour qu'elle atteigne approximativement la taille cible spécifiée.
	 * Si la liste contient un nombre d'éléments inférieur ou égal à la taille cible, la liste est retournée inchangée.
	 * Si la taille de la liste est supérieure à la taille cible, les éléments sont sélectionnés à des intervalles
	 * réguliers pour créer une liste réduite d'une taille approximativement égale à celle souhaitée.
	 * Le dernier élément de la liste originale est toujours inclus dans la liste réduite.
	 *
	 * @param list       la liste à réduire
	 * @param targetSize la taille approximative souhaitée de la liste réduite
	 * @return la nouvelle liste après traitement
	 */
	public static <T> @NonNull List<T> reduceListToApproxSize(@NonNull List<T> list, int targetSize) {
		if (list.size() <= targetSize) return list;

		List<T> result = new ArrayList<>();
		int step = list.size() / targetSize;

		for (int i = 0; i < list.size(); i += step) {
			result.add(list.get(i));
		}

		if (!result.contains(list.getLast())) {
			result.add(list.getLast());
		}

		return result;
	}

	private static final class CompactSuffixFormat extends NumberFormat {
		private static final NumberFormat BASE = NumberFormat.getInstance(Locale.US);
		private final int maxFractionDigits;

		CompactSuffixFormat(int maxFractionDigits) {
			this.maxFractionDigits = maxFractionDigits;
		}

		@Override
		public @NonNull StringBuffer format(double number, StringBuffer toAppendTo, FieldPosition pos) {
			double abs = Math.abs(number);
			String suffix;
			double divided;

			if (abs >= 1_000_000_000) {
				divided = number / 1_000_000_000;
				suffix = "B";
			} else if (abs >= 1_000_000) {
				divided = number / 1_000_000;
				suffix = "M";
			} else if (abs >= 1_000) {
				divided = number / 1_000;
				suffix = "k";
			} else {
				divided = number;
				suffix = "";
			}

			int fractionDigits = maxFractionDigits == 0
					? 0
					: divided < 10 ? maxFractionDigits
					: divided < 100 ? Math.min(1, maxFractionDigits)
					: 0;

			NumberFormat nf = make(BASE, f -> {
				f.setMinimumFractionDigits(fractionDigits);
				f.setMaximumFractionDigits(fractionDigits);
			});

			return toAppendTo.append(nf.format(divided)).append(suffix);
		}

		@Override
		public @NonNull StringBuffer format(long number, StringBuffer toAppendTo, FieldPosition pos) {
			return format((double) number, toAppendTo, pos);
		}

		@Override
		public Number parse(String source, ParsePosition parsePosition) {
			throw new UnsupportedOperationException("Parsing is not supported");
		}
	}
}
