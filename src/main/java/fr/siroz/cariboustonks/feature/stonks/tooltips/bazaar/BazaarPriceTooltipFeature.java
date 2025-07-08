package fr.siroz.cariboustonks.feature.stonks.tooltips.bazaar;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.core.data.hypixel.HypixelDataSource;
import fr.siroz.cariboustonks.core.data.algo.BazaarItemAnalytics;
import fr.siroz.cariboustonks.core.data.hypixel.bazaar.Product;
import fr.siroz.cariboustonks.core.skyblock.AttributeAPI;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.feature.Feature;
import fr.siroz.cariboustonks.feature.stonks.tooltips.TooltipPriceDisplayType;
import fr.siroz.cariboustonks.manager.container.ContainerMatcherTrait;
import fr.siroz.cariboustonks.manager.container.tooltip.ContainerTooltipAppender;
import fr.siroz.cariboustonks.util.ItemUtils;
import fr.siroz.cariboustonks.util.StonksUtils;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class BazaarPriceTooltipFeature extends Feature implements ContainerMatcherTrait, ContainerTooltipAppender {

	private final HypixelDataSource hypixelDataSource;
	private final int priority;

	public BazaarPriceTooltipFeature(int priority) {
		this.priority = priority;
		this.hypixelDataSource = CaribouStonks.core().getHypixelDataSource();
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock() && ConfigManager.getConfig().general.stonks.bazaarTooltipPrice;
	}

	@Override
	public @Nullable Pattern getTitlePattern() {
		return null;
	}

	@Override
	public void appendToTooltip(@Nullable Slot focusedSlot, @NotNull ItemStack item, @NotNull List<Text> lines) {
		if (hypixelDataSource.isBazaarInUpdate()) {
			lines.add(Text.literal("Bazaar is currently updating...").formatted(Formatting.RED));
			return;
		}

		String skyBlockApiId = ItemUtils.getSkyBlockApiId(item);

		// Fix - The Foraging Update 0.23 - HuntingBox, Attribute Menu & Fusion Machine - Shard API ID Hypixel wtf
		skyBlockApiId = AttributeAPI.getSkyBlockApiIdFromNewShard(skyBlockApiId, item, lines);
		// Fix - end

		if (hypixelDataSource.hasBazaarItem(skyBlockApiId)) {
			Optional<Product> product = hypixelDataSource.getBazaarItem(skyBlockApiId);
			if (product.isEmpty()) {
				lines.add(Text.literal("Bazaar item error.").formatted(Formatting.RED));
				return;
			}

			int count = item.getCount();

			switch (ConfigManager.getConfig().general.stonks.bazaarTooltipPriceType) {
				case ALL -> {
					addBazaarLine(lines, "Bazaar Buy: ", BazaarItemAnalytics.buyPrice(product.get()), count);
					addBazaarLine(lines, "Bazaar Sell: ", BazaarItemAnalytics.sellPrice(product.get()), count);
					addBazaarLine(lines, "Bazaar Buy-Avg: ", BazaarItemAnalytics.weightedAverageBuyPrice(product.get()), 1);
					addBazaarLine(lines, "Bazaar Sell-Avg: ", BazaarItemAnalytics.weightedAverageSellPrice(product.get()), 1);

					if (!Screen.hasShiftDown() && count > 1) {
						lines.add(Text.literal("[Press SHIFT for x" + count + "]").formatted(Formatting.DARK_GRAY));
					}
				}
				case NORMAL -> {
					addBazaarLine(lines, "Bazaar Buy: ", BazaarItemAnalytics.buyPrice(product.get()), count);
					addBazaarLine(lines, "Bazaar Sell: ", BazaarItemAnalytics.sellPrice(product.get()), count);

					if (!Screen.hasShiftDown() && count > 1) {
						lines.add(Text.literal("[Press SHIFT for x" + count + "]").formatted(Formatting.DARK_GRAY));
					}
				}
				case AVERAGE -> {
					addBazaarLine(lines, "Bazaar Buy-Avg: ", BazaarItemAnalytics.weightedAverageBuyPrice(product.get()), 1);
					addBazaarLine(lines, "Bazaar Sell-Avg: ", BazaarItemAnalytics.weightedAverageSellPrice(product.get()), 1);
				}
				case null, default -> {
				}
			}
		}
	}

	@Override
	public int getPriority() {
		return priority;
	}

	private void addBazaarLine(@NotNull List<Text> lines, @NotNull String label, double value, int count) {
		if (value < 0) {
			lines.add(Text.literal(label).formatted(Formatting.YELLOW)
						.append(Text.literal(" No Data").formatted(Formatting.RED)));
			return;
		}

		TooltipPriceDisplayType displayType = ConfigManager.getConfig().general.stonks.bazaarTooltipPriceDisplayType;
		String display;
		if (value < 100) {
			display = StonksUtils.FLOAT_NUMBERS.format(value);
		} else {

			if (Screen.hasShiftDown() && count > 1) value *= count;

			if (displayType == TooltipPriceDisplayType.SHORT) {
				display = StonksUtils.SHORT_FLOAT_NUMBERS.format(value);
			} else {
				display = StonksUtils.INTEGER_NUMBERS.format(value);
			}
		}

		switch (displayType) {
			case FULL, SHORT -> lines.add(Text.literal(label).formatted(Formatting.YELLOW)
					.append(Text.literal(display + " Coins").formatted(Formatting.GOLD))
			);
			case ALL -> lines.add(Text.literal(label).formatted(Formatting.YELLOW)
					.append(Text.literal(display + " Coins").formatted(Formatting.GOLD))
					.append(Text.literal(" (").formatted(Formatting.GRAY))
					.append(Text.literal(StonksUtils.SHORT_FLOAT_NUMBERS.format(value)).formatted(Formatting.GOLD))
					.append(Text.literal(")").formatted(Formatting.GRAY))
			);
			case null, default -> {
			}
		}
	}
}
