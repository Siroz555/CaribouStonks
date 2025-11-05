package fr.siroz.cariboustonks.feature.stonks;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.core.data.hypixel.item.Rarity;
import fr.siroz.cariboustonks.core.data.hypixel.item.SkyBlockItemData;
import fr.siroz.cariboustonks.core.data.mod.SkyBlockEnchantment;
import fr.siroz.cariboustonks.core.scheduler.TickScheduler;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.core.skyblock.item.SkyblockItemStack;
import fr.siroz.cariboustonks.core.skyblock.item.calculator.Calculation;
import fr.siroz.cariboustonks.core.skyblock.item.calculator.CalculatorConstants;
import fr.siroz.cariboustonks.core.skyblock.item.calculator.ItemValueCalculator;
import fr.siroz.cariboustonks.core.skyblock.item.calculator.ItemValueResult;
import fr.siroz.cariboustonks.core.skyblock.item.metadata.Gemstones;
import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.event.ItemRenderEvents;
import fr.siroz.cariboustonks.feature.Feature;
import fr.siroz.cariboustonks.util.DeveloperTools;
import fr.siroz.cariboustonks.util.RomanNumeralUtils;
import fr.siroz.cariboustonks.util.StonksUtils;
import fr.siroz.cariboustonks.util.colors.Colors;
import it.unimi.dsi.fastutil.Pair;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Feature that displays a detailed summary of an item's value from {@link ItemValueCalculator}
 * on the side of the inventory when hovering over an item.
 * <p>
 * The Skyhanni Mod in 1.8 introduced this feature. A wonderful idea!
 * Here, it's roughly the same principle, except that the backend is completely different.
 */
@ApiStatus.Experimental
public class ItemValueViewerFeature extends Feature {

	private static final int PADDING_LEFT = 20;
	private static final int START_Y = 20;
	private static final int LINE_HEIGHT = 12;

	private static final int MAX_ENCHANT_DISPLAY = 5;
	private static final String ARROW = "⤷";
	private static final String[] MASTER_STARS_CIRCLED = {"➊", "➋", "➌", "➍", "➎"};

	private final BooleanSupplier configUseNetworth = () -> ConfigManager.getConfig().general.stonks.useNetworthItemValue;

	@Nullable
	private ItemStack currentItem = null;
	private final List<Text> lines = new ArrayList<>();

	public ItemValueViewerFeature() {
		ItemRenderEvents.POST_TOOLTIP.register((_ctx, item, _x, _y, _w, _h, tr, _c) -> this.onPostTooltip(item));
		ScreenEvents.AFTER_INIT.register((_c, screen, _sw, _sh) -> {
			ScreenEvents.afterRender(screen).register(this::render);
			ScreenEvents.remove(screen).register(_s -> this.reset(true));
		});
		TickScheduler.getInstance().runRepeating(() -> this.reset(false), 1, TimeUnit.SECONDS);
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock() && ConfigManager.getConfig().general.stonks.itemValueViewer.enabled;
	}

	@EventHandler(event = "ItemRenderEvents.POST_TOOLTIP")
	private void onPostTooltip(ItemStack itemStack) {
		if (itemStack == null || itemStack.isEmpty() || SkyBlockAPI.getSkyBlockItemUuid(itemStack).isEmpty()) {
			reset(true);
			return;
		}

		if (isEnabled()) {
			if (currentItem == null || !currentItem.equals(itemStack)) {
				reset(true);
				currentItem = itemStack;
				updateForItem(currentItem);
			}
		}
	}

	@EventHandler(event = "ScreenEvents.afterRender")
	private void render(Screen screen, DrawContext ctx, int mouseX, int mouseY, float tickDelta) {
		if (currentItem == null || lines.isEmpty()) return;

		ctx.getMatrices().pushMatrix();
		ctx.getMatrices().scale(getScale(), getScale());

		int y = START_Y;
		for (Text text : lines) {
			ctx.drawTextWithShadow(CLIENT.textRenderer, text, PADDING_LEFT, y, Colors.WHITE.asInt());
			if (text.getString().isBlank()) {
				y += (LINE_HEIGHT / 2);
			} else {
				y += LINE_HEIGHT;
			}
		}
		ctx.getMatrices().popMatrix();
	}

