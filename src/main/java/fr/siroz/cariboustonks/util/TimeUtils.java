package fr.siroz.cariboustonks.util;

import java.time.DateTimeException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jspecify.annotations.NonNull;

public final class TimeUtils {

	private static final Locale LOCALE = Locale.getDefault();
	private static final ZoneId ZONE_ID = ZoneId.systemDefault();
	public static final ZoneId UTC = ZoneOffset.UTC.normalized();

	/**
	 * | => 10s | => 50m 10s | => 16h 50m 10s | => 4d 16h 50m 10s
	 */
	public static final Pattern TIME_PATTERN = Pattern.compile(
			"(?:(\\d+)d)?\\s*(?:(\\d+)h)?\\s*(?:(\\d+)m)?\\s*(?:(\\d+)s)?"
	);

	/**
	 * => (FR) {@code 17:16} | EN {@code 5:16 PM}
	 */
	public static final DateTimeFormatter TIME_HH_MM = DateTimeFormatter.
			ofLocalizedTime(FormatStyle.SHORT).withLocale(LOCALE);

	/**
	 * => (FR) {@code 17:16:45} | EN {@code 5:16:45 PM}
	 */
	public static final DateTimeFormatter TIME_HH_MM_SS = DateTimeFormatter
			.ofPattern("HH:mm:ss", LOCALE);

	/**
	 * => (FR) {@code 7/3/2025} | (EN) {@code 03/07/2025}
	 */
	public static final DateTimeFormatter DATE_SHORT = DateTimeFormatter
			.ofLocalizedDate(FormatStyle.SHORT).withLocale(LOCALE);

	/**
	 * => (FR) {@code Mardi 7 mars 2025} | (EN) {@code Tuesday March 7 2025}
	 */
	public static final DateTimeFormatter DATE_FULL = DateTimeFormatter
			.ofLocalizedDate(FormatStyle.FULL).withLocale(LOCALE);

	/**
	 * => (FR) {@code 7/3/2025, 17:16} | (EN) {@code 3/7/2025, 5:16}
	 */
	public static final DateTimeFormatter DATE_TIME_SHORT = DateTimeFormatter
			.ofLocalizedDateTime(FormatStyle.SHORT).withLocale(LOCALE);

	/**
	 * => (FR) {@code Mardi Mars 7 2025, 17:16} | (EN) {@code Tuesday, March, 7, 2025, 5:16}
	 */
	public static final DateTimeFormatter DATE_TIME_FULL = DateTimeFormatter
			.ofLocalizedDateTime(FormatStyle.FULL, FormatStyle.MEDIUM).withLocale(LOCALE);

	private TimeUtils() {
	}

	/**
	 * Extracts the {@link Duration} from the given input ({@link TimeUtils#TIME_PATTERN})
	 * <p>
	 * Input: 4d 16h 50m 10s => Duration: 4d 16h 50m 10s
	 *
	 * @param input the input String
	 * @return the Duration result {@code or} {@link Duration#ZERO}
	 */
	public static Duration extractDuration(@NonNull String input) {
		Matcher matcher = TIME_PATTERN.matcher(input);
		if (!matcher.find()) return Duration.ZERO;
		try {
			long days = matcher.group(1) != null ? Long.parseLong(matcher.group(1)) : 0;
			long hours = matcher.group(2) != null ? Long.parseLong(matcher.group(2)) : 0;
			long minutes = matcher.group(3) != null ? Long.parseLong(matcher.group(3)) : 0;
			long seconds = matcher.group(4) != null ? Long.parseLong(matcher.group(4)) : 0;
			return Duration.ofDays(days).plusHours(hours).plusMinutes(minutes).plusSeconds(seconds);
		} catch (Exception _) {
			return Duration.ZERO;
		}
	}

	/**
	 * Format an {@link Instant} with the given {@link DateTimeFormatter}.
	 * <p>
	 * Note: The default Locale and ZoneID is used
	 *
	 * @param instant   the instant
	 * @param formatter the formatter
	 * @return the formatted {@link ZonedDateTime}
	 * @throws DateTimeException if an error occurs during printing
	 * @see #formatInstant(Instant, DateTimeFormatter, Locale)
	 */
	public static @NonNull String formatInstant(@NonNull Instant instant, @NonNull DateTimeFormatter formatter) {
		return formatInstant(instant, formatter, LOCALE, ZONE_ID);
	}

