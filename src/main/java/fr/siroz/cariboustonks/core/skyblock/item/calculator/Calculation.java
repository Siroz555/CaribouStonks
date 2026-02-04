package fr.siroz.cariboustonks.core.skyblock.item.calculator;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a single calculation.
 *
 * @param type       the {@link Type}
 * @param skyBlockId the skyBlockId
 * @param price      the price
 * @param count      the count
 */
public record Calculation(
		@NotNull Type type,
		@NotNull String skyBlockId,
		double price,
		int count
) {

	@Contract("_, _, _ -> new")
	static @NotNull Calculation of(Type type, String skyBlockId, double price) {
		return new Calculation(type, skyBlockId, price, 1);
	}

	@Contract("_, _, _, _ -> new")
	static @NotNull Calculation of(Type type, String skyBlockId, double price, int count) {
		return new Calculation(type, skyBlockId, price, count);
	}

	/**
	 * Represents the type of {@link Calculation}.
	 */
	public enum Type {
		// Cosmetics
		SKIN,
		DYE,
		RUNE,
		// Special Auction
		SHEN_AUCTION,
		WINNING_BID,
		// Armor Upgrades
		PRESTIGE,
		// Base
		REFORGE,
		// Enchanted Books
		ENCHANTED_BOOK,
		ULTIMATE_ENCHANTED_BOOK,
		// Regular enchantments
		ENCHANTMENT,
		ULTIMATE_ENCHANTMENT,
		ENCHANTMENT_UPGRADE,
		// Books
		FUMING_POTATO_BOOK,
		HOT_POTATO_BOOK,
		STATS_BOOK,
		ART_OF_WAR,
		ART_OF_PEACE,
		FARMING_FOR_DUMMIES,
		POLARVOID_BOOK,
		JALAPENO_BOOK,
		WET_BOOK,
		// Modifiers
		RECOMBOBULATOR,
		MASTER_STAR,
		TALISMAN_ENRICHMENT,
		WOOD_SINGULARITY,
		MANA_DISINTEGRATOR,
		TRANSMISSION_TUNER,
		ETHERWARP_CONDUIT,
		POCKET_SACK_IN_A_SACK,
		DIVAN_POWDER_COATING,
		POWER_SCROLL,
		WITHER_SCROLL,
		OVERCLOCKER,
		// Gemstones
		GEMSTONE,
		GEMSTONE_SLOT,
		// Parts
		DRILL_PART,
		ROD_PART,
		// Boosters
		BOOSTERS,
		;
	}
}
