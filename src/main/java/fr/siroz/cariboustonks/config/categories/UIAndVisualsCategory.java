package fr.siroz.cariboustonks.config.categories;

import dev.isxander.yacl3.api.ButtonOption;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.LabelOption;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.controller.ColorControllerBuilder;
import fr.siroz.cariboustonks.config.Config;
import fr.siroz.cariboustonks.screen.HudConfigScreen;
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
						.option(LabelOption.create(Text.literal(" ")))
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
