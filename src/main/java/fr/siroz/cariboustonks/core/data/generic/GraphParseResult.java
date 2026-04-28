package fr.siroz.cariboustonks.core.data.generic;

import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record GraphParseResult(List<ItemPrice> prices, @Nullable AuctionStatistics auctionStats) {
	public static @NotNull GraphParseResult ofBazaar(List<ItemPrice> prices) {
		return new GraphParseResult(prices, null);
	}

	public static @NotNull GraphParseResult ofAuction(List<ItemPrice> prices, AuctionStatistics stats) {
		return new GraphParseResult(prices, stats);
	}
}
