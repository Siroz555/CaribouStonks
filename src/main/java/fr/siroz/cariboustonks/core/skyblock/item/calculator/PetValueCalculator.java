package fr.siroz.cariboustonks.core.skyblock.item.calculator;

import fr.siroz.cariboustonks.core.skyblock.SkyBlockConstants;
import fr.siroz.cariboustonks.core.skyblock.data.hypixel.item.Rarity;
import fr.siroz.cariboustonks.core.skyblock.item.metadata.PetInfo;
import fr.siroz.cariboustonks.util.StonksUtils;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.ToDoubleFunction;
import org.jspecify.annotations.NonNull;

final class PetValueCalculator {
	public static final String TIER_BOOST_ITEM_ID = "PET_ITEM_TIER_BOOST";
	private static final List<String> PET_RARITIES;
	private static final Object2IntMap<String> PET_RARITY_OFFSETS;
	private static final IntList PET_LEVELS;

	private PetValueCalculator() {
	}

	public static ItemValueResult handle(@NonNull PetInfo petInfo, @NonNull ToDoubleFunction<String> prices, boolean networth) {
		if (petInfo.equals(PetInfo.EMPTY)) return ItemValueResult.EMPTY;

		double lvl1 = prices.applyAsDouble(petInfo.type() + ";" + Rarity.fromName(petInfo.tier()).getIndex());
		double lvl100 = prices.applyAsDouble(petInfo.type() + ";" + Rarity.fromName(petInfo.tier()).getIndex() + "+100");
		double lvl200 = prices.applyAsDouble(petInfo.type() + ";" + Rarity.fromName(petInfo.tier()).getIndex() + "+200");

		// Le prix au niveau 1 est inconnu, rien à calculer.
		// Le prix au niveau 100 est inconnu ET ce n'est pas un Golden/Jade/Rose Dragon.
		if (lvl1 == 0 || (lvl100 == 0 && !SkyBlockConstants.PET_SPECIALS.containsKey(petInfo.type()))) {
			return ItemValueResult.EMPTY;
		}

		double price = lvl1;
		int level = calculatePetLevel(petInfo);
		if (level == 100) {
			price = lvl100 != 0 ? lvl100 : lvl1;
		} else if (level == 200) {
			price = lvl200 != 0 ? lvl200 : lvl100 != 0 ? lvl100 : lvl1;
		}

		PriceAccumulator acc = new PriceAccumulator(price);

		// Skin
		Optional<String> skin = petInfo.skin();
		if (skin.isPresent()) {
			Calculation calc = Calculation.of(
					Calculation.Type.SKIN,
					skin.get(),
					prices.applyAsDouble("PET_SKIN_" + skin.get()) * worth("skins", networth)
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

	private static int calculatePetLevel(@NonNull PetInfo info) {
		String petRarity = info.heldItem().orElse("").equals(TIER_BOOST_ITEM_ID)
				? PET_RARITIES.get(PET_RARITIES.indexOf(info.tier()) + 1)
				: info.tier();
		int maxPetLevel = SkyBlockConstants.PET_SPECIALS.getOrDefault(info.type(), 100);
		int petOffset = PET_RARITY_OFFSETS.getInt(info.type().equals("BINGO") ? "COMMON" : petRarity);
		IntList petLevels = new IntArrayList(
				PET_LEVELS.subList(petOffset, petOffset + maxPetLevel - 1)
		);

		int level = 1;
		int totalXp = 0;
		for (int lvlXp : petLevels) {
			totalXp += lvlXp;
			if (totalXp > info.exp()) {
				break;
			}
			level++;
		}

		return Math.min(level, maxPetLevel);
	}

	private static double worth(@NonNull String skyBlockId, boolean networth) {
		return networth ? CalculatorHelper.WORTH.applyAsDouble(skyBlockId) : 1d;
	}

	static {
		PET_RARITIES = List.of(
				"COMMON",
				"UNCOMMON",
				"RARE",
				"EPIC",
				"LEGENDARY",
				"MYTHIC"
		);

		PET_RARITY_OFFSETS = Object2IntMaps.unmodifiable(StonksUtils.make(new Object2IntOpenHashMap<>(), map -> {
			map.put("COMMON", 0);
			map.put("UNCOMMON", 6);
			map.put("RARE", 11);
			map.put("EPIC", 16);
			map.put("LEGENDARY", 20);
			map.put("MYTHIC", 20);
		}));

		PET_LEVELS = IntList.of(100, 110, 120, 130, 145, 160, 175, 190, 210, 230, 250, 275, 300, 330, 360, 400, 440, 490, 540, 600, 660, 730, 800,
				880, 960, 1050, 1150, 1260, 1380, 1510, 1650, 1800, 1960, 2130, 2310, 2500, 2700, 2920, 3160, 3420, 3700, 4000, 4350,
				4750, 5200, 5700, 6300, 7000, 7800, 8700, 9700, 10800, 12000, 13300, 14700, 16200, 17800, 19500, 21300, 23200, 25200,
				27400, 29800, 32400, 35200, 38200, 41400, 44800, 48400, 52200, 56200, 60400, 64800, 69400, 74200, 79200, 84700, 90700,
				97200, 104200, 111700, 119700, 128200, 137200, 146700, 156700, 167700, 179700, 192700, 206700, 221700, 237700, 254700,
				272700, 291700, 311700, 333700, 357700, 383700, 411700, 441700, 476700, 516700, 561700, 611700, 666700, 726700,
				791700, 861700, 936700, 1016700, 1101700, 1191700, 1286700, 1386700, 1496700, 1616700, 1746700, 1886700, 0, 5555,
				1886700, 1886700, 1886700, 1886700, 1886700, 1886700, 1886700, 1886700, 1886700, 1886700, 1886700, 1886700, 1886700,
				1886700, 1886700, 1886700, 1886700, 1886700, 1886700, 1886700, 1886700, 1886700, 1886700, 1886700, 1886700, 1886700,
				1886700, 1886700, 1886700, 1886700, 1886700, 1886700, 1886700, 1886700, 1886700, 1886700, 1886700, 1886700, 1886700,
				1886700, 1886700, 1886700, 1886700, 1886700, 1886700, 1886700, 1886700, 1886700, 1886700, 1886700, 1886700, 1886700,
				1886700, 1886700, 1886700, 1886700, 1886700, 1886700, 1886700, 1886700, 1886700, 1886700, 1886700, 1886700, 1886700,
				1886700, 1886700, 1886700, 1886700, 1886700, 1886700, 1886700, 1886700, 1886700, 1886700, 1886700, 1886700, 1886700,
				1886700, 1886700, 1886700, 1886700, 1886700, 1886700, 1886700, 1886700, 1886700, 1886700, 1886700, 1886700, 1886700,
				1886700, 1886700, 1886700, 1886700, 1886700, 1886700, 1886700
		);
	}
}
