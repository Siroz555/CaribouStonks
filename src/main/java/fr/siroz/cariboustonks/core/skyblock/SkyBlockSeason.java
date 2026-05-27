package fr.siroz.cariboustonks.core.skyblock;

public enum SkyBlockSeason {
	SPRING,
	SUMMER,
	AUTUMN,
	WINTER;

	public static final SkyBlockSeason[] VALUES = values();

	public enum Month {
		EARLY_SPRING,
		SPRING,
		LATE_SPRING,
		EARLY_SUMMER,
		SUMMER,
		LATE_SUMMER,
		EARLY_AUTUMN,
		AUTUMN,
		LATE_AUTUMN,
		EARLY_WINTER,
		WINTER,
		LATE_WINTER;

		public static final Month[] VALUES = values();
	}
}
