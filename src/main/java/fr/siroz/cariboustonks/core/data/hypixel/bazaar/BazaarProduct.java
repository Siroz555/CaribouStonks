package fr.siroz.cariboustonks.core.data.hypixel.bazaar;

import fr.siroz.cariboustonks.core.data.hypixel.fetcher.BazaarFetcher;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a Product in the Bazaar.
 * <p>
 * The data is officially provided by the Hypixel SKyBlock Bazaar API.
 * <p>
 * The various values present in this Product snapshot are calculated and manipulated during
 * the fetch by {@link BazaarFetcher} every 5 minutes.
 * <p>
 * The {@code spread}, {@code spreadPercentage},
 * {@code buyPriceStdDev}, {@code sellPriceStdDev},
 * {@code buyVelocity} and {@code sellVelocity}
 * are calculated by {@link BazaarItemAnalytics}.
 *
 * @param skyBlockId               the SkyBlock ID
 * @param buyPrice                 the Instant Buy Price. The price displayed when you want to instantly buy a product;
 *                                 it is the best offer among the {@code sell orders}.
 * @param sellPrice                the Instant Sell Price. The price displayed when you want to instantly sell a product;
 *                                 it is the best offer among the {@code buy orders}.
 * @param weightedAverageBuyPrice  the Weighted Buy Average of the top 2% of orders by volume. It is not the buy price displayed, but rather the average.
 * @param weightedAverageSellPrice the Weighted Sell Average of the top 2% of orders by volume. It is not the sell price displayed, but rather the average.
 * @param buyVolume                the Buy sum item amounts in all orders.
 * @param sellVolume               the Sell sum item amounts in all orders.
 * @param buyMovingWeek            the Buy historic transacted volume from the last 7d + live state.
 * @param sellMovingWeek           the Sell historic transacted volume from the last 7d + live state.
 * @param buyOrders                the count of active Buy orders.
 * @param sellOrders               the count of active Sell orders.
 * @param spread                   the absolute spread between buy and sell prices.
 * @param spreadPercentage         the spread as a percentage of the sell price.
 *                                 <li>{@code < 0.5%} = Highly competitive market, low profit margin</li>
 *                                 <li>{@code 0.5-2%} = Normal opportunity</li>
 *                                 <li>{@code > 2%} = Excellent opportunity (or illiquid market)</li>
 * @param buyMedianPrice           the median Buy price
 * @param sellMedianPrice          the median Sell price
 * @param buyPriceStdDev           the standard deviation of the Buy prices.
 *                                 Standard deviation measures price volatility and market stability.
 *                                 Lower values indicate stable prices, higher values indicate volatility.
 * @param sellPriceStdDev          the standard deviation of the Sell prices.
 *                                 Standard deviation measures price volatility and market stability.
 *                                 Lower values indicate stable prices, higher values indicate volatility.
 * @param buyVelocity              the Buy price velocity (momentum indicator).
 *                                 Compares current volume to the daily average from the past week.
 *                                 If velocity = 2.5, volume is 2.5 times higher than normal > strong demand > price will likely rise.
 *                                 <li>{@code < 0.5}= Very low activity (discontinued product)</li>
 *                                 <li>{@code 0.5-1.5}= Normal activity</li>
 *                                 <li>{@code 1.5-3}= High activity (growing interest)</li>
 *                                 <li>{@code 3}= Peak activity (event, speculation)</li>
 * @param sellVelocity             the Sell price velocity (momentum indicator).
 *                                 Compares current volume to the daily average from the past week.
 *                                 If velocity = 2.5, volume is 2.5 times higher than normal > strong demand > price will likely rise.
 *                                 <li>{@code < 0.5}= Very low activity (discontinued product)</li>
 *                                 <li>{@code 0.5-1.5}= Normal activity</li>
 *                                 <li>{@code 1.5-3}= High activity (growing interest)</li>
 *                                 <li>{@code 3}= Peak activity (event, speculation)</li>
 * @see BazaarItemAnalytics
 */
public record BazaarProduct(
		@NotNull String skyBlockId,
		double buyPrice,
		double sellPrice,
		double weightedAverageBuyPrice,
		double weightedAverageSellPrice,
		long buyVolume,
		long sellVolume,
		long buyMovingWeek,
		long sellMovingWeek,
		long buyOrders,
		long sellOrders,
		double spread,
		double spreadPercentage,
		double buyMedianPrice,
		double sellMedianPrice,
		double buyPriceStdDev,
		double sellPriceStdDev,
		double buyVelocity,
		double sellVelocity
) {

	/**
	 * @see #spread()
	 */
	@Deprecated
	public double getFlipProfitMargin() {
		return buyPrice - sellPrice;
	}

	/**
	 * @see #spreadPercentage()
	 */
	@Deprecated
	public double getFlipProfitPercentage() {
		if (sellPrice == 0) return 0;
		return ((buyPrice - sellPrice) / sellPrice) * 100;
	}
}
