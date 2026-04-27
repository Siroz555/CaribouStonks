package fr.siroz.cariboustonks.core.skyblock.data.generic;

import java.util.List;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public record GraphParseResult(List<ItemPrice> prices, @Nullable AuctionStatistics auctionStats) {
	public static @NonNull GraphParseResult ofBazaar(List<ItemPrice> prices) {
		return new GraphParseResult(prices, null);
	}

	public static @NonNull GraphParseResult ofAuction(List<ItemPrice> prices, AuctionStatistics stats) {
		return new GraphParseResult(prices, stats);
	}
}
