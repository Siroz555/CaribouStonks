package fr.siroz.cariboustonks.config.categories;

import dev.isxander.yacl3.api.ButtonOption;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.controller.ColorControllerBuilder;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import fr.siroz.cariboustonks.config.Config;
import fr.siroz.cariboustonks.screen.HudConfigScreen;
import fr.siroz.cariboustonks.screen.mobtracking.MobTrackingScreen;
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
				.group(OptionGroup.createBuilder()
						.name(Text.literal("Mob Tracking").formatted(Formatting.BOLD).append(BETA))
						.description(OptionDescription.of(
								Text.literal("Mob Tracking combines features that allow you to view information about a specific Mob in real time."),
								Text.literal(SPACE + "Allow the display of mob health and other information in a custom Boss Bar or via a HUD. Also allows you to be alerted when a mob spawns."),
								Text.literal(SPACE + "These features are in BETA. Adjustments will be made.").formatted(Formatting.RED)))
						.collapsed(false)
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Enable Mob Tracking"))
								.description(OptionDescription.of(
										Text.literal("Once activated, mobs such as Slayers, rare fishing mobs, and others will be displayed in the form of a Boss Bar or HUD."),
										Text.literal(SPACE + "The display shows the life and other information of the mob being tracked in real time."),
										Text.literal(SPACE + "Each mob can be activated or deactivated, display an alert, in the dedicated menu."),
										Text.literal(SPACE + "These features are in BETA. Adjustments will be made.").formatted(Formatting.RED)))
								.binding(defaults.uiAndVisuals.mobTracking.enabled,
										() -> current.uiAndVisuals.mobTracking.enabled,
										newValue -> current.uiAndVisuals.mobTracking.enabled = newValue)
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
