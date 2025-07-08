package fr.siroz.cariboustonks.core.data.algo;

import fr.siroz.cariboustonks.core.data.hypixel.bazaar.Product;
import fr.siroz.cariboustonks.core.data.hypixel.bazaar.Summary;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Interface for analyzing Bazaar item statistics and market dynamics derived from order data.
 * Provides methods to calculate pricing trends, market spreads, imbalances, and other financial indicators.
 * <p>
 * TODO :
 *  Pour moi le #spreadPercentage, #orderImbalancePercentage, #vwap, #standardDeviation sont GOOD.
 *  Mais #calculateSellSideLiquiditySlope me retourne pas le résultat que je veux ? a vérifier -_-.
 */
public interface BazaarItemAnalytics {

	/**
	 * Determines the minimum price per unit from the "Buy Summary" of a given product.
	 * If no orders are available, it returns -1.
	 * <h3>Important:</h3>
	 * Dans l'API d'Hypixel, le {@code buySummary} est en réalité le côté <b>"SELL"</b>.
	 * C'est ce que l'on voit quand on survole un item dans le Bazaar, le "Buy Price : X"
	 * A ne pas confondre avec les Buy Orders.
	 *
	 * @param product the product
	 * @return the lowest price per unit
	 */
	static double buyPrice(@NotNull Product product) {
		List<Summary> summaries = product.buySummary();
		if (summaries.isEmpty()) {
			return -1;
		}

		return summaries.stream()
				.map(Summary::pricePerUnit)
				.min(Double::compareTo)
				.orElse(0.0);
	}

	/**
	 * Determines the maximum price per unit from the "Sell Summary" of a given product.
	 * If no orders are available, it returns -1.
	 * <h3>Important:</h3>
	 * Dans l'API d'Hypixel, le {@code sellSummary} est en réalité le côté <b>"BUY"</b>.
	 * C'est ce que l'on voit quand on survole un item dans le Bazaar, le "Sell Price : X"
	 * A ne pas confondre avec les Sell Orders.
	 *
	 * @param product the product
	 * @return the lowest price per unit
	 */
	static double sellPrice(@NotNull Product product) {
		List<Summary> summaries = product.sellSummary();
		if (summaries.isEmpty()) {
			return -1;
		}

		return summaries.stream()
				.map(Summary::pricePerUnit)
				.max(Double::compareTo)
				.orElse(0.0);
	}

	/**
	 * Retrieves the {@code weighted average buy price} of a given product.
	 *
	 * @param product the product
	 * @return the weighted average buy price of the product
	 */
	static double weightedAverageBuyPrice(@NotNull Product product) {
		return product.quickStatus().buyPrice();
	}

	/**
	 * Retrieves the {@code weighted average sell price} of a given product.
	 *
	 * @param product the product
	 * @return the weighted average sell price of the product
	 */
	static double weightedAverageSellPrice(@NotNull Product product) {
		return product.quickStatus().sellPrice();
	}

	/**
	 * Spread between the price of BUY and SELL.
	 *
	 * @param product the product
	 * @return the Spread %
	 */
	static double spreadPercentage(@NotNull Product product) {
		List<Summary> sellSummaries = product.sellSummary();
		List<Summary> buySummaries = product.buySummary();

		if (sellSummaries.isEmpty() || buySummaries.isEmpty()) {
			return -1;
		}

		// Meilleure offre d'ACHAT (buyers) = le PLUS HAUT prix (max)
		double bestBuy = sellSummaries.stream()
				.map(Summary::pricePerUnit)
				.max(Double::compareTo)
				.orElse(Double.NaN);

		// Meilleure offre de VENTE (sellers) = le PLUS BAS prix (min)
		double bestSell = buySummaries.stream()
				.map(Summary::pricePerUnit)
				.min(Double::compareTo)
				.orElse(Double.NaN);

		if (Double.isNaN(bestBuy) || Double.isNaN(bestSell) || bestSell == 0) {
			return -1;
		}

		// Formule : (Meilleur prix d'achat - Meilleur prix de vente) / Meilleur prix de vente * 100
		return ((bestBuy - bestSell) / bestSell) * 100;
	}

	/**
	 * Imbalance in the orders.
	 *
	 * @param product the product
	 * @return the Order Imbalance %
	 */
	static double orderImbalancePercentage(@NotNull Product product) {
		long numberOfSellers = product.quickStatus().buyOrders(); // Offres de VENTE
		long numberOfBuyers = product.quickStatus().sellOrders(); // Offres d'ACHAT
		long totalOrders = numberOfSellers + numberOfBuyers;

		if (totalOrders == 0) {
			return 0;
		}

		// Formule : (Vendeurs - Acheteurs) / Total
		return ((double) (numberOfSellers - numberOfBuyers) / totalOrders) * 100;
	}

