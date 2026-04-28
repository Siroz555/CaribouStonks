package fr.siroz.cariboustonks.screen.stonks;

import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.core.data.generic.AuctionStatistics;
import fr.siroz.cariboustonks.core.data.hypixel.bazaar.BazaarProduct;
import fr.siroz.cariboustonks.util.StonksUtils;
import fr.siroz.cariboustonks.util.colors.Colors;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

class InformationWidget extends AbstractStonksWidget {

	@Nullable
	private final BazaarProduct bazaarProduct;
	@Nullable
	private final AuctionStatistics auction;

	InformationWidget(
			@Nullable BazaarProduct bazaarProduct,
			@Nullable AuctionStatistics auction,
			int width,
			int height
	) {
		super(width, height);
		this.bazaarProduct = bazaarProduct;
		this.auction = auction;
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, int x, int y) {
		if (bazaarProduct != null) {
			renderBazaarItem(context, x, y, bazaarProduct);
		}
		if (auction != null) {
			renderAuction(context, x, y, auction);
		}
	}

	private void renderBazaarItem(DrawContext context, int x, int y, BazaarProduct bazaarItem) {
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

	private void renderAuction(DrawContext guiGraphics, int x, int y, AuctionStatistics stats) {
		// ---------- Aujourd'hui ----------
		AuctionStatistics.PeriodStats today = stats.today();
		if (today.daysWithData() > 0) {
			Text soldToday = Text.literal("Sold Today: ").formatted(Formatting.WHITE)
					.append(Text.literal("" + today.totalItemsSold()).formatted(Formatting.GREEN));
			guiGraphics.drawTextWithShadow(textRenderer, soldToday, x + 20, y + 20, Colors.WHITE.asInt());

			Text minPriceToday = Text.literal("Lowest BIN (Today): ").formatted(Formatting.YELLOW)
					.append(Text.literal(StonksUtils.INTEGER_NUMBERS.format(today.minPrice())).formatted(Formatting.GOLD))
					.append(Text.literal(" (").formatted(Formatting.GRAY))
					.append(Text.literal(StonksUtils.SHORT_FLOAT_NUMBERS.format(today.minPrice())).formatted(Formatting.GOLD))
					.append(Text.literal(")").formatted(Formatting.GRAY));
			guiGraphics.drawTextWithShadow(textRenderer, minPriceToday, x + 20, y + 30, Colors.WHITE.asInt());

			Text avgPriceToday = Text.literal("Avg price (Today): ").formatted(Formatting.YELLOW)
					.append(Text.literal(StonksUtils.INTEGER_NUMBERS.format(today.avgPrice())).formatted(Formatting.GOLD))
					.append(Text.literal(" (").formatted(Formatting.GRAY))
					.append(Text.literal(StonksUtils.SHORT_FLOAT_NUMBERS.format(today.avgPrice())).formatted(Formatting.GOLD))
					.append(Text.literal(")").formatted(Formatting.GRAY));
			guiGraphics.drawTextWithShadow(textRenderer, avgPriceToday, x + 20, y + 40, Colors.WHITE.asInt());
		} else {
			Text soldTodayNoData = Text.literal("Sold Today: ").formatted(Formatting.WHITE)
					.append(Text.literal("No Data").formatted(Formatting.RED));
			guiGraphics.drawTextWithShadow(textRenderer, soldTodayNoData, x + 20, y + 20, Colors.WHITE.asInt());
		}

		// ---------- 7 derniers jours ----------
		AuctionStatistics.PeriodStats week = stats.last7Days();
		Text sold7d = Text.literal("Sold (7d): ").formatted(Formatting.WHITE)
				.append(Text.literal("" + week.totalItemsSold()).formatted(Formatting.GREEN));
		guiGraphics.drawTextWithShadow(textRenderer, sold7d, x + 20, y + 60, Colors.WHITE.asInt());

		if (week.hasPriceData()) {
			Text avgPrice7d = Text.literal("Avg price (7d): ").formatted(Formatting.YELLOW)
					.append(Text.literal(StonksUtils.INTEGER_NUMBERS.format(week.avgPrice())).formatted(Formatting.GOLD))
					.append(Text.literal(" (").formatted(Formatting.GRAY))
					.append(Text.literal(StonksUtils.SHORT_FLOAT_NUMBERS.format(week.avgPrice())).formatted(Formatting.GOLD))
					.append(Text.literal(")").formatted(Formatting.GRAY));
			guiGraphics.drawTextWithShadow(textRenderer, avgPrice7d, x + 20, y + 70, Colors.WHITE.asInt());
		} else {
			Text warningWeekPrice = Text.literal("No Data (7d)").formatted(Formatting.RED);
			guiGraphics.drawTextWithShadow(textRenderer, warningWeekPrice, x + 20, y + 70, Colors.WHITE.asInt());
		}

		// ---------- 30 derniers jours ----------
		AuctionStatistics.PeriodStats month = stats.last30Days();
		Text sold30d = Text.literal("Sold (30d): ").formatted(Formatting.WHITE)
				.append(Text.literal("" + month.totalItemsSold()).formatted(Formatting.GREEN));
		guiGraphics.drawTextWithShadow(textRenderer, sold30d, x + 20, y + 90, Colors.WHITE.asInt());

		if (month.hasPriceData()) {
			Text avgPrice30d = Text.literal("Avg price (30d): ").formatted(Formatting.YELLOW)
					.append(Text.literal(StonksUtils.INTEGER_NUMBERS.format(month.avgPrice())).formatted(Formatting.GOLD))
					.append(Text.literal(" (").formatted(Formatting.GRAY))
					.append(Text.literal(StonksUtils.SHORT_FLOAT_NUMBERS.format(month.avgPrice())).formatted(Formatting.GOLD))
					.append(Text.literal(")").formatted(Formatting.GRAY));
			guiGraphics.drawTextWithShadow(textRenderer, avgPrice30d, x + 20, y + 100, Colors.WHITE.asInt());
		} else {
			Text warningMouthPrice = Text.literal("No Data (30d)").formatted(Formatting.RED);
			guiGraphics.drawTextWithShadow(textRenderer, warningMouthPrice, x + 20, y + 100, Colors.WHITE.asInt());
		}

		Text activeDays = Text.literal("Active days: ").formatted(Formatting.DARK_PURPLE)
				.append(Text.literal(month.daysWithData() + "/30").formatted(Formatting.LIGHT_PURPLE));
		guiGraphics.drawTextWithShadow(textRenderer, activeDays, x + 20, y + 120, Colors.WHITE.asInt());

		// ---------- Comparaison semaine vs mois pour détecter une tendance ----------
		if (stats.last7Days().daysWithData() != 0 && stats.last30Days().daysWithData() != 0) {
			double weeklyAvg = stats.last7Days().avgDailyItemsSold();
			double monthlyAvg = stats.last30Days().avgDailyItemsSold();
			if (monthlyAvg == 0) return;

			double trendPercent = ((weeklyAvg - monthlyAvg) / monthlyAvg) * 100;
			Text trend = trendPercent > 10
					? Text.literal("↑ Trending up").formatted(Formatting.GREEN)
					: trendPercent < -10
					  ? Text.literal("↓ Trending down").formatted(Formatting.RED)
					  : Text.literal("→ Stable").formatted(Formatting.GRAY);

			Text textTrend = Text.literal("Sold Trend: ").formatted(Formatting.WHITE)
					.append(trend)
					.append(Text.literal(" (" + String.format("%+.1f%%", trendPercent) + ")").formatted(Formatting.GREEN));
			guiGraphics.drawTextWithShadow(textRenderer, textTrend, x + 20, y + 130, Colors.WHITE.asInt());

			Text textTrendInfo = Text.literal("(Week vs. mouth comparison)").formatted(Formatting.DARK_GRAY);
			guiGraphics.drawTextWithShadow(textRenderer, textTrendInfo, x + 20, y + 140, Colors.WHITE.asInt());
		} else {
			Text warningTrend = Text.literal("Trend Error: No Data").formatted(Formatting.RED);
			guiGraphics.drawTextWithShadow(textRenderer, warningTrend, x + 20, y + 130, Colors.WHITE.asInt());
		}

		// ---------- Tendance de prix entre 7j et 30j ----------
		if (week.hasPriceData() && month.hasPriceData() && month.avgPrice() > 0) {
			double priceTrendPercent = ((week.avgPrice() - month.avgPrice()) / month.avgPrice()) * 100;

			Text trend = priceTrendPercent > 5
					? Text.literal("↑ Rising").formatted(Formatting.GREEN)
					: priceTrendPercent < -5
					  ? Text.literal("↓ Falling").formatted(Formatting.RED)
					  : Text.literal("→ Stable").formatted(Formatting.GRAY);

			Text textTrend = Text.literal("Price Trend: ").formatted(Formatting.WHITE)
					.append(trend)
					.append(Text.literal(" (" + String.format("%+.1f%%", priceTrendPercent) + ")").formatted(Formatting.GREEN));
			guiGraphics.drawTextWithShadow(textRenderer, textTrend, x + 20, y + 155, Colors.WHITE.asInt());

			Text textPriceTrendInfo = Text.literal("(Price trend over 7d to 30 days)").formatted(Formatting.DARK_GRAY);
			guiGraphics.drawTextWithShadow(textRenderer, textPriceTrendInfo, x + 20, y + 165, Colors.WHITE.asInt());
		} else {
			Text warningPriceTrend = Text.literal("Price Trend Error: No Data").formatted(Formatting.RED);
			guiGraphics.drawTextWithShadow(textRenderer, warningPriceTrend, x + 20, y + 155, Colors.WHITE.asInt());
		}

		// Warning
		Text warningInfo = Text.literal("Statistics should be use with caution!").formatted(Formatting.DARK_RED, Formatting.UNDERLINE);
		guiGraphics.drawTextWithShadow(textRenderer, warningInfo, x + 20, y + 185, Colors.WHITE.asInt());
	}
}
