package fr.siroz.cariboustonks.core.skyblock.item.calculator;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.core.skyblock.data.hypixel.bazaar.BazaarProduct;
import fr.siroz.cariboustonks.core.skyblock.data.hypixel.item.SkyBlockItemData;
import fr.siroz.cariboustonks.util.ItemLookupKey;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.ToDoubleFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class CalculatorHelper {

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
		} catch (NumberFormatException ex) {
			return Optional.empty();
		}
	}

	public record UnlockedSlot(String type, int index) {
	}

	public static void computeUpgradeCosts(
			ToDoubleFunction<String> prices,
			PriceAccumulator acc,
			@NotNull List<SkyBlockItemData.GearUpgrade> upgradesCosts,
			String prestigeItem
	) {
		double cost = 0;
		for (SkyBlockItemData.GearUpgrade upgrade : upgradesCosts) {
			boolean isEssenceUpgrade = upgrade.essenceType().isPresent();
			double upgradePrice = (isEssenceUpgrade
					? prices.applyAsDouble("ESSENCE_" + upgrade.essenceType().get())
					: prices.applyAsDouble(upgrade.itemId().orElse(""))
			) * upgrade.amount().orElse(0);

			upgradePrice *= isEssenceUpgrade ? CalculatorConstants.WORTH.applyAsDouble("essence") : 1;
			cost += upgradePrice;
		}

		if (cost > 0) {
			Calculation calc = Calculation.of(Calculation.Type.PRESTIGE, prestigeItem, cost);
			acc.add(calc.price());
			acc.push(calc);
		}
	}
}
