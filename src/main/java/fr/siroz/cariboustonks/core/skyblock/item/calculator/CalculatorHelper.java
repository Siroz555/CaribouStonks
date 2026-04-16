package fr.siroz.cariboustonks.core.skyblock.item.calculator;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.core.skyblock.data.hypixel.bazaar.BazaarProduct;
import fr.siroz.cariboustonks.core.skyblock.data.hypixel.item.SkyBlockItemData;
import fr.siroz.cariboustonks.util.ItemLookupKey;
import fr.siroz.cariboustonks.util.StonksUtils;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleMaps;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.ToDoubleFunction;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

final class CalculatorHelper {

	/**
	 * Represents the worth of an item type.
	 * <h2>Worth Mapping</h2>
	 * See <a href="https://github.com/Altpapier/SkyHelper-Networth/blob/master/constants/applicationWorth.js">SkyHelper Networth Calculator GitHub</a>
	 * <p>
	 * SIROZ-NOTE : J'ai volontairement modifié certaine valeur de SkyHelper, pour avoir de meilleurs résultats.
	 * Avec les mises à jour du SkyBlock, certain item type ont de meilleurs prix (moins volatil) (selon moi).
	 */
	public static final Object2DoubleMap<String> WORTH = Object2DoubleMaps.unmodifiable(StonksUtils.make(new Object2DoubleOpenHashMap<>(), map -> {
		// Cosmetics
		map.put("skins", 0.67); // Community (-33% when applied)
		map.put("dye", 0.9);
		map.put("runes", 0.6);
		// Pets
		map.put("petItem", 1);
		// Special Auction
		map.put("specialAuctionPrice", 1);
		map.put("winningBid", 1);
		// Base
		map.put("reforge", 1);
		// Enchantements
		map.put("enchantments", 0.95); // 0.85
		map.put("enchantmentUpgrades", 0.95); // 0.8
		// Books
		map.put("fumingPotatoBook", 0.75); // 0.6
		map.put("hotPotatoBook", 1);
		map.put("artOfWar", 0.8); // 0.6
		map.put("artOfPeace", 0.8);
		map.put("farmingForDummies", 0.6); // 0.5
		map.put("polarvoidBook", 0.8); // 1
		map.put("jalapenoBook", 0.8);
		// Modifiers
		map.put("recombobulator", 0.95); // 0.8
		map.put("enrichment", 0.75); // 0.5
		map.put("woodSingularity", 0.7); // 0.5
		map.put("manaDisintegrator", 0.8);
		map.put("transmissionTuner", 0.7);
		map.put("etherwarp", 1);
		map.put("pocketSackInASack", 0.85); // 0.7
		map.put("divanPowderCoating", 0.8);
		map.put("powerScroll", 0.6); // 0.5
		map.put("witherScroll", 1);
		map.put("overclocker", 0.8); // -
		// Gemstones
		map.put("gemstones", 1);
		map.put("gemstoneSlots", 0.75); // 0.6
		// Parts
		map.put("drillPart", 1);
		map.put("rodPart", 1);
		// Boosters
		map.put("boosters", 0.8);
		// Others
		map.put("essence", 0.9); // 0.9
		map.put("masterStars", 1);
		// Default
		map.defaultReturnValue(1d);
	}));

	private CalculatorHelper() {
	}

	public static double getPrice(String skyBlockId) {
		if (skyBlockId == null || skyBlockId.isEmpty()) {
			return 0.0D;
		}
		return CaribouStonks.skyBlock().getHypixelDataSource()
				.getBazaarItem(skyBlockId)
				.map(BazaarProduct::buyPrice)
				.filter(d -> d > 0)
				.orElseGet(() -> CaribouStonks.skyBlock().getGenericDataSource()
						.getLowestBin(ItemLookupKey.ofNeuId(skyBlockId.replace(":", "-")))
						.filter(d -> d > 0)
						.orElse(0.0D)
				);
	}

	public static SkyBlockItemData getItemData(String skyBlockId) {
		return CaribouStonks.skyBlock().getHypixelDataSource().getSkyBlockItem(skyBlockId);
	}

	public static Optional<UnlockedSlot> parseUnlockedSlot(@Nullable String raw) {
		if (raw == null || raw.isBlank()) return Optional.empty();
		// Parse "TYPE_index"
		String[] parts = raw.split("_");
		if (parts.length < 2) {
			return Optional.empty();
		}

		try {
			String type = parts[0].toUpperCase(Locale.ENGLISH);
			int idx = Integer.parseInt(parts[1]);
			return Optional.of(new UnlockedSlot(type, idx));
		} catch (NumberFormatException _) {
			return Optional.empty();
		}
	}

	public record UnlockedSlot(String type, int index) {
	}

	public static void computeUpgradeCosts(
			ToDoubleFunction<String> prices,
			PriceAccumulator acc,
			@NonNull List<SkyBlockItemData.GearUpgrade> upgradesCosts,
			String prestigeItem
	) {
		double cost = 0;
		for (SkyBlockItemData.GearUpgrade upgrade : upgradesCosts) {
			boolean isEssenceUpgrade = upgrade.essenceType().isPresent();
			double upgradePrice = (isEssenceUpgrade
					? prices.applyAsDouble("ESSENCE_" + upgrade.essenceType().get())
					: prices.applyAsDouble(upgrade.itemId().orElse(""))
			) * upgrade.amount().orElse(0);

			upgradePrice *= isEssenceUpgrade ? WORTH.applyAsDouble("essence") : 1;
			cost += upgradePrice;
		}

		if (cost > 0) {
			Calculation calc = Calculation.of(Calculation.Type.PRESTIGE, prestigeItem, cost);
			acc.add(calc.price());
			acc.push(calc);
		}
	}
}
