package fr.siroz.cariboustonks.feature.stonks;

import com.mojang.brigadier.arguments.StringArgumentType;
import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.skyblock.data.hypixel.HypixelDataSource;
import fr.siroz.cariboustonks.skyblock.data.hypixel.bazaar.BazaarProduct;
import fr.siroz.cariboustonks.skyblock.data.hypixel.item.SkyBlockItemData;
import fr.siroz.cariboustonks.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.feature.Feature;
import fr.siroz.cariboustonks.system.command.CommandComponent;
import fr.siroz.cariboustonks.screen.stonks.StonksScreen;
import fr.siroz.cariboustonks.util.Client;
import fr.siroz.cariboustonks.util.ItemLookupKey;
import fr.siroz.cariboustonks.util.NotEnoughUpdatesUtils;
import fr.siroz.cariboustonks.util.StonksUtils;
import fr.siroz.cariboustonks.util.colors.Colors;
import java.util.Optional;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import org.jetbrains.annotations.NotNull;

public class StonksCommandFeature extends Feature {

	// SIROZ-NOTE :: Implémentation pour les Auctions Items

	private static final String SEPARATOR = "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬";

	private final HypixelDataSource hypixelDataSource;

	private String lastItem = "";

	public StonksCommandFeature() {
		this.hypixelDataSource = CaribouStonks.skyBlock().getHypixelDataSource();

		addComponent(CommandComponent.class, d -> d.register(ClientCommandManager.literal("stonks")
				.executes(context -> {
					if (lastItem != null && !lastItem.isBlank()) {
						context.getSource().getClient().setScreen(StonksScreen.create(ItemLookupKey.of(
								NotEnoughUpdatesUtils.getNeuIdFromSkyBlockId(lastItem),
								lastItem
						)));
					}
					return 1;
				})
				.then(ClientCommandManager.argument("item", StringArgumentType.greedyString())
						.suggests((context, builder) -> SharedSuggestionProvider.suggest(hypixelDataSource.getSkyBlockItemsIds(), builder))
						.executes(context -> handle(context.getSource(), StringArgumentType.getString(context, "item"))))
		));
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock();
	}

	@Override
	protected void onClientJoinServer() {
		lastItem = "";
	}

