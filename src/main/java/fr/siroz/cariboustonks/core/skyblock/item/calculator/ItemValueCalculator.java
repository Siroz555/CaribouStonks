package fr.siroz.cariboustonks.core.skyblock.item.calculator;

import fr.siroz.cariboustonks.core.skyblock.data.hypixel.item.SkyBlockItemData;
import fr.siroz.cariboustonks.core.skyblock.item.ItemMetadata;
import fr.siroz.cariboustonks.core.skyblock.item.SkyblockItemStack;
import fr.siroz.cariboustonks.core.skyblock.item.metadata.Books;
import fr.siroz.cariboustonks.core.skyblock.item.metadata.CosmeticInfo;
import fr.siroz.cariboustonks.core.skyblock.item.metadata.DrillInfo;
import fr.siroz.cariboustonks.core.skyblock.item.metadata.Enchantments;
import fr.siroz.cariboustonks.core.skyblock.item.metadata.Gemstones;
import fr.siroz.cariboustonks.core.skyblock.item.metadata.Modifiers;
import fr.siroz.cariboustonks.core.skyblock.item.metadata.RodInfo;
import fr.siroz.cariboustonks.core.skyblock.item.metadata.SpecialAuctionInfo;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;
import org.jspecify.annotations.NonNull;

/**
 * Responsible for calculating the estimated value of an item by composing price components
 * that can either modify the running price or short-circuit the pipeline when an exclusive rule applies.
 * <p>
 * The entire system and related classes for calculating and manipulating data
 * are inspired by the Item Values of mods / Discord Bot or website.
 * <h3>Credits</h3>
 * Mostly from <a href="https://github.com/Altpapier/SkyHelper-Networth">Altpapier SkyHelper-Networth</a>
 * and <a href="https://github.com/AzureAaron/networth-calculator">AzureAaron networth-calculator</a>.
 * <p>
 * The rewrite is different; I wanted to have a more maintainable, be more free, and independent design.
 * SkyHelper-Networth or networth-calculator does not handle Enchantment Upgrades,
 * special cases for Enchantments, Rod Parts and other small technical elements
 * that allow me to display the results via a display.
 */
public final class ItemValueCalculator {

	private static ItemValueCalculator instance;

	private ItemValueCalculator() {
	}

	/**
	 * Returns the singleton instance of the {@code ItemValueCalculator} class.
	 *
	 * @return the singleton instance of {@code ItemValueCalculator}
	 */
	public static ItemValueCalculator getInstance() {
		return instance == null ? instance = new ItemValueCalculator() : instance;
	}

	/**
	 * Decision returned by a {@link PriceComponent} to indicate whether the pipeline
	 * should continue with the next component or stop immediately.
	 */
	private enum ComponentDecision {
		CONTINUE, STOP
	}

	/**
	 * Functional contract for a price component.
	 */
	@FunctionalInterface
	private interface PriceComponent {
		/**
		 * Applies this price component to the given context and accumulator.
		 *
		 * @param ctx the immutable item context
		 * @param acc the mutable accumulator to update
		 * @return the {@link ComponentDecision} indicating whether the pipeline should continue
		 */
		ComponentDecision apply(ItemContext ctx, PriceAccumulator acc);
	}

	/**
	 * Immutable context passed to every {@code PriceComponent} during calculation.
	 *
	 * @param skyBlockId the skyBlockId of the item
	 * @param metadata   parsed item metadata
	 * @param prices     function mapping a price-key ({@code String}) to a {@code double} price value
	 */
	private record ItemContext(String skyBlockId, ItemMetadata metadata, ToDoubleFunction<String> prices, boolean networth) {
	}

	/**
	 * Calculates the full value of the provided item by running a composed pipeline of {@link PriceComponent} instances.
	 *
	 * @param item the item to evaluate or returns {@link ItemValueResult#EMPTY} if {@code skyBlockId} is empty
	 * @return an {@link ItemValueResult} containing the resulting price, base price and detailed calculations
	 */
	public @NonNull ItemValueResult calculateValue(@NonNull SkyblockItemStack item, boolean networth) {
		try {
			return process(item, CalculatorHelper::getPrice, networth);
		} catch (Throwable throwable) {
			return ItemValueResult.fail(throwable);
		}
	}

