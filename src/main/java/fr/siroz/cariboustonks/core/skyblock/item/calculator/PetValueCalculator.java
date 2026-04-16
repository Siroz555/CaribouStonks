package fr.siroz.cariboustonks.core.skyblock.item.calculator;

import fr.siroz.cariboustonks.core.skyblock.SkyBlockConstants;
import fr.siroz.cariboustonks.core.skyblock.data.hypixel.item.Rarity;
import fr.siroz.cariboustonks.core.skyblock.item.metadata.PetInfo;
import java.util.Optional;
import java.util.function.ToDoubleFunction;
import org.jspecify.annotations.NonNull;

final class PetValueCalculator {

	private PetValueCalculator() {
	}

	public static ItemValueResult handle(@NonNull PetInfo petInfo, @NonNull ToDoubleFunction<String> prices, boolean networth) {
		if (petInfo.equals(PetInfo.EMPTY)) return ItemValueResult.EMPTY;

		// SIROZ-NOTE: Gestion du level 200

		double lvl1 = prices.applyAsDouble(petInfo.type() + ";" + Rarity.fromName(petInfo.tier()).getIndex());
		double lvl100 = prices.applyAsDouble(petInfo.type() + ";" + Rarity.fromName(petInfo.tier()).getIndex() + "+100");

		// Le prix au niveau 1 est inconnu, rien à calculer.
		// Le prix au niveau 100 est inconnu ET ce n'est pas un Golden/Jade/Rose Dragon. (Pour lvl200 dans le futur)
		if (lvl1 == 0 || (lvl100 == 0 && !SkyBlockConstants.LVL_200_PETS.contains(petInfo.type()))) {
			return ItemValueResult.EMPTY;
		}

		double price = lvl100 != 0 ? lvl100 : lvl1;
		PriceAccumulator acc = new PriceAccumulator(price);

		// Skin
		Optional<String> skin = petInfo.skin();
		if (skin.isPresent()) {
			Calculation calc = Calculation.of(
					Calculation.Type.SKIN,
					skin.get(),
					prices.applyAsDouble(skin.get()) * worth("skins", networth)
			);
			acc.add(calc.price());
			acc.push(calc);
		}

		// Held Item
		Optional<String> heldItem = petInfo.heldItem();
		if (heldItem.isPresent()) {
			Calculation calc = Calculation.of(
					Calculation.Type.PET_ITEM,
					heldItem.get(),
					prices.applyAsDouble(heldItem.get()) * worth("petItem", networth)
			);
			acc.add(calc.price());
			acc.push(calc);
		}

		return ItemValueResult.success(acc.price(), acc.base(), acc.calculations());
	}

	private static double worth(@NonNull String skyBlockId, boolean networth) {
		return networth ? CalculatorHelper.WORTH.applyAsDouble(skyBlockId) : 1d;
	}
}
