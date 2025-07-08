package fr.siroz.cariboustonks.feature.stonks.info;

import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.core.data.algo.BazaarItemAnalytics;
import fr.siroz.cariboustonks.core.data.hypixel.bazaar.Product;
import fr.siroz.cariboustonks.core.data.hypixel.bazaar.Summary;
import fr.siroz.cariboustonks.feature.stonks.AbstractItemStonksWidget;
import fr.siroz.cariboustonks.util.StonksUtils;
import fr.siroz.cariboustonks.util.colors.Colors;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ItemInformationWidget extends AbstractItemStonksWidget { // TODO le code ne ressemble a rien après 1687 modifs

	private boolean bazaar;

	private double buyPrice, sellPrice;
	private double buyAvg, sellAvg;
	private double buyMedian, sellMedian;
	private long buyVolume, sellVolume;
	private long buyOrders, sellOrders;
	private long buyMovingWeek, sellMovingWeek;

	private double spreadPercentage;
	private double orderImbalancePercentage;
	private double vwap;
	private double standardDeviationBuy, standardDeviationSell;
	private double sellSideLiquiditySlope;

	//private double currentSMA, oneWeekAgoSMA, oneMouthAgoSMA;
	//private double currentEMA, oneWeekAgoEMA, oneMouthAgoEMA;

	public ItemInformationWidget(
			@Nullable Product bazaarItem,
			int width,
			int height
	) {
		super(width, height);
		if (bazaarItem != null) {
			this.bazaar = true;
			this.buyPrice = BazaarItemAnalytics.buyPrice(bazaarItem);
			this.sellPrice = BazaarItemAnalytics.sellPrice(bazaarItem);
			this.buyAvg = BazaarItemAnalytics.weightedAverageBuyPrice(bazaarItem);
			this.sellAvg = BazaarItemAnalytics.weightedAverageSellPrice(bazaarItem);
			List<Double> buyPrices = bazaarItem.buySummary().stream().map(Summary::pricePerUnit).toList();
			List<Double> sellPrices = bazaarItem.sellSummary().stream().map(Summary::pricePerUnit).toList();
			this.buyMedian = StonksUtils.calculateMedian(buyPrices);
			this.sellMedian = StonksUtils.calculateMedian(sellPrices);
			this.buyVolume = bazaarItem.quickStatus().buyVolume();
			this.sellVolume = bazaarItem.quickStatus().sellVolume();
			this.buyOrders = bazaarItem.quickStatus().buyOrders();
			this.sellOrders = bazaarItem.quickStatus().sellOrders();
			this.buyMovingWeek = bazaarItem.quickStatus().buyMovingWeek();
			this.sellMovingWeek = bazaarItem.quickStatus().sellMovingWeek();
			this.spreadPercentage = BazaarItemAnalytics.spreadPercentage(bazaarItem);
			this.orderImbalancePercentage = BazaarItemAnalytics.orderImbalancePercentage(bazaarItem);
			this.vwap = BazaarItemAnalytics.vwap(bazaarItem);
			this.standardDeviationBuy = BazaarItemAnalytics.standardDeviation(bazaarItem.buySummary());
			this.standardDeviationSell = BazaarItemAnalytics.standardDeviation(bazaarItem.sellSummary());
			this.sellSideLiquiditySlope = BazaarItemAnalytics.calculateSellSideLiquiditySlope(bazaarItem, 95);
		}
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, int x, int y) {
		if (bazaar) {

			// Buy

			Text textBuy = Text.literal("Buy Price: ").formatted(Formatting.YELLOW)
					.append(Text.literal(StonksUtils.INTEGER_NUMBERS.format(buyPrice)).formatted(Formatting.GOLD))
					.append(Text.literal(" (").formatted(Formatting.GRAY))
					.append(Text.literal(StonksUtils.SHORT_FLOAT_NUMBERS.format(buyPrice)).formatted(Formatting.GOLD))
					.append(Text.literal(")").formatted(Formatting.GRAY));

			context.drawTextWithShadow(textRenderer, textBuy, x + 20, y + 20, Colors.WHITE.asInt());

			Text textAvgBuy = Text.literal("Avg. Price: ").formatted(Formatting.YELLOW)
					.append(Text.literal(StonksUtils.SHORT_FLOAT_NUMBERS.format(buyAvg)).formatted(Formatting.GOLD));

			context.drawTextWithShadow(textRenderer, textAvgBuy, x + 20, y + 30, Colors.WHITE.asInt());

			Text textBuyMedian = Text.literal("*Median Price: ").formatted(Formatting.YELLOW)
					.append(Text.literal(StonksUtils.SHORT_FLOAT_NUMBERS.format(buyMedian)).formatted(Formatting.GOLD));

			context.drawTextWithShadow(textRenderer, textBuyMedian, x + 20, y + 40, Colors.WHITE.asInt());

			// Buy - Infos

			Text textBuyOrderInfos = Text.literal(StonksUtils.SHORT_FLOAT_NUMBERS.format(buyVolume)).formatted(Formatting.DARK_GRAY)
					.append(" in " + StonksUtils.SHORT_FLOAT_NUMBERS.format(buyOrders) + " orders");

			context.drawTextWithShadow(textRenderer, textBuyOrderInfos, x + 20, y + 55, Colors.WHITE.asInt());

			Text textBuyMoving = Text.literal(StonksUtils.SHORT_FLOAT_NUMBERS.format(buyMovingWeek)).formatted(Formatting.DARK_GRAY)
					.append(Text.literal(" insta-buys in 7d"));

			context.drawTextWithShadow(textRenderer, textBuyMoving, x + 20, y + 65, Colors.WHITE.asInt());

			// Sell

			Text textSell = Text.literal("Sell Price: ").formatted(Formatting.YELLOW)
					.append(Text.literal(StonksUtils.INTEGER_NUMBERS.format(sellPrice)).formatted(Formatting.GOLD))
					.append(Text.literal(" (").formatted(Formatting.GRAY))
					.append(Text.literal(StonksUtils.SHORT_FLOAT_NUMBERS.format(sellPrice)).formatted(Formatting.GOLD))
					.append(Text.literal(")").formatted(Formatting.GRAY));

			context.drawTextWithShadow(textRenderer, textSell, x + 20, y + 80, Colors.WHITE.asInt());

			Text textAvgSell = Text.literal("Avg. Price: ").formatted(Formatting.YELLOW)
					.append(Text.literal(StonksUtils.SHORT_FLOAT_NUMBERS.format(sellAvg)).formatted(Formatting.GOLD));

			context.drawTextWithShadow(textRenderer, textAvgSell, x + 20, y + 90, Colors.WHITE.asInt());

			Text textSellMedian = Text.literal("*Median Price: ").formatted(Formatting.YELLOW)
					.append(Text.literal(StonksUtils.SHORT_FLOAT_NUMBERS.format(sellMedian)).formatted(Formatting.GOLD));

			context.drawTextWithShadow(textRenderer, textSellMedian, x + 20, y + 100, Colors.WHITE.asInt());

			// Sell - Infos

			Text textSellOrderInfos = Text.literal(StonksUtils.SHORT_FLOAT_NUMBERS.format(sellVolume)).formatted(Formatting.DARK_GRAY)
					.append(" in " + StonksUtils.SHORT_FLOAT_NUMBERS.format(sellOrders) + " orders");

			context.drawTextWithShadow(textRenderer, textSellOrderInfos, x + 20, y + 115, Colors.WHITE.asInt());

			Text textSellMoving = Text.literal(StonksUtils.SHORT_FLOAT_NUMBERS.format(sellMovingWeek)).formatted(Formatting.DARK_GRAY)
					.append(Text.literal(" insta-sells in 7d"));

			context.drawTextWithShadow(textRenderer, textSellMoving, x + 20, y + 125, Colors.WHITE.asInt());

			// ============= ANALYTICS =============

			if (ConfigManager.getConfig().general.stonks.showAllDataInInfoScreen) {
				// %

				Text textSpread = Text.literal("*Spread: ").formatted(Formatting.RED)
						.append(StonksUtils.FLOAT_NUMBERS.format(spreadPercentage) + " %").formatted(Formatting.RED);

				context.drawTextWithShadow(textRenderer, textSpread, x + 20, y + 145, Colors.WHITE.asInt());

				Text textImbalance = Text.literal("*Imbalance: ").formatted(Formatting.DARK_PURPLE)
						.append(Text.literal(StonksUtils.FLOAT_NUMBERS.format(orderImbalancePercentage) + " %")
								.formatted(Formatting.LIGHT_PURPLE));

				context.drawTextWithShadow(textRenderer, textImbalance, x + 20, y + 155, Colors.WHITE.asInt());

				// Trade Prices

				Text textVWAP = Text.literal("*VWAP: ").formatted(Formatting.LIGHT_PURPLE)
						.append(Text.literal(StonksUtils.SHORT_FLOAT_NUMBERS.format(vwap)).formatted(Formatting.DARK_PURPLE));

				context.drawTextWithShadow(textRenderer, textVWAP, x + 20, y + 165, Colors.WHITE.asInt());

				// Sell Orders Slope

				Text textSellOrderSlope = Text.literal("*Sell Orders Slope: ").formatted(Formatting.BLUE);
				context.drawTextWithShadow(textRenderer, textSellOrderSlope, x + 20, y + 175, Colors.WHITE.asInt());

				Text textSellOrderSlopeValue = Text.literal("+~ " + StonksUtils.DOUBLE_NUMBERS.format(sellSideLiquiditySlope))
						.formatted(Formatting.AQUA);
				context.drawTextWithShadow(textRenderer, textSellOrderSlopeValue, x + 20, y + 185, Colors.WHITE.asInt());

				// Standard Deviation - Buy/Sell

				Text textStandardDeviation = Text.literal("*Standard Deviation: ").formatted(Formatting.DARK_RED);
				context.drawTextWithShadow(textRenderer, textStandardDeviation, x + 20, y + 195, Colors.WHITE.asInt());

				Text textStandardDeviationValues = Text.literal(
								"Buy: " + StonksUtils.SHORT_FLOAT_NUMBERS.format(standardDeviationBuy) +
										" | Sell: " + StonksUtils.SHORT_FLOAT_NUMBERS.format(standardDeviationSell))
						.formatted(Formatting.RED);

				context.drawTextWithShadow(textRenderer, textStandardDeviationValues, x + 20, y + 205, Colors.WHITE.asInt());
			}

			// Warning
			Text warningInfo = Text.literal("* Not a true representation of all orders.").formatted(Formatting.DARK_GRAY, Formatting.ITALIC);
			context.drawTextWithShadow(textRenderer, warningInfo, x + 20, y + 225, Colors.WHITE.asInt());
		}
	}
}