	private ItemValueResult process(@NonNull SkyblockItemStack item, @NonNull ToDoubleFunction<String> prices, boolean networth) {
		String rawId = item.skyBlockId();
		if (rawId.isEmpty()) {
			return ItemValueResult.EMPTY;
		}

		ItemMetadata metadata = item.metadata();
		String id = rawId;
		if (metadata.cosmeticInfo().isShiny()) {
			id = "SHINY_" + rawId;
		}

		ItemContext ctx = new ItemContext(id, metadata, prices, networth);
		// Base price (multiplié par la quantité)
		double basePricePerUnit = prices.applyAsDouble(id);
		double initial = basePricePerUnit * item.amount();
		PriceAccumulator acc = new PriceAccumulator(initial);

		// Pets - Je les gère pas pour le moment
		if (ctx.skyBlockId().equals("PET")) {
			return ItemValueResult.EMPTY;
		}

		// Special Auction (Shen, Dark Auction)
		SpecialAuctionInfo specialAuction = metadata.specialAuctionInfo();
		if (specialAuction.price().isPresent()) {
			acc = new PriceAccumulator(specialAuction.price().getAsLong());
		}

		// Compose pipeline : l'ordre importe
		List<PriceComponent> pipeline = new ArrayList<>();
		pipeline.add(enchantedBook()); // Blocking
		pipeline.add(ultimateEnchantedBook()); // Blocking
		pipeline.add(prestiges());
		pipeline.add(specialAuction());
		pipeline.add(reforge());
		pipeline.add(cosmetics()); // À voir avec une option dans le ItemValueOption
		pipeline.add(regularEnchantments());
		pipeline.add(regularUltimateEnchantment());
		pipeline.add(books());
		pipeline.add(masterStars());
		pipeline.add(modifiers());
		pipeline.add(gemstones());
		pipeline.add(drillParts());
		pipeline.add(rodParts());
		pipeline.add(boosters());

		// Execute
		for (PriceComponent component : pipeline) {
			ComponentDecision decision = component.apply(ctx, acc);
			if (decision == ComponentDecision.STOP) {
				break;
			}
		}

		return ItemValueResult.success(acc.price(), acc.base(), acc.calculations());
	}

	private @NonNull PriceComponent prestiges() {
		return (ctx, acc) -> {
			if (CalculatorConstants.PRESTIGES.containsKey(ctx.skyBlockId())) {
				for (String prestigeItemId : CalculatorConstants.PRESTIGES.get(ctx.skyBlockId())) {
					SkyBlockItemData itemData = CalculatorHelper.getItemData(prestigeItemId);
					if (itemData != null && itemData.prestige().isPresent()) {
						CalculatorHelper.computeUpgradeCosts(ctx.prices(), acc, itemData.prestige().get().costs(), prestigeItemId);
					}
				}
			}
			return ComponentDecision.CONTINUE;
		};
	}

	private @NonNull PriceComponent specialAuction() {
		return (ctx, acc) -> {
			SpecialAuctionInfo specialAuction = ctx.metadata().specialAuctionInfo();
			// Shen Auction
			if (specialAuction.price().isPresent() && specialAuction.auction().isPresent() && specialAuction.bid().isPresent()) {
				double scaledPrice = specialAuction.price().getAsLong() * worth("specialAuctionPrice", ctx.networth());
				if (scaledPrice > acc.price()) {
					Calculation calc = Calculation.of(
							Calculation.Type.SHEN_AUCTION,
							ctx.skyBlockId(),
							scaledPrice
					);
					acc.set(calc.price());
					acc.push(calc);
				}
			}
			// Dark Auction - Midas Sword / Midas Staff
			if (CalculatorConstants.MIDAS_WEAPONS.containsKey(ctx.skyBlockId())) {
				Pair<Long, String> midasWeapon = CalculatorConstants.MIDAS_WEAPONS.get(ctx.skyBlockId());
				long maxBid = midasWeapon.left();
				String apiId = midasWeapon.right();
				if (specialAuction.winningBid().isPresent()) {
					long winningBid = specialAuction.winningBid().getAsLong();
					Calculation calc;
					// Dans le cas où le prix bid est supérieur à max
					if (winningBid >= maxBid) {
						calc = Calculation.of(Calculation.Type.WINNING_BID, apiId, winningBid * worth("winningBid", ctx.networth()));
					} else {
						// Sinon ça récupère le prix de l'Auction
						calc = Calculation.of(Calculation.Type.WINNING_BID, apiId, ctx.prices().applyAsDouble(apiId));
					}
					acc.set(calc.price());
					acc.push(calc);
				}
			}
			return ComponentDecision.CONTINUE;
		};
	}

