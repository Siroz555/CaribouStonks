package fr.siroz.cariboustonks.skyblock.data.hypixel.bazaar;

import java.util.List;
import org.jetbrains.annotations.NotNull;

/**
 * Interface for analyzing Bazaar Products statistics and market dynamics derived from order data.
 */
public interface BazaarItemAnalytics {

	/**
	 * Retrieves the instant buy price.
	 *
	 * @param summaryPrices the summary prices of the product
	 * @return the instant buy price or -1 if no orders are available
	 */
	static double buyPrice(@NotNull List<Double> summaryPrices) {
		return summaryPrices.isEmpty() ? -1 : summaryPrices.stream().min(Double::compareTo).orElse(0.0);
	}

	/**
	 * Retrieves the instant sell price.
	 *
	 * @param summaryPrices the summary prices of the product
	 * @return the sell price or -1 if no orders are available
	 */
	static double sellPrice(@NotNull List<Double> summaryPrices) {
		return summaryPrices.isEmpty() ? -1 : summaryPrices.stream().max(Double::compareTo).orElse(0.0);
	}

	/**
	 * Calculates the absolute spread between buy and sell prices.
	 * The spread represents the immediate profit margin available for flipping.
	 * <p>
	 * Siroz NOTE:
	 * <p>
	 * Le spread est la différence brute entre le prix d'achat instantané et le prix de vente instantané.
	 * Plus le spread est élevé, plus la marge potentielle est grande pour faire du flip.
	 *
	 * @param buyPrice  the lowest ask price (instant buy price)
	 * @param sellPrice the highest bid price (instant sell price)
	 * @return the absolute price difference between buy and sell
	 */
	static double spread(double buyPrice, double sellPrice) {
		return buyPrice - sellPrice;
	}

	/**
	 * Calculates the spread as a percentage of the sell price.
	 * This normalized metric allows comparison across products with different price ranges.
	 * <p>
	 * Siroz NOTE:
	 * <p>
	 * Le spread exprimé en pourcentage du prix de vente. Cette métrique normalise le spread.
	 *
	 * @param buyPrice  the lowest ask price (instant buy price)
	 * @param sellPrice the highest bid price (instant sell price)
	 * @return the spread percentage, or 0 if sellPrice is 0
	 */
	static double spreadPercentage(double buyPrice, double sellPrice) {
		if (sellPrice == 0) return 0;
		// Formule : (Meilleur prix d'achat - Meilleur prix de vente) / Meilleur prix de vente * 100
		return ((buyPrice - sellPrice) / sellPrice) * 100;
	}

	/**
	 * Calculates the standard deviation of a list of prices.
	 * Standard deviation measures price volatility and market stability.
	 * Lower values indicate stable prices, higher values indicate volatility.
	 * <p>
	 * Siroz NOTE:
	 * <p>
	 * L'écart-type mesure la dispersion des prix autour de la moyenne.
	 * C'est l'indicateur de volatilité "le plus utilisé en finance".
	 * C'est pour identifier les produits stables vs spéculatifs.
	 *
	 * @param prices list of prices to analyze
	 * @return the standard deviation, or 0 if the list is empty
	 */
	static double standardDeviation(@NotNull List<Double> prices) {
		if (prices.isEmpty()) return 0;

		double mean = prices.stream()
				.mapToDouble(Double::doubleValue)
				.average()
				.orElse(0);

		double variance = prices.stream()
				.mapToDouble(price -> Math.pow(price - mean, 2))
				.average()
				.orElse(0);

		return Math.sqrt(variance);
	}

	/**
	 * Calculates the price velocity (momentum indicator).
	 * Compares current volume to the daily average from the past week.
	 * Values > 1 indicate above-average activity, suggesting price movement potential.
	 * <p>
	 * Siroz NOTE:
	 * <p>
	 * La vélocité compare le volume actuel à la moyenne quotidienne de la semaine passée.
	 * C'est un indicateur de momentum.
	 * Permet de détecter les tendances et anticiper les mouvements de prix.
	 *
	 * @param currentVolume    the current trading volume
	 * @param movingWeekVolume the total volume from the past 7 days
	 * @return the velocity ratio, or 0 if movingWeekVolume is 0
	 */
	static double priceVelocity(long currentVolume, long movingWeekVolume) {
		if (movingWeekVolume == 0) return 0;
		// Ratio du volume actuel vs moyenne hebdomadaire (indicateur de momentum)
		double dailyAverage = movingWeekVolume / 7.0;
		return currentVolume / dailyAverage;
	}
}