	/**
	 * Format an {@link Instant} with the given {@link DateTimeFormatter}.
	 * <p>
	 * Note: The default ZoneID is used
	 *
	 * @param instant   the instant
	 * @param formatter the formatter
	 * @param locale    the locale
	 * @return the formatted {@link ZonedDateTime}
	 * @throws DateTimeException if an error occurs during printing
	 * @see #formatInstant(Instant, DateTimeFormatter)
	 */
	public static @NonNull String formatInstant(
			@NonNull Instant instant,
			@NonNull DateTimeFormatter formatter,
			@NonNull Locale locale
	) {
		return formatInstant(instant, formatter, locale, ZONE_ID);
	}

	/**
	 * Format an {@link Instant} with the given {@link DateTimeFormatter}.
	 *
	 * @param instant   the instant
	 * @param formatter the formatter
	 * @param locale    the locale
	 * @param zoneId    the zoneId
	 * @return the formatted {@link ZonedDateTime}
	 */
	public static @NonNull String formatInstant(
			@NonNull Instant instant,
			@NonNull DateTimeFormatter formatter,
			@NonNull Locale locale,
			@NonNull ZoneId zoneId
	) {
		ZonedDateTime dateTime = instant.atZone(zoneId);
		return dateTime.format(formatter.localizedBy(locale));
	}

	/**
	 * Returns a string formatted with the time difference between
	 * {@link Instant#now()} and the after {@link Instant}
	 * <p>
	 * Note: if the time difference is negative, "?" is returned
	 * <p>
	 * Duration formatted output:
	 * <pre>
	 *     "14s"
	 *     "1m 6s"
	 *     "6h 42m 12s"
	 *     "1d 18h 42m 16s"
	 * </pre>
	 *
	 * @param after the after time
	 * @return the formatted string
	 * @see #getDurationFormatted(Instant, boolean)
	 */
	public static @NonNull String getDurationFormatted(@NonNull Instant after) {
		return getDurationFormatted(Instant.now(), after, false);
	}

	/**
	 * Returns a string formatted with the time difference between
	 * {@link Instant#now()} and the after {@link Instant}.
	 * <p>
	 * Note: if the time difference is negative, "?" is returned
	 * <p>
	 * If the boolean parameter {@code all} is set to true, all time parts is returned:
	 * <pre>
	 *     "0d 0h 42m 16s"
	 *     "12d 6h 2m 28s"
	 * </pre>
	 * Otherwise:
	 * <pre>
	 *     "14s"
	 *     "1m 6s"
	 *     "6h 42m 12s"
	 *     "1d 18h 42m 16s"
	 * </pre>
	 *
	 * @param after the after time
	 * @param all   if the returned string must contain all time parts
	 * @return the formatted string
	 * @see #getDurationFormatted (Instant)
	 */
	public static @NonNull String getDurationFormatted(@NonNull Instant after, boolean all) {
		return getDurationFormatted(Instant.now(), after, all);
	}

	/**
	 * Returns a string formatted with the time difference between
	 * now {@link Instant} and the after {@link Instant}.
	 * <p>
	 * Note: if the time difference is negative, "?" is returned
	 * <p>
	 * If the boolean parameter {@code all} is set to true, all time parts are returned.
	 * <p>
	 * Duration formatted output:
	 * <pre>
	 *     "0d 0h 42m 16s"
	 *     "12d 6h 2m 28s"
	 *  </pre>
	 * Otherwise:
	 * <pre>
	 *     "14s"
	 *     "1m 6s"
	 *     "6h 42m 12s"
	 *     "1d 18h 42m 16s"
	 * </pre>
	 *
	 * @param now   the now time
	 * @param after the after time
	 * @param all   if the returned string must contain all time parts
	 * @return the formatted string
	 * @see #getDurationFormatted (Instant, Instant)
	 */
	public static @NonNull String getDurationFormatted(@NonNull Instant now, @NonNull Instant after, boolean all) {
		Duration duration = Duration.between(now, after);

		String durationFormatted = String.format("%sd %sh %sm %ss",
				duration.toDaysPart(),
				duration.toHoursPart(),
				duration.toMinutesPart(),
				duration.toSecondsPart());

		if (all || duration.toDaysPart() > 0) {
			return duration.toSecondsPart() > 0 ? durationFormatted : "?";

		} else if (duration.toHoursPart() > 0) {
			return String.format("%sh %sm %ss", duration.toHoursPart(), duration.toMinutesPart(), duration.toSecondsPart());

		} else if (duration.toMinutesPart() > 0) {
			return String.format("%sm %ss", duration.toMinutesPart(), duration.toSecondsPart());

		} else if (duration.toSecondsPart() > 0) {
			return String.format("%ss", duration.toSecondsPart());

		} else {
			return "?";
		}
	}
}