	private @NonNull PriceComponent reforge() {
		return (ctx, acc) -> {
			if (ctx.metadata().reforge().isPresent()) {
				String reforge = ctx.metadata().reforge().get();
				String apiId = CalculatorConstants.REFORGES.getOrDefault(reforge, "");
				// Ça évite de recup le prix d'une reforge de base (non présente au Bazaar).
				double price = apiId.isEmpty()
						? 0 :
						ctx.prices().applyAsDouble(apiId) * worth("reforge", ctx.networth());
				Calculation calc = Calculation.of(
						Calculation.Type.REFORGE,
						apiId.isEmpty() ? reforge : apiId, // La reforge est quand même passé pour le Viewer
						price
				);
				acc.add(calc.price());
				acc.push(calc);
			}
			return ComponentDecision.CONTINUE;
		};
	}

	private @NonNull PriceComponent cosmetics() {
		return (ctx, acc) -> {
			CosmeticInfo cosmetic = ctx.metadata().cosmeticInfo();
			// Skin
			if (cosmetic.skin().isPresent()) {
				String skin = cosmetic.skin().get();
				Calculation calc = Calculation.of(
						Calculation.Type.SKIN,
						skin,
						ctx.prices().applyAsDouble(skin) * worth("skins", ctx.networth())
				);
				acc.add(calc.price());
				acc.push(calc);
			}
			// Dye
			if (cosmetic.dye().isPresent()) {
				String dye = cosmetic.dye().get();
				Calculation calc = Calculation.of(
						Calculation.Type.DYE,
						dye,
						ctx.prices().applyAsDouble(dye) * worth("dye", ctx.networth())
				);
				acc.add(calc.price());
				acc.push(calc);
			}
			return ComponentDecision.CONTINUE;
		};
	}

	private @NonNull PriceComponent enchantedBook() {
		return (ctx, acc) -> {
			if (!ctx.skyBlockId().equals("ENCHANTED_BOOK")) return ComponentDecision.CONTINUE;

			Enchantments enchantments = ctx.metadata().enchantments();
			if (enchantments.enchantments().isEmpty()) return ComponentDecision.CONTINUE;

			if (enchantments.enchantments().size() == 1) {
				Object2IntMap.Entry<String> first = enchantments.enchantments().object2IntEntrySet().iterator().next();
				String baseName = first.getKey().toUpperCase(Locale.ENGLISH);
				int lvl = first.getIntValue();
				String apiId = baseName + "_" + lvl;
				Calculation calc = Calculation.of(
						Calculation.Type.ENCHANTED_BOOK,
						baseName,
						ctx.prices().applyAsDouble("ENCHANTMENT_" + apiId) * worth("enchantments", ctx.networth()),
						lvl
				);
				acc.set(calc.price());
				acc.push(calc);
			} else {
				double sum = 0;
				for (Object2IntMap.Entry<String> entry : Object2IntMaps.fastIterable(enchantments.enchantments())) {
					String baseName = entry.getKey().toUpperCase(Locale.ENGLISH);
					int lvl = entry.getIntValue();
					String apiId = baseName + "_" + lvl;
					Calculation calc = Calculation.of(
							Calculation.Type.ENCHANTED_BOOK,
							baseName,
							ctx.prices().applyAsDouble("ENCHANTMENT_" + apiId) * worth("enchantments", ctx.networth()),
							lvl
					);
					sum += calc.price();
					acc.push(calc);
				}
				acc.set(sum);
			}
			return ComponentDecision.STOP;
		};
	}

	private @NonNull PriceComponent ultimateEnchantedBook() {
		return (ctx, acc) -> {
			if (!ctx.skyBlockId().equals("ENCHANTED_BOOK")) return ComponentDecision.CONTINUE;

			Optional<Pair<String, Integer>> opt = ctx.metadata().enchantments().ultimateEnchantment();
			if (opt.isEmpty()) return ComponentDecision.CONTINUE;

			Pair<String, Integer> ultimate = opt.get();
			String baseName = ultimate.left().toUpperCase(Locale.ENGLISH);
			String apiId = baseName + "_" + ultimate.right();
			Calculation calc = Calculation.of(
					Calculation.Type.ULTIMATE_ENCHANTED_BOOK,
					baseName,
					ctx.prices().applyAsDouble("ENCHANTMENT_" + apiId) * worth("enchantments", ctx.networth())
			);
			acc.set(calc.price());
			acc.push(calc);
			return ComponentDecision.STOP;
		};
	}

