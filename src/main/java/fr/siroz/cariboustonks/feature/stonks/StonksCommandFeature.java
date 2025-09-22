package fr.siroz.cariboustonks.feature.stonks;

import com.mojang.brigadier.arguments.StringArgumentType;
import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.core.data.hypixel.HypixelDataSource;
import fr.siroz.cariboustonks.core.data.hypixel.bazaar.BazaarProduct;
import fr.siroz.cariboustonks.core.data.hypixel.item.SkyBlockItem;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.feature.Feature;
import fr.siroz.cariboustonks.manager.command.CommandComponent;
import fr.siroz.cariboustonks.util.Client;
import fr.siroz.cariboustonks.util.ItemLookupKey;
import fr.siroz.cariboustonks.util.NotEnoughUpdatesUtils;
import fr.siroz.cariboustonks.util.StonksUtils;
import fr.siroz.cariboustonks.util.colors.Colors;
import java.util.Optional;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents;
import net.minecraft.command.CommandSource;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

public class StonksCommandFeature extends Feature {

	// SIROZ-NOTE :: Implémentation pour les Auctions Items

	private static final String SEPARATOR = "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬";

	private final HypixelDataSource hypixelDataSource;

	private String lastItem = "";

	public StonksCommandFeature() {
		this.hypixelDataSource = CaribouStonks.core().getHypixelDataSource();
		ClientWorldEvents.AFTER_CLIENT_WORLD_CHANGE.register((_mc, _world) -> lastItem = "");

		addComponent(CommandComponent.class, d -> d.register(ClientCommandManager.literal("stonks")
				.executes(context -> {
					if (lastItem != null && !lastItem.isBlank()) {
						context.getSource().getClient().setScreen(new StonksScreen(ItemLookupKey.of(
								NotEnoughUpdatesUtils.getNeuIdFromSkyBlockId(lastItem),
								lastItem
						)));
					}
					return 1;
				})
				.then(ClientCommandManager.argument("item", StringArgumentType.greedyString())
						.suggests((context, builder) -> CommandSource.suggestMatching(hypixelDataSource.getSkyBlockItemsIds(), builder))
						.executes(context -> handle(context.getSource(), StringArgumentType.getString(context, "item"))))
		));
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock();
	}

	private int handle(FabricClientCommandSource source, String item) {
		int result = 1;

		if (hypixelDataSource.isBazaarInUpdate()) {
			source.sendFeedback(Text.literal("Bazaar is currently updating.. Retry in few seconds.").formatted(Formatting.RED));
			return result;
		}

		if (!hypixelDataSource.hasBazaarItem(item)) {
			source.sendFeedback(Text.literal("Unable to find '" + item + "' item in the Bazaar.").formatted(Formatting.RED));
			return result;
		}

		Optional<BazaarProduct> productOptional = hypixelDataSource.getBazaarItem(item);
		productOptional.ifPresent(product -> {
			lastItem = item;
			showBazaarInfo(source, item, product);
		});

		return result;
	}