	private float getScale() {
		return ConfigManager.getConfig().general.stonks.itemValueViewer.scale;
	}

	private void updateForItem(@NotNull ItemStack item) {
		try {
			ItemValueResult result = ItemValueCalculator.getInstance().calculateValue(SkyblockItemStack.of(item), configUseNetworth.getAsBoolean());
			// Pas d'accès a l'API ou bien les NBT de l'item sont invalides
			if (result.base() < 1 && result.price() < 1 && result.calculations().isEmpty()) {
				lines.clear();
				return;
			}
			// Si c'est globalement un Enchanted Book ou un item avec un prix unique du Bazaar ou de l'Auction
			if (result.calculations().size() < 2) {
				lines.clear();
				return;
			}

			List<Text> newLines = buildDisplayLines(item, result);
			lines.clear();
			lines.addAll(newLines);
		} catch (Exception ex) {
			// Ce n'est pas possible de throw ici "normalement", mais bien avant pendant la calculation.
			// On évite le crash :/
			if (DeveloperTools.isInDevelopment()) {
				CaribouStonks.LOGGER.error("[ItemValueViewerFeature] An error occured while updating the item viewer", ex);
			}
			lines.clear();
		}
	}

	private @NotNull List<Text> buildDisplayLines(@NotNull ItemStack item, @NotNull ItemValueResult result) {
		List<Text> out = new ArrayList<>();

		if (result.state() == ItemValueResult.State.FAIL) {
			out.add(Text.literal("Internal error occured during the").formatted(Formatting.RED));
			out.add(Text.literal("calculation of the item value.").formatted(Formatting.RED));
			if (result.error() != null) {
				out.add(Text.literal("Error: ").formatted(Formatting.RED, Formatting.BOLD));
				out.add(Text.literal(result.error().getMessage()).formatted(Formatting.RED));
			}
			return out;
		}

		out.add(item.getName());

		boolean hasBase = true;
		if (result.base() > 0) {
			out.add(Text.empty()
					.append(Text.literal("Base: ").formatted(Formatting.GREEN))
					.append(priceFormat(result.base())).formatted(Formatting.GOLD)
					.append(priceShortFormat(result.base()))
			);
		} else {
			hasBase = false;
			out.add(Text.empty()
					.append(Text.literal("Base: ").formatted(Formatting.RED))
					.append(Text.literal("No data available at this time").formatted(Formatting.RED))
			);
		}
		out.add(Text.literal(" "));

		addEnchantedBook(result, out);
		addUltimateEnchantedBook(result, out);
		addCosmetic(result, out);
		addReforge(result, out);
		addRecombobulator(result, out);
		addEnrichment(result, out);
		addPocketSackInASack(result, out);
		addHotPotatoBook(result, out);
		addFumingPotatoBook(result, out);
		addArtOfWar(result, out);
		addArtOfPeace(result, out);
		addWoodSingularity(result, out);
		addFarmingForDummies(result, out);
		addBookOfStats(result, out);
		addPolarvoidBook(result, out);
		addJalapenoBook(result, out);
		addWetBook(result, out);
		addManaDisintegrator(result, out);
		addTransmissionTuner(result, out);
		addEtherwarp(result, out);
		addDivanPowderCoating(result, out);
		addPowerScroll(result, out);
		addMasterStars(result, out);
		addWitherScroll(result, out);
		addGemstones(result, out);
		addEnchantments(result, out);
		addPrestiges(result, out);
		addDrillParts(result, out);
		addRodParts(result, out);
		addBoosters(result, out);

		if (!result.calculations().isEmpty()) {
			out.add(Text.literal(" "));
			out.add(Text.empty()
					.append(Text.literal("Est. Total: ").formatted(Formatting.GREEN))
					.append(priceFormat(result.price()))
					.append(priceShortFormat(result.price()))
			);
			if (!hasBase) {
				out.add(Text.empty()
						.append(Text.literal("Missing base item value!").formatted(Formatting.RED))
				);
			}
		}

		return out;
	}