	private @NonNull PriceComponent regularEnchantments() {
		return (ctx, acc) -> {
			if (ctx.skyBlockId().equals("ENCHANTED_BOOK")) return ComponentDecision.CONTINUE;

			Enchantments enchantments = ctx.metadata().enchantments();
			if (enchantments.enchantments().isEmpty()) return ComponentDecision.CONTINUE;

			for (Object2IntMap.Entry<String> entry : Object2IntMaps.fastIterable(enchantments.enchantments())) {
				String enchantmentId = entry.getKey().toUpperCase(Locale.ENGLISH);
				int lvl = entry.getIntValue();
				// Enchantment Upgrades (SCAVENGER -> GOLDEN_BOUNTY, ENDER_SLAYER -> ENDSTONE_IDOL, ..)
				Map<Integer, String> upgrades = CalculatorConstants.ENCHANTMENT_UPGRADES.getOrDefault(enchantmentId, null);
				if (upgrades != null) {
					String upgradeKey = upgrades.getOrDefault(lvl, null);
					if (upgradeKey != null) {
						Calculation calc = Calculation.of(
								Calculation.Type.ENCHANTMENT_UPGRADE,
								upgradeKey,
								ctx.prices().applyAsDouble(upgradeKey) * worth("enchantmentUpgrades", ctx.networth())
						);
						acc.add(calc.price());
						acc.push(calc);
						// À voir, mais ça me semble logique
						continue;
					}
				}

				// Il n'y a que les level 1 dispo au Bazaar pour Expertise, Champion, ...
				if (CalculatorConstants.STACKING_ENCHANTMENTS.contains(enchantmentId)) {
					lvl = 1;
				}

				Calculation calc = Calculation.of(
						Calculation.Type.ENCHANTMENT,
						enchantmentId,
						ctx.prices().applyAsDouble("ENCHANTMENT_" + enchantmentId + "_" + lvl) * worth("enchantments", ctx.networth()),
						lvl
				);
				acc.add(calc.price());
				acc.push(calc);
			}
			return ComponentDecision.CONTINUE;
		};
	}

	private @NonNull PriceComponent regularUltimateEnchantment() {
		return (ctx, acc) -> {
			if (ctx.skyBlockId().equals("ENCHANTED_BOOK")) return ComponentDecision.CONTINUE;

			Optional<Pair<String, Integer>> opt = ctx.metadata().enchantments().ultimateEnchantment();
			if (opt.isEmpty()) return ComponentDecision.CONTINUE;

			Pair<String, Integer> ultimate = opt.get();
			String ultimateEnchantId = ultimate.left().toUpperCase(Locale.ENGLISH);
			int targetLvl = ultimate.right();
			// Pourquoi networth-calculator de Aaron ne fait pas ça ?
			int minLevel = CalculatorConstants.ULTIMATE_BASE_LEVELS.getOrDefault(ultimateEnchantId, 1);
			int unitsNeeded = (targetLvl >= minLevel) ? (int) Math.pow(2, targetLvl - minLevel) : 1; // fallback si problème

			Calculation calc = Calculation.of(
					Calculation.Type.ULTIMATE_ENCHANTMENT,
					ultimateEnchantId,
					ctx.prices().applyAsDouble("ENCHANTMENT_" + ultimateEnchantId + "_" + minLevel) * unitsNeeded * worth("enchantments", ctx.networth()),
					unitsNeeded
			);
			acc.add(calc.price());
			acc.push(calc);
			return ComponentDecision.CONTINUE;
		};
	}