	/**
	 * The Volume Weighted Average Price (VWAP) is an average that takes into account the volumes of each order.
	 *
	 * @param product the product
	 * @return the VWAP
	 */
	static double vwap(@NotNull Product product) {
		List<Summary> summaries = product.buySummary();
		if (summaries.isEmpty()) {
			return -1;
		}

		double totalValue = summaries.stream()
				.mapToDouble(s -> s.pricePerUnit() * s.amount())
				.sum();

		long totalVolume = summaries.stream()
				.mapToLong(Summary::amount)
				.sum();

		if (totalVolume == 0) {
			return -1;
		}

		return totalValue / totalVolume;
	}

	/**
	 * Calculates the standard deviation of prices in a Summary list.
	 * <p>
	 * This measures price volatility. Higher values indicate stronger fluctuations.
	 *
	 * @param summaries the summary of BUY or SELL orders
	 * @return the standard deviation
	 */
	static double standardDeviation(@NotNull List<Summary> summaries) {
		int count = summaries.size();
		if (count == 0) {
			return -1;
		}

		double sum = 0;
		double squareSum = 0;
		int validCount = 0;

		for (Summary s : summaries) {
			double price = s.pricePerUnit();
			if (Double.isFinite(price) && price > 0) {
				sum += price;
				squareSum += price * price;
				validCount++;
			}
		}

		if (validCount == 0) {
			return -1;
		}

		double mean = sum / validCount;
		double variance = (squareSum / validCount) - (mean * mean);

		return variance > 0 ? Math.sqrt(variance) : 0;
	}

	/**
	 * Order Curve analysis. Measure price variation as a function of cumulative volume.
	 * <p>
	 * Cette "pente" mesure l'élasticité-prix du marché :
	 * <ul>
	 *     <li>Pente faible (ex: 0.5) = Marché liquide (grands volumes échangés sans impact important sur le prix)</li>
	 *     <li>Pente forte (ex: 5.0) = Marché illiquide (petits volumes font bouger le prix significativement)</li>
	 * </ul>
	 *
	 * @param product                the product
	 * @param volumeTargetPercentage the volume target percentage
	 * @return the Slope
	 */
	static double calculateSellSideLiquiditySlope(@NotNull Product product, @Range(from = 1, to = 100) double volumeTargetPercentage) {

		// Mesurer la "pente" de la courbe d'ordres pour les sell orders,
		// c'est-à-dire de quantifier à quel point le prix évolue lorsque l'on cumule du volume.
		// Elle tente d'indiquer combien le prix augmente en moyenne
		// par unité de volume lorsqu'on monte dans les orders.

		List<Summary> sellOrders = product.buySummary();
		if (sellOrders.isEmpty() || volumeTargetPercentage <= 0 || volumeTargetPercentage > 100) {
			return -1;
		}

		// Trier par ordre croissant des prix
		List<Summary> sorted = new ArrayList<>(sellOrders);
		sorted.sort(Comparator.comparingDouble(Summary::pricePerUnit));

		// Calcul du volume total en double pour éviter les erreurs d'arrondi
		double totalVolume = sorted.stream().mapToDouble(Summary::amount).sum();
		double targetVolume = totalVolume * (volumeTargetPercentage / 100.0D);

		if (targetVolume <= 0) {
			return -1;
		}

		double cumulativeVolume = 0D;
		double startPrice = sorted.getFirst().pricePerUnit();
		double targetPrice = startPrice;

		for (Summary s : sorted) {
			double orderVolume = s.amount();
			// Gérer le cas où l'ordre dépasse le volume cible
			if (cumulativeVolume + orderVolume >= targetVolume) {
				double remainingVolume = targetVolume - cumulativeVolume;
				// Pondération du prix pour le volume partiel
				targetPrice = (cumulativeVolume * targetPrice + remainingVolume * s.pricePerUnit()) / targetVolume;
				break;
			}

			cumulativeVolume += orderVolume;
			targetPrice = s.pricePerUnit();
		}

		// Le slope, représente l'augmentation moyenne du prix par unité de volume cumulée jusqu'au seuil cible.
		// Une pente faible indique que même en cumulant beaucoup de volume,
		// le prix ne change pas beaucoup (ce qui peut être signe d'une bonne liquidité).
		// À l'inverse, une pente élevée peut indiquer une liquidité plus faible

		// Slope = (différence de prix) / volume cible
		return (targetPrice - startPrice) / targetVolume;
	}
}
