package fr.siroz.cariboustonks.core.skyblock;

/**
 * Represents a point in time within SkyBlock's custom calendar system.
 */
public record SkyBlockTime(
		int year,
		int month,
		int day,
		int hour,
		int minute,
		int second
) implements Comparable<SkyBlockTime> {

	public static final SkyBlockTime DEFAULT = new SkyBlockTime(1, 1, 1, 0, 0, 0);

	@Override
	public int compareTo(SkyBlockTime other) {
		if (year != other.year()) return Integer.compare(year, other.year());
		if (month != other.month()) return Integer.compare(month, other.month());
		if (day != other.day()) return Integer.compare(day, other.day());
		if (hour != other.hour()) return Integer.compare(hour, other.hour());
		if (minute != other.minute()) return Integer.compare(minute, other.minute());
		return Integer.compare(second, other.second);
	}
}