	private @NonNull PriceComponent books() {
		return (ctx, acc) -> {
			Books books = ctx.metadata().books();
			// Hot/Fuming Potato Book
			if (books.hotPotatoes().isPresent()) {
				int hotPotatoes = books.hotPotatoes().getAsInt();
				if (hotPotatoes > 10) {
					int fumingPotato = hotPotatoes - 10;
					Calculation calc = Calculation.of(
							Calculation.Type.FUMING_POTATO_BOOK,
							"FUMING_POTATO_BOOK",
							ctx.prices().applyAsDouble("FUMING_POTATO_BOOK") * fumingPotato * worth("FumingPotatoBook", ctx.networth()),
							fumingPotato
					);
					acc.add(calc.price());
					acc.push(calc);
				}
				int hotPotato = Math.min(hotPotatoes, 10);
				Calculation calc = Calculation.of(
						Calculation.Type.HOT_POTATO_BOOK,
						"HOT_POTATO_BOOK",
						ctx.prices().applyAsDouble("HOT_POTATO_BOOK") * hotPotato * worth("hotPotatoBook", ctx.networth()),
						hotPotato
				);
				acc.add(calc.price());
				acc.push(calc);
			}
			// Book of Stats
			if (books.stats().isPresent()) {
				Calculation calc = Calculation.of(
						Calculation.Type.STATS_BOOK,
						"BOOK_OF_STATS",
						ctx.prices().applyAsDouble("BOOK_OF_STATS")
				);
				acc.add(calc.price());
				acc.push(calc);
			}
			// Art of War
			if (books.artOfWar().isPresent()) {
				Calculation calc = Calculation.of(
						Calculation.Type.ART_OF_WAR,
						"THE_ART_OF_WAR",
						ctx.prices().applyAsDouble("THE_ART_OF_WAR") * worth("artOfWar", ctx.networth())
				);
				acc.add(calc.price());
				acc.push(calc);
			}
			// Art of Peace
			if (books.artOfPeace().isPresent()) {
				Calculation calc = Calculation.of(
						Calculation.Type.ART_OF_PEACE,
						"THE_ART_OF_PEACE",
						ctx.prices().applyAsDouble("THE_ART_OF_PEACE") * worth("artOfPeace", ctx.networth())
				);
				acc.add(calc.price());
				acc.push(calc);
			}
			// Farming for Dummies
			if (books.farmingForDummies().isPresent()) {
				int farmingForDummies = books.farmingForDummies().getAsInt();
				Calculation calc = Calculation.of(
						Calculation.Type.FARMING_FOR_DUMMIES,
						"FARMING_FOR_DUMMIES",
						ctx.prices().applyAsDouble("FARMING_FOR_DUMMIES") * farmingForDummies * worth("farmingForDummies", ctx.networth()),
						farmingForDummies
				);
				acc.add(calc.price());
				acc.push(calc);
			}
			// Polarvoid
			if (books.polarvoid().isPresent()) {
				int polarvoid = books.polarvoid().getAsInt();
				Calculation calc = Calculation.of(
						Calculation.Type.POLARVOID_BOOK,
						"POLARVOID_BOOK",
						ctx.prices().applyAsDouble("POLARVOID_BOOK") * polarvoid * worth("polarvoidBook", ctx.networth()),
						polarvoid
				);
				acc.add(calc.price());
				acc.push(calc);
			}
			// Jalapeno Book
			if (books.jalapeno().isPresent()) {
				Calculation calc = Calculation.of(
						Calculation.Type.JALAPENO_BOOK,
						"JALAPENO_BOOK",
						ctx.prices().applyAsDouble("JALAPENO_BOOK") * worth("jalapenoBook", ctx.networth())
				);
				acc.add(calc.price());
				acc.push(calc);
			}
			// Wet Book
			if (books.wet().isPresent()) {
				int wet = books.wet().getAsInt();
				Calculation calc = Calculation.of(
						Calculation.Type.WET_BOOK,
						"WET_BOOK",
						ctx.prices().applyAsDouble("WET_BOOK") * wet,
						wet
				);
				acc.add(calc.price());
				acc.push(calc);
			}
			return ComponentDecision.CONTINUE;
		};
	}

	private @NonNull PriceComponent masterStars() {
		return (ctx, acc) -> {
			int dungeonItemLevel = ctx.metadata().modifiers().dungeonItemLevel().orElse(0);
			int upgradeLevel = ctx.metadata().modifiers().upgradeLevel();
			if (dungeonItemLevel > 5 || upgradeLevel > 5) {
				int starsUsedDungeons = dungeonItemLevel - 5;
				int starsUsedUpgrade = upgradeLevel - 5;
				int starsUsed = Math.max(starsUsedDungeons, starsUsedUpgrade);
				for (int i = 0; i < starsUsed; i++) {
					String apiId = CalculatorConstants.MASTER_STARS.get(i);
					Calculation calc = Calculation.of(
							Calculation.Type.MASTER_STAR,
							apiId,
							ctx.prices().applyAsDouble(apiId) * worth("masterStars", ctx.networth())
					);
					acc.add(calc.price());
					acc.push(calc);
				}
			}
			return ComponentDecision.CONTINUE;
		};
	}

