package fr.siroz.cariboustonks.core.skyblock.item.metadata;

import java.util.OptionalInt;
import java.util.OptionalLong;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the {@code Special Auction} applied to an item.
 *
 * @param winningBid the winning bid
 * @param price      the price paid
 * @param bid        the bid
 * @param auction    the auction
 */
public record SpecialAuctionInfo(
		OptionalLong winningBid,
		OptionalLong price,
		OptionalInt bid,
		OptionalInt auction
) {

	public static final SpecialAuctionInfo EMPTY = new SpecialAuctionInfo(
			OptionalLong.empty(),
			OptionalLong.empty(),
			OptionalInt.empty(),
			OptionalInt.empty()
	);

	public static SpecialAuctionInfo ofNbt(@NotNull CompoundTag customData) {
		try {
			OptionalLong winningBid = customData.getLong("winning_bid")
					.map(OptionalLong::of)
					.orElse(OptionalLong.empty());
			OptionalLong price = customData.getLong("price")
					.map(OptionalLong::of)
					.orElse(OptionalLong.empty());
			OptionalInt bid = customData.getInt("bid")
					.map(OptionalInt::of)
					.orElse(OptionalInt.empty());
			OptionalInt auction = customData.getInt("auction")
					.map(OptionalInt::of)
					.orElse(OptionalInt.empty());

			return new SpecialAuctionInfo(winningBid, price, bid, auction);
		} catch (Exception ignored) {
			return EMPTY;
		}
	}
}
