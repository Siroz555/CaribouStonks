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
import fr.siroz.cariboustonks.screen.keyshortcut.KeyShortcutScreen;
import fr.siroz.cariboustonks.util.Client;
import fr.siroz.cariboustonks.util.colors.Colors;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

@SuppressWarnings("checkstyle:linelength")
public class GeneralCategory extends AbstractCategory {

	public GeneralCategory(Config defaults, Config current) {
		super(defaults, current);
	}

	@Override
	public ConfigCategory create() {
		return ConfigCategory.createBuilder()
				.name(Component.literal("General"))
				.tooltip(Component.literal("General Settings"))
				.option(ButtonOption.createBuilder()
						.name(Component.literal("CaribouStonks Menu"))
						.text(Component.literal("Open"))
						.action((screen, opt) -> Minecraft.getInstance().setScreen(new CaribouStonksMenuScreen()))
						.build())
				.option(ButtonOption.createBuilder()
						.name(Component.literal("Change HUD positions"))
						.text(Component.literal("Open"))
						.action((screen, opt) -> Minecraft.getInstance().setScreen(HudConfigScreen.create(screen)))
						.build())
				.option(LabelOption.create(Component.literal("| Key Shortcuts").withStyle(ChatFormatting.BOLD)))
				.option(ButtonOption.createBuilder()
						.name(Component.literal("KeyShortcut Menu"))
						.text(Component.literal("Open"))
						.action((screen, opt) -> Minecraft.getInstance().setScreen(KeyShortcutScreen.create(screen)))
						.build())
				.option(Option.<Integer>createBuilder()
						.name(Component.literal("KeyShortcut Cooldown"))
						.description(OptionDescription.of(
								Component.literal("Allows you to change the cooldown in milliseconds between each action."),
								Component.literal(SPACE + "Note: It is possible to be kicked for spamming if this value is too low. Generally, when it's “instant,” even though Hypixel prevents you by default.").withStyle(ChatFormatting.GOLD)))
						.binding(defaults.general.keyShortcutCooldown,
								() -> current.general.keyShortcutCooldown,
								newValue -> current.general.keyShortcutCooldown = newValue)
						.controller(opt -> createIntegerMsController(opt, 2500))
						.build())
				.group(OptionGroup.createBuilder()
						.name(Component.literal("Stonks").withStyle(ChatFormatting.BOLD))
						.description(OptionDescription.of(
								Component.literal("Stonks Settings")))
						.collapsed(false)
						// Tooltips
						.option(LabelOption.create(Component.literal("| Tooltips").withStyle(ChatFormatting.BOLD)))
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Bazaar Prices"))
								.description(OptionDescription.of(
										Component.literal("Displays item prices in its Tooltip. For the Bazaar, Buy-Order & Sell-Order will be added."),
										Component.literal(SPACE + "§c§lNote: §fThe prices displayed can be interpreted both as Insta-Buy/Sell prices, or as the prices of the best orders when you create a Buy/Sell order."),
										Component.literal(SPACE + "The same information appears when you hover over an item in the Bazaar. In addition, prices are updated every 5 minutes from Hypixel's official API.").withStyle(ChatFormatting.ITALIC)))
								.binding(defaults.general.stonks.bazaarTooltipPrice,
										() -> current.general.stonks.bazaarTooltipPrice,
										newValue -> {
											current.general.stonks.bazaarTooltipPrice = newValue;
											if (newValue) {
												Client.sendMessageWithPrefix(Component.literal("Bazaar Prices Enabled.").withStyle(ChatFormatting.GREEN)
														.append(Component.literal(" Prices will be available in the next 5 minutes.").withStyle(ChatFormatting.YELLOW, ChatFormatting.ITALIC)));
											}
										})
								.controller(this::createBooleanController)
								.build())
						.option(Option.<BazaarTooltipPriceType>createBuilder()
								.name(Component.literal("Bazaar Prices - Type"))
								.description(OptionDescription.of(
										Component.literal("Select the display type."),
										Component.literal(SPACE + "NORMAL :").withStyle(ChatFormatting.UNDERLINE),
										Component.literal(SPACE + "Show Buy and Sell prices only."),
										Component.literal(SPACE + "AVERAGE :").withStyle(ChatFormatting.UNDERLINE),
										Component.literal(SPACE + "Show only weighted averages of Buy and Sell prices."),
										Component.literal(SPACE + "ALL :").withStyle(ChatFormatting.UNDERLINE),
										Component.literal(SPACE + "Display Buy and Sell prices with weighted averages.")))
								.binding(defaults.general.stonks.bazaarTooltipPriceType,
										() -> current.general.stonks.bazaarTooltipPriceType,
										newValue -> current.general.stonks.bazaarTooltipPriceType = newValue)
								.controller(this::createEnumCyclingController)
								.build())
						.option(Option.<TooltipPriceDisplayType>createBuilder()
								.name(Component.literal("Bazaar Prices - Format"))
								.description(OptionDescription.of(
										Component.literal("Select the format displayed for Bazaar prices."),
										Component.empty()
												.append(Component.literal(SPACE + "FULL:").withStyle(ChatFormatting.UNDERLINE))
												.append(Component.literal(" 47,100,000").withStyle(ChatFormatting.GOLD)),
										Component.empty()
												.append(Component.literal(SPACE + "SHORT:").withStyle(ChatFormatting.UNDERLINE))
												.append(Component.literal(" 47,1M").withStyle(ChatFormatting.GOLD)),
										Component.empty()
												.append(Component.literal(SPACE + "ALL:").withStyle(ChatFormatting.UNDERLINE))
												.append(Component.literal(" 47,100,000").withStyle(ChatFormatting.GOLD))
												.append(Component.literal(" (").withStyle(ChatFormatting.GRAY))
												.append(Component.literal("47,1M").withStyle(ChatFormatting.GOLD))
												.append(Component.literal(")").withStyle(ChatFormatting.GRAY))))
								.binding(defaults.general.stonks.bazaarTooltipPriceDisplayType,
										() -> current.general.stonks.bazaarTooltipPriceDisplayType,
										newValue -> current.general.stonks.bazaarTooltipPriceDisplayType = newValue)
								.controller(this::createEnumCyclingController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Bazaar Prices - Stats"))
								.description(OptionDescription.of(
										Component.literal("Allows you to display advanced statistics on Bazaar product tooltips."),
										Component.literal(SPACE + "Statistics displayed:").withStyle(ChatFormatting.UNDERLINE),
										Component.literal(SPACE + "> Spread").withStyle(ChatFormatting.RED),
										Component.literal(" | Spread % with the absolute spread between Buy and Sell.").withColor(Colors.RED.asInt())))
								.binding(defaults.general.stonks.bazaarTooltipMoreData,
										() -> current.general.stonks.bazaarTooltipMoreData,
										newValue -> current.general.stonks.bazaarTooltipMoreData = newValue)
								.controller(this::createYesNoController)
								.build())
						.option(LabelOption.create(Component.literal("")))
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Auction Price"))
								.description(OptionDescription.of(
										Component.literal("Displays the price of an item in its Tooltip.")))
								.binding(defaults.general.stonks.auctionTooltipPrice,
										() -> current.general.stonks.auctionTooltipPrice,
										newValue -> {
											current.general.stonks.auctionTooltipPrice = newValue;
											if (newValue) {
												Client.sendMessageWithPrefix(Component.literal("Auction Prices Enabled.").withStyle(ChatFormatting.GREEN)
														.append(Component.literal(" Prices will be available in the next 5 minutes.").withStyle(ChatFormatting.YELLOW, ChatFormatting.ITALIC)));
											}
										})
								.controller(this::createBooleanController)
								.build())
						.option(Option.<AuctionTooltipPriceType>createBuilder()
								.available(false)
								.name(Component.literal("Auction Price - Type"))
								.description(OptionDescription.of(
										Component.literal("Select the display type."),
										Component.literal(SPACE + "LOWEST_BIN :").withStyle(ChatFormatting.UNDERLINE),
										Component.literal(SPACE + "Show only the lowest price in BIN."),
										Component.literal(SPACE + "AVERAGE_3_DAYS :").withStyle(ChatFormatting.UNDERLINE),
										Component.literal(SPACE + "Show only the average price for the last 3 days."),
										Component.literal(SPACE + "LOWEST_BIN_AND_AVERAGE :").withStyle(ChatFormatting.UNDERLINE),
										Component.literal(SPACE + "Display the lowest price in BIN and the average price over the last 3 days.")))
								.binding(defaults.general.stonks.auctionTooltipPriceType,
										() -> current.general.stonks.auctionTooltipPriceType,
										newValue -> current.general.stonks.auctionTooltipPriceType = newValue)
								.controller(this::createEnumCyclingController)
								.build())
						.option(Option.<TooltipPriceDisplayType>createBuilder()
								.name(Component.literal("Auction Prices - Format"))
								.description(OptionDescription.of(
										Component.literal("Select the format displayed for Auction prices."),
										Component.empty()
												.append(Component.literal(SPACE + "FULL:").withStyle(ChatFormatting.UNDERLINE))
												.append(Component.literal(" 47,100,000").withStyle(ChatFormatting.GOLD)),
										Component.empty()
												.append(Component.literal(SPACE + "SHORT:").withStyle(ChatFormatting.UNDERLINE))
												.append(Component.literal(" 47,1M").withStyle(ChatFormatting.GOLD)),
										Component.empty()
												.append(Component.literal(SPACE + "ALL:").withStyle(ChatFormatting.UNDERLINE))
												.append(Component.literal(" 47,100,000").withStyle(ChatFormatting.GOLD))
												.append(Component.literal(" (").withStyle(ChatFormatting.GRAY))
												.append(Component.literal("47,1M").withStyle(ChatFormatting.GOLD))
												.append(Component.literal(")").withStyle(ChatFormatting.GRAY))))
								.binding(defaults.general.stonks.auctionTooltipPriceDisplayType,
										() -> current.general.stonks.auctionTooltipPriceDisplayType,
										newValue -> current.general.stonks.auctionTooltipPriceDisplayType = newValue)
								.controller(this::createEnumCyclingController)
								.build())
						// Item Value
						.option(LabelOption.create(Component.literal("| Item Value").withStyle(ChatFormatting.BOLD)))
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Estimated Item Value Tooltip"))
								.description(OptionDescription.of(
										Component.literal("Displays the Estimated Value of an item in its Tooltip.")))
								.binding(defaults.general.stonks.itemValueTooltip,
										() -> current.general.stonks.itemValueTooltip,
										newValue -> current.general.stonks.itemValueTooltip = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Estimated Item Value Viewer").append(BETA))
								.description(OptionDescription.of(
										Component.literal("Displays the detailed summary of an item's value when you hover over it."),
										Component.literal(SPACE + "Displays all details of the item's value on the side of the inventory."),
										Component.literal(SPACE + "This viewer is in BETA phase.").withStyle(ChatFormatting.YELLOW)))
								.binding(defaults.general.stonks.itemValueViewer.enabled,
										() -> current.general.stonks.itemValueViewer.enabled,
										newValue -> current.general.stonks.itemValueViewer.enabled = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Float>createBuilder()
								.name(Component.literal("Estimated Item Value Viewer Scale"))
								.description(OptionDescription.of(
										Component.literal("Scale the Display of the Estimated Item Value Viewer.")))
								.binding(defaults.general.stonks.itemValueViewer.scale,
										() -> current.general.stonks.itemValueViewer.scale,
										newValue -> current.general.stonks.itemValueViewer.scale = newValue)
								.controller(opt -> FloatSliderControllerBuilder.create(opt)
										.range(0.5f, 2.5f)
										.step(0.1f)
										.formatValue(d -> Component.nullToEmpty("x " + String.format("%.1f", d))))
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Use Networth Item Value"))
								.description(OptionDescription.of(
										Component.literal("Yes:").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD),
										Component.literal("The value given does not reflect actual market prices.").withStyle(ChatFormatting.DARK_PURPLE),
										Component.literal("Prices are adjusted as with Discord bots or Websites. (Networth)").withStyle(ChatFormatting.DARK_PURPLE),
										Component.literal(SPACE + "No:").withStyle(ChatFormatting.RED, ChatFormatting.BOLD),
										Component.literal("The value given reflects actual market prices.").withStyle(ChatFormatting.LIGHT_PURPLE),
										Component.literal("Prices are not modified; they are retrieved directly at the time of calculation.").withStyle(ChatFormatting.LIGHT_PURPLE)))
								.binding(defaults.general.stonks.useNetworthItemValue,
										() -> current.general.stonks.useNetworthItemValue,
										newValue -> current.general.stonks.useNetworthItemValue = newValue)
								.controller(this::createYesNoController)
								.build())
						// Bazaar
						.option(LabelOption.create(Component.literal("| Bazaar").withStyle(ChatFormatting.BOLD)))
						.option(Option.<Boolean>createBuilder()
								.available(false)
								.name(Component.literal("Orders Tracker"))
								.description(OptionDescription.of(
										Component.literal("When you create an order, this allows you to track it: if you get outbid in the order list, you'll get a notification with information about the current market status.")))
								.binding(defaults.general.stonks.bazaarOrderTracker,
										() -> current.general.stonks.bazaarOrderTracker,
										newValue -> current.general.stonks.bazaarOrderTracker = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Press ENTER to validate orders"))
								.description(OptionDescription.of(
										Component.literal("Validate Buy/Sell orders by pressing"),
										Component.literal("ENTER").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD)))
								.binding(defaults.general.stonks.bazaarSignEditEnterValidation,
										() -> current.general.stonks.bazaarSignEditEnterValidation,
										newValue -> current.general.stonks.bazaarSignEditEnterValidation = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(LabelOption.create(Component.literal("| Stonks Screen & Commands").withStyle(ChatFormatting.BOLD)))
						.option(ButtonOption.createBuilder()
								.name(Component.literal("/stonks Command"))
								.text(Component.literal("/stonks <item>"))
								.action((screen, option) -> {})
								.description(OptionDescription.of(
										Component.literal("Use /stonks to display prices and other information simply anywhere."),
										Component.literal(SPACE + "Only works for Bazaar Items at the moment").withStyle(ChatFormatting.YELLOW, ChatFormatting.ITALIC)))
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Show gradiant in Graph Screen").append(BETA))
								.description(OptionDescription.of(
										Component.literal("Show in the Graphic Screen price representation, a colored gradiant.")))
								.binding(defaults.general.stonks.showGradientInGraphScreen,
										() -> current.general.stonks.showGradientInGraphScreen,
										newValue -> current.general.stonks.showGradientInGraphScreen = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Show all data in Info Screen").append(BETA))
								.description(OptionDescription.of(
										Component.literal("Show in the Info Screen price representation, all calculated data.")))
								.binding(defaults.general.stonks.showAllDataInInfoScreen,
										() -> current.general.stonks.showAllDataInInfoScreen,
										newValue -> current.general.stonks.showAllDataInInfoScreen = newValue)
								.controller(this::createBooleanController)
								.build())
						.build())
				.group(OptionGroup.createBuilder()
						.name(Component.literal("Reminders").withStyle(ChatFormatting.BOLD))
						.description(OptionDescription.of(
								Component.literal("Activate reminders for different aspects of the game.")))
						.collapsed(false)
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Greenhouse Growth Stage Reminder"))
								.description(OptionDescription.of(
										Component.literal("Allows you to activate a reminder when your Greenhouse reaches the Next Growth Stage."),
										Component.literal(SPACE + "See Skill > Farming - Garden for more options.").withStyle(ChatFormatting.YELLOW)))
								.binding(defaults.farming.garden.greenhouseGrowthStageReminder,
										() -> current.farming.garden.greenhouseGrowthStageReminder,
										newValue -> current.farming.garden.greenhouseGrowthStageReminder = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Booster Cookie").append(BETA))
								.description(OptionDescription.of(
										Component.literal("Activate a reminder when your Booster Cookie time is low.")))
								.binding(defaults.general.reminders.boosterCookie,
										() -> current.general.reminders.boosterCookie,
										newValue -> current.general.reminders.boosterCookie = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Chocolate Factory | Max Chocolates"))
								.description(OptionDescription.of(
										Component.literal("Activate reminders when chocolate production is nearing its limit.")))
								.binding(defaults.general.reminders.chocolateFactoryMaxChocolates,
										() -> current.general.reminders.chocolateFactoryMaxChocolates,
										newValue -> current.general.reminders.chocolateFactoryMaxChocolates = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Rift | Ubik's Cube"))
								.description(OptionDescription.of(
										Component.literal("Activate a reminder when your Ubik's Cube is ready for use.")))
								.binding(defaults.general.reminders.ubikCube,
										() -> current.general.reminders.ubikCube,
										newValue -> current.general.reminders.ubikCube = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Enchanted Cloak"))
								.description(OptionDescription.of(
										Component.literal("Activate a reminder when items are available in the Enchanted Cloak.")))
								.binding(defaults.general.reminders.enchantedCloak,
										() -> current.general.reminders.enchantedCloak,
										newValue -> current.general.reminders.enchantedCloak = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Forge"))
								.description(OptionDescription.of(
										Component.literal("Activate a reminder when an item in the forge is finished.")))
								.binding(defaults.general.reminders.forge,
										() -> current.general.reminders.forge,
										newValue -> current.general.reminders.forge = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Play Sound"))
								.description(OptionDescription.of(
										Component.literal("Play a Sound for Reminders.")))
								.binding(defaults.general.reminders.playSound,
										() -> current.general.reminders.playSound,
										newValue -> current.general.reminders.playSound = newValue)
								.controller(this::createYesNoController)
								.build())
						.option(LabelOption.create(Component.empty()))
						.build())
				.group(OptionGroup.createBuilder()
						.name(Component.literal("Danger Zone").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
						.description(OptionDescription.of(
								Component.literal("Control of internal parameters in Mod.").withStyle(ChatFormatting.RED)))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Check for updates"))
								.description(OptionDescription.of(
										Component.literal("Check if updates are available at game launch.").withStyle(ChatFormatting.RED)))
								.binding(defaults.general.internal.checkForUpdates,
										() -> current.general.internal.checkForUpdates,
										newValue -> current.general.internal.checkForUpdates = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Fetch Bazaar Data"))
								.description(OptionDescription.of(
										Component.literal("Completely disable data recovery from Bazaar.").withStyle(ChatFormatting.RED),
										Component.literal("Please note that many features depend on this option. ").withStyle(ChatFormatting.RED)))
								.binding(defaults.general.internal.fetchBazaarData,
										() -> current.general.internal.fetchBazaarData,
										newValue -> current.general.internal.fetchBazaarData = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Fetch Auction Data"))
								.description(OptionDescription.of(
										Component.literal("Completely disable data recovery from Auction House.").withStyle(ChatFormatting.RED),
										Component.literal("Please note that many features depend on this option. ").withStyle(ChatFormatting.RED)))
								.binding(defaults.general.internal.fetchAuctionData,
										() -> current.general.internal.fetchAuctionData,
										newValue -> current.general.internal.fetchAuctionData = newValue)
								.controller(this::createBooleanController)
								.build())
						.build())
				.build();
	}
}
