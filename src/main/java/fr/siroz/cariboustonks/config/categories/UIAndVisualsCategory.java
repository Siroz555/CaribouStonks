package fr.siroz.cariboustonks.config.categories;

import dev.isxander.yacl3.api.ButtonOption;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.controller.ColorControllerBuilder;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import fr.siroz.cariboustonks.config.Config;
import fr.siroz.cariboustonks.manager.waypoint.Waypoint;
import fr.siroz.cariboustonks.screen.HudConfigScreen;
import fr.siroz.cariboustonks.screen.mobtracking.MobTrackingScreen;
import fr.siroz.cariboustonks.util.Client;
import fr.siroz.cariboustonks.util.render.animation.AnimationUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.awt.Color;

@SuppressWarnings("checkstyle:linelength")
public class UIAndVisualsCategory extends AbstractCategory {

	public UIAndVisualsCategory(Config defaults, Config current) {
		super(defaults, current);
	}

	@Override
	public ConfigCategory create() {
		return ConfigCategory.createBuilder()
				.name(Text.literal("UI & Visuals"))
				.tooltip(Text.literal("User Interface and Visual Settings"))
				.option(Option.<Boolean>createBuilder()
						.name(Text.literal("Display a Shadow on HUD text"))
						.description(OptionDescription.of(
								Text.literal("Display a Shadow on all HUD text of the Mod.")))
						.binding(defaults.uiAndVisuals.shadowTextHud,
								() -> current.uiAndVisuals.shadowTextHud,
								newValue -> current.uiAndVisuals.shadowTextHud = newValue)
						.controller(this::createBooleanController)
						.build())
				.option(Option.<Boolean>createBuilder()
						.name(Text.literal("Beacon Beams through blocks"))
						.description(OptionDescription.of(
								Text.literal("If enabled, Beacon Beams will always be visible through blocks."),
								Text.literal(SPACE + "Requires a game restart to take effect.")))
						.binding(defaults.uiAndVisuals.beaconBeamWithNoDepthTest,
								() -> current.uiAndVisuals.beaconBeamWithNoDepthTest,
								newValue -> current.uiAndVisuals.beaconBeamWithNoDepthTest = newValue)
						.controller(this::createBooleanController)
						.build())
				.option(Option.<Boolean>createBuilder()
						.name(Text.literal("Highlight Selected Pet in the Pet Menu"))
						.description(OptionDescription.of(
								Text.literal("Highlight the current equipped pet in the Pet's Menu.")))
						.binding(defaults.uiAndVisuals.highlightSelectedPet,
								() -> current.uiAndVisuals.highlightSelectedPet,
								newValue -> current.uiAndVisuals.highlightSelectedPet = newValue)
						.controller(this::createBooleanController)
						.build())
				.option(Option.<Boolean>createBuilder()
						.name(Text.literal("Abiphone Favorite Contacts"))
						.description(OptionDescription.of(
								Text.literal("Create and highlight contacts in your Abiphone Menu.")))
						.binding(defaults.uiAndVisuals.abiphoneFavoriteContacts,
								() -> current.uiAndVisuals.abiphoneFavoriteContacts,
								newValue -> current.uiAndVisuals.abiphoneFavoriteContacts = newValue)
						.controller(this::createBooleanController)
						.build())
				.group(OptionGroup.createBuilder()
						.name(Text.literal("Deployable").formatted(Formatting.BOLD))
						.description(OptionDescription.of(
								Text.literal(""),
								Text.literal(SPACE + ".").formatted(Formatting.RED)))
						.collapsed(false)
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Enable Deployable"))
								.description(OptionDescription.of(
										Text.literal("Once activated, deployable such as Power Orbs, Flares and Personal will be displayed in a HUD with their respective timer.")))
								.binding(defaults.uiAndVisuals.deployables.enabled,
										() -> current.uiAndVisuals.deployables.enabled,
										newValue -> current.uiAndVisuals.deployables.enabled = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Show in a HUD"))
								.description(OptionDescription.of(
										Text.literal("If Deployable is enabled, this allows you to view all active deployable close to you along with their respective timer."),
										Text.literal(SPACE + "Only one type of deployable can be displayed at a time. However, multiple types may still appear, such as a Black Hole with a Plasmaflux Power Orb.").formatted(Formatting.YELLOW)))
								.binding(defaults.uiAndVisuals.deployables.hud.showHud,
										() -> current.uiAndVisuals.deployables.hud.showHud,
										newValue -> current.uiAndVisuals.deployables.hud.showHud = newValue)
								.controller(this::createYesNoController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Detect > Power Orbs Deployable"))
								.description(OptionDescription.of(
										Text.literal("If Deployable is enabled, this allows you to detect Power Orbs:"),
										Text.literal(SPACE),
										Text.literal("- Mana Flux").formatted(Formatting.BLUE),
										Text.literal("- Overflux").formatted(Formatting.DARK_PURPLE),
										Text.literal("- Plasmaflux").formatted(Formatting.LIGHT_PURPLE),
										Text.literal("- Umberella").formatted(Formatting.BLUE),
										Text.literal("- Titanium Lantern").formatted(Formatting.BLUE),
										Text.literal("- Glacite Lantern").formatted(Formatting.DARK_PURPLE),
										Text.literal("- Will-o'-wisp").formatted(Formatting.GOLD)))
								.binding(defaults.uiAndVisuals.deployables.detectPowerOrbs,
										() -> current.uiAndVisuals.deployables.detectPowerOrbs,
										newValue -> current.uiAndVisuals.deployables.detectPowerOrbs = newValue)
								.controller(this::createYesNoController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Detect > Flares Deployable").append(BETA))
								.description(OptionDescription.of(
										Text.literal("If Deployable is enabled, this allows you to detect Flares:"),
										Text.literal(SPACE),
										Text.literal("- Alert Flare").formatted(Formatting.BLUE),
										Text.literal("- SOS Flare").formatted(Formatting.DARK_PURPLE),
										Text.literal(SPACE + "[!] Flares detection is experimental.").formatted(Formatting.GOLD)))
								.binding(defaults.uiAndVisuals.deployables.detectFlares,
										() -> current.uiAndVisuals.deployables.detectFlares,
										newValue -> current.uiAndVisuals.deployables.detectFlares = newValue)
								.controller(this::createYesNoController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Detect > Personal Deployable"))
								.description(OptionDescription.of(
										Text.literal("If Deployable is enabled, this allows you to detect Personal Deployable:"),
										Text.literal(SPACE),
										Text.literal("- Black Hole").formatted(Formatting.DARK_PURPLE)))
								.binding(defaults.uiAndVisuals.deployables.detectPersonals,
										() -> current.uiAndVisuals.deployables.detectPersonals,
										newValue -> current.uiAndVisuals.deployables.detectPersonals = newValue)
								.controller(this::createYesNoController)
								.build())
						.build())
				.group(OptionGroup.createBuilder()
						.name(Text.literal("Mob Tracking").formatted(Formatting.BOLD))
						.description(OptionDescription.of(
								Text.literal("Mob Tracking combines features that allow you to view information about a specific Mob in real time."),
								Text.literal(SPACE + "Allow the display of mob health and other information in a custom Boss Bar or via a HUD. Also allows you to be alerted when a mob spawns.")))
						.collapsed(false)
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Enable Mob Tracking"))
								.description(OptionDescription.of(
										Text.literal("Once activated, mobs such as Slayers, rare fishing mobs, and others will be displayed in the form of a Boss Bar or HUD."),
										Text.literal(SPACE + "The display shows the life and other information of the mob being tracked in real time."),
										Text.literal(SPACE + "Each mob can be activated or deactivated, display an alert, in the dedicated menu.")))
								.binding(defaults.uiAndVisuals.mobTracking.tracking,
										() -> current.uiAndVisuals.mobTracking.tracking,
										newValue -> current.uiAndVisuals.mobTracking.tracking = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Show in Boss Bar"))
								.description(OptionDescription.of(
										Text.literal("If Mob Tracking is enabled, this allows you to view the highest Mob Tracking in a custom Boss Bar.")))
								.binding(defaults.uiAndVisuals.mobTracking.showInBossBar,
										() -> current.uiAndVisuals.mobTracking.showInBossBar,
										newValue -> current.uiAndVisuals.mobTracking.showInBossBar = newValue)
								.controller(this::createYesNoController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Show in a HUD"))
								.description(OptionDescription.of(
										Text.literal("If Mob Tracking is enabled, this allows you to view all Mob Tracking in a HUD.")))
								.binding(defaults.uiAndVisuals.mobTracking.hud.showInHud,
										() -> current.uiAndVisuals.mobTracking.hud.showInHud,
										newValue -> current.uiAndVisuals.mobTracking.hud.showInHud = newValue)
								.controller(this::createYesNoController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Track Slayer Boss"))
								.description(OptionDescription.of(
										Text.literal("If Mob Tracking is enabled, this allows you to view Slayer Boss information.")))
								.binding(defaults.uiAndVisuals.mobTracking.enableSlayer,
										() -> current.uiAndVisuals.mobTracking.enableSlayer,
										newValue -> current.uiAndVisuals.mobTracking.enableSlayer = newValue)
								.controller(this::createYesNoController)
								.build())
						.option(Option.<String>createBuilder()
								.name(Text.literal("Track Spawn Message"))
								.description(OptionDescription.of(
										Text.literal("If Mob Tracking is enabled, this allows you to customize the spawn message. Supports Minecraft color codes (§c, §b, etc)."),
										Text.literal(SPACE + "Note: The message appears just above the mob's name; it does not contain the mob's name.").formatted(Formatting.YELLOW)))
								.binding(defaults.uiAndVisuals.mobTracking.spawnMessage,
										() -> current.uiAndVisuals.mobTracking.spawnMessage,
										newValue -> current.uiAndVisuals.mobTracking.spawnMessage = newValue)
								.controller(StringControllerBuilder::create)
								.build())
						.option(Option.<Color>createBuilder()
								.name(Text.literal("Track Glowing Color"))
								.description(OptionDescription.of(
										Text.literal("Change the Glowing color"),
										Text.literal(SPACE + "Note: Must be enabled in the Mob Tracking menu for each desired entity").formatted(Formatting.GOLD)))
								.binding(defaults.uiAndVisuals.mobTracking.highlightColor,
										() -> current.uiAndVisuals.mobTracking.highlightColor,
										newValue -> current.uiAndVisuals.mobTracking.highlightColor = newValue)
								.controller(ColorControllerBuilder::create)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Track Spawn Sound"))
								.description(OptionDescription.of(
										Text.literal("If Mob Tracking is enabled, this allows a sound to be played."),
										Text.literal(SPACE + "The sound is not played for the Slayers. You can enable the “Notify when spawned” option in the dedicated menu.")))
								.binding(defaults.uiAndVisuals.mobTracking.playSoundWhenSpawn,
										() -> current.uiAndVisuals.mobTracking.playSoundWhenSpawn,
										newValue -> current.uiAndVisuals.mobTracking.playSoundWhenSpawn = newValue)
								.controller(this::createYesNoController)
								.build())
						.option(ButtonOption.createBuilder()
								.name(Text.literal("Configure each tracked mob"))
								.text(Text.literal("Open"))
								.action((screen, opt) -> MinecraftClient.getInstance().setScreen(MobTrackingScreen.create(screen)))
								.build())
						.build())
				.group(OptionGroup.createBuilder()
						.name(Text.literal("TabList Widgets Extractor").formatted(Formatting.BOLD))
						.description(OptionDescription.of(
								Text.literal("Widgets Extractor")))
						.collapsed(false)
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Enable Widgets Extractor"))
								.description(OptionDescription.of(
										Text.literal("Allows you to extract widgets from the TabList and display them as a HUD."),
										Text.literal("Check each widget below, and make sure to enable the widgets on the islands whenever you want using /widgets"),
										Text.literal(SPACE + "Currently, the HUD display is a single view; if multiple widgets are enabled, they will all be displayed one after another.").formatted(Formatting.GOLD)))
								.binding(defaults.uiAndVisuals.tabListWidget.hud.enabled,
										() -> current.uiAndVisuals.tabListWidget.hud.enabled,
										newValue -> {
											current.uiAndVisuals.tabListWidget.hud.enabled = newValue;
											if (newValue) {
												Client.sendMessageWithPrefix(Text.literal("[Widgets Extractor] Currently, the HUD display is a single view; if multiple widgets are enabled, they will all be displayed one after another.").formatted(Formatting.RED));
												Client.sendMessageWithPrefix(Text.literal("[Widgets Extractor] You must enable or disable widgets on each desired Island via /widgets").formatted(Formatting.GOLD));
											}
										})
								.controller(this::createBooleanController)
								.build())
						.option(Option.<String>createBuilder()
								.name(Text.literal("Add custom Widgets"))
								.description(OptionDescription.of(
										Text.literal("Allows you to add custom widgets."),
										Text.literal("Enter the widget name, followed by a §f§lcomma (,) §r§fto add additional names if you want multiple widgets. Leave this field blank if you don't want custom widgets."),
										Text.literal(SPACE + "§f§lFor example: §dJacob's Contest, Event Tracker"),
										Text.literal(SPACE + "§eNames are displayed in bold before the “:”, such as “§d§nJacob's Contest§r§e: 1m 16s”")))
								.binding(defaults.uiAndVisuals.tabListWidget.customWidgets,
										() -> current.uiAndVisuals.tabListWidget.customWidgets,
										newValue -> current.uiAndVisuals.tabListWidget.customWidgets = newValue)
								.controller(StringControllerBuilder::create)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Widget > Bestiary"))
								.description(OptionDescription.of(
										Text.literal("")))
								.binding(defaults.uiAndVisuals.tabListWidget.bestiary,
										() -> current.uiAndVisuals.tabListWidget.bestiary,
										newValue -> current.uiAndVisuals.tabListWidget.bestiary = newValue)
								.controller(this::createYesNoController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Widget > Slayer"))
								.description(OptionDescription.of(
										Text.literal("")))
								.binding(defaults.uiAndVisuals.tabListWidget.slayer,
										() -> current.uiAndVisuals.tabListWidget.slayer,
										newValue -> current.uiAndVisuals.tabListWidget.slayer = newValue)
								.controller(this::createYesNoController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Widget > Pet"))
								.description(OptionDescription.of(
										Text.literal("")))
								.binding(defaults.uiAndVisuals.tabListWidget.pet,
										() -> current.uiAndVisuals.tabListWidget.pet,
										newValue -> current.uiAndVisuals.tabListWidget.pet = newValue)
								.controller(this::createYesNoController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Widget > Pickaxe Ability"))
								.description(OptionDescription.of(
										Text.literal("")))
								.binding(defaults.uiAndVisuals.tabListWidget.pickaxeAbility,
										() -> current.uiAndVisuals.tabListWidget.pickaxeAbility,
										newValue -> current.uiAndVisuals.tabListWidget.pickaxeAbility = newValue)
								.controller(this::createYesNoController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Widget > Pity"))
								.description(OptionDescription.of(
										Text.literal("")))
								.binding(defaults.uiAndVisuals.tabListWidget.pity,
										() -> current.uiAndVisuals.tabListWidget.pity,
										newValue -> current.uiAndVisuals.tabListWidget.pity = newValue)
								.controller(this::createYesNoController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Widget > Commissions"))
								.description(OptionDescription.of(
										Text.literal("")))
								.binding(defaults.uiAndVisuals.tabListWidget.commissions,
										() -> current.uiAndVisuals.tabListWidget.commissions,
										newValue -> current.uiAndVisuals.tabListWidget.commissions = newValue)
								.controller(this::createYesNoController)
								.build())
						.build())
				.group(OptionGroup.createBuilder()
						.name(Text.literal("Colored Enchantments").formatted(Formatting.BOLD))
						.description(OptionDescription.of(
								Text.literal("Colored enchantments in the item tooltips.")))
						.collapsed(false)
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Show max Enchantments"))
								.description(OptionDescription.of(
										Text.literal("Show max Enchantments with a color.")))
								.binding(defaults.uiAndVisuals.coloredEnchantment.showMaxEnchants,
										() -> current.uiAndVisuals.coloredEnchantment.showMaxEnchants,
										newValue -> current.uiAndVisuals.coloredEnchantment.showMaxEnchants = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Show max Enchantments in Rainbow"))
								.description(OptionDescription.of(
										Text.literal("Change the color of maxed enchantments to an" + SPACE),
										AnimationUtils.applyRainbow("animated Rainbow gradient o/"),
										Text.literal("(As an example)").formatted(Formatting.GRAY, Formatting.ITALIC)))
								.binding(defaults.uiAndVisuals.coloredEnchantment.maxEnchantsRainbow,
										() -> current.uiAndVisuals.coloredEnchantment.maxEnchantsRainbow,
										newValue -> current.uiAndVisuals.coloredEnchantment.maxEnchantsRainbow = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Color>createBuilder()
								.name(Text.literal("Max Enchantments Color"))
								.description(OptionDescription.of(
										Text.literal("Change the color for the max Enchantments."),
										Text.literal(SPACE + "Warning: If the Rainbow is activated, the color will not be applied.").formatted(Formatting.YELLOW)))
								.binding(defaults.uiAndVisuals.coloredEnchantment.maxEnchantsColor,
										() -> current.uiAndVisuals.coloredEnchantment.maxEnchantsColor,
										newValue -> current.uiAndVisuals.coloredEnchantment.maxEnchantsColor = newValue)
								.controller(ColorControllerBuilder::create)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Show good Enchantments"))
								.description(OptionDescription.of(
										Text.literal("Show good Enchantments with a color.")))
								.binding(defaults.uiAndVisuals.coloredEnchantment.showGoodEnchants,
										() -> current.uiAndVisuals.coloredEnchantment.showGoodEnchants,
										newValue -> current.uiAndVisuals.coloredEnchantment.showGoodEnchants = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Color>createBuilder()
								.name(Text.literal("Good Enchantments Color"))
								.description(OptionDescription.of(
										Text.literal("Change the color for the good Enchantments.")))
								.binding(defaults.uiAndVisuals.coloredEnchantment.goodEnchantsColor,
										() -> current.uiAndVisuals.coloredEnchantment.goodEnchantsColor,
										newValue -> current.uiAndVisuals.coloredEnchantment.goodEnchantsColor = newValue)
								.controller(ColorControllerBuilder::create)
								.build())
						.build())
				.group(OptionGroup.createBuilder()
						.name(Text.literal("Coloring Tooltip Borders on Items").formatted(Formatting.BOLD))
						.description(OptionDescription.of(
								Text.literal("Control the coloring of tooltip borders on items, according to their rarity.")))
						.collapsed(false)
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Enable item border coloring"))
								.description(OptionDescription.of(
										Text.literal("Display colored borders on items, according to their rarity.")))
								.binding(defaults.uiAndVisuals.toolTipDecorator.enabled,
										() -> current.uiAndVisuals.toolTipDecorator.enabled,
										newValue -> current.uiAndVisuals.toolTipDecorator.enabled = newValue)
								.controller(this::createBooleanController)
								.build())
						.build())
				.group(OptionGroup.createBuilder()
						.name(Text.literal("Waypoints shared between players").formatted(Formatting.BOLD))
						.description(OptionDescription.of(
								Text.literal("Control Waypoint display when players share their position in Chat"),
								Text.literal("Use /sendCoords to share your Position in the Chat!").formatted(Formatting.GREEN)))
						.collapsed(false)
						.option(ButtonOption.createBuilder()
								.name(Text.literal("Share your Position"))
								.text(Text.literal("/sendCoords"))
								.description(OptionDescription.of(
										Text.literal("Use /sendCoords to share your Position in the Chat!").formatted(Formatting.GREEN)))
								.action((screen, buttonOption) -> {})
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Share your Position with Area"))
								.description(OptionDescription.of(
										Text.literal("When you share your position, this displays your coordinates and the area where you are.")))
								.binding(defaults.uiAndVisuals.sharedPositionWaypoint.shareWithArea,
										() -> current.uiAndVisuals.sharedPositionWaypoint.shareWithArea,
										newValue -> current.uiAndVisuals.sharedPositionWaypoint.shareWithArea = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Enable Waypoint display"))
								.description(OptionDescription.of(
										Text.literal("When a player shares his position in the chat, a Waypoint is created."),
										Text.literal(SPACE + "This is automatic, no interaction is required.").formatted(Formatting.ITALIC)))
								.binding(defaults.uiAndVisuals.sharedPositionWaypoint.enabled,
										() -> current.uiAndVisuals.sharedPositionWaypoint.enabled,
										newValue -> current.uiAndVisuals.sharedPositionWaypoint.enabled = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Integer>createBuilder()
								.name(Text.literal("Waypoint display time"))
								.description(OptionDescription.of(
										Text.literal("Controls the waypoint display time in seconds.")))
								.binding(defaults.uiAndVisuals.sharedPositionWaypoint.showTime,
										() -> current.uiAndVisuals.sharedPositionWaypoint.showTime,
										newValue -> current.uiAndVisuals.sharedPositionWaypoint.showTime = newValue)
								.controller(opt -> createIntegerSecondesController(opt, 256))
								.build())
						.option(Option.<Waypoint.Type>createBuilder()
								.name(Text.literal("Waypoint Type"))
								.description(OptionDescription.of(
										Text.literal("Change the display type of the Waypoint."),
										Text.literal(SPACE + "Types:"),
										Text.literal(SPACE + " BEAM:").formatted(Formatting.UNDERLINE),
										Text.literal(SPACE + " Colored beacon beam"),
										Text.literal(SPACE + " WAYPOINT:").formatted(Formatting.UNDERLINE),
										Text.literal(SPACE + " Combines a beacon beam with a box at its base"),
										Text.literal(SPACE + " OUTLINED_WAYPOINT:").formatted(Formatting.UNDERLINE),
										Text.literal(SPACE + " Similar to WAYPOINT but with an outlined box"),
										Text.literal(SPACE + " HIGHLIGHT:").formatted(Formatting.UNDERLINE),
										Text.literal(SPACE + " Highlight with an outlined box"),
										Text.literal(SPACE + " OUTLINE:").formatted(Formatting.UNDERLINE),
										Text.literal(SPACE + " Creates only an outline around the target")))
								.binding(defaults.uiAndVisuals.sharedPositionWaypoint.type,
										() -> current.uiAndVisuals.sharedPositionWaypoint.type,
										newValue -> current.uiAndVisuals.sharedPositionWaypoint.type = newValue)
								.controller(this::createEnumCyclingController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Waypoint in Rainbow mode"))
								.description(OptionDescription.of(
										Text.literal("If enabled, the Waypoint displayed will be in Rainbow mode. Deactivating this option allows you to choose the color in the next option.")))
								.binding(defaults.uiAndVisuals.sharedPositionWaypoint.rainbow,
										() -> current.uiAndVisuals.sharedPositionWaypoint.rainbow,
										newValue -> current.uiAndVisuals.sharedPositionWaypoint.rainbow = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Color>createBuilder()
								.name(Text.literal("Waypoint color"))
								.description(OptionDescription.of(
										Text.literal("If the Waypoint is not in Rainbow mode, selects the Waypoint color.")))
								.binding(defaults.uiAndVisuals.sharedPositionWaypoint.color,
										() -> current.uiAndVisuals.sharedPositionWaypoint.color,
										newValue -> current.uiAndVisuals.sharedPositionWaypoint.color = newValue)
								.controller(ColorControllerBuilder::create)
								.build())
						.build())
				.group(OptionGroup.createBuilder()
						.name(Text.literal("Overlay").formatted(Formatting.BOLD))
						.collapsed(false)
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Show Etherwarp Teleport Target"))
								.description(OptionDescription.of(
										Text.literal("Shows the block you will teleport to with the Etherwarp Transmission ability.")))
								.binding(defaults.uiAndVisuals.overlay.etherWarp,
										() -> current.uiAndVisuals.overlay.etherWarp,
										newValue -> current.uiAndVisuals.overlay.etherWarp = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Show Gyrokinetic Wand Radius"))
								.description(OptionDescription.of(
										Text.literal("Display an Overlay for the Gyrokinetic Wand radius.")))
								.binding(defaults.uiAndVisuals.overlay.gyrokineticWand,
										() -> current.uiAndVisuals.overlay.gyrokineticWand,
										newValue -> current.uiAndVisuals.overlay.gyrokineticWand = newValue)
								.controller(this::createBooleanController)
								.build())
						.build())
				.group(OptionGroup.createBuilder()
						.name(Text.literal("Party Chat").formatted(Formatting.BOLD))
						.description(OptionDescription.of(
								Text.literal("Party chat options.")))
						.collapsed(false)
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Enable Party Chat coloring"))
								.description(OptionDescription.of(
										Text.literal("Change the color of messages in Party chat.")))
								.binding(defaults.chat.chatParty.chatPartyColored,
										() -> current.chat.chatParty.chatPartyColored,
										newValue -> current.chat.chatParty.chatPartyColored = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Color>createBuilder()
								.name(Text.literal("Color of party messages"))
								.description(OptionDescription.of(
										Text.literal("Color to be applied to Party messages.")))
								.binding(defaults.chat.chatParty.chatPartyColor,
										() -> current.chat.chatParty.chatPartyColor,
										newValue -> current.chat.chatParty.chatPartyColor = newValue)
								.controller(ColorControllerBuilder::create)
								.build())
						.build())
				.group(OptionGroup.createBuilder()
						.name(Text.literal("Guild Chat").formatted(Formatting.BOLD))
						.description(OptionDescription.of(
								Text.literal("Guild chat options.")))
						.collapsed(false)
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Enable Guild Chat coloring"))
								.description(OptionDescription.of(
										Text.literal("Change the color of messages in Guild chat.")))
								.binding(defaults.chat.chatGuild.chatGuildColored,
										() -> current.chat.chatGuild.chatGuildColored,
										newValue -> current.chat.chatGuild.chatGuildColored = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Color>createBuilder()
								.name(Text.literal("Color of guild messages"))
								.description(OptionDescription.of(
										Text.literal("Color to be applied to Guild messages.")))
								.binding(defaults.chat.chatGuild.chatGuildColor,
										() -> current.chat.chatGuild.chatGuildColor,
										newValue -> current.chat.chatGuild.chatGuildColor = newValue)
								.controller(ColorControllerBuilder::create)
								.build())
						.build())
				.group(OptionGroup.createBuilder()
						.name(Text.literal("HUD").formatted(Formatting.BOLD))
						.description(OptionDescription.of(
								Text.literal("HUD on the screen")))
						.collapsed(false)
						.option(ButtonOption.createBuilder()
								.name(Text.literal("Changing HUD positions"))
								.text(Text.literal("Open"))
								.action((screen, opt) -> MinecraftClient.getInstance().setScreen(HudConfigScreen.create(screen)))
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Ping"))
								.description(OptionDescription.of(
										Text.literal("Display Ping in a Hud.")))
								.binding(defaults.uiAndVisuals.pingHud.enabled,
										() -> current.uiAndVisuals.pingHud.enabled,
										newValue -> current.uiAndVisuals.pingHud.enabled = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Fps"))
								.description(OptionDescription.of(
										Text.literal("Display FPS in a Hud.")))
								.binding(defaults.uiAndVisuals.fpsHud.enabled,
										() -> current.uiAndVisuals.fpsHud.enabled,
										newValue -> current.uiAndVisuals.fpsHud.enabled = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Tps"))
								.description(OptionDescription.of(
										Text.literal("Display TPS in a Hud.")))
								.binding(defaults.uiAndVisuals.tpsHud.enabled,
										() -> current.uiAndVisuals.tpsHud.enabled,
										newValue -> current.uiAndVisuals.tpsHud.enabled = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Day"))
								.description(OptionDescription.of(
										Text.literal("Display the current Day of the World in a Hud.")))
								.binding(defaults.uiAndVisuals.dayHud.enabled,
										() -> current.uiAndVisuals.dayHud.enabled,
										newValue -> current.uiAndVisuals.dayHud.enabled = newValue)
								.controller(this::createBooleanController)
								.build())
						.build())
				.build();
	}
}
