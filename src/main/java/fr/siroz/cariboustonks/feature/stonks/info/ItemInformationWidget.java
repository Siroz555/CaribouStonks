package fr.siroz.cariboustonks.feature.stonks.info;

import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.core.data.hypixel.bazaar.BazaarProduct;
import fr.siroz.cariboustonks.feature.stonks.AbstractItemStonksWidget;
import fr.siroz.cariboustonks.util.StonksUtils;
import fr.siroz.cariboustonks.util.colors.Colors;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

public class ItemInformationWidget extends AbstractItemStonksWidget {

	@Nullable
	private final BazaarProduct bazaarItem;

	public ItemInformationWidget(
			@Nullable BazaarProduct bazaarItem,
			int width,
			int height
	) {
		super(width, height);
		this.bazaarItem = bazaarItem;
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, int x, int y) {
		if (bazaarItem != null) {

			// Buy

			double buyPrice = bazaarItem.buyPrice();
			Text textBuy = Text.literal("Buy Price: ").formatted(Formatting.YELLOW)
					.append(Text.literal(StonksUtils.INTEGER_NUMBERS.format(buyPrice)).formatted(Formatting.GOLD))
					.append(Text.literal(" (").formatted(Formatting.GRAY))
					.append(Text.literal(StonksUtils.SHORT_FLOAT_NUMBERS.format(buyPrice)).formatted(Formatting.GOLD))
					.append(Text.literal(")").formatted(Formatting.GRAY));

			context.drawTextWithShadow(textRenderer, textBuy, x + 20, y + 20, Colors.WHITE.asInt());

			Text textAvgBuy = Text.literal("Avg. Price: ").formatted(Formatting.YELLOW)
					.append(Text.literal(StonksUtils.SHORT_FLOAT_NUMBERS.format(bazaarItem.weightedAverageBuyPrice())).formatted(Formatting.GOLD));

			context.drawTextWithShadow(textRenderer, textAvgBuy, x + 20, y + 30, Colors.WHITE.asInt());

			// Buy - Infos

			Text textBuyOrderInfos = Text.literal(StonksUtils.SHORT_FLOAT_NUMBERS.format(bazaarItem.buyVolume())).formatted(Formatting.DARK_GRAY)
					.append(" in " + StonksUtils.SHORT_FLOAT_NUMBERS.format(bazaarItem.buyOrders()) + " orders");

			context.drawTextWithShadow(textRenderer, textBuyOrderInfos, x + 20, y + 45, Colors.WHITE.asInt());

			Text textBuyMoving = Text.literal(StonksUtils.SHORT_FLOAT_NUMBERS.format(bazaarItem.buyMovingWeek())).formatted(Formatting.DARK_GRAY)
					.append(Text.literal(" insta-buys in 7d"));

			context.drawTextWithShadow(textRenderer, textBuyMoving, x + 20, y + 55, Colors.WHITE.asInt());

			// Sell

			double sellPrice = bazaarItem.sellPrice();
			Text textSell = Text.literal("Sell Price: ").formatted(Formatting.YELLOW)
					.append(Text.literal(StonksUtils.INTEGER_NUMBERS.format(sellPrice)).formatted(Formatting.GOLD))
					.append(Text.literal(" (").formatted(Formatting.GRAY))
					.append(Text.literal(StonksUtils.SHORT_FLOAT_NUMBERS.format(sellPrice)).formatted(Formatting.GOLD))
					.append(Text.literal(")").formatted(Formatting.GRAY));

			context.drawTextWithShadow(textRenderer, textSell, x + 20, y + 80, Colors.WHITE.asInt());

			Text textAvgSell = Text.literal("Avg. Price: ").formatted(Formatting.YELLOW)
					.append(Text.literal(StonksUtils.SHORT_FLOAT_NUMBERS.format(bazaarItem.weightedAverageSellPrice())).formatted(Formatting.GOLD));

			context.drawTextWithShadow(textRenderer, textAvgSell, x + 20, y + 90, Colors.WHITE.asInt());

			// Sell - Infos

			Text textSellOrderInfos = Text.literal(StonksUtils.SHORT_FLOAT_NUMBERS.format(bazaarItem.sellVolume())).formatted(Formatting.DARK_GRAY)
					.append(" in " + StonksUtils.SHORT_FLOAT_NUMBERS.format(bazaarItem.sellOrders()) + " orders");

			context.drawTextWithShadow(textRenderer, textSellOrderInfos, x + 20, y + 105, Colors.WHITE.asInt());

			Text textSellMoving = Text.literal(StonksUtils.SHORT_FLOAT_NUMBERS.format(bazaarItem.sellMovingWeek())).formatted(Formatting.DARK_GRAY)
					.append(Text.literal(" insta-sells in 7d"));

			context.drawTextWithShadow(textRenderer, textSellMoving, x + 20, y + 115, Colors.WHITE.asInt());

			// ============= ANALYTICS =============

			// Spread & %

			Text textSpread = Text.literal("Spreed: ").formatted(Formatting.RED)
					.append(Text.literal(StonksUtils.FLOAT_NUMBERS.format(bazaarItem.spreadPercentage()) + "%").withColor(Colors.RED.asInt()))
					.append(Text.literal(" | ").formatted(Formatting.GRAY))
					.append(Text.literal(StonksUtils.INTEGER_NUMBERS.format(bazaarItem.spread())).withColor(Colors.RED.asInt()))
					.append(Text.literal(" (").formatted(Formatting.GRAY))
					.append(Text.literal(StonksUtils.SHORT_FLOAT_NUMBERS.format(bazaarItem.spread())).withColor(Colors.RED.asInt()))
					.append(Text.literal(")").formatted(Formatting.GRAY));

			context.drawTextWithShadow(textRenderer, textSpread, x + 20, y + 145, Colors.WHITE.asInt());

			// Velocity - Buy/Sell

			Text textVelocity = Text.literal("Velocity: ").formatted(Formatting.DARK_AQUA);

			context.drawTextWithShadow(textRenderer, textVelocity, x + 20, y + 160, Colors.WHITE.asInt());

			Text textVelocityValues = Text.empty()
					.append(Text.literal("Buy: ").formatted(Formatting.DARK_AQUA))
					.append(Text.literal(StonksUtils.FLOAT_NUMBERS.format(bazaarItem.buyVelocity())).formatted(Formatting.AQUA))
					.append(Text.literal(" | ").formatted(Formatting.GRAY))
					.append(Text.literal("Sell: ").formatted(Formatting.DARK_AQUA))
					.append(Text.literal(StonksUtils.FLOAT_NUMBERS.format(bazaarItem.sellVelocity())).formatted(Formatting.AQUA));

			context.drawTextWithShadow(textRenderer, textVelocityValues, x + 20, y + 170, Colors.WHITE.asInt());

			if (ConfigManager.getConfig().general.stonks.showAllDataInInfoScreen) {

				// Standard Deviation - Buy/Sell

				Text textStandardDeviation = Text.literal("*Standard Deviation: ").formatted(Formatting.DARK_GREEN);
				context.drawTextWithShadow(textRenderer, textStandardDeviation, x + 20, y + 195, Colors.WHITE.asInt());

				Text textStandardDeviationValues = Text.literal(
								"Buy: " + StonksUtils.SHORT_FLOAT_NUMBERS.format(bazaarItem.buyPriceStdDev()) +
										" | Sell: " + StonksUtils.SHORT_FLOAT_NUMBERS.format(bazaarItem.sellPriceStdDev()))
						.formatted(Formatting.GREEN);

				context.drawTextWithShadow(textRenderer, textStandardDeviationValues, x + 20, y + 205, Colors.WHITE.asInt());

				// Warning
				Text warningInfo = Text.literal("* Not a true representation of all orders.").formatted(Formatting.DARK_GRAY, Formatting.ITALIC);
				context.drawTextWithShadow(textRenderer, warningInfo, x + 20, y + 225, Colors.WHITE.asInt());
			}
		}
	}
}