	private void addCosmetic(@NotNull ItemValueResult result, List<Text> out) {
		Calculation skin = result.get(Calculation.Type.SKIN);
		Calculation dye = result.get(Calculation.Type.DYE);
		if (skin != null || dye != null) {
			double total = (skin != null ? skin.price() : 0) + (dye != null ? dye.price() : 0);
			out.add(Text.empty()
					.append(Text.literal("Cosmetic:").formatted(Formatting.GRAY))
					.append(priceShortFormat(total))
			);
		}
		if (skin != null) {
			String displayName = StonksUtils.capitalize(skin.skyBlockId());
			out.add(Text.empty()
					.append(Text.literal(ARROW + " Skin: ").formatted(Formatting.GRAY, Formatting.ITALIC))
					.append(Text.literal(displayName).formatted(Formatting.LIGHT_PURPLE))
					.append(priceShortFormat(skin.price()))
			);
		}
		if (dye != null) {
			String displayName = StonksUtils.capitalize(dye.skyBlockId());
			out.add(Text.empty()
					.append(Text.literal(ARROW + " Dye: ").formatted(Formatting.GRAY, Formatting.ITALIC))
					.append(Text.literal(displayName).formatted(Formatting.LIGHT_PURPLE))
					.append(priceShortFormat(dye.price()))
			);
		}
	}

	private void addEnchantedBook(@NotNull ItemValueResult result, List<Text> out) {
		List<Calculation> enchants = result.getList(Calculation.Type.ENCHANTED_BOOK);
		if (!enchants.isEmpty()) {
			out.add(Text.literal("Enchanted Book:").formatted(Formatting.GRAY));
			enchants.forEach(enchant -> {
				String enchantName = getInfos(enchant.skyBlockId().toLowerCase(Locale.ENGLISH)).left() + " " + enchant.count();
				out.add(Text.empty()
						.append(Text.literal(ARROW + " " + enchantName).formatted(Formatting.BLUE))
						.append(priceShortFormat(enchant.price()))
				);
			});
		}
	}