	private @NonNull PriceComponent modifiers() {
		return (ctx, acc) -> {
			Modifiers modifiers = ctx.metadata().modifiers();
			// Recombobulator 3000
			// Mais si jamais à l'avenir le rarityUpgrades est > 1 ? Les autres Networth calculator seront faux ?
			// Ici, je vérifie avec rarityUpgrades == 1.
			if (modifiers.isRecombobulated()) {
				Calculation calc = Calculation.of(
						Calculation.Type.RECOMBOBULATOR,
						"RECOMBOBULATOR_3000",
						ctx.prices().applyAsDouble("RECOMBOBULATOR_3000") * worth("recombobulator", ctx.networth())
				);
				acc.add(calc.price());
				acc.push(calc);
			}
			// Enrichment
			if (modifiers.enrichment().isPresent()) {
				String enrichment = modifiers.enrichment().get();
				// Même prix en bits, je respecte comme les autres calculator de récupérer le cheapest.
				double price = CalculatorConstants.ENRICHMENTS.stream()
						.mapToDouble(value -> ctx.prices().applyAsDouble(value))
						.filter(d -> d > 0)
						.min()
						.orElse(0.0);
				Calculation calc = Calculation.of(
						Calculation.Type.TALISMAN_ENRICHMENT,
						enrichment,
						price * worth("enrichment", ctx.networth())
				);
				acc.add(calc.price());
				acc.push(calc);
			}
			// Wood Singularity
			if (modifiers.woodSingularity().isPresent()) {
				int woodSingularity = modifiers.woodSingularity().getAsInt();
				Calculation calc = Calculation.of(
						Calculation.Type.WOOD_SINGULARITY,
						"WOOD_SINGULARITY",
						ctx.prices().applyAsDouble("WOOD_SINGULARITY") * woodSingularity * worth("woodSingularity", ctx.networth()),
						woodSingularity
				);
				acc.add(calc.price());
				acc.push(calc);
			}
			// Mana Disintegrator
			if (modifiers.manaDisintegrators().isPresent()) {
				int manaDisintegrator = modifiers.manaDisintegrators().getAsInt();
				Calculation calc = Calculation.of(
						Calculation.Type.MANA_DISINTEGRATOR,
						"MANA_DISINTEGRATOR",
						ctx.prices().applyAsDouble("MANA_DISINTEGRATOR") * manaDisintegrator * worth("manaDisintegrator", ctx.networth()),
						manaDisintegrator
				);
				acc.add(calc.price());
				acc.push(calc);
			}
			// Transmission Tuners
			if (modifiers.transmissionTuners().isPresent()) {
				int transmissionTuners = modifiers.transmissionTuners().getAsInt();
				Calculation calc = Calculation.of(
						Calculation.Type.TRANSMISSION_TUNER,
						"TRANSMISSION_TUNER",
						ctx.prices().applyAsDouble("TRANSMISSION_TUNER") * transmissionTuners * worth("transmissionTuner", ctx.networth()),
						transmissionTuners
				);
				acc.add(calc.price());
				acc.push(calc);
			}
			// Etherwarp Conduit (Ethermerge)
			if (modifiers.ethermerge().isPresent()) {
				Calculation calc = Calculation.of(
						Calculation.Type.ETHERWARP_CONDUIT,
						"ETHERWARP_CONDUIT",
						ctx.prices().applyAsDouble("ETHERWARP_CONDUIT") * worth("etherwarp", ctx.networth())
				);
				acc.add(calc.price());
				acc.push(calc);
			}
			// Pocket Sack-in-a-Sack
			if (modifiers.pocketSackInASack().isPresent()) {
				int pocketSackInASack = modifiers.pocketSackInASack().getAsInt();
				Calculation calc = Calculation.of(
						Calculation.Type.POCKET_SACK_IN_A_SACK,
						"POCKET_SACK_IN_A_SACK",
						ctx.prices().applyAsDouble("POCKET_SACK_IN_A_SACK") * pocketSackInASack * worth("pocketSackInASack", ctx.networth())
				);
				acc.add(calc.price());
				acc.push(calc);
			}
			// Divan Powder Coating
			if (modifiers.divanPowderCoating().isPresent()) {
				Calculation calc = Calculation.of(
						Calculation.Type.DIVAN_POWDER_COATING,
						"DIVAN_POWDER_COATING",
						ctx.prices().applyAsDouble("DIVAN_POWDER_COATING") * worth("divanPowderCoating", ctx.networth())
				);
				acc.add(calc.price());
				acc.push(calc);
			}
			// Power Scroll
			if (modifiers.powerScroll().isPresent()) {
				String powerScroll = modifiers.powerScroll().get();
				Calculation calc = Calculation.of(
						Calculation.Type.POWER_SCROLL,
						powerScroll,
						ctx.prices().applyAsDouble(powerScroll) * worth("powerScroll", ctx.networth())
				);
				acc.add(calc.price());
				acc.push(calc);
			}
			// Ability Scrolls (Wither Scrolls)
			if (modifiers.abilityScrolls().isPresent()) {
				for (String abilityScroll : modifiers.abilityScrolls().get()) {
					Calculation calc = Calculation.of(
							Calculation.Type.WITHER_SCROLL,
							abilityScroll,
							ctx.prices().applyAsDouble(abilityScroll) * worth("witherScroll", ctx.networth())
					);
					acc.add(calc.price());
					acc.push(calc);
				}
			}
			// Overclocker 3000
			if (modifiers.overclockers().isPresent()) {
				int overclockers = modifiers.overclockers().getAsInt();
				Calculation calc = Calculation.of(
						Calculation.Type.OVERCLOCKER,
						"OVERCLOCKER_3000",
						ctx.prices().applyAsDouble("OVERCLOCKER_3000") * overclockers * worth("overclocker", ctx.networth()),
						overclockers
				);
				acc.add(calc.price());
				acc.push(calc);
			}
			return ComponentDecision.CONTINUE;
		};
	}

