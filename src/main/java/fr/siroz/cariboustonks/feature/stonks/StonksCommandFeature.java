package fr.siroz.cariboustonks.feature.stonks;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.core.data.algo.BazaarItemAnalytics;
import fr.siroz.cariboustonks.core.data.hypixel.HypixelDataSource;
import fr.siroz.cariboustonks.core.data.hypixel.SkyBlockItem;
import fr.siroz.cariboustonks.core.data.hypixel.bazaar.Product;
import fr.siroz.cariboustonks.feature.Feature;
import fr.siroz.cariboustonks.manager.command.CommandRegistration;
import fr.siroz.cariboustonks.util.Client;
import fr.siroz.cariboustonks.util.StonksUtils;
import fr.siroz.cariboustonks.util.Symbols;
import fr.siroz.cariboustonks.util.colors.Colors;
import java.util.Optional;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

public class StonksCommandFeature extends Feature implements CommandRegistration {

	// TODO :: Implémentation pour les Auctions Items

	private static final String SEPARATOR = "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬";

	private final HypixelDataSource hypixelDataSource;

	public StonksCommandFeature() {
		this.hypixelDataSource = CaribouStonks.core().getHypixelDataSource();
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public void register(@NotNull CommandDispatcher<FabricClientCommandSource> dispatcher, @NotNull CommandRegistryAccess _ra) {
		dispatcher.register(ClientCommandManager.literal("stonks")
				.then(ClientCommandManager.argument("item", StringArgumentType.greedyString())
						.suggests((context, builder) -> CommandSource.suggestMatching(hypixelDataSource.getSkyBlockItemsIds(), builder))
						.executes(context -> handle(context.getSource(), StringArgumentType.getString(context, "item")))));
	}

	private int handle(FabricClientCommandSource source, String item) {
		int result = Command.SINGLE_SUCCESS;

		if (hypixelDataSource.isBazaarInUpdate()) {
			source.sendFeedback(Text.literal("Bazaar is currently updating.. Retry in few seconds.").formatted(Formatting.RED));
			return result;
		}

		if (!hypixelDataSource.hasBazaarItem(item)) {
			source.sendFeedback(Text.literal("Unable to find '" + item + "' item in the Bazaar.").formatted(Formatting.RED));
			return result;
		}

		Optional<Product> productOptional = hypixelDataSource.getBazaarItem(item);
		if (productOptional.isPresent()) {
			Product product = productOptional.get();

			Client.playSound(SoundEvents.BLOCK_TRIAL_SPAWNER_ABOUT_TO_SPAWN_ITEM, 1f, 1f);

			source.sendFeedback(Text.literal(SEPARATOR).formatted(Formatting.RED));

			SkyBlockItem skyBlockItem = hypixelDataSource.getSkyBlockItem(item);
			if (skyBlockItem == null) {
				source.sendFeedback(Text.empty().append(Symbols.getStyled(Symbols.STAR, Colors.ORANGE.asInt()))
						.append(" " + Text.literal(product.productId() + " :").formatted(Formatting.GOLD)));
			} else {
				source.sendFeedback(Text.empty().append(Symbols.getStyled(Symbols.STAR, Colors.ORANGE.asInt()))
						.append(Text.literal(" " + skyBlockItem.getName()).withColor(skyBlockItem.getTier().getColor()))
						.append(Text.literal(" (" + product.productId() + ")").formatted(Formatting.DARK_GRAY)));
			}

			source.sendFeedback(Text.empty());

			double buyPrice = BazaarItemAnalytics.buyPrice(product);
			double buyAvgPrice = BazaarItemAnalytics.weightedAverageBuyPrice(product);
			double vwap = BazaarItemAnalytics.vwap(product);
			double standardDeviationBuy = BazaarItemAnalytics.standardDeviation(product.buySummary());
			source.sendFeedback(Text.literal("Buy: ").formatted(Formatting.YELLOW)
					.append(Text.literal(StonksUtils.INTEGER_NUMBERS.format(buyPrice) + " Coins").formatted(Formatting.GOLD))
					.append(Text.literal(" (").formatted(Formatting.GRAY))
					.append(Text.literal(StonksUtils.SHORT_FLOAT_NUMBERS.format(buyPrice)).formatted(Formatting.GOLD))
					.append(Text.literal(")").formatted(Formatting.GRAY))
					.append(Text.literal(" (Hover)").formatted(Formatting.AQUA))
					.styled(style -> style.withHoverEvent(new HoverEvent.ShowText(
							Text.literal("Buy-Avg: ").formatted(Formatting.YELLOW)
									.append(Text.literal(StonksUtils.INTEGER_NUMBERS.format(buyAvgPrice) + " Coins").formatted(Formatting.GOLD))
									.append(Text.literal(" (").formatted(Formatting.GRAY))
									.append(Text.literal(StonksUtils.SHORT_FLOAT_NUMBERS.format(buyAvgPrice)).formatted(Formatting.GOLD))
									.append(Text.literal(")").formatted(Formatting.GRAY))
									.append(Text.literal("\n\n"))
									.append(Text.literal("*VWAP: ").formatted(Formatting.LIGHT_PURPLE))
									.append(Text.literal(StonksUtils.SHORT_FLOAT_NUMBERS.format(vwap)).formatted(Formatting.DARK_PURPLE))
									.append(Text.literal("\n"))
									.append(Text.literal("(An average that takes into account the volumes of each order)").formatted(Formatting.GRAY, Formatting.ITALIC))
									.append(Text.literal("\n\n"))
									.append(Text.literal("*Standard Deviation: ").formatted(Formatting.DARK_RED))
									.append(Text.literal(StonksUtils.SHORT_FLOAT_NUMBERS.format(standardDeviationBuy)).formatted(Formatting.RED))
									.append(Text.literal("\n"))
									.append(Text.literal("(This measures price volatility. Higher values indicate stronger fluctuations)").formatted(Formatting.GRAY, Formatting.ITALIC))
									.append(Text.literal("\n\n"))
									.append(Text.literal("* Not a true representation of all orders").formatted(Formatting.GRAY, Formatting.ITALIC))
					))));

			long buyVolume = product.quickStatus().buyVolume();
			long buyOrders = product.quickStatus().buyOrders();
			long buyMovingWeek = product.quickStatus().buyMovingWeek();
			source.sendFeedback(Text.literal(StonksUtils.SHORT_FLOAT_NUMBERS.format(buyVolume)).formatted(Formatting.DARK_GRAY)
					.append(" in " + StonksUtils.SHORT_FLOAT_NUMBERS.format(buyOrders) + " orders")
					.append(Text.literal(" | ").formatted(Formatting.GRAY))
					.append(StonksUtils.SHORT_FLOAT_NUMBERS.format(buyMovingWeek)).formatted(Formatting.DARK_GRAY)
					.append(Text.literal(" insta-buys in 7d").formatted(Formatting.DARK_GRAY))
			);

			source.sendFeedback(Text.empty());

			double sellPrice = BazaarItemAnalytics.sellPrice(product);
			double sellAvgPrice = BazaarItemAnalytics.weightedAverageSellPrice(product);
			double standardDeviationSell = BazaarItemAnalytics.standardDeviation(product.sellSummary());
			source.sendFeedback(Text.literal("Sell: ").formatted(Formatting.YELLOW)
					.append(Text.literal(StonksUtils.INTEGER_NUMBERS.format(sellPrice) + " Coins").formatted(Formatting.GOLD))
					.append(Text.literal(" (").formatted(Formatting.GRAY))
					.append(Text.literal(StonksUtils.SHORT_FLOAT_NUMBERS.format(sellPrice)).formatted(Formatting.GOLD))
					.append(Text.literal(")").formatted(Formatting.GRAY))
					.append(Text.literal(" (Hover)").formatted(Formatting.AQUA))
					.styled(style -> style.withHoverEvent(new HoverEvent.ShowText(
							Text.literal("Sell-Avg: ").formatted(Formatting.YELLOW)
									.append(Text.literal(StonksUtils.INTEGER_NUMBERS.format(sellAvgPrice) + " Coins").formatted(Formatting.GOLD))
									.append(Text.literal(" (").formatted(Formatting.GRAY))
									.append(Text.literal(StonksUtils.SHORT_FLOAT_NUMBERS.format(sellAvgPrice)).formatted(Formatting.GOLD))
									.append(Text.literal(")").formatted(Formatting.GRAY))
									.append(Text.literal("\n\n"))
									.append(Text.literal("*Standard Deviation: ").formatted(Formatting.DARK_RED))
									.append(Text.literal(StonksUtils.SHORT_FLOAT_NUMBERS.format(standardDeviationSell)).formatted(Formatting.RED))
									.append(Text.literal("\n"))
									.append(Text.literal("(This measures price volatility. Higher values indicate stronger fluctuations)").formatted(Formatting.GRAY, Formatting.ITALIC))
									.append(Text.literal("\n\n"))
									.append(Text.literal("* Not a true representation of all orders").formatted(Formatting.GRAY, Formatting.ITALIC))
					)))
			);

			long sellVolume = product.quickStatus().sellVolume();
			long sellOrders = product.quickStatus().sellOrders();
			long sellMovingWeek = product.quickStatus().sellMovingWeek();
			source.sendFeedback(Text.literal(StonksUtils.SHORT_FLOAT_NUMBERS.format(sellVolume)).formatted(Formatting.DARK_GRAY)
					.append(" in " + StonksUtils.SHORT_FLOAT_NUMBERS.format(sellOrders) + " orders")
					.append(Text.literal(" | ").formatted(Formatting.GRAY))
					.append(StonksUtils.SHORT_FLOAT_NUMBERS.format(sellMovingWeek)).formatted(Formatting.DARK_GRAY)
					.append(Text.literal(" insta-sells in 7d").formatted(Formatting.DARK_GRAY))
			);

			if (ConfigManager.getConfig().general.stonks.showAllDataInStonksCommand) {
				source.sendFeedback(Text.empty());

				double spreadPercentage = BazaarItemAnalytics.spreadPercentage(product);
				double orderImbalancePercentage = BazaarItemAnalytics.orderImbalancePercentage(product);
				source.sendFeedback(Text.literal("Spreed: ").formatted(Formatting.RED)
						.append(Text.literal(StonksUtils.FLOAT_NUMBERS.format(spreadPercentage) + " %").withColor(Colors.RED.asInt()))
						.append(Text.literal(" | ").formatted(Formatting.GRAY))
						.append(Text.literal("Imbalance: ").formatted(Formatting.DARK_PURPLE))
						.append(Text.literal(StonksUtils.FLOAT_NUMBERS.format(orderImbalancePercentage) + " %").withColor(Colors.PURPLE.asInt()))
				);
			}

			source.sendFeedback(Text.literal(SEPARATOR).formatted(Formatting.RED));
		}

		return result;
	}
}
