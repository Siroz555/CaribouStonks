package fr.siroz.cariboustonks.screen.stonks;

import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.core.data.hypixel.bazaar.BazaarProduct;
import fr.siroz.cariboustonks.util.StonksUtils;
import fr.siroz.cariboustonks.util.colors.Colors;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import org.jetbrains.annotations.Nullable;

class InformationWidget extends AbstractStonksWidget {

	@Nullable
	private final BazaarProduct bazaarItem;

	public InformationWidget(
			@Nullable BazaarProduct bazaarItem,
			int width,
			int height
	) {
		super(width, height);
		this.bazaarItem = bazaarItem;
	}

	@Override
	public void render(GuiGraphics context, int mouseX, int mouseY, int x, int y) {
		if (bazaarItem != null) {

			// Buy

			double buyPrice = bazaarItem.buyPrice();
			Component textBuy = Component.literal("Buy Price: ").withStyle(ChatFormatting.YELLOW)
					.append(Component.literal(StonksUtils.INTEGER_NUMBERS.format(buyPrice)).withStyle(ChatFormatting.GOLD))
					.append(Component.literal(" (").withStyle(ChatFormatting.GRAY))
					.append(Component.literal(StonksUtils.SHORT_FLOAT_NUMBERS.format(buyPrice)).withStyle(ChatFormatting.GOLD))
					.append(Component.literal(")").withStyle(ChatFormatting.GRAY));

			context.drawString(textRenderer, textBuy, x + 20, y + 20, Colors.WHITE.asInt());

			Component textAvgBuy = Component.literal("Avg. Price: ").withStyle(ChatFormatting.YELLOW)
					.append(Component.literal(StonksUtils.SHORT_FLOAT_NUMBERS.format(bazaarItem.weightedAverageBuyPrice())).withStyle(ChatFormatting.GOLD));

			context.drawString(textRenderer, textAvgBuy, x + 20, y + 30, Colors.WHITE.asInt());

			// Buy - Infos

			Component textBuyOrderInfos = Component.literal(StonksUtils.SHORT_FLOAT_NUMBERS.format(bazaarItem.buyVolume())).withStyle(ChatFormatting.DARK_GRAY)
					.append(" in " + StonksUtils.SHORT_FLOAT_NUMBERS.format(bazaarItem.buyOrders()) + " orders");

			context.drawString(textRenderer, textBuyOrderInfos, x + 20, y + 45, Colors.WHITE.asInt());

			Component textBuyMoving = Component.literal(StonksUtils.SHORT_FLOAT_NUMBERS.format(bazaarItem.buyMovingWeek())).withStyle(ChatFormatting.DARK_GRAY)
					.append(Component.literal(" insta-buys in 7d"));

			context.drawString(textRenderer, textBuyMoving, x + 20, y + 55, Colors.WHITE.asInt());

			// Sell

			double sellPrice = bazaarItem.sellPrice();
			Component textSell = Component.literal("Sell Price: ").withStyle(ChatFormatting.YELLOW)
					.append(Component.literal(StonksUtils.INTEGER_NUMBERS.format(sellPrice)).withStyle(ChatFormatting.GOLD))
					.append(Component.literal(" (").withStyle(ChatFormatting.GRAY))
					.append(Component.literal(StonksUtils.SHORT_FLOAT_NUMBERS.format(sellPrice)).withStyle(ChatFormatting.GOLD))
					.append(Component.literal(")").withStyle(ChatFormatting.GRAY));

			context.drawString(textRenderer, textSell, x + 20, y + 80, Colors.WHITE.asInt());

			Component textAvgSell = Component.literal("Avg. Price: ").withStyle(ChatFormatting.YELLOW)
					.append(Component.literal(StonksUtils.SHORT_FLOAT_NUMBERS.format(bazaarItem.weightedAverageSellPrice())).withStyle(ChatFormatting.GOLD));

			context.drawString(textRenderer, textAvgSell, x + 20, y + 90, Colors.WHITE.asInt());

			// Sell - Infos

			Component textSellOrderInfos = Component.literal(StonksUtils.SHORT_FLOAT_NUMBERS.format(bazaarItem.sellVolume())).withStyle(ChatFormatting.DARK_GRAY)
					.append(" in " + StonksUtils.SHORT_FLOAT_NUMBERS.format(bazaarItem.sellOrders()) + " orders");

			context.drawString(textRenderer, textSellOrderInfos, x + 20, y + 105, Colors.WHITE.asInt());

			Component textSellMoving = Component.literal(StonksUtils.SHORT_FLOAT_NUMBERS.format(bazaarItem.sellMovingWeek())).withStyle(ChatFormatting.DARK_GRAY)
					.append(Component.literal(" insta-sells in 7d"));

			context.drawString(textRenderer, textSellMoving, x + 20, y + 115, Colors.WHITE.asInt());

			// ============= ANALYTICS =============

			// Spread & %

			Component textSpread = Component.literal("Spreed: ").withStyle(ChatFormatting.RED)
					.append(Component.literal(StonksUtils.FLOAT_NUMBERS.format(bazaarItem.spreadPercentage()) + "%").withColor(Colors.RED.asInt()))
					.append(Component.literal(" | ").withStyle(ChatFormatting.GRAY))
					.append(Component.literal(StonksUtils.INTEGER_NUMBERS.format(bazaarItem.spread())).withColor(Colors.RED.asInt()))
					.append(Component.literal(" (").withStyle(ChatFormatting.GRAY))
					.append(Component.literal(StonksUtils.SHORT_FLOAT_NUMBERS.format(bazaarItem.spread())).withColor(Colors.RED.asInt()))
					.append(Component.literal(")").withStyle(ChatFormatting.GRAY));

			context.drawString(textRenderer, textSpread, x + 20, y + 145, Colors.WHITE.asInt());

			// Velocity - Buy/Sell

			Component textVelocity = Component.literal("Velocity: ").withStyle(ChatFormatting.DARK_AQUA);

			context.drawString(textRenderer, textVelocity, x + 20, y + 160, Colors.WHITE.asInt());

			Component textVelocityValues = Component.empty()
					.append(Component.literal("Buy: ").withStyle(ChatFormatting.DARK_AQUA))
					.append(Component.literal(StonksUtils.FLOAT_NUMBERS.format(bazaarItem.buyVelocity())).withStyle(ChatFormatting.AQUA))
					.append(Component.literal(" | ").withStyle(ChatFormatting.GRAY))
					.append(Component.literal("Sell: ").withStyle(ChatFormatting.DARK_AQUA))
					.append(Component.literal(StonksUtils.FLOAT_NUMBERS.format(bazaarItem.sellVelocity())).withStyle(ChatFormatting.AQUA));

			context.drawString(textRenderer, textVelocityValues, x + 20, y + 170, Colors.WHITE.asInt());

			if (ConfigManager.getConfig().general.stonks.showAllDataInInfoScreen) {

				// Standard Deviation - Buy/Sell

				Component textStandardDeviation = Component.literal("*Standard Deviation: ").withStyle(ChatFormatting.DARK_GREEN);
				context.drawString(textRenderer, textStandardDeviation, x + 20, y + 195, Colors.WHITE.asInt());

				Component textStandardDeviationValues = Component.literal(
								"Buy: " + StonksUtils.SHORT_FLOAT_NUMBERS.format(bazaarItem.buyPriceStdDev()) +
										" | Sell: " + StonksUtils.SHORT_FLOAT_NUMBERS.format(bazaarItem.sellPriceStdDev()))
						.withStyle(ChatFormatting.GREEN);

				context.drawString(textRenderer, textStandardDeviationValues, x + 20, y + 205, Colors.WHITE.asInt());

				// Warning
				Component warningInfo = Component.literal("* Not a true representation of all orders.").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC);
				context.drawString(textRenderer, warningInfo, x + 20, y + 225, Colors.WHITE.asInt());
			}
		}
	}
}
