package fr.siroz.cariboustonks.util;

import java.time.DateTimeException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;

public final class TimeUtils {

	private static final Locale LOCALE = Locale.getDefault();
	private static final ZoneId ZONE_ID = ZoneId.systemDefault();

	private static final Pattern TIME_PATTERN = Pattern.compile(
			"(\\d+)d?|([01]?\\d|2[0-3])h?|([0-5]?\\d)m?|([0-5]?\\d)s?"
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
	 * Gets the current unix time in milliseconds.
	 *
	 * @return the current unix time
	 */
	public static long nowMillis() {
		return System.currentTimeMillis();
	}

	/**
	 * Gets the difference between {@link Instant#now()} and another instant.
	 *
	 * @param other the other instant
	 * @return the difference
	 */
	public static Duration diffToNow(Instant other) {
		return Duration.between(Instant.now(), other).abs();
	}

	/**
	 * Gets a {@link Duration} for a {@link TimeUnit} and amount.
	 *
	 * @param unit   the unit
	 * @param amount the amount
	 * @return the duration
	 */
	public static Duration duration(@NotNull TimeUnit unit, long amount) {
		return switch (unit) {
			case NANOSECONDS -> Duration.ofNanos(amount);
			case MICROSECONDS -> Duration.ofNanos(TimeUnit.MICROSECONDS.toNanos(amount));
			case MILLISECONDS -> Duration.ofMillis(amount);
			case SECONDS -> Duration.ofSeconds(amount);
			case MINUTES -> Duration.ofMinutes(amount);
			case HOURS -> Duration.ofHours(amount);
			case DAYS -> Duration.ofDays(amount);
		};
	}

	public static Optional<Instant> parseToInstant(@NotNull String input) {
		Duration duration = Duration.ZERO;
		Matcher matcher = TIME_PATTERN.matcher(input);

		while (matcher.find()) {
			if (matcher.group(1) != null) duration = duration.plusDays(Long.parseLong(matcher.group(1)));
			if (matcher.group(2) != null) duration = duration.plusHours(Long.parseLong(matcher.group(2)));
			if (matcher.group(3) != null) duration = duration.plusMinutes(Long.parseLong(matcher.group(3)));
			if (matcher.group(4) != null) duration = duration.plusSeconds(Long.parseLong(matcher.group(4)));
		}

		if (duration.isZero() || duration.isNegative()) {
			return Optional.empty();
		}

		return Optional.of(Instant.now().plus(duration));
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
	public static @NotNull String formatInstant(@NotNull Instant instant, @NotNull DateTimeFormatter formatter) {
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
	public static @NotNull String formatInstant(
			@NotNull Instant instant,
			@NotNull DateTimeFormatter formatter,
			@NotNull Locale locale
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
	public static @NotNull String formatInstant(
			@NotNull Instant instant,
			@NotNull DateTimeFormatter formatter,
			@NotNull Locale locale,
			@NotNull ZoneId zoneId
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
	public static @NotNull String getDurationFormatted(@NotNull Instant after) {
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
	public static @NotNull String getDurationFormatted(@NotNull Instant after, boolean all) {
		return getDurationFormatted(Instant.now(), after, all);
	}

	/**
	 * Returns a string formatted with the time difference between
	 * now {@link Instant} and the after {@link Instant}.
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
	 * @param now   the now time
	 * @param after the after time
	 * @return the formatted string
	 * @see #getDurationFormatted(Instant, Instant, boolean)
	 */
	public static @NotNull String getDurationFormatted(@NotNull Instant now, @NotNull Instant after) {
		return getDurationFormatted(now, after, false);
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
	public static @NotNull String getDurationFormatted(@NotNull Instant now, @NotNull Instant after, boolean all) {
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

	public static @NotNull String getSimpleTime(int seconds) {
		if (seconds >= 3600) return "> 1h";

		int sec = seconds % 60;
		int min = (seconds / 60) % 60;

		String strSec = (sec < 10) ? "0" + sec : Integer.toString(sec);
		String strMin = (min < 10) ? "0" + min : Integer.toString(min);

		return strMin + ":" + strSec;
	}
}
