package fr.siroz.cariboustonks.feature.stonks.graph;

import fr.siroz.cariboustonks.core.data.generic.ItemPrice;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Deprecated
public interface PriceCompleter {

	static List<ItemPrice> completeMissingPrices(List<ItemPrice> prices, ChronoUnit unit) {
		List<ItemPrice> completedPrices = new ArrayList<>();

		for (int i = 0; i < prices.size() - 1; i++) {
			ItemPrice current = prices.get(i);
			ItemPrice next = prices.get(i + 1);

			completedPrices.add(current);

			// Vérifier s'il manque des périodes entre current et next
			Instant time = current.time();
			while (time.plus(1, unit).isBefore(next.time())) {
				time = time.plus(1, unit);
				double interpolatedPrice = interpolatePrice(current, next, time);
				completedPrices.add(new ItemPrice(time, interpolatedPrice, 0.0));
			}
		}

		completedPrices.add(prices.getLast()); // Ajouter le dernier prix
		return completedPrices;
	}

	private static double interpolatePrice(ItemPrice current, ItemPrice next, Instant time) {
		double totalDuration = ChronoUnit.SECONDS.between(current.time(), next.time());
		double elapsedDuration = ChronoUnit.SECONDS.between(current.time(), time);

		double currentPrice = current.buyPrice();
		double nextPrice = next.buyPrice();

		// Interpolation linéaire
		return currentPrice + (nextPrice - currentPrice) * (elapsedDuration / totalDuration);
	}
}
