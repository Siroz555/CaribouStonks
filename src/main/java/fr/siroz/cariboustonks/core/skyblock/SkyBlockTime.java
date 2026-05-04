package fr.siroz.cariboustonks.core.skyblock;

/**
 * Represents a point in time within SkyBlock's custom calendar system.
 */
public record SkyBlockTime(
		int year,
		int month,
		int day,
		int hour
) implements Comparable<SkyBlockTime> {

	public static final SkyBlockTime DEFAULT = new SkyBlockTime(1, 1, 1, 0);

	private static final long HOUR_MILLIS = 50_000;
	private static final long DAY_MILLIS = HOUR_MILLIS * 24;
	private static final long MONTH_MILLIS = DAY_MILLIS * 31;
	private static final long SEASON_MILLIS = MONTH_MILLIS * 3;
	private static final long YEAR_MILLIS = SEASON_MILLIS * 4;

	public static SkyBlockTime of(long millis) {
		return new SkyBlockTime(
				(int) (millis / YEAR_MILLIS + 1), // 1-based
				(int) (millis / MONTH_MILLIS % 12), // 0-based
				(int) (millis / DAY_MILLIS % 31 + 1), // 1-based
				(int) (millis / HOUR_MILLIS % 24)  // 0-based
		);
	}

	@Override
	public int compareTo(SkyBlockTime other) {
		if (year != other.year()) return Integer.compare(year, other.year());
		if (month != other.month()) return Integer.compare(month, other.month());
		if (day != other.day()) return Integer.compare(day, other.day());
		return Integer.compare(hour, other.hour());
	}
}
