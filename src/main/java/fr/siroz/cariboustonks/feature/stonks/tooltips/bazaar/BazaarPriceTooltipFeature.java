package fr.siroz.cariboustonks.feature.stonks.tooltips.bazaar;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.core.component.ContainerMatcherComponent;
import fr.siroz.cariboustonks.core.component.TooltipAppenderComponent;
import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.core.skyblock.AttributeAPI;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.core.skyblock.data.hypixel.HypixelDataSource;
import fr.siroz.cariboustonks.core.skyblock.data.hypixel.bazaar.BazaarProduct;
import fr.siroz.cariboustonks.feature.stonks.tooltips.TooltipPriceDisplayType;
import fr.siroz.cariboustonks.util.Client;
import fr.siroz.cariboustonks.util.StonksUtils;
import fr.siroz.cariboustonks.util.colors.Colors;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BazaarPriceTooltipFeature extends Feature {

	private final HypixelDataSource hypixelDataSource;

	public BazaarPriceTooltipFeature(int priority) {
		this.hypixelDataSource = CaribouStonks.skyBlock().getHypixelDataSource();

		this.addComponent(ContainerMatcherComponent.class, ContainerMatcherComponent.empty());
		this.addComponent(TooltipAppenderComponent.class, TooltipAppenderComponent.builder()
				.priority(priority)
				.appender(this::appendToTooltip)
				.build());
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock() && ConfigManager.getConfig().general.stonks.bazaarTooltipPrice;
	}

	private void appendToTooltip(@Nullable Slot focusedSlot, @NotNull ItemStack item, @NotNull List<Component> lines) {
		if (hypixelDataSource.isBazaarInUpdate()) {
			lines.add(Component.literal("Bazaar is currently updating...").withStyle(ChatFormatting.RED));
			return;
		}

		String skyBlockApiId = SkyBlockAPI.getSkyBlockApiId(item);

		// Fix - The Foraging Update 0.23 - HuntingBox, Attribute Menu & Fusion Machine - Shard API ID Hypixel wtf
		skyBlockApiId = AttributeAPI.getSkyBlockApiIdFromNewShard(skyBlockApiId, item, lines);
		// Fix - end

		if (hypixelDataSource.hasBazaarItem(skyBlockApiId)) {
			Optional<BazaarProduct> product = hypixelDataSource.getBazaarItem(skyBlockApiId);
			if (product.isEmpty()) {
				lines.add(Component.literal("Bazaar item error.").withStyle(ChatFormatting.RED));
				return;
			}

			int count = item.getCount();

			switch (ConfigManager.getConfig().general.stonks.bazaarTooltipPriceType) {
				case ALL -> {
					addBazaarLine(lines, "Bazaar Buy: ", product.get().buyPrice(), count);
					addBazaarLine(lines, "Bazaar Sell: ", product.get().sellPrice(), count);
					addBazaarLine(lines, "Bazaar Buy-Avg: ", product.get().weightedAverageBuyPrice(), 1);
					addBazaarLine(lines, "Bazaar Sell-Avg: ", product.get().weightedAverageSellPrice(), 1);

					if (!Client.hasShiftDown() && count > 1) {
						lines.add(Component.literal("[Press SHIFT for x" + count + "]").withStyle(ChatFormatting.DARK_GRAY));
					}
				}
				case NORMAL -> {
					addBazaarLine(lines, "Bazaar Buy: ", product.get().buyPrice(), count);
					addBazaarLine(lines, "Bazaar Sell: ", product.get().sellPrice(), count);

					if (!Client.hasShiftDown() && count > 1) {
						lines.add(Component.literal("[Press SHIFT for x" + count + "]").withStyle(ChatFormatting.DARK_GRAY));
					}
				}
				case AVERAGE -> {
					addBazaarLine(lines, "Bazaar Buy-Avg: ", product.get().weightedAverageBuyPrice(), 1);
					addBazaarLine(lines, "Bazaar Sell-Avg: ", product.get().weightedAverageSellPrice(), 1);
				}
				case null, default -> {
				}
			}

			if (ConfigManager.getConfig().general.stonks.bazaarTooltipMoreData) {
				double absoluteSpread = product.get().spread();
				double spreadPercentage = product.get().spreadPercentage();
				Component spread = Component.empty()
						.append(Component.literal(" | Spreed: ").withStyle(ChatFormatting.RED))
						.append(Component.literal(StonksUtils.FLOAT_NUMBERS.format(spreadPercentage) + "%").withColor(Colors.RED.asInt()))
						.append(Component.literal(" | ").withStyle(ChatFormatting.GRAY))
						.append(Component.literal(StonksUtils.INTEGER_NUMBERS.format(absoluteSpread)).withColor(Colors.RED.asInt()))
						.append(Component.literal(" (").withStyle(ChatFormatting.GRAY))
						.append(Component.literal(StonksUtils.SHORT_FLOAT_NUMBERS.format(absoluteSpread)).withColor(Colors.RED.asInt()))
						.append(Component.literal(")").withStyle(ChatFormatting.GRAY));
				lines.add(spread);
			}
		}
	}

	private void addBazaarLine(@NotNull List<Component> lines, @NotNull String label, double value, int count) {
		if (value < 0) {
			lines.add(Component.literal(label).withStyle(ChatFormatting.YELLOW)
						.append(Component.literal(" No Data").withStyle(ChatFormatting.RED)));
			return;
		}

		TooltipPriceDisplayType displayType = ConfigManager.getConfig().general.stonks.bazaarTooltipPriceDisplayType;
		String display;
		if (value < 100) {
			display = StonksUtils.FLOAT_NUMBERS.format(value);
		} else {

			if (Client.hasShiftDown() && count > 1) value *= count;

			if (displayType == TooltipPriceDisplayType.SHORT) {
				display = StonksUtils.SHORT_FLOAT_NUMBERS.format(value);
			} else {
				display = StonksUtils.INTEGER_NUMBERS.format(value);
			}
		}

		switch (displayType) {
			case FULL, SHORT -> lines.add(Component.literal(label).withStyle(ChatFormatting.YELLOW)
					.append(Component.literal(display + " Coins").withStyle(ChatFormatting.GOLD))
			);
			case ALL -> lines.add(Component.literal(label).withStyle(ChatFormatting.YELLOW)
					.append(Component.literal(display + " Coins").withStyle(ChatFormatting.GOLD))
					.append(Component.literal(" (").withStyle(ChatFormatting.GRAY))
					.append(Component.literal(StonksUtils.SHORT_FLOAT_NUMBERS.format(value)).withStyle(ChatFormatting.GOLD))
					.append(Component.literal(")").withStyle(ChatFormatting.GRAY))
			);
			case null, default -> {
			}
		}
	}
}