	private int handle(FabricClientCommandSource source, String item) {
		int result = 1;

		if (hypixelDataSource.isBazaarInUpdate()) {
			source.sendFeedback(Component.literal("Bazaar is currently updating.. Retry in few seconds.").withStyle(ChatFormatting.RED));
			return result;
		}

		if (!hypixelDataSource.hasBazaarItem(item)) {
			source.sendFeedback(Component.literal("Unable to find '" + item + "' item in the Bazaar.").withStyle(ChatFormatting.RED));
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
		Client.playSound(SoundEvents.TRIAL_SPAWNER_ABOUT_TO_SPAWN_ITEM, 1f, 1f);

		source.sendFeedback(Component.literal(SEPARATOR).withStyle(ChatFormatting.RED));

		SkyBlockItemData skyBlockItem = hypixelDataSource.getSkyBlockItem(item);
		if (skyBlockItem == null) {
			source.sendFeedback(Component.empty().append(Component.literal("⭐").withColor(Colors.ORANGE.asInt()))
					.append(" " + Component.literal(bazaarProduct.skyBlockId() + " :").withStyle(ChatFormatting.GOLD)));
		} else {
			source.sendFeedback(Component.empty().append(Component.literal("⭐").withColor(Colors.ORANGE.asInt()))
					.append(Component.literal(" " + skyBlockItem.name()).withColor(skyBlockItem.tier().getColor()))
					.append(Component.literal(" (" + bazaarProduct.skyBlockId() + ")").withStyle(ChatFormatting.DARK_GRAY)));
		}

		source.sendFeedback(Component.empty());

		double buyPrice = bazaarProduct.buyPrice();
		double buyAvgPrice = bazaarProduct.weightedAverageBuyPrice();
		double buyVelocity = bazaarProduct.buyVelocity();
		double standardDeviationBuy = bazaarProduct.buyPriceStdDev();
		source.sendFeedback(Component.literal("Buy: ").withStyle(ChatFormatting.YELLOW)
				.append(Component.literal(StonksUtils.INTEGER_NUMBERS.format(buyPrice) + " Coins").withStyle(ChatFormatting.GOLD))
				.append(Component.literal(" (").withStyle(ChatFormatting.GRAY))
				.append(Component.literal(StonksUtils.SHORT_FLOAT_NUMBERS.format(buyPrice)).withStyle(ChatFormatting.GOLD))
				.append(Component.literal(")").withStyle(ChatFormatting.GRAY))
				.append(Component.literal(" (Hover)").withStyle(ChatFormatting.AQUA))
				.withStyle(style -> style.withHoverEvent(new HoverEvent.ShowText(
						Component.literal("Buy-Avg: ").withStyle(ChatFormatting.YELLOW)
								.append(Component.literal(StonksUtils.INTEGER_NUMBERS.format(buyAvgPrice) + " Coins").withStyle(ChatFormatting.GOLD))
								.append(Component.literal(" (").withStyle(ChatFormatting.GRAY))
								.append(Component.literal(StonksUtils.SHORT_FLOAT_NUMBERS.format(buyAvgPrice)).withStyle(ChatFormatting.GOLD))
								.append(Component.literal(")").withStyle(ChatFormatting.GRAY))
								.append(Component.literal("\n\n"))
								.append(Component.literal("Velocity: ").withStyle(ChatFormatting.DARK_AQUA))
								.append(Component.literal(StonksUtils.FLOAT_NUMBERS.format(buyVelocity)).withStyle(ChatFormatting.AQUA))
								.append(Component.literal("\n"))
								.append(Component.literal("(Compares current volume to the daily average from the past week)").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC))
								.append(Component.literal("\n"))
								.append(Component.literal("- < 0.5    = Very low activity").withStyle(ChatFormatting.GRAY))
								.append(Component.literal("\n"))
								.append(Component.literal("- 0.5-1.5 = Normal activity").withStyle(ChatFormatting.GRAY))
								.append(Component.literal("\n"))
								.append(Component.literal("- 1.5-3   = High activity (growing interest)").withStyle(ChatFormatting.GRAY))
								.append(Component.literal("\n"))
								.append(Component.literal("- > 3      = Peak activity (event, speculation)").withStyle(ChatFormatting.GRAY))
								.append(Component.literal("\n\n"))
								.append(Component.literal("*Standard Deviation: ").withStyle(ChatFormatting.DARK_GREEN))
								.append(Component.literal(StonksUtils.SHORT_FLOAT_NUMBERS.format(standardDeviationBuy)).withStyle(ChatFormatting.GREEN))
								.append(Component.literal("\n"))
								.append(Component.literal("(This measures price volatility. Higher values indicate stronger fluctuations)").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC))
								.append(Component.literal("\n\n"))
								.append(Component.literal("* Not a true representation of all orders").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC))
				))));

		long buyVolume = bazaarProduct.buyVolume();
		long buyOrders = bazaarProduct.buyOrders();
		long buyMovingWeek = bazaarProduct.buyMovingWeek();
		source.sendFeedback(Component.literal(StonksUtils.SHORT_FLOAT_NUMBERS.format(buyVolume)).withStyle(ChatFormatting.DARK_GRAY)
				.append(" in " + StonksUtils.SHORT_FLOAT_NUMBERS.format(buyOrders) + " orders")
				.append(Component.literal(" | ").withStyle(ChatFormatting.GRAY))
				.append(StonksUtils.SHORT_FLOAT_NUMBERS.format(buyMovingWeek)).withStyle(ChatFormatting.DARK_GRAY)
				.append(Component.literal(" insta-buys in 7d").withStyle(ChatFormatting.DARK_GRAY))
		);

		source.sendFeedback(Component.empty());

		double sellPrice = bazaarProduct.sellPrice();
		double sellAvgPrice = bazaarProduct.weightedAverageSellPrice();
		double sellVelocity = bazaarProduct.sellVelocity();
		double standardDeviationSell = bazaarProduct.sellPriceStdDev();
		source.sendFeedback(Component.literal("Sell: ").withStyle(ChatFormatting.YELLOW)
				.append(Component.literal(StonksUtils.INTEGER_NUMBERS.format(sellPrice) + " Coins").withStyle(ChatFormatting.GOLD))
				.append(Component.literal(" (").withStyle(ChatFormatting.GRAY))
				.append(Component.literal(StonksUtils.SHORT_FLOAT_NUMBERS.format(sellPrice)).withStyle(ChatFormatting.GOLD))
				.append(Component.literal(")").withStyle(ChatFormatting.GRAY))
				.append(Component.literal(" (Hover)").withStyle(ChatFormatting.AQUA))
				.withStyle(style -> style.withHoverEvent(new HoverEvent.ShowText(
						Component.literal("Sell-Avg: ").withStyle(ChatFormatting.YELLOW)
								.append(Component.literal(StonksUtils.INTEGER_NUMBERS.format(sellAvgPrice) + " Coins").withStyle(ChatFormatting.GOLD))
								.append(Component.literal(" (").withStyle(ChatFormatting.GRAY))
								.append(Component.literal(StonksUtils.SHORT_FLOAT_NUMBERS.format(sellAvgPrice)).withStyle(ChatFormatting.GOLD))
								.append(Component.literal(")").withStyle(ChatFormatting.GRAY))
								.append(Component.literal("\n\n"))
								.append(Component.literal("Velocity: ").withStyle(ChatFormatting.DARK_AQUA))
								.append(Component.literal(StonksUtils.FLOAT_NUMBERS.format(sellVelocity)).withStyle(ChatFormatting.AQUA))
								.append(Component.literal("\n"))
								.append(Component.literal("(Compares current volume to the daily average from the past week)").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC))
								.append(Component.literal("\n"))
								.append(Component.literal("- < 0.5    = Very low activity").withStyle(ChatFormatting.GRAY))
								.append(Component.literal("\n"))
								.append(Component.literal("- 0.5-1.5 = Normal activity").withStyle(ChatFormatting.GRAY))
								.append(Component.literal("\n"))
								.append(Component.literal("- 1.5-3   = High activity (growing interest)").withStyle(ChatFormatting.GRAY))
								.append(Component.literal("\n"))
								.append(Component.literal("- > 3      = Peak activity (event, speculation)").withStyle(ChatFormatting.GRAY))
								.append(Component.literal("\n\n"))
								.append(Component.literal("*Standard Deviation: ").withStyle(ChatFormatting.DARK_GREEN))
								.append(Component.literal(StonksUtils.SHORT_FLOAT_NUMBERS.format(standardDeviationSell)).withStyle(ChatFormatting.GREEN))
								.append(Component.literal("\n"))
								.append(Component.literal("(This measures price volatility. Higher values indicate stronger fluctuations)").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC))
								.append(Component.literal("\n\n"))
								.append(Component.literal("* Not a true representation of all orders").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC))
				)))
		);

		long sellVolume = bazaarProduct.sellVolume();
		long sellOrders = bazaarProduct.sellOrders();
		long sellMovingWeek = bazaarProduct.sellMovingWeek();
		source.sendFeedback(Component.literal(StonksUtils.SHORT_FLOAT_NUMBERS.format(sellVolume)).withStyle(ChatFormatting.DARK_GRAY)
				.append(" in " + StonksUtils.SHORT_FLOAT_NUMBERS.format(sellOrders) + " orders")
				.append(Component.literal(" | ").withStyle(ChatFormatting.GRAY))
				.append(StonksUtils.SHORT_FLOAT_NUMBERS.format(sellMovingWeek)).withStyle(ChatFormatting.DARK_GRAY)
				.append(Component.literal(" insta-sells in 7d").withStyle(ChatFormatting.DARK_GRAY))
		);

		source.sendFeedback(Component.empty());

		double spread = bazaarProduct.spread();
		double spreadPercentage = bazaarProduct.spreadPercentage();
		source.sendFeedback(Component.literal("Spreed: ").withStyle(ChatFormatting.RED)
				.append(Component.literal(StonksUtils.FLOAT_NUMBERS.format(spreadPercentage) + "%").withColor(Colors.RED.asInt()))
				.append(Component.literal(" | ").withStyle(ChatFormatting.GRAY))
				.append(Component.literal(StonksUtils.INTEGER_NUMBERS.format(spread)).withColor(Colors.RED.asInt()))
				.append(Component.literal(" (").withStyle(ChatFormatting.GRAY))
				.append(Component.literal(StonksUtils.SHORT_FLOAT_NUMBERS.format(spread)).withColor(Colors.RED.asInt()))
				.append(Component.literal(")").withStyle(ChatFormatting.GRAY))
		);
		source.sendFeedback(Component.empty());
		source.sendFeedback(Component.literal(" Click HERE to show in the Graph Screen!").withStyle(ChatFormatting.YELLOW)
				.withStyle(style -> style
						.withHoverEvent(new HoverEvent.ShowText(Component.literal("Click to open in the Graph Screen!").withStyle(ChatFormatting.YELLOW)))
						.withClickEvent(new ClickEvent.RunCommand("/stonks"))));

		source.sendFeedback(Component.literal(SEPARATOR).withStyle(ChatFormatting.RED));
	}
}