	private @NonNull PriceComponent gemstones() {
		return (ctx, acc) -> {
			Gemstones gemstones = ctx.metadata().gemstones();
			// Gemstones
			for (Gemstones.GemstoneApplied slot : gemstones.gemstoneApplied()) {
				String apiId = slot.getSkyBlockGemstoneId();
				Calculation calc = Calculation.of(
						Calculation.Type.GEMSTONE,
						apiId,
						ctx.prices().applyAsDouble(apiId) * worth("gemstones", ctx.networth())
				);
				acc.add(calc.price());
				acc.push(calc);
			}
			// Gemstones - UnlockedSlots
			List<String> unlockedSlots = gemstones.unlockedSlots();
			if (!unlockedSlots.isEmpty()) {
				// Cas en peu particulier comparé aux autres projets qui calculs les Item values.
				// Peut-être trop complex ce que j'ai fait, mais c'est plus clair, je trouve, à voir.
				SkyBlockItemData itemData = CalculatorHelper.getItemData(ctx.skyBlockId());
				if (itemData != null && itemData.gemstoneSlots().isPresent()) {
					Map<String, List<SkyBlockItemData.GemstoneSlot>> slotsByType = itemData.gemstoneSlots().get().stream()
							.collect(Collectors.groupingBy(
									s -> s.slotType().toUpperCase(Locale.ENGLISH),
									LinkedHashMap::new,
									Collectors.toList()
							));

					for (String unlocked : unlockedSlots) {
						Optional<CalculatorHelper.UnlockedSlot> slot = CalculatorHelper.parseUnlockedSlot(unlocked);
						if (slot.isEmpty()) {
							continue;
						}

						String type = slot.get().type();
						int index = slot.get().index();
						List<SkyBlockItemData.GemstoneSlot> candidates = slotsByType.get(type);
						if (candidates == null || index < 0 || index >= candidates.size()) {
							continue;
						}

						SkyBlockItemData.GemstoneSlot matched = candidates.get(index);
						if (matched.costs().isPresent()) {
							for (SkyBlockItemData.GemstoneSlot.GemstoneSlotCost cost : matched.costs().get()) {
								switch (cost.type()) {
									case "COINS" -> {
										double price = cost.coins().orElse(0) * worth("gemstoneSlots", ctx.networth());
										Calculation calc = Calculation.of(
												Calculation.Type.GEMSTONE_SLOT,
												type + "_" + index + "_COINS",
												price
										);
										acc.add(calc.price());
										acc.push(calc);
									}
									case "ITEM" -> {
										Optional<String> maybeItemId = cost.itemId();
										if (maybeItemId.isEmpty()) {
											continue;
										}

										String costItemId = maybeItemId.get();
										int needed = cost.amount().orElse(1);
										double price = (ctx.prices().applyAsDouble(costItemId) * needed) * worth("gemstoneSlots", ctx.networth());
										Calculation calc = Calculation.of(
												Calculation.Type.GEMSTONE_SLOT,
												costItemId,
												price,
												needed
										);
										acc.add(calc.price());
										acc.push(calc);
									}
									case null, default -> {
									}
								}
							}
						}
					}
				}
			}
			return ComponentDecision.CONTINUE;
		};
	}

