package fr.siroz.cariboustonks.core.data.generic;

import fr.siroz.cariboustonks.util.TimeUtils;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

/**
 * Auction statistics.
 */
public final class AuctionStatistics {

	/**
	 * A single raw data point, representing a snapshot of auction activity at a given moment in time
	 *
	 * @param timestamp      the instant
	 * @param itemsSold      the number of items sold up
	 * @param lowestBinPrice the lowest observed BIN price
	 */
	public record AuctionDataPoint(
			Instant timestamp,
			int itemsSold,
			OptionalDouble lowestBinPrice
	) {
	}

	/**
	 * A fully aggregated summary for a single UTC calendar day
	 *
	 * @param date           the UTC calendar date
	 * @param totalItemsSold total number of items sold (sum)
	 * @param avgPrice       average lowest BIN price across all snapshots
	 * @param minPrice       lowest BIN price observed
	 * @param hasPrice       whether at least one price data point was available
	 */
	private record DailyAggregate(
			LocalDate date,
			int totalItemsSold, // somme, c'est un comptage
			double avgPrice,
			double minPrice,
			boolean hasPrice
	) {
	}

	/**
	 * Summarized auction statistics for a specific time period
	 *
	 * @param totalItemsSold    total number of items sold
	 * @param avgDailyItemsSold average number of items sold per day
	 * @param daysWithData      number of calendar days for which data was available
	 * @param avgPrice          the average lowest BIN prices
	 * @param minPrice          the lowest BIN price
	 */
	public record PeriodStats(
			long totalItemsSold,
			double avgDailyItemsSold,
			int daysWithData,
			double avgPrice,
			double minPrice
	) {
		public static final PeriodStats EMPTY = new PeriodStats(0, 0.0, 0, 0.0, 0.0);

		public boolean hasPriceData() {
			return avgPrice > 0;
		}
	}

	private final PeriodStats today;
	private final PeriodStats last7Days;
	private final PeriodStats last30Days;

	private AuctionStatistics(PeriodStats today, PeriodStats last7Days, PeriodStats last30Days) {
		this.today = today;
		this.last7Days = last7Days;
		this.last30Days = last30Days;
	}

	public PeriodStats today() {
		return today;
	}

	public PeriodStats last7Days() {
		return last7Days;
	}

	public PeriodStats last30Days() {
		return last30Days;
	}

	/**
	 * Point d'entrée unique. Prend les points bruts de l'API
	 * (30 jours, intervalles irréguliers) et produit des statistiques harmonisées.
	 *
	 * @param rawPoints the raw data points
	 * @return {@link AuctionStatistics}
	 */
	public static @NotNull AuctionStatistics compute(@NotNull List<AuctionDataPoint> rawPoints) {
		if (rawPoints.isEmpty()) {
			return new AuctionStatistics(PeriodStats.EMPTY, PeriodStats.EMPTY, PeriodStats.EMPTY);
		}

		LocalDate now = LocalDate.now(TimeUtils.UTC);
		// Regroupement par jour
		Map<LocalDate, DailyAggregate> dailies = rawPoints.stream()
				.collect(Collectors.groupingBy(
						p -> p.timestamp().atZone(TimeUtils.UTC).toLocalDate()
				))
				.entrySet().stream()
				.collect(Collectors.toMap(
						Map.Entry::getKey,
						e -> aggregateDay(e.getKey(), e.getValue())
				));

		rawPoints.clear();

		return new AuctionStatistics(
				computePeriod(dailies, now, now),
				computePeriod(dailies, now.minusDays(6), now),
				computePeriod(dailies, now.minusDays(29), now)
		);
	}

	private static @NotNull DailyAggregate aggregateDay(LocalDate date, @NotNull List<AuctionDataPoint> points) {
		// Aggregate la liste de points avec le même jour calendaire UTC.
		// binListings est une moyenne, car il représente une snapshot des actives
		// itemsSold est un sum, parce qu'il représente un chiffre cumulatif

		int totalItemsSold = points.stream()
				.mapToInt(AuctionDataPoint::itemsSold)
				.sum();

		// Filtre uniquement les points qui ont un prix
		DoubleSummaryStatistics priceStats = points.stream()
				.map(AuctionDataPoint::lowestBinPrice)
				.filter(OptionalDouble::isPresent)
				.mapToDouble(OptionalDouble::getAsDouble)
				.summaryStatistics();

		boolean hasPrice = priceStats.getCount() > 0;

		return new DailyAggregate(
				date,
				totalItemsSold,
				hasPrice ? priceStats.getAverage() : 0.0,
				hasPrice ? priceStats.getMin() : 0.0,
				hasPrice
		);
	}

	private static PeriodStats computePeriod(
			@NotNull Map<LocalDate, DailyAggregate> dailies,
			LocalDate from,
			LocalDate to
	) {
		// Computes PeriodStats summary a partir des données pre-aggregated.
		// [from, to] inclusive si possible

		List<DailyAggregate> inRange = dailies.values().stream()
				.filter(d -> !d.date().isBefore(from) && !d.date().isAfter(to))
				.sorted(Comparator.comparing(DailyAggregate::date))
				.toList();

		if (inRange.isEmpty()) return PeriodStats.EMPTY;

		IntSummaryStatistics soldStats = inRange.stream()
				.mapToInt(DailyAggregate::totalItemsSold)
				.summaryStatistics();

		// Uniquement les jours qui ont un prix
		List<DailyAggregate> withPrice = inRange.stream()
				.filter(DailyAggregate::hasPrice)
				.toList();

		double avgPrice = withPrice.stream()
				.mapToDouble(DailyAggregate::avgPrice)
				.average()
				.orElse(0.0);

		double minPrice = withPrice.stream()
				.mapToDouble(DailyAggregate::minPrice)
				.min()
				.orElse(0.0);

		return new PeriodStats(
				soldStats.getSum(),
				soldStats.getAverage(),
				inRange.size(),
				avgPrice,
				minPrice
		);
	}
}
