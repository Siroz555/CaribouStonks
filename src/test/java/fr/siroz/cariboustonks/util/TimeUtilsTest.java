package fr.siroz.cariboustonks.util;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Locale;
import java.util.Optional;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class TimeUtilsTest {

	private static Instant testInstant;

	@BeforeAll
	public static void setup() {
		// "Mardi 7 Mars 2025, 17:16:45" en UTC
		testInstant = Instant.parse("2025-03-07T17:16:45Z");
		TimeZone.setDefault(TimeZone.getTimeZone("UTC")); // GitHub Actions
	}

	@Test
	public void testDuration_Nanoseconds() {
		Duration duration = TimeUtils.duration(TimeUnit.NANOSECONDS, 1000);
		assertEquals(1000, duration.toNanos(), "The duration in nanoseconds is incorrect");
	}

	@Test
	public void testDuration_Microseconds() {
		Duration duration = TimeUtils.duration(TimeUnit.MICROSECONDS, 1);
		assertEquals(1000, duration.toNanos(), "Conversion from microseconds to nanoseconds is incorrect");
	}

	@Test
	public void testDuration_Milliseconds() {
		Duration duration = TimeUtils.duration(TimeUnit.MILLISECONDS, 1);
		assertEquals(1, duration.toMillis(), "The duration in millisecondes is incorrect");
	}

	@Test
	public void testDuration_Seconds() {
		Duration duration = TimeUtils.duration(TimeUnit.SECONDS, 10);
		assertEquals(10, duration.toSeconds(), "The duration in secondes is incorrect");
	}

	@Test
	public void testDuration_Minutes() {
		Duration duration = TimeUtils.duration(TimeUnit.MINUTES, 5);
		assertEquals(5, duration.toMinutes(), "The duration in minutes is incorrect");
	}

	@Test
	public void testDuration_Hours() {
		Duration duration = TimeUtils.duration(TimeUnit.HOURS, 2);
		assertEquals(2, duration.toHours(), "The duration in hours is incorrect");
	}

	@Test
	public void testDuration_Days() {
		Duration duration = TimeUtils.duration(TimeUnit.DAYS, 1);
		assertEquals(1, duration.toDays(), "The duration in days is incorrect");
	}

	@Test
	public void testDuration_ZeroValue() {
		Duration duration = TimeUtils.duration(TimeUnit.SECONDS, 0);
		assertEquals(Duration.ZERO, duration, "The duration of 0 should be Duration.ZERO");
	}

	@Test
	public void testDuration_LargeValue() {
		Duration duration = TimeUtils.duration(TimeUnit.HOURS, 1000);
		assertEquals(1000, duration.toHours(), "The duration for a large value in hours is incorrect");
	}

	@Test
	void testParseToInstant_validInput() {
		Optional<Instant> result = TimeUtils.parseToInstant("1h 32m 16s");
		assertTrue(result.isPresent(), "The Instant shouldn't be empty");
		assertTrue(result.get().isAfter(Instant.now()), "The Instant returned must be after now");

		result = TimeUtils.parseToInstant("6d 4h 16m 16s");
		assertTrue(result.isPresent());
		assertTrue(result.get().isAfter(Instant.now()));

		result = TimeUtils.parseToInstant("4h 32m");
		assertTrue(result.isPresent());
		assertTrue(result.get().isAfter(Instant.now()));

		result = TimeUtils.parseToInstant("32m 10s");
		assertTrue(result.isPresent());
		assertTrue(result.get().isAfter(Instant.now()));

		result = TimeUtils.parseToInstant("10s");
		assertTrue(result.isPresent());
		assertTrue(result.get().isAfter(Instant.now()));
	}

	@Test
	void testParseToInstant_invalidInput() {
		Optional<Instant> result = TimeUtils.parseToInstant("invalid input");
		assertFalse(result.isPresent(), "The Instant shouldn't be present");

		result = TimeUtils.parseToInstant("");
		assertFalse(result.isPresent(), "The Instant shouldn't be present");

		result = TimeUtils.parseToInstant("test 4h 15m 16s");
		assertTrue(result.isPresent(), "The Instant should be valid even if the text is present before the time");
	}

	@Test
	void testParseToInstant_multipleOccurrences() {
		Optional<Instant> result = TimeUtils.parseToInstant("2d 4h 16m 10s");
		assertTrue(result.isPresent());
		assertTrue(result.get().isAfter(Instant.now()));
	}

	@Test
	void testParseToInstant_singleUnit() {
		Optional<Instant> result = TimeUtils.parseToInstant("15m");
		assertTrue(result.isPresent());
		assertTrue(result.get().isAfter(Instant.now()));

		result = TimeUtils.parseToInstant("30s");
		assertTrue(result.isPresent());
		assertTrue(result.get().isAfter(Instant.now()));
	}

	@Test
	void testFormatInstant_withDifferentLocales() {
		// US Format (American English)
		String formattedUS = TimeUtils.formatInstant(testInstant, TimeUtils.DATE_FULL, Locale.US);
		assertEquals("Friday, March 7, 2025", formattedUS, "Invalid US date format");

		// UK Format (British English)
		String formattedUK = TimeUtils.formatInstant(testInstant, TimeUtils.DATE_FULL, Locale.UK);
		assertEquals("Friday, 7 March 2025", formattedUK, "Invalid UK date format");

		// German Format
		String formattedDE = TimeUtils.formatInstant(testInstant, TimeUtils.DATE_FULL, Locale.GERMANY);
		assertEquals("Freitag, 7. März 2025", formattedDE, "Invalid German date format");

		// Japanese Format
		String formattedJA = TimeUtils.formatInstant(testInstant, TimeUtils.DATE_FULL, Locale.JAPAN);
		assertEquals("2025年3月7日金曜日", formattedJA, "Invalid Japanese date format");
	}

	@Test
	void testFormatInstant_withDifferentTimeZones() {
		// New York (UTC-5)
		String formattedNY = TimeUtils.formatInstant(testInstant, TimeUtils.TIME_HH_MM, Locale.US, ZoneId.of("America/New_York"));
		// Note : Il y a bien le "AM/PM", mais obligé de le remove, car caractère spécial.
		assertTrue(formattedNY.contains("12:16"), "Invalid format for New York timezone");

		// Tokyo (UTC+9)
		String formattedTokyo = TimeUtils.formatInstant(testInstant, TimeUtils.TIME_HH_MM, Locale.JAPAN, ZoneId.of("Asia/Tokyo"));
		assertEquals("2:16", formattedTokyo, "Invalid format for Tokyo timezone");
	}

	@Test
	void testFormatInstant_timeHHMM() {
		String formatted = TimeUtils.formatInstant(testInstant, TimeUtils.TIME_HH_MM, Locale.FRANCE);
		assertTrue(formatted.matches("\\d{1,2}:\\d{2}"), "The format is invalid");
	}

	@Test
	void testFormatInstant_timeHHMMSS() {
		String formatted = TimeUtils.formatInstant(testInstant, TimeUtils.TIME_HH_MM_SS, Locale.FRANCE);
		assertEquals("17:16:45", formatted, "The format is invalid");
	}

	@Test
	void testFormatInstant_dateShort() {
		String formatted = TimeUtils.formatInstant(testInstant, TimeUtils.DATE_SHORT, Locale.FRANCE);
		assertTrue(formatted.matches("\\d{1,2}/\\d{1,2}/\\d{2,4}"), "The format is invalid");
	}

	@Test
	void testFormatInstant_dateLong() {
		String formatted = TimeUtils.formatInstant(testInstant, TimeUtils.DATE_FULL, Locale.FRANCE);
		assertTrue(formatted.contains("7 mars 2025"), "The format is invalid" + formatted);
	}

	@Test
	void testFormatInstant_dateTimeShort() {
		String formatted = TimeUtils.formatInstant(testInstant, TimeUtils.DATE_TIME_SHORT, Locale.FRANCE);
		assertTrue(formatted.contains("7/3/25") || formatted.contains("07/03/2025"), "The format is invalid");
	}

	@Test
	void testFormatInstant_dateTimeFull() {
		String formatted = TimeUtils.formatInstant(testInstant, TimeUtils.DATE_TIME_FULL, Locale.FRANCE);
		assertTrue(formatted.contains("vendredi 7 mars 2025, 17:16:45"), "The format is invalid");
	}

	@Test
	void testFormatInstant_dateTimeFull_withDifferentTimeZones() {
		// New York (UTC-5)
		String newYorkFormatted = TimeUtils.formatInstant(testInstant, TimeUtils.DATE_TIME_FULL, Locale.US, ZoneId.of("America/New_York"));
		// Note : Il y a bien le "AM/PM", mais obligé de le remove, car caractère spécial.
		assertTrue(newYorkFormatted.contains("Friday, March 7, 2025, 12:16:45"), "Invalid format for New York timezone");

		// Tokyo (UTC+9)
		String tokyoFormatted = TimeUtils.formatInstant(testInstant, TimeUtils.DATE_TIME_FULL, Locale.JAPAN, ZoneId.of("Asia/Tokyo"));
		assertEquals("2025年3月8日土曜日 2:16:45", tokyoFormatted, "Invalid format for Tokyo timezone");
	}

	@Test
	public void testGetDurationFormatted_FullFormat() {
		Instant now = Instant.now();
		Instant after = now.plus(Duration.ofDays(2)
				.plusHours(5)
				.plusMinutes(30)
				.plusSeconds(15));

		String result = TimeUtils.getDurationFormatted(now, after, true);
		assertEquals("2d 5h 30m 15s", result, "The complete format is incorrect");
	}

	@Test
	public void testGetDurationFormatted_DaysIncluded() {
		Instant now = Instant.now();
		Instant after = now.plus(Duration.ofDays(1)
				.plusHours(3)
				.plusMinutes(45)
				.plusSeconds(10));

		String result = TimeUtils.getDurationFormatted(now, after, false);
		assertEquals("1d 3h 45m 10s", result, "Days should be included");
	}

	@Test
	public void testGetDurationFormatted_HoursOnly() {
		Instant now = Instant.now();
		Instant after = now.plus(Duration.ofHours(4)
				.plusMinutes(20)
				.plusSeconds(5));

		String result = TimeUtils.getDurationFormatted(now, after, false);
		assertEquals("4h 20m 5s", result, "Hours should be correctly formatted.");
	}

	@Test
	public void testGetDurationFormatted_MinutesOnly() {
		Instant now = Instant.now();
		Instant after = now.plus(Duration.ofMinutes(45)
				.plusSeconds(30));

		String result = TimeUtils.getDurationFormatted(now, after, false);
		assertEquals("45m 30s", result, "Minutes should be correctly formatted");
	}

	@Test
	public void testGetDurationFormatted_SecondsOnly() {
		Instant now = Instant.now();
		Instant after = now.plus(Duration.ofSeconds(10));

		String result = TimeUtils.getDurationFormatted(now, after, false);
		assertEquals("10s", result, "Only seconds should be displayed");
	}

	@Test
	public void testGetDurationFormatted_ZeroDuration() {
		Instant now = Instant.now();

		String result = TimeUtils.getDurationFormatted(now, now, false);
		assertEquals("?", result, "A zero duration should return '?'");
	}

	@Test
	public void testGetDurationFormatted_AllTrueWithZero() {
		Instant now = Instant.now();

		String result = TimeUtils.getDurationFormatted(now, now, true);
		assertEquals("?", result, "With `all = true`, a duration of zero should return '?'");
	}

	@Test
	public void testGetDurationFormatted_WithDefaultNow() {
		Instant after = Instant.now().plus(Duration.ofHours(1).plusMinutes(15));

		String result = TimeUtils.getDurationFormatted(after);
		assertNotNull(result, "The result should not be null");
	}

	@Test
	public void testGetSimpleTime_ExactMinutes() {
		assertEquals("00:00", TimeUtils.getSimpleTime(0), "0 seconds should give '00:00'");
		assertEquals("01:00", TimeUtils.getSimpleTime(60), "60 seconds should give '01:00'");
		assertEquals("10:00", TimeUtils.getSimpleTime(600), "600 seconds should give '10:00'");
	}

	@Test
	public void testGetSimpleTime_SecondsOnly() {
		assertEquals("00:05", TimeUtils.getSimpleTime(5), "5 seconds should give '00:05'");
		assertEquals("00:30", TimeUtils.getSimpleTime(30), "30 seconds should give '00:30'");
		assertEquals("00:59", TimeUtils.getSimpleTime(59), "59 seconds should give '00:59'");
	}

	@Test
	public void testGetSimpleTime_MinutesAndSeconds() {
		assertEquals("01:01", TimeUtils.getSimpleTime(61), "61 seconds should give '01:01'");
		assertEquals("02:45", TimeUtils.getSimpleTime(165), "165 seconds should give '02:45'");
		assertEquals("10:59", TimeUtils.getSimpleTime(659), "659 seconds should give '10:59'");
	}

	@Test
	public void testGetSimpleTime_LargeValues() {
		assertEquals("59:59", TimeUtils.getSimpleTime(3599), "3599 seconds should give '59:59'");
		assertEquals("> 1h", TimeUtils.getSimpleTime(3600), "3600 seconds should give '> 1h'");
		assertEquals("> 1h", TimeUtils.getSimpleTime(7200), "7200 seconds should give '> 1h'");
		assertEquals("> 1h", TimeUtils.getSimpleTime(5999), "5999 seconds should give '> 1h'");
	}
}