	private @NonNull PriceComponent drillParts() {
		return (ctx, acc) -> {
			DrillInfo drill = ctx.metadata().drillInfo();
			// Fuel Tank
			if (drill.fuelTank().isPresent()) {
				String fuelTank = drill.fuelTank().get().toUpperCase(Locale.ENGLISH);
				Calculation calc = Calculation.of(
						Calculation.Type.DRILL_PART,
						fuelTank,
						ctx.prices().applyAsDouble(fuelTank) * worth("drillPart", ctx.networth())
				);
				acc.add(calc.price());
				acc.push(calc);
			}
			// Engine
			if (drill.engine().isPresent()) {
				String engine = drill.engine().get().toUpperCase(Locale.ENGLISH);
				Calculation calc = Calculation.of(
						Calculation.Type.DRILL_PART,
						engine,
						ctx.prices().applyAsDouble(engine) * worth("drillPart", ctx.networth())
				);
				acc.add(calc.price());
				acc.push(calc);
			}
			// Upgrade Module
			if (drill.upgradeModule().isPresent()) {
				String upgradeModule = drill.upgradeModule().get().toUpperCase(Locale.ENGLISH);
				Calculation calc = Calculation.of(
						Calculation.Type.DRILL_PART,
						upgradeModule,
						ctx.prices().applyAsDouble(upgradeModule) * worth("drillPart", ctx.networth())
				);
				acc.add(calc.price());
				acc.push(calc);
			}
			return ComponentDecision.CONTINUE;
		};
	}

	private @NonNull PriceComponent rodParts() {
		return (ctx, acc) -> {
			RodInfo rod = ctx.metadata().rodInfo();
			// Line
			if (rod.line().isPresent()) {
				String line = rod.line().get().toUpperCase(Locale.ENGLISH);
				Calculation calc = Calculation.of(
						Calculation.Type.ROD_PART,
						line,
						ctx.prices().applyAsDouble(line) * worth("rodPart", ctx.networth())
				);
				acc.add(calc.price());
				acc.push(calc);
			}
			// Hook
			if (rod.hook().isPresent()) {
				String hook = rod.hook().get().toUpperCase(Locale.ENGLISH);
				Calculation calc = Calculation.of(
						Calculation.Type.ROD_PART,
						hook,
						ctx.prices().applyAsDouble(hook) * worth("rodPart", ctx.networth())
				);
				acc.add(calc.price());
				acc.push(calc);
			}
			// Sinker
			if (rod.sinker().isPresent()) {
				String sinker = rod.sinker().get().toUpperCase(Locale.ENGLISH);
				Calculation calc = Calculation.of(
						Calculation.Type.ROD_PART,
						sinker,
						ctx.prices().applyAsDouble(sinker) * worth("rodPart", ctx.networth())
				);
				acc.add(calc.price());
				acc.push(calc);
			}
			return ComponentDecision.CONTINUE;
		};
	}

	private @NonNull PriceComponent boosters() {
		return (ctx, acc) -> {
			if (ctx.metadata().modifiers().boosters().isPresent()) {
				for (String booster : ctx.metadata().modifiers().boosters().get()) {
					String apiId = booster.toUpperCase(Locale.ENGLISH) + "_BOOSTER";
					Calculation calc = Calculation.of(
							Calculation.Type.BOOSTERS,
							apiId,
							ctx.prices().applyAsDouble(apiId) * worth("boosters", ctx.networth())
					);
					acc.add(calc.price());
					acc.push(calc);
				}
			}
			return ComponentDecision.CONTINUE;
		};
	}

	private double worth(@NonNull String skyBlockId, boolean networth) {
		return networth ? CalculatorConstants.WORTH.applyAsDouble(skyBlockId) : 1d;
	}
}
