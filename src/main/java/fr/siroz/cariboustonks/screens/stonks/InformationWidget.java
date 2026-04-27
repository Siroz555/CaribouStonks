package fr.siroz.cariboustonks.screens.stonks;

import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.core.module.color.Colors;
import fr.siroz.cariboustonks.core.skyblock.data.generic.AuctionStatistics;
import fr.siroz.cariboustonks.core.skyblock.data.hypixel.bazaar.BazaarProduct;
import fr.siroz.cariboustonks.util.StonksUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

class InformationWidget extends AbstractStonksWidget {
	private final @Nullable BazaarProduct bazaarItem;
	private final @Nullable AuctionStatistics auctionStats;

	InformationWidget(@Nullable BazaarProduct bazaarItem, @Nullable AuctionStatistics auctionStats, int width, int height) {
		super(width, height);
		this.bazaarItem = bazaarItem;
		this.auctionStats = auctionStats;
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, int x, int y) {
		if (bazaarItem != null) {
			renderBazaar(bazaarItem, guiGraphics, x, y);
		} else if (auctionStats != null) {
			renderAuction(auctionStats, guiGraphics, x, y);
		}
	}

	private void renderBazaar(@NonNull BazaarProduct bazaarItem, @NonNull GuiGraphics guiGraphics, int x, int y) {
		// Buy

		double buyPrice = bazaarItem.buyPrice();
		Component textBuy = Component.literal("Buy Price: ").withStyle(ChatFormatting.YELLOW)
				.append(Component.literal(StonksUtils.INTEGER_NUMBERS.format(buyPrice)).withStyle(ChatFormatting.GOLD))
				.append(Component.literal(" (").withStyle(ChatFormatting.GRAY))
				.append(Component.literal(StonksUtils.SHORT_FLOAT_NUMBERS.format(buyPrice)).withStyle(ChatFormatting.GOLD))
				.append(Component.literal(")").withStyle(ChatFormatting.GRAY));
		guiGraphics.drawString(textRenderer, textBuy, x + 20, y + 20, Colors.WHITE.asInt());

		Component textAvgBuy = Component.literal("Avg. Price: ").withStyle(ChatFormatting.YELLOW)
				.append(Component.literal(StonksUtils.SHORT_FLOAT_NUMBERS.format(bazaarItem.weightedAverageBuyPrice())).withStyle(ChatFormatting.GOLD));
		guiGraphics.drawString(textRenderer, textAvgBuy, x + 20, y + 30, Colors.WHITE.asInt());

		// Buy - Infos

		Component textBuyOrderInfos = Component.literal(StonksUtils.SHORT_FLOAT_NUMBERS.format(bazaarItem.buyVolume())).withStyle(ChatFormatting.DARK_GRAY)
				.append(" in " + StonksUtils.SHORT_FLOAT_NUMBERS.format(bazaarItem.buyOrders()) + " orders");
		guiGraphics.drawString(textRenderer, textBuyOrderInfos, x + 20, y + 45, Colors.WHITE.asInt());

		Component textBuyMoving = Component.literal(StonksUtils.SHORT_FLOAT_NUMBERS.format(bazaarItem.buyMovingWeek())).withStyle(ChatFormatting.DARK_GRAY)
				.append(Component.literal(" insta-buys in 7d"));
		guiGraphics.drawString(textRenderer, textBuyMoving, x + 20, y + 55, Colors.WHITE.asInt());

		// Sell

		double sellPrice = bazaarItem.sellPrice();
		Component textSell = Component.literal("Sell Price: ").withStyle(ChatFormatting.YELLOW)
				.append(Component.literal(StonksUtils.INTEGER_NUMBERS.format(sellPrice)).withStyle(ChatFormatting.GOLD))
				.append(Component.literal(" (").withStyle(ChatFormatting.GRAY))
				.append(Component.literal(StonksUtils.SHORT_FLOAT_NUMBERS.format(sellPrice)).withStyle(ChatFormatting.GOLD))
				.append(Component.literal(")").withStyle(ChatFormatting.GRAY));

		guiGraphics.drawString(textRenderer, textSell, x + 20, y + 80, Colors.WHITE.asInt());

		Component textAvgSell = Component.literal("Avg. Price: ").withStyle(ChatFormatting.YELLOW)
				.append(Component.literal(StonksUtils.SHORT_FLOAT_NUMBERS.format(bazaarItem.weightedAverageSellPrice())).withStyle(ChatFormatting.GOLD));
		guiGraphics.drawString(textRenderer, textAvgSell, x + 20, y + 90, Colors.WHITE.asInt());
		// Sell - Infos

		Component textSellOrderInfos = Component.literal(StonksUtils.SHORT_FLOAT_NUMBERS.format(bazaarItem.sellVolume())).withStyle(ChatFormatting.DARK_GRAY)
				.append(" in " + StonksUtils.SHORT_FLOAT_NUMBERS.format(bazaarItem.sellOrders()) + " orders");
		guiGraphics.drawString(textRenderer, textSellOrderInfos, x + 20, y + 105, Colors.WHITE.asInt());

		Component textSellMoving = Component.literal(StonksUtils.SHORT_FLOAT_NUMBERS.format(bazaarItem.sellMovingWeek())).withStyle(ChatFormatting.DARK_GRAY)
				.append(Component.literal(" insta-sells in 7d"));
		guiGraphics.drawString(textRenderer, textSellMoving, x + 20, y + 115, Colors.WHITE.asInt());

		// ============= ANALYTICS =============

		// Spread & %

		Component textSpread = Component.literal("Spreed: ").withStyle(ChatFormatting.RED)
				.append(Component.literal(StonksUtils.FLOAT_NUMBERS.format(bazaarItem.spreadPercentage()) + "%").withColor(Colors.RED.asInt()))
				.append(Component.literal(" | ").withStyle(ChatFormatting.GRAY))
				.append(Component.literal(StonksUtils.INTEGER_NUMBERS.format(bazaarItem.spread())).withColor(Colors.RED.asInt()))
				.append(Component.literal(" (").withStyle(ChatFormatting.GRAY))
				.append(Component.literal(StonksUtils.SHORT_FLOAT_NUMBERS.format(bazaarItem.spread())).withColor(Colors.RED.asInt()))
				.append(Component.literal(")").withStyle(ChatFormatting.GRAY));
		guiGraphics.drawString(textRenderer, textSpread, x + 20, y + 145, Colors.WHITE.asInt());

		// Velocity - Buy/Sell

		Component textVelocity = Component.literal("Velocity: ").withStyle(ChatFormatting.DARK_AQUA);
		guiGraphics.drawString(textRenderer, textVelocity, x + 20, y + 160, Colors.WHITE.asInt());

		Component textVelocityValues = Component.empty()
				.append(Component.literal("Buy: ").withStyle(ChatFormatting.DARK_AQUA))
				.append(Component.literal(StonksUtils.FLOAT_NUMBERS.format(bazaarItem.buyVelocity())).withStyle(ChatFormatting.AQUA))
				.append(Component.literal(" | ").withStyle(ChatFormatting.GRAY))
				.append(Component.literal("Sell: ").withStyle(ChatFormatting.DARK_AQUA))
				.append(Component.literal(StonksUtils.FLOAT_NUMBERS.format(bazaarItem.sellVelocity())).withStyle(ChatFormatting.AQUA));
		guiGraphics.drawString(textRenderer, textVelocityValues, x + 20, y + 170, Colors.WHITE.asInt());

		if (ConfigManager.getConfig().general.stonks.showAllDataInInfoScreen) {

			// Standard Deviation - Buy/Sell

			Component textStandardDeviation = Component.literal("*Standard Deviation: ").withStyle(ChatFormatting.DARK_GREEN);
			guiGraphics.drawString(textRenderer, textStandardDeviation, x + 20, y + 195, Colors.WHITE.asInt());

			Component textStandardDeviationValues = Component.literal(
							"Buy: " + StonksUtils.SHORT_FLOAT_NUMBERS.format(bazaarItem.buyPriceStdDev()) +
									" | Sell: " + StonksUtils.SHORT_FLOAT_NUMBERS.format(bazaarItem.sellPriceStdDev()))
					.withStyle(ChatFormatting.GREEN);
			guiGraphics.drawString(textRenderer, textStandardDeviationValues, x + 20, y + 205, Colors.WHITE.asInt());

			// Warning
			Component warningInfo = Component.literal("* Not a true representation of all orders.").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC);
			guiGraphics.drawString(textRenderer, warningInfo, x + 20, y + 225, Colors.WHITE.asInt());
		}
	}

	private void renderAuction(@NonNull AuctionStatistics stats, @NonNull GuiGraphics guiGraphics, int x, int y) {
		// ---------- Aujourd'hui ----------
		AuctionStatistics.PeriodStats today = stats.today();
		if (today.daysWithData() > 0) {
			Component soldToday = Component.literal("Sold Today: ").withStyle(ChatFormatting.WHITE)
					.append(Component.literal("" + today.totalItemsSold()).withStyle(ChatFormatting.GREEN));
			guiGraphics.drawString(textRenderer, soldToday, x + 20, y + 20, Colors.WHITE.asInt());

			Component minPriceToday = Component.literal("Lowest BIN (Today): ").withStyle(ChatFormatting.YELLOW)
					.append(Component.literal(StonksUtils.INTEGER_NUMBERS.format(today.minPrice())).withStyle(ChatFormatting.GOLD))
					.append(Component.literal(" (").withStyle(ChatFormatting.GRAY))
					.append(Component.literal(StonksUtils.SHORT_FLOAT_NUMBERS.format(today.minPrice())).withStyle(ChatFormatting.GOLD))
					.append(Component.literal(")").withStyle(ChatFormatting.GRAY));
			guiGraphics.drawString(textRenderer, minPriceToday, x + 20, y + 30, Colors.WHITE.asInt());

			Component avgPriceToday = Component.literal("Avg price (Today): ").withStyle(ChatFormatting.YELLOW)
					.append(Component.literal(StonksUtils.INTEGER_NUMBERS.format(today.avgPrice())).withStyle(ChatFormatting.GOLD))
					.append(Component.literal(" (").withStyle(ChatFormatting.GRAY))
					.append(Component.literal(StonksUtils.SHORT_FLOAT_NUMBERS.format(today.avgPrice())).withStyle(ChatFormatting.GOLD))
					.append(Component.literal(")").withStyle(ChatFormatting.GRAY));
			guiGraphics.drawString(textRenderer, avgPriceToday, x + 20, y + 40, Colors.WHITE.asInt());
		} else {
			Component soldTodayNoData = Component.literal("Sold Today: ").withStyle(ChatFormatting.WHITE)
					.append(Component.literal("No Data").withStyle(ChatFormatting.RED));
			guiGraphics.drawString(textRenderer, soldTodayNoData, x + 20, y + 20, Colors.WHITE.asInt());
		}

		// ---------- 7 derniers jours ----------
		AuctionStatistics.PeriodStats week = stats.last7Days();
		Component sold7d = Component.literal("Sold (7d): ").withStyle(ChatFormatting.WHITE)
				.append(Component.literal("" + week.totalItemsSold()).withStyle(ChatFormatting.GREEN));
		guiGraphics.drawString(textRenderer, sold7d, x + 20, y + 60, Colors.WHITE.asInt());

		if (week.hasPriceData()) {
			Component avgPrice7d = Component.literal("Avg price (7d): ").withStyle(ChatFormatting.YELLOW)
					.append(Component.literal(StonksUtils.INTEGER_NUMBERS.format(week.avgPrice())).withStyle(ChatFormatting.GOLD))
					.append(Component.literal(" (").withStyle(ChatFormatting.GRAY))
					.append(Component.literal(StonksUtils.SHORT_FLOAT_NUMBERS.format(week.avgPrice())).withStyle(ChatFormatting.GOLD))
					.append(Component.literal(")").withStyle(ChatFormatting.GRAY));
			guiGraphics.drawString(textRenderer, avgPrice7d, x + 20, y + 70, Colors.WHITE.asInt());
		} else {
			Component warningWeekPrice = Component.literal("No Data (7d)").withStyle(ChatFormatting.RED);
			guiGraphics.drawString(textRenderer, warningWeekPrice, x + 20, y + 70, Colors.WHITE.asInt());
		}

		// ---------- 30 derniers jours ----------
		AuctionStatistics.PeriodStats month = stats.last30Days();
		Component sold30d = Component.literal("Sold (30d): ").withStyle(ChatFormatting.WHITE)
				.append(Component.literal("" + month.totalItemsSold()).withStyle(ChatFormatting.GREEN));
		guiGraphics.drawString(textRenderer, sold30d, x + 20, y + 90, Colors.WHITE.asInt());

		if (month.hasPriceData()) {
			Component avgPrice30d = Component.literal("Avg price (30d): ").withStyle(ChatFormatting.YELLOW)
					.append(Component.literal(StonksUtils.INTEGER_NUMBERS.format(month.avgPrice())).withStyle(ChatFormatting.GOLD))
					.append(Component.literal(" (").withStyle(ChatFormatting.GRAY))
					.append(Component.literal(StonksUtils.SHORT_FLOAT_NUMBERS.format(month.avgPrice())).withStyle(ChatFormatting.GOLD))
					.append(Component.literal(")").withStyle(ChatFormatting.GRAY));
			guiGraphics.drawString(textRenderer, avgPrice30d, x + 20, y + 100, Colors.WHITE.asInt());
		} else {
			Component warningMouthPrice = Component.literal("No Data (30d)").withStyle(ChatFormatting.RED);
			guiGraphics.drawString(textRenderer, warningMouthPrice, x + 20, y + 100, Colors.WHITE.asInt());
		}

		Component activeDays = Component.literal("Active days: ").withStyle(ChatFormatting.DARK_PURPLE)
				.append(Component.literal(month.daysWithData() + "/30").withStyle(ChatFormatting.LIGHT_PURPLE));
		guiGraphics.drawString(textRenderer, activeDays, x + 20, y + 120, Colors.WHITE.asInt());

		// ---------- Comparaison semaine vs mois pour détecter une tendance ----------
		if (stats.last7Days().daysWithData() != 0 && stats.last30Days().daysWithData() != 0) {
			double weeklyAvg = stats.last7Days().avgDailyItemsSold();
			double monthlyAvg = stats.last30Days().avgDailyItemsSold();
			if (monthlyAvg == 0) return;

			double trendPercent = ((weeklyAvg - monthlyAvg) / monthlyAvg) * 100;
			Component trend = trendPercent > 10
					? Component.literal("↑ Trending up").withStyle(ChatFormatting.GREEN)
					: trendPercent < -10
					  ? Component.literal("↓ Trending down").withStyle(ChatFormatting.RED)
					  : Component.literal("→ Stable").withStyle(ChatFormatting.GRAY);

			Component textTrend = Component.literal("Sold Trend: ").withStyle(ChatFormatting.WHITE)
					.append(trend)
					.append(Component.literal(" (" + String.format("%+.1f%%", trendPercent) + ")").withStyle(ChatFormatting.GREEN));
			guiGraphics.drawString(textRenderer, textTrend, x + 20, y + 130, Colors.WHITE.asInt());

			Component textTrendInfo = Component.literal("(Week vs. mouth comparison)").withStyle(ChatFormatting.DARK_GRAY);
			guiGraphics.drawString(textRenderer, textTrendInfo, x + 20, y + 140, Colors.WHITE.asInt());
		} else {
			Component warningTrend = Component.literal("Trend Error: No Data").withStyle(ChatFormatting.RED);
			guiGraphics.drawString(textRenderer, warningTrend, x + 20, y + 130, Colors.WHITE.asInt());
		}

		// ---------- Tendance de prix entre 7j et 30j ----------
		if (week.hasPriceData() && month.hasPriceData() && month.avgPrice() > 0) {
			double priceTrendPercent = ((week.avgPrice() - month.avgPrice()) / month.avgPrice()) * 100;

			Component trend = priceTrendPercent > 5
					? Component.literal("↑ Rising").withStyle(ChatFormatting.GREEN)
					: priceTrendPercent < -5
					  ? Component.literal("↓ Falling").withStyle(ChatFormatting.RED)
					  : Component.literal("→ Stable").withStyle(ChatFormatting.GRAY);

			Component textTrend = Component.literal("Price Trend: ").withStyle(ChatFormatting.WHITE)
					.append(trend)
					.append(Component.literal(" (" + String.format("%+.1f%%", priceTrendPercent) + ")").withStyle(ChatFormatting.GREEN));
			guiGraphics.drawString(textRenderer, textTrend, x + 20, y + 155, Colors.WHITE.asInt());

			Component textPriceTrendInfo = Component.literal("(Price trend over 7d to 30 days)").withStyle(ChatFormatting.DARK_GRAY);
			guiGraphics.drawString(textRenderer, textPriceTrendInfo, x + 20, y + 165, Colors.WHITE.asInt());
		} else {
			Component warningPriceTrend = Component.literal("Price Trend Error: No Data").withStyle(ChatFormatting.RED);
			guiGraphics.drawString(textRenderer, warningPriceTrend, x + 20, y + 155, Colors.WHITE.asInt());
		}

		// Warning
		Component warningInfo = Component.literal("Statistics should be use with caution!").withStyle(ChatFormatting.DARK_RED, ChatFormatting.UNDERLINE);
		guiGraphics.drawString(textRenderer, warningInfo, x + 20, y + 185, Colors.WHITE.asInt());
	}
}
