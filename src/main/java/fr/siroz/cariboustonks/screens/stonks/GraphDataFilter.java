package fr.siroz.cariboustonks.screens.stonks;

import fr.siroz.cariboustonks.core.skyblock.data.generic.ItemPrice;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.NonNull;

/**
 * Méthodes pour filtrer les données pour le graphique selon le type de {@link Granularity}.
 */
interface GraphDataFilter {

	/**
	 * Type de granularité temporel
	 */
	enum Granularity {
		HOUR, DAY, WEEK, MONTH
	}

	/**
	 * Appliquer un filtre temporel {@link Granularity} et trie ensuite les éléments du plus ancien au plus récent.
	 * <p>
	 * Collecte les éléments tout en éliminant les doublons.
	 * <p>
	 * Si le type est {@link Granularity#HOUR} / {@link Granularity#DAY}, tous les prices sont keep,
	 * car les granularités sont déjà très précises.
	 * <p>
	 * Si le type est {@link Granularity#WEEK} / {@link Granularity#MONTH}, uen réduction est appliqué,
	 * tout en gardant un échantillonnage régulier. Utilisation d'une troncature temporelle.
	 * Pour {@link Granularity#WEEK}, garde un point par heure, mais essaie de prendre le point
	 * le plus proche de l'heure actuelle ({@link Instant#now()} dans chaque heure).
	 * Pour {@link Granularity#MONTH}, garde un point par jour, mais essaie de prendre le point
	 * le plus proche de l'heure actuelle dans chaque jour.
	 * L'ItemPrice le plus ancien et le plus récent sont conservés.
	 *
	 * @param prices      the prices to filter
	 * @param granularity the temporal granularity
	 * @return a new list of filtered prices
	 */
	static List<ItemPrice> filterData(@NonNull List<ItemPrice> prices, @NonNull Granularity granularity) {
		Instant now = Instant.now();
		// ChronoUnit.WEEKS -> UnsupportedTemporalTypeException: Unsupported unit: Weeks
		// parce que c'est des Instant..
		Instant threshold = switch (granularity) {
			case HOUR -> now.minus(1, ChronoUnit.HOURS);
			case DAY -> now.minus(1, ChronoUnit.DAYS);
			case WEEK -> now.minus(7, ChronoUnit.DAYS);
			case MONTH -> now.minus(30, ChronoUnit.DAYS);
		};

		List<ItemPrice> filteredPrices = prices.stream()
				.filter(item -> !item.time().isBefore(threshold))
				.sorted(Comparator.comparing(ItemPrice::time))
				.toList();

		if (granularity == Granularity.WEEK || granularity == Granularity.MONTH) {
			Map<Instant, ItemPrice> sampledPoints = new LinkedHashMap<>();
			ChronoUnit unit = (granularity == Granularity.WEEK) ? ChronoUnit.HOURS : ChronoUnit.DAYS;

			for (ItemPrice item : filteredPrices) {
				Instant rounded = item.time().truncatedTo(unit); // Tronquer à l'heure ou au jour
				ItemPrice closest = sampledPoints.getOrDefault(rounded, null);

				// Garde le point le plus proche dans cette période tronquée
				if (closest == null || Math.abs(Duration.between(item.time(), rounded).toMillis()) <
						Math.abs(Duration.between(closest.time(), rounded).toMillis())) {
					sampledPoints.put(rounded, item);
				}
			}

			List<ItemPrice> reducedPrices = new ArrayList<>(sampledPoints.values());
			if (!reducedPrices.contains(filteredPrices.getFirst())) {
				reducedPrices.addFirst(filteredPrices.getFirst()); // le plus ancien
			}

			if (!reducedPrices.contains(filteredPrices.getLast())) {
				reducedPrices.add(filteredPrices.getLast()); // le plus récent
			}

			reducedPrices.sort(Comparator.comparing(ItemPrice::time));

			return reducedPrices;
		}

		return filteredPrices;
	}
}
