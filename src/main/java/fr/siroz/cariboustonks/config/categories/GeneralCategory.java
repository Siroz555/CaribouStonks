package fr.siroz.cariboustonks.config.categories;

import dev.isxander.yacl3.api.ButtonOption;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.LabelOption;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.controller.FloatSliderControllerBuilder;
import fr.siroz.cariboustonks.config.Config;
import fr.siroz.cariboustonks.feature.stonks.tooltips.auction.AuctionTooltipPriceType;
import fr.siroz.cariboustonks.feature.stonks.tooltips.bazaar.BazaarTooltipPriceType;
import fr.siroz.cariboustonks.feature.stonks.tooltips.TooltipPriceDisplayType;
import fr.siroz.cariboustonks.screen.CaribouStonksMenuScreen;
import fr.siroz.cariboustonks.screen.HudConfigScreen;
import fr.siroz.cariboustonks.util.Client;
import fr.siroz.cariboustonks.util.colors.Colors;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

@SuppressWarnings("checkstyle:linelength")
public class GeneralCategory extends AbstractCategory {

	public GeneralCategory(Config defaults, Config current) {
		super(defaults, current);
	}

	@Override
	public ConfigCategory create() {
		return ConfigCategory.createBuilder()
				.name(Text.literal("General"))
				.tooltip(Text.literal("General Settings"))
				.option(ButtonOption.createBuilder()
						.name(Text.literal("CaribouStonks Menu"))
						.text(Text.literal("Open"))
						.action((screen, opt) -> MinecraftClient.getInstance().setScreen(new CaribouStonksMenuScreen()))
						.build())
				.option(ButtonOption.createBuilder()
						.name(Text.literal("Change HUD positions"))
						.text(Text.literal("Open"))
						.action((screen, opt) -> MinecraftClient.getInstance().setScreen(HudConfigScreen.create(screen)))
						.build())
				.group(OptionGroup.createBuilder()
						.name(Text.literal("Stonks").formatted(Formatting.BOLD))
						.description(OptionDescription.of(
								Text.literal("Stonks Settings")))
						.collapsed(false)
						// Tooltips
						.option(LabelOption.create(Text.literal("| Tooltips").formatted(Formatting.BOLD)))
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Bazaar Prices"))
								.description(OptionDescription.of(
										Text.literal("Displays item prices in its Tooltip. For the Bazaar, Buy-Order & Sell-Order will be added."),
										Text.literal(SPACE + "§c§lNote: §fThe prices displayed can be interpreted both as Insta-Buy/Sell prices, or as the prices of the best orders when you create a Buy/Sell order."),
										Text.literal(SPACE + "The same information appears when you hover over an item in the Bazaar. In addition, prices are updated every 5 minutes from Hypixel's official API.").formatted(Formatting.ITALIC)))
								.binding(defaults.general.stonks.bazaarTooltipPrice,
										() -> current.general.stonks.bazaarTooltipPrice,
										newValue -> {
											current.general.stonks.bazaarTooltipPrice = newValue;
											if (newValue) {
												Client.sendMessageWithPrefix(Text.literal("Bazaar Prices Enabled.").formatted(Formatting.GREEN)
														.append(Text.literal(" Prices will be available in the next 5 minutes.").formatted(Formatting.YELLOW, Formatting.ITALIC)));
											}
										})
								.controller(this::createBooleanController)
								.build())
						.option(Option.<BazaarTooltipPriceType>createBuilder()
								.name(Text.literal("Bazaar Prices - Type"))
								.description(OptionDescription.of(
										Text.literal("Select the display type."),
										Text.literal(SPACE + "NORMAL :").formatted(Formatting.UNDERLINE),
										Text.literal(SPACE + "Show Buy and Sell prices only."),
										Text.literal(SPACE + "AVERAGE :").formatted(Formatting.UNDERLINE),
										Text.literal(SPACE + "Show only weighted averages of Buy and Sell prices."),
										Text.literal(SPACE + "ALL :").formatted(Formatting.UNDERLINE),
										Text.literal(SPACE + "Display Buy and Sell prices with weighted averages.")))
								.binding(defaults.general.stonks.bazaarTooltipPriceType,
										() -> current.general.stonks.bazaarTooltipPriceType,
										newValue -> current.general.stonks.bazaarTooltipPriceType = newValue)
								.controller(this::createEnumCyclingController)
								.build())
						.option(Option.<TooltipPriceDisplayType>createBuilder()
								.name(Text.literal("Bazaar Prices - Format"))
								.description(OptionDescription.of(
										Text.literal("Select the format displayed for Bazaar prices."),
										Text.empty()
												.append(Text.literal(SPACE + "FULL:").formatted(Formatting.UNDERLINE))
												.append(Text.literal(" 47,100,000").formatted(Formatting.GOLD)),
										Text.empty()
												.append(Text.literal(SPACE + "SHORT:").formatted(Formatting.UNDERLINE))
												.append(Text.literal(" 47,1M").formatted(Formatting.GOLD)),
										Text.empty()
												.append(Text.literal(SPACE + "ALL:").formatted(Formatting.UNDERLINE))
												.append(Text.literal(" 47,100,000").formatted(Formatting.GOLD))
												.append(Text.literal(" (").formatted(Formatting.GRAY))
												.append(Text.literal("47,1M").formatted(Formatting.GOLD))
												.append(Text.literal(")").formatted(Formatting.GRAY))))
								.binding(defaults.general.stonks.bazaarTooltipPriceDisplayType,
										() -> current.general.stonks.bazaarTooltipPriceDisplayType,
										newValue -> current.general.stonks.bazaarTooltipPriceDisplayType = newValue)
								.controller(this::createEnumCyclingController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Bazaar Prices - Stats"))
								.description(OptionDescription.of(
										Text.literal("Allows you to display advanced statistics on Bazaar product tooltips."),
										Text.literal(SPACE + "Statistics displayed:").formatted(Formatting.UNDERLINE),
										Text.literal(SPACE + "> Spread").formatted(Formatting.RED),
										Text.literal(" | Spread % with the absolute spread between Buy and Sell.").withColor(Colors.RED.asInt())))
								.binding(defaults.general.stonks.bazaarTooltipMoreData,
										() -> current.general.stonks.bazaarTooltipMoreData,
										newValue -> current.general.stonks.bazaarTooltipMoreData = newValue)
								.controller(this::createYesNoController)
								.build())
						.option(LabelOption.create(Text.literal("")))
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Auction Price"))
								.description(OptionDescription.of(
										Text.literal("Displays the price of an item in its Tooltip.")))
								.binding(defaults.general.stonks.auctionTooltipPrice,
										() -> current.general.stonks.auctionTooltipPrice,
										newValue -> {
											current.general.stonks.auctionTooltipPrice = newValue;
											if (newValue) {
												Client.sendMessageWithPrefix(Text.literal("Auction Prices Enabled.").formatted(Formatting.GREEN)
														.append(Text.literal(" Prices will be available in the next 5 minutes.").formatted(Formatting.YELLOW, Formatting.ITALIC)));
											}
										})
								.controller(this::createBooleanController)
								.build())
						.option(Option.<AuctionTooltipPriceType>createBuilder()
								.available(false)
								.name(Text.literal("Auction Price - Type"))
								.description(OptionDescription.of(
										Text.literal("Select the display type."),
										Text.literal(SPACE + "LOWEST_BIN :").formatted(Formatting.UNDERLINE),
										Text.literal(SPACE + "Show only the lowest price in BIN."),
										Text.literal(SPACE + "AVERAGE_3_DAYS :").formatted(Formatting.UNDERLINE),
										Text.literal(SPACE + "Show only the average price for the last 3 days."),
										Text.literal(SPACE + "LOWEST_BIN_AND_AVERAGE :").formatted(Formatting.UNDERLINE),
										Text.literal(SPACE + "Display the lowest price in BIN and the average price over the last 3 days.")))
								.binding(defaults.general.stonks.auctionTooltipPriceType,
										() -> current.general.stonks.auctionTooltipPriceType,
										newValue -> current.general.stonks.auctionTooltipPriceType = newValue)
								.controller(this::createEnumCyclingController)
								.build())
						.option(Option.<TooltipPriceDisplayType>createBuilder()
								.name(Text.literal("Auction Prices - Format"))
								.description(OptionDescription.of(
										Text.literal("Select the format displayed for Auction prices."),
										Text.empty()
												.append(Text.literal(SPACE + "FULL:").formatted(Formatting.UNDERLINE))
												.append(Text.literal(" 47,100,000").formatted(Formatting.GOLD)),
										Text.empty()
												.append(Text.literal(SPACE + "SHORT:").formatted(Formatting.UNDERLINE))
												.append(Text.literal(" 47,1M").formatted(Formatting.GOLD)),
										Text.empty()
												.append(Text.literal(SPACE + "ALL:").formatted(Formatting.UNDERLINE))
												.append(Text.literal(" 47,100,000").formatted(Formatting.GOLD))
												.append(Text.literal(" (").formatted(Formatting.GRAY))
												.append(Text.literal("47,1M").formatted(Formatting.GOLD))
												.append(Text.literal(")").formatted(Formatting.GRAY))))
								.binding(defaults.general.stonks.auctionTooltipPriceDisplayType,
										() -> current.general.stonks.auctionTooltipPriceDisplayType,
										newValue -> current.general.stonks.auctionTooltipPriceDisplayType = newValue)
								.controller(this::createEnumCyclingController)
								.build())
						// Item Value
						.option(LabelOption.create(Text.literal("| Item Value").formatted(Formatting.BOLD)))
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Estimated Item Value Tooltip"))
								.description(OptionDescription.of(
										Text.literal("Displays the Estimated Value of an item in its Tooltip.")))
								.binding(defaults.general.stonks.itemValueTooltip,
										() -> current.general.stonks.itemValueTooltip,
										newValue -> current.general.stonks.itemValueTooltip = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Estimated Item Value Viewer").append(BETA))
								.description(OptionDescription.of(
										Text.literal("Displays the detailed summary of an item's value when you hover over it."),
										Text.literal(SPACE + "Displays all details of the item's value on the side of the inventory."),
										Text.literal(SPACE + "This viewer is in BETA phase.").formatted(Formatting.YELLOW)))
								.binding(defaults.general.stonks.itemValueViewer.enabled,
										() -> current.general.stonks.itemValueViewer.enabled,
										newValue -> current.general.stonks.itemValueViewer.enabled = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Float>createBuilder()
								.name(Text.literal("Estimated Item Value Viewer Scale"))
								.description(OptionDescription.of(
										Text.literal("Scale the Display of the Estimated Item Value Viewer.")))
								.binding(defaults.general.stonks.itemValueViewer.scale,
										() -> current.general.stonks.itemValueViewer.scale,
										newValue -> current.general.stonks.itemValueViewer.scale = newValue)
								.controller(opt -> FloatSliderControllerBuilder.create(opt)
										.range(0.5f, 2.5f)
										.step(0.1f)
										.formatValue(d -> Text.of("x " + String.format("%.1f", d))))
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Use Networth Item Value"))
								.description(OptionDescription.of(
										Text.literal("Yes:").formatted(Formatting.GREEN, Formatting.BOLD),
										Text.literal("The value given does not reflect actual market prices.").formatted(Formatting.DARK_PURPLE),
										Text.literal("Prices are adjusted as with Discord bots or Websites. (Networth)").formatted(Formatting.DARK_PURPLE),
										Text.literal(SPACE + "No:").formatted(Formatting.RED, Formatting.BOLD),
										Text.literal("The value given reflects actual market prices.").formatted(Formatting.LIGHT_PURPLE),
										Text.literal("Prices are not modified; they are retrieved directly at the time of calculation.").formatted(Formatting.LIGHT_PURPLE)))
								.binding(defaults.general.stonks.useNetworthItemValue,
										() -> current.general.stonks.useNetworthItemValue,
										newValue -> current.general.stonks.useNetworthItemValue = newValue)
								.controller(this::createYesNoController)
								.build())
						// Bazaar
						.option(LabelOption.create(Text.literal("| Bazaar").formatted(Formatting.BOLD)))
						.option(Option.<Boolean>createBuilder()
								.available(false)
								.name(Text.literal("Orders Tracker"))
								.description(OptionDescription.of(
										Text.literal("When you create an order, this allows you to track it: if you get outbid in the order list, you'll get a notification with information about the current market status.")))
								.binding(defaults.general.stonks.bazaarOrderTracker,
										() -> current.general.stonks.bazaarOrderTracker,
										newValue -> current.general.stonks.bazaarOrderTracker = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Press ENTER to validate orders"))
								.description(OptionDescription.of(
										Text.literal("Validate Buy/Sell orders by pressing"),
										Text.literal("ENTER").formatted(Formatting.AQUA, Formatting.BOLD)))
								.binding(defaults.general.stonks.bazaarSignEditEnterValidation,
										() -> current.general.stonks.bazaarSignEditEnterValidation,
										newValue -> current.general.stonks.bazaarSignEditEnterValidation = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(LabelOption.create(Text.literal("| Stonks Screen & Commands").formatted(Formatting.BOLD)))
						.option(ButtonOption.createBuilder()
								.name(Text.literal("/stonks Command"))
								.text(Text.literal("/stonks <item>"))
								.action((screen, option) -> {})
								.description(OptionDescription.of(
										Text.literal("Use /stonks to display prices and other information simply anywhere."),
										Text.literal(SPACE + "Only works for Bazaar Items at the moment").formatted(Formatting.YELLOW, Formatting.ITALIC)))
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Show gradiant in Graph Screen").append(BETA))
								.description(OptionDescription.of(
										Text.literal("Show in the Graphic Screen price representation, a colored gradiant.")))
								.binding(defaults.general.stonks.showGradientInGraphScreen,
										() -> current.general.stonks.showGradientInGraphScreen,
										newValue -> current.general.stonks.showGradientInGraphScreen = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Show all data in Info Screen").append(BETA))
								.description(OptionDescription.of(
										Text.literal("Show in the Info Screen price representation, all calculated data.")))
								.binding(defaults.general.stonks.showAllDataInInfoScreen,
										() -> current.general.stonks.showAllDataInInfoScreen,
										newValue -> current.general.stonks.showAllDataInInfoScreen = newValue)
								.controller(this::createBooleanController)
								.build())
						.build())
				.group(OptionGroup.createBuilder()
						.name(Text.literal("Reminders").formatted(Formatting.BOLD))
						.description(OptionDescription.of(
								Text.literal("Activate reminders for different aspects of the game.")))
						.collapsed(false)
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Greenhouse Growth Stage Reminder"))
								.description(OptionDescription.of(
										Text.literal("Allows you to activate a reminder when your Greenhouse reaches the Next Growth Stage."),
										Text.literal(SPACE + "See Skill > Farming - Garden for more options.").formatted(Formatting.YELLOW)))
								.binding(defaults.farming.garden.greenhouseGrowthStageReminder,
										() -> current.farming.garden.greenhouseGrowthStageReminder,
										newValue -> current.farming.garden.greenhouseGrowthStageReminder = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Booster Cookie").append(BETA))
								.description(OptionDescription.of(
										Text.literal("Activate a reminder when your Booster Cookie time is low.")))
								.binding(defaults.general.reminders.boosterCookie,
										() -> current.general.reminders.boosterCookie,
										newValue -> current.general.reminders.boosterCookie = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Chocolate Factory | Max Chocolates"))
								.description(OptionDescription.of(
										Text.literal("Activate reminders when chocolate production is nearing its limit.")))
								.binding(defaults.general.reminders.chocolateFactoryMaxChocolates,
										() -> current.general.reminders.chocolateFactoryMaxChocolates,
										newValue -> current.general.reminders.chocolateFactoryMaxChocolates = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Rift | Ubik's Cube"))
								.description(OptionDescription.of(
										Text.literal("Activate a reminder when your Ubik's Cube is ready for use.")))
								.binding(defaults.general.reminders.ubikCube,
										() -> current.general.reminders.ubikCube,
										newValue -> current.general.reminders.ubikCube = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Enchanted Cloak"))
								.description(OptionDescription.of(
										Text.literal("Activate a reminder when items are available in the Enchanted Cloak.")))
								.binding(defaults.general.reminders.enchantedCloak,
										() -> current.general.reminders.enchantedCloak,
										newValue -> current.general.reminders.enchantedCloak = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Forge"))
								.description(OptionDescription.of(
										Text.literal("Activate a reminder when an item in the forge is finished.")))
								.binding(defaults.general.reminders.forge,
										() -> current.general.reminders.forge,
										newValue -> current.general.reminders.forge = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Play Sound"))
								.description(OptionDescription.of(
										Text.literal("Play a Sound for Reminders.")))
								.binding(defaults.general.reminders.playSound,
										() -> current.general.reminders.playSound,
										newValue -> current.general.reminders.playSound = newValue)
								.controller(this::createYesNoController)
								.build())
						.option(LabelOption.create(Text.empty()))
						.build())
				.group(OptionGroup.createBuilder()
						.name(Text.literal("Danger Zone").formatted(Formatting.RED, Formatting.BOLD))
						.description(OptionDescription.of(
								Text.literal("Control of internal parameters in Mod.").formatted(Formatting.RED)))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Check for updates"))
								.description(OptionDescription.of(
										Text.literal("Check if updates are available at game launch.").formatted(Formatting.RED)))
								.binding(defaults.general.internal.checkForUpdates,
										() -> current.general.internal.checkForUpdates,
										newValue -> current.general.internal.checkForUpdates = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Fetch Bazaar Data"))
								.description(OptionDescription.of(
										Text.literal("Completely disable data recovery from Bazaar.").formatted(Formatting.RED),
										Text.literal("Please note that many features depend on this option. ").formatted(Formatting.RED)))
								.binding(defaults.general.internal.fetchBazaarData,
										() -> current.general.internal.fetchBazaarData,
										newValue -> current.general.internal.fetchBazaarData = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Fetch Auction Data"))
								.description(OptionDescription.of(
										Text.literal("Completely disable data recovery from Auction House.").formatted(Formatting.RED),
										Text.literal("Please note that many features depend on this option. ").formatted(Formatting.RED)))
								.binding(defaults.general.internal.fetchAuctionData,
										() -> current.general.internal.fetchAuctionData,
										newValue -> current.general.internal.fetchAuctionData = newValue)
								.controller(this::createBooleanController)
								.build())
						.build())
				.build();
	}
}