	private void showBazaarInfo(@NotNull FabricClientCommandSource source, @NotNull String item, @NotNull BazaarProduct bazaarProduct) {
		Client.playSound(SoundEvents.BLOCK_TRIAL_SPAWNER_ABOUT_TO_SPAWN_ITEM, 1f, 1f);

		source.sendFeedback(Text.literal(SEPARATOR).formatted(Formatting.RED));

		SkyBlockItem skyBlockItem = hypixelDataSource.getSkyBlockItem(item);
		if (skyBlockItem == null) {
			source.sendFeedback(Text.empty().append(Text.literal("⭐").withColor(Colors.ORANGE.asInt()))
					.append(" " + Text.literal(bazaarProduct.skyBlockId() + " :").formatted(Formatting.GOLD)));
		} else {
			source.sendFeedback(Text.empty().append(Text.literal("⭐").withColor(Colors.ORANGE.asInt()))
					.append(Text.literal(" " + skyBlockItem.name()).withColor(skyBlockItem.tier().getColor()))
					.append(Text.literal(" (" + bazaarProduct.skyBlockId() + ")").formatted(Formatting.DARK_GRAY)));
		}

		source.sendFeedback(Text.empty());

		double buyPrice = bazaarProduct.buyPrice();
		double buyAvgPrice = bazaarProduct.weightedAverageBuyPrice();
		double buyVelocity = bazaarProduct.buyVelocity();
		double standardDeviationBuy = bazaarProduct.buyPriceStdDev();
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
								.append(Text.literal("Velocity: ").formatted(Formatting.DARK_AQUA))
								.append(Text.literal(StonksUtils.FLOAT_NUMBERS.format(buyVelocity)).formatted(Formatting.AQUA))
								.append(Text.literal("\n"))
								.append(Text.literal("(Compares current volume to the daily average from the past week)").formatted(Formatting.GRAY, Formatting.ITALIC))
								.append(Text.literal("\n"))
								.append(Text.literal("- < 0.5    = Very low activity").formatted(Formatting.GRAY))
								.append(Text.literal("\n"))
								.append(Text.literal("- 0.5-1.5 = Normal activity").formatted(Formatting.GRAY))
								.append(Text.literal("\n"))
								.append(Text.literal("- 1.5-3   = High activity (growing interest)").formatted(Formatting.GRAY))
								.append(Text.literal("\n"))
								.append(Text.literal("- > 3      = Peak activity (event, speculation)").formatted(Formatting.GRAY))
								.append(Text.literal("\n\n"))
								.append(Text.literal("*Standard Deviation: ").formatted(Formatting.DARK_GREEN))
								.append(Text.literal(StonksUtils.SHORT_FLOAT_NUMBERS.format(standardDeviationBuy)).formatted(Formatting.GREEN))
								.append(Text.literal("\n"))
								.append(Text.literal("(This measures price volatility. Higher values indicate stronger fluctuations)").formatted(Formatting.GRAY, Formatting.ITALIC))
								.append(Text.literal("\n\n"))
								.append(Text.literal("* Not a true representation of all orders").formatted(Formatting.GRAY, Formatting.ITALIC))
				))));

		long buyVolume = bazaarProduct.buyVolume();
		long buyOrders = bazaarProduct.buyOrders();
		long buyMovingWeek = bazaarProduct.buyMovingWeek();
		source.sendFeedback(Text.literal(StonksUtils.SHORT_FLOAT_NUMBERS.format(buyVolume)).formatted(Formatting.DARK_GRAY)
				.append(" in " + StonksUtils.SHORT_FLOAT_NUMBERS.format(buyOrders) + " orders")
				.append(Text.literal(" | ").formatted(Formatting.GRAY))
				.append(StonksUtils.SHORT_FLOAT_NUMBERS.format(buyMovingWeek)).formatted(Formatting.DARK_GRAY)
				.append(Text.literal(" insta-buys in 7d").formatted(Formatting.DARK_GRAY))
		);

		source.sendFeedback(Text.empty());

		double sellPrice = bazaarProduct.sellPrice();
		double sellAvgPrice = bazaarProduct.weightedAverageSellPrice();
		double sellVelocity = bazaarProduct.sellVelocity();
		double standardDeviationSell = bazaarProduct.sellPriceStdDev();
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
								.append(Text.literal("Velocity: ").formatted(Formatting.DARK_AQUA))
								.append(Text.literal(StonksUtils.FLOAT_NUMBERS.format(sellVelocity)).formatted(Formatting.AQUA))
								.append(Text.literal("\n"))
								.append(Text.literal("(Compares current volume to the daily average from the past week)").formatted(Formatting.GRAY, Formatting.ITALIC))
								.append(Text.literal("\n"))
								.append(Text.literal("- < 0.5    = Very low activity").formatted(Formatting.GRAY))
								.append(Text.literal("\n"))
								.append(Text.literal("- 0.5-1.5 = Normal activity").formatted(Formatting.GRAY))
								.append(Text.literal("\n"))
								.append(Text.literal("- 1.5-3   = High activity (growing interest)").formatted(Formatting.GRAY))
								.append(Text.literal("\n"))
								.append(Text.literal("- > 3      = Peak activity (event, speculation)").formatted(Formatting.GRAY))
								.append(Text.literal("\n\n"))
								.append(Text.literal("*Standard Deviation: ").formatted(Formatting.DARK_GREEN))
								.append(Text.literal(StonksUtils.SHORT_FLOAT_NUMBERS.format(standardDeviationSell)).formatted(Formatting.GREEN))
								.append(Text.literal("\n"))
								.append(Text.literal("(This measures price volatility. Higher values indicate stronger fluctuations)").formatted(Formatting.GRAY, Formatting.ITALIC))
								.append(Text.literal("\n\n"))
								.append(Text.literal("* Not a true representation of all orders").formatted(Formatting.GRAY, Formatting.ITALIC))
				)))
		);

		long sellVolume = bazaarProduct.sellVolume();
		long sellOrders = bazaarProduct.sellOrders();
		long sellMovingWeek = bazaarProduct.sellMovingWeek();
		source.sendFeedback(Text.literal(StonksUtils.SHORT_FLOAT_NUMBERS.format(sellVolume)).formatted(Formatting.DARK_GRAY)
				.append(" in " + StonksUtils.SHORT_FLOAT_NUMBERS.format(sellOrders) + " orders")
				.append(Text.literal(" | ").formatted(Formatting.GRAY))
				.append(StonksUtils.SHORT_FLOAT_NUMBERS.format(sellMovingWeek)).formatted(Formatting.DARK_GRAY)
				.append(Text.literal(" insta-sells in 7d").formatted(Formatting.DARK_GRAY))
		);

		source.sendFeedback(Text.empty());

		double spread = bazaarProduct.spread();
		double spreadPercentage = bazaarProduct.spreadPercentage();
		source.sendFeedback(Text.literal("Spreed: ").formatted(Formatting.RED)
				.append(Text.literal(StonksUtils.FLOAT_NUMBERS.format(spreadPercentage) + "%").withColor(Colors.RED.asInt()))
				.append(Text.literal(" | ").formatted(Formatting.GRAY))
				.append(Text.literal(StonksUtils.INTEGER_NUMBERS.format(spread)).withColor(Colors.RED.asInt()))
				.append(Text.literal(" (").formatted(Formatting.GRAY))
				.append(Text.literal(StonksUtils.SHORT_FLOAT_NUMBERS.format(spread)).withColor(Colors.RED.asInt()))
				.append(Text.literal(")").formatted(Formatting.GRAY))
		);
		source.sendFeedback(Text.empty());
		source.sendFeedback(Text.literal(" Click HERE to show in the Graph Screen!").formatted(Formatting.YELLOW)
				.styled(style -> style
						.withHoverEvent(new HoverEvent.ShowText(Text.literal("Click to open in the Graph Screen!").formatted(Formatting.YELLOW)))
						.withClickEvent(new ClickEvent.RunCommand("/stonks"))));

		source.sendFeedback(Text.literal(SEPARATOR).formatted(Formatting.RED));
	}
}