	private void addUltimateEnchantedBook(@NotNull ItemValueResult result, List<Text> out) {
		Calculation ultimate = result.get(Calculation.Type.ULTIMATE_ENCHANTED_BOOK);
		if (ultimate != null) {
			String level = ultimate.count() > 0 ? " " + RomanNumeralUtils.generate(ultimate.count()) : " " + ultimate.count();
			String ultimateName = StonksUtils.capitalize(ultimate.skyBlockId()) + level;
			out.add(Text.empty()
					.append(Text.literal(" " + ultimateName).formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
					.append(priceShortFormat(ultimate.price())));
		}
	}

	private void addReforge(@NotNull ItemValueResult result, List<Text> out) {
		Calculation reforge = result.get(Calculation.Type.REFORGE);
		if (reforge != null) {
			Pair<String, Rarity> infos = getInfos(reforge.skyBlockId());
			String displayName = infos.left();
			Formatting color = infos.right() == Rarity.UNKNOWN ? Formatting.BLUE : infos.right().getFormatting();
			boolean fromBazaar = true;
			if (displayName.equals(reforge.skyBlockId())) {
				displayName = StonksUtils.capitalize(reforge.skyBlockId()) + "*";
				color = Formatting.BLUE;
				fromBazaar = false;
			}
			out.add(Text.empty()
					.append(Text.literal("Reforge: ").formatted(Formatting.GRAY))
					.append(Text.literal(displayName).formatted(color))
					.append(fromBazaar ? priceShortFormat(reforge.price()) : Text.empty())
			);
		}
	}

	private void addRecombobulator(@NotNull ItemValueResult result, List<Text> out) {
		Calculation recombobulator = result.get(Calculation.Type.RECOMBOBULATOR);
		if (recombobulator != null) {
			out.add(formatHaving("Recombobulator", recombobulator.price()));
		}
	}

	private void addEnrichment(@NotNull ItemValueResult result, List<Text> out) {
		Calculation enrichment = result.get(Calculation.Type.TALISMAN_ENRICHMENT);
		if (enrichment != null) {
			out.add(formatHaving("Enrichment", enrichment.price()));
		}
	}

	private void addPocketSackInASack(@NotNull ItemValueResult result, List<Text> out) {
		Calculation pocketSackInASack = result.get(Calculation.Type.POCKET_SACK_IN_A_SACK);
		if (pocketSackInASack != null) {
			out.add(formatProgress("Pocket Sack-in-a-Sack", pocketSackInASack.count(), 3, pocketSackInASack.price()));
		}
	}

	private void addHotPotatoBook(@NotNull ItemValueResult result, List<Text> out) {
		Calculation hotPotato = result.get(Calculation.Type.HOT_POTATO_BOOK);
		if (hotPotato != null) {
			out.add(formatProgress("Hot Potato", hotPotato.count(), 10, hotPotato.price()));
		}
	}

	private void addFumingPotatoBook(@NotNull ItemValueResult result, List<Text> out) {
		Calculation fumingPotato = result.get(Calculation.Type.FUMING_POTATO_BOOK);
		if (fumingPotato != null) {
			out.add(formatProgress("Fuming", fumingPotato.count(), 5, fumingPotato.price()));
		}
	}

	private void addArtOfWar(@NotNull ItemValueResult result, List<Text> out) {
		Calculation war = result.get(Calculation.Type.ART_OF_WAR);
		if (war != null) {
			out.add(formatHaving("Art Of War", war.price()));
		}
	}

	private void addArtOfPeace(@NotNull ItemValueResult result, List<Text> out) {
		Calculation peace = result.get(Calculation.Type.ART_OF_PEACE);
		if (peace != null) {
			out.add(formatHaving("Art Of Peace", peace.price()));
		}
	}

	private void addWoodSingularity(@NotNull ItemValueResult result, List<Text> out) {
		Calculation woodSingularity = result.get(Calculation.Type.WOOD_SINGULARITY);
		if (woodSingularity != null) {
			out.add(formatHaving("Wood Singularity", woodSingularity.price()));
		}
	}

	private void addFarmingForDummies(@NotNull ItemValueResult result, List<Text> out) {
		Calculation farming4Dummies = result.get(Calculation.Type.FARMING_FOR_DUMMIES);
		if (farming4Dummies != null) {
			out.add(formatProgress("Farming For Dummies", farming4Dummies.count(), 5, farming4Dummies.price()));
		}
	}

	private void addBookOfStats(@NotNull ItemValueResult result, List<Text> out) {
		Calculation stats = result.get(Calculation.Type.STATS_BOOK);
		if (stats != null) {
			out.add(formatHaving("Book of Stats", stats.price()));
		}
	}

	private void addPolarvoidBook(@NotNull ItemValueResult result, List<Text> out) {
		Calculation polarvoid = result.get(Calculation.Type.POLARVOID_BOOK);
		if (polarvoid != null) {
			out.add(formatProgress("Polarvoid Book", polarvoid.count(), 5, polarvoid.price()));
		}
	}

	private void addJalapenoBook(@NotNull ItemValueResult result, List<Text> out) {
		Calculation jalapeno = result.get(Calculation.Type.JALAPENO_BOOK);
		if (jalapeno != null) {
			out.add(formatHaving("Jalapeno Book", jalapeno.price()));
		}
	}

	private void addWetBook(@NotNull ItemValueResult result, List<Text> out) {
		Calculation wet = result.get(Calculation.Type.WET_BOOK);
		if (wet != null) {
			out.add(formatProgress("Wet Book", wet.count(), 5, wet.price()));
		}
	}

	private void addManaDisintegrator(@NotNull ItemValueResult result, List<Text> out) {
		Calculation manaDisintegrator = result.get(Calculation.Type.MANA_DISINTEGRATOR);
		if (manaDisintegrator != null) {
			out.add(formatProgress("Mana Disintegrator", manaDisintegrator.count(), 10, manaDisintegrator.price()));
		}
	}

	private void addTransmissionTuner(@NotNull ItemValueResult result, List<Text> out) {
		Calculation transmissionTuner = result.get(Calculation.Type.TRANSMISSION_TUNER);
		if (transmissionTuner != null) {
			out.add(formatProgress("Transmission Tuner", transmissionTuner.count(), 4, transmissionTuner.price()));
		}
	}

	private void addEtherwarp(@NotNull ItemValueResult result, List<Text> out) {
		Calculation etherwarp = result.get(Calculation.Type.ETHERWARP_CONDUIT);
		if (etherwarp != null) {
			out.add(formatHaving("Etherwarp Conduit", etherwarp.price()));
		}
	}

	private void addDivanPowderCoating(@NotNull ItemValueResult result, List<Text> out) {
		Calculation divanPowderCoating = result.get(Calculation.Type.DIVAN_POWDER_COATING);
		if (divanPowderCoating != null) {
			out.add(formatHaving("Divan Powder Coating", divanPowderCoating.price()));
		}
	}

	private void addPowerScroll(@NotNull ItemValueResult result, List<Text> out) {
		Calculation power = result.get(Calculation.Type.POWER_SCROLL);
		if (power != null) {
			Pair<String, Rarity> infos = getInfos(power.skyBlockId());
			out.add(formatHaving("Power Scroll", power.price()));
			out.add(Text.literal(ARROW + " " + infos.left())
					.formatted(infos.right() == Rarity.UNKNOWN ? Formatting.DARK_PURPLE : infos.right().getFormatting()));
		}
	}

	private void addMasterStars(@NotNull ItemValueResult result, List<Text> out) {
		List<Calculation> masterStars = result.getList(Calculation.Type.MASTER_STAR);
		if (!masterStars.isEmpty()) {
			double price = masterStars.stream().mapToDouble(Calculation::price).sum();
			int lvl = Math.min(masterStars.size(), 5);
			MutableText text = Text.empty().append(Text.literal("Master Stars: ").formatted(Formatting.GRAY));
			for (int i = 0; i < lvl; i++) {
				text.append(Text.literal(MASTER_STARS_CIRCLED[i]).formatted(Formatting.RED));
			}
			text.append(priceShortFormat(price));
			out.add(text);
		}
	}

	private void addWitherScroll(@NotNull ItemValueResult result, List<Text> out) {
		List<Calculation> wither = result.getList(Calculation.Type.WITHER_SCROLL);
		if (!wither.isEmpty()) {
			out.add(Text.empty().append(Text.literal("Wither Scrolls:").formatted(Formatting.GRAY)));
			wither.forEach(scroll -> {
				String displayName = getInfos(scroll.skyBlockId()).left();
				out.add(Text.empty()
						.append(Text.literal(ARROW + " " + displayName).formatted(Formatting.DARK_PURPLE))
						.append(priceShortFormat(scroll.price()))
				);
			});
		}
	}

	private void addGemstones(@NotNull ItemValueResult result, List<Text> out) {
		List<Calculation> gems = result.getList(Calculation.Type.GEMSTONE);
		if (!gems.isEmpty()) {
			out.add(Text.literal("Gemstones:").formatted(Formatting.GRAY));

			List<Calculation> slotCosts = result.getList(Calculation.Type.GEMSTONE_SLOT);
			if (!slotCosts.isEmpty()) {
				double price = slotCosts.stream().mapToDouble(Calculation::price).sum();
				out.add(Text.empty()
						.append(Text.literal(ARROW + " Slot Costs:").formatted(Formatting.GRAY))
						.append(priceShortFormat(price))
				);
			}

			for (Calculation calc : gems) {
				Optional<Gemstones.GemstoneApplied> gemSlot = Gemstones.GemstoneApplied.fromSkyBlockGemstoneId(calc.skyBlockId());
				if (gemSlot.isPresent()) {
					Gemstones.GemstoneApplied gem = gemSlot.get();
					out.add(Text.empty()
							.append(Text.literal(" " + gem.type().getStatIcon()
									+ " " + gem.quality().getDisplayName()
									+ " " + gem.type().getDisplayName()
									+ " Gemstone").formatted(gem.quality().getColor()))
							.append(priceShortFormat(calc.price()))
					);
				} else {
					String gemName = getInfos(calc.skyBlockId()).left();
					out.add(Text.empty()
							.append(Text.literal(" " + gemName))
							.append(priceShortFormat(calc.price()))
					);
				}
			}
		}
	}

	private void addEnchantments(@NotNull ItemValueResult result, List<Text> out) {
		Calculation ultimate = result.get(Calculation.Type.ULTIMATE_ENCHANTMENT);
		List<Calculation> enchants = result.getList(Calculation.Type.ENCHANTMENT);
		List<Calculation> upgrades = result.getList(Calculation.Type.ENCHANTMENT_UPGRADE);
		boolean showEnchantSection = ultimate != null || !enchants.isEmpty() || !upgrades.isEmpty();
		if (showEnchantSection) {
			out.add(Text.literal("Enchantments:").formatted(Formatting.GRAY));
			// Ultimate enchantment en priorité
			if (ultimate != null) {
				int base = CalculatorConstants.ULTIMATE_BASE_LEVELS.getOrDefault(ultimate.skyBlockId(), 1);
				String level = RomanNumeralUtils.generate(base);
				String ultimateName = StonksUtils.capitalize(ultimate.skyBlockId()) + " " + level;
				// Pourquoi j'ai fait comme ça ? je ne sais pas, mais avoir une map dédié serait pas mal, je pense.
				if (ultimateName.contains("Ultimate Wise")) {
					ultimateName = "Ultimate Wise I";
				} else {
					ultimateName = ultimateName.replace("Ultimate ", "");
				}
				out.add(Text.empty()
						.append(ARROW + " " + ultimate.count() + "x").formatted(Formatting.DARK_GRAY)
						.append(Text.literal(" " + ultimateName).formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
						.append(priceShortFormat(ultimate.price())));
			}
			// Upgrades
			upgrades.forEach(upgrade -> {
				String upgradeName = getInfos(upgrade.skyBlockId()).left();
				out.add(Text.empty()
						.append(Text.literal(ARROW + " " + upgradeName).formatted(Formatting.DARK_PURPLE))
						.append(priceShortFormat(upgrade.price()))
				);
			});
			// Les autres enchantments
			enchants.stream()
					.limit(MAX_ENCHANT_DISPLAY)
					.forEach(enchant -> {
						String enchantName = getInfos(enchant.skyBlockId().toLowerCase(Locale.ENGLISH)).left() + " " + enchant.count();
						out.add(Text.empty()
								.append(Text.literal(ARROW + " " + enchantName).formatted(Formatting.BLUE))
								.append(priceShortFormat(enchant.price()))
						);
					});
			// S'il y en a plus
			if (enchants.size() > MAX_ENCHANT_DISPLAY) {
				out.add(Text.literal(" … and " + (enchants.size() - MAX_ENCHANT_DISPLAY) + " more").formatted(Formatting.GRAY, Formatting.ITALIC));
			}
		}
	}

	private void addPrestiges(@NotNull ItemValueResult result, List<Text> out) {
		List<Calculation> prestiges = result.getList(Calculation.Type.PRESTIGE);
		if (!prestiges.isEmpty()) {
			double price = prestiges.stream().mapToDouble(Calculation::price).sum();
			out.add(Text.empty()
					.append(Text.literal("Prestiges").formatted(Formatting.GRAY))
					.append(Text.literal("*").formatted(Formatting.RED))
					.append(Text.literal(": ").formatted(Formatting.GRAY))
					.append(Text.literal("" + prestiges.size()).formatted(Formatting.DARK_PURPLE))
					.append(priceShortFormat(price))
			);
		}
	}

	private void addDrillParts(@NotNull ItemValueResult result, List<Text> out) {
		List<Calculation> drillParts = result.getList(Calculation.Type.DRILL_PART);
		if (!drillParts.isEmpty()) {
			out.add(Text.literal("Drill Parts:").formatted(Formatting.GRAY));
			drillParts.forEach(drillPart -> {
				Pair<String, Rarity> infos = getInfos(drillPart.skyBlockId());
				String displayName = infos.left();
				Formatting color = infos.right() == Rarity.UNKNOWN ? Formatting.GREEN : infos.right().getFormatting();
				out.add(Text.empty()
						.append(Text.literal(ARROW + " " + displayName).formatted(color))
						.append(priceShortFormat(drillPart.price()))
				);
			});
		}
	}

	private void addRodParts(@NotNull ItemValueResult result, List<Text> out) {
		List<Calculation> rodParts = result.getList(Calculation.Type.ROD_PART);
		if (!rodParts.isEmpty()) {
			out.add(Text.literal("Rod Parts:").formatted(Formatting.GRAY));
			rodParts.forEach(drillPart -> {
				Pair<String, Rarity> infos = getInfos(drillPart.skyBlockId());
				String displayName = infos.left();
				Formatting color = infos.right() == Rarity.UNKNOWN ? Formatting.AQUA : infos.right().getFormatting();
				out.add(Text.empty()
						.append(Text.literal(ARROW + " " + displayName).formatted(color))
						.append(priceShortFormat(drillPart.price()))
				);
			});
		}
	}

	private void addBoosters(@NotNull ItemValueResult result, List<Text> out) {
		List<Calculation> boosters = result.getList(Calculation.Type.BOOSTERS);
		if (!boosters.isEmpty()) {
			double price = boosters.stream().mapToDouble(Calculation::price).sum();
			out.add(Text.empty()
					.append(Text.literal("Boosters:").formatted(Formatting.GRAY))
					.append(Text.literal(" " + boosters.size()).formatted(Formatting.AQUA))
					.append(priceShortFormat(price))
			);
		}
	}

	private @NotNull Pair<String, Rarity> getInfos(String skyBlockId) {
		SkyBlockItemData itemData = CaribouStonks.core().getHypixelDataSource().getSkyBlockItem(skyBlockId);
		if (itemData != null) {
			return Pair.of(itemData.name(), itemData.tier());
		} else {
			SkyBlockEnchantment enchantment = CaribouStonks.core().getModDataSource().getSkyBlockEnchantment(skyBlockId);
			if (enchantment != null) {
				return Pair.of(enchantment.name(), Rarity.UNKNOWN);
			} else {
				return Pair.of(skyBlockId, Rarity.UNKNOWN);
			}
		}
	}

	private Text formatHaving(@NotNull String label, double price) {
		return Text.empty()
				.append(Text.literal(label + ": ").formatted(Formatting.GRAY))
				.append(Text.literal("✔").formatted(Formatting.GREEN))
				.append(priceShortFormat(price));
	}

	private Text formatProgress(@NotNull String label, int current, int max, double price) {
		return Text.empty()
				.append(Text.literal(label + ": ").formatted(Formatting.GRAY))
				.append(Text.literal("" + current).formatted(Formatting.GREEN))
				.append(Text.literal("/").formatted(Formatting.GRAY))
				.append(Text.literal("" + max).formatted(Formatting.YELLOW))
				.append(priceShortFormat(price));
	}

	private Text priceFormat(double value) {
		return Text.literal(StonksUtils.INTEGER_NUMBERS.format(value)).formatted(Formatting.GOLD);
	}

	private Text priceShortFormat(double value) {
		return Text.empty()
				.append(Text.literal(" (").formatted(Formatting.GRAY))
				.append(Text.literal(StonksUtils.SHORT_FLOAT_NUMBERS.format(value)).formatted(Formatting.GOLD))
				.append(Text.literal(")").formatted(Formatting.GRAY));
	}

	public void reset(boolean full) {
		currentItem = null;
		if (full) {
			lines.clear();
		}
	}
}
