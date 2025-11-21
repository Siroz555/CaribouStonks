package fr.siroz.cariboustonks.config.categories;

import dev.isxander.yacl3.api.ButtonOption;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.controller.ColorControllerBuilder;
import fr.siroz.cariboustonks.config.Config;
import fr.siroz.cariboustonks.screen.HudConfigScreen;
import fr.siroz.cariboustonks.util.render.animation.AnimationUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.awt.Color;

@SuppressWarnings("checkstyle:linelength")
public class UIAndVisualsCategory extends AbstractCategory {

	public UIAndVisualsCategory(Config defaults, Config current) {
		super(defaults, current);
	}

	@Override
	public ConfigCategory create() {
		return ConfigCategory.createBuilder()
				.name(Component.literal("UI & Visuals"))
				.tooltip(Component.literal("User Interface and Visual Settings"))
				.option(Option.<Boolean>createBuilder()
						.name(Component.literal("Beacon Beams through blocks"))
						.description(OptionDescription.of(
								Component.literal("If enabled, Beacon Beams will always be visible through blocks."),
								Component.literal(SPACE + "Requires a game restart to take effect.")))
						.binding(defaults.uiAndVisuals.beaconBeamWithNoDepthTest,
								() -> current.uiAndVisuals.beaconBeamWithNoDepthTest,
								newValue -> current.uiAndVisuals.beaconBeamWithNoDepthTest = newValue)
						.controller(this::createBooleanController)
						.build())
				.option(Option.<Boolean>createBuilder()
						.name(Component.literal("Highlight Selected Pet in the Pet Menu"))
						.description(OptionDescription.of(
								Component.literal("Highlight the current equipped pet in the Pet's Menu.")))
						.binding(defaults.uiAndVisuals.highlightSelectedPet,
								() -> current.uiAndVisuals.highlightSelectedPet,
								newValue -> current.uiAndVisuals.highlightSelectedPet = newValue)
						.controller(this::createBooleanController)
						.build())
				.group(OptionGroup.createBuilder()
						.name(Component.literal("Colored Enchantments").withStyle(ChatFormatting.BOLD))
						.description(OptionDescription.of(
								Component.literal("Colored enchantments in the item tooltips.")))
						.collapsed(false)
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Show max Enchantments"))
								.description(OptionDescription.of(
										Component.literal("Show max Enchantments with a color.")))
								.binding(defaults.uiAndVisuals.coloredEnchantment.showMaxEnchants,
										() -> current.uiAndVisuals.coloredEnchantment.showMaxEnchants,
										newValue -> current.uiAndVisuals.coloredEnchantment.showMaxEnchants = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Show max Enchantments in Rainbow"))
								.description(OptionDescription.of(
										Component.literal("Change the color of maxed enchantments to an" + SPACE),
										AnimationUtils.applyRainbow("animated Rainbow gradient o/"),
										Component.literal("(As an example)").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)))
								.binding(defaults.uiAndVisuals.coloredEnchantment.maxEnchantsRainbow,
										() -> current.uiAndVisuals.coloredEnchantment.maxEnchantsRainbow,
										newValue -> current.uiAndVisuals.coloredEnchantment.maxEnchantsRainbow = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Color>createBuilder()
								.name(Component.literal("Max Enchantments Color"))
								.description(OptionDescription.of(
										Component.literal("Change the color for the max Enchantments."),
										Component.literal(SPACE + "Warning: If the Rainbow is activated, the color will not be applied.").withStyle(ChatFormatting.YELLOW)))
								.binding(defaults.uiAndVisuals.coloredEnchantment.maxEnchantsColor,
										() -> current.uiAndVisuals.coloredEnchantment.maxEnchantsColor,
										newValue -> current.uiAndVisuals.coloredEnchantment.maxEnchantsColor = newValue)
								.controller(ColorControllerBuilder::create)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Show good Enchantments"))
								.description(OptionDescription.of(
										Component.literal("Show good Enchantments with a color.")))
								.binding(defaults.uiAndVisuals.coloredEnchantment.showGoodEnchants,
										() -> current.uiAndVisuals.coloredEnchantment.showGoodEnchants,
										newValue -> current.uiAndVisuals.coloredEnchantment.showGoodEnchants = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Color>createBuilder()
								.name(Component.literal("Good Enchantments Color"))
								.description(OptionDescription.of(
										Component.literal("Change the color for the good Enchantments.")))
								.binding(defaults.uiAndVisuals.coloredEnchantment.goodEnchantsColor,
										() -> current.uiAndVisuals.coloredEnchantment.goodEnchantsColor,
										newValue -> current.uiAndVisuals.coloredEnchantment.goodEnchantsColor = newValue)
								.controller(ColorControllerBuilder::create)
								.build())
						.build())
				.group(OptionGroup.createBuilder()
						.name(Component.literal("Coloring Tooltip Borders on Items").withStyle(ChatFormatting.BOLD))
						.description(OptionDescription.of(
								Component.literal("Control the coloring of tooltip borders on items, according to their rarity.")))
						.collapsed(false)
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Enable item border coloring"))
								.description(OptionDescription.of(
										Component.literal("Display colored borders on items, according to their rarity.")))
								.binding(defaults.uiAndVisuals.toolTipDecorator.enabled,
										() -> current.uiAndVisuals.toolTipDecorator.enabled,
										newValue -> current.uiAndVisuals.toolTipDecorator.enabled = newValue)
								.controller(this::createBooleanController)
								.build())
						.build())
				.group(OptionGroup.createBuilder()
						.name(Component.literal("Waypoints shared between players").withStyle(ChatFormatting.BOLD))
						.description(OptionDescription.of(
								Component.literal("Control Waypoint display when players share their position in Chat"),
								Component.literal("Use /sendCoords to share your Position in the Chat!").withStyle(ChatFormatting.GREEN)))
						.collapsed(false)
						.option(ButtonOption.createBuilder()
								.name(Component.literal("Share your Position"))
								.text(Component.literal("/sendCoords"))
								.description(OptionDescription.of(
										Component.literal("Use /sendCoords to share your Position in the Chat!").withStyle(ChatFormatting.GREEN)))
								.action((screen, buttonOption) -> {})
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Share your Position with Area"))
								.description(OptionDescription.of(
										Component.literal("When you share your position, this displays your coordinates and the area where you are.")))
								.binding(defaults.uiAndVisuals.sharedPositionWaypoint.shareWithArea,
										() -> current.uiAndVisuals.sharedPositionWaypoint.shareWithArea,
										newValue -> current.uiAndVisuals.sharedPositionWaypoint.shareWithArea = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Enable Waypoint display"))
								.description(OptionDescription.of(
										Component.literal("When a player shares his position in the chat, a Waypoint is created."),
										Component.literal(SPACE + "This is automatic, no interaction is required.").withStyle(ChatFormatting.ITALIC)))
								.binding(defaults.uiAndVisuals.sharedPositionWaypoint.enabled,
										() -> current.uiAndVisuals.sharedPositionWaypoint.enabled,
										newValue -> current.uiAndVisuals.sharedPositionWaypoint.enabled = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Integer>createBuilder()
								.name(Component.literal("Waypoint display time"))
								.description(OptionDescription.of(
										Component.literal("Controls the waypoint display time in seconds.")))
								.binding(defaults.uiAndVisuals.sharedPositionWaypoint.showTime,
										() -> current.uiAndVisuals.sharedPositionWaypoint.showTime,
										newValue -> current.uiAndVisuals.sharedPositionWaypoint.showTime = newValue)
								.controller(opt -> createIntegerSecondesController(opt, 256))
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Waypoint in Rainbow mode"))
								.description(OptionDescription.of(
										Component.literal("If enabled, the Waypoint displayed will be in Rainbow mode. Deactivating this option allows you to choose the color in the next option.")))
								.binding(defaults.uiAndVisuals.sharedPositionWaypoint.rainbow,
										() -> current.uiAndVisuals.sharedPositionWaypoint.rainbow,
										newValue -> current.uiAndVisuals.sharedPositionWaypoint.rainbow = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Color>createBuilder()
								.name(Component.literal("Waypoint color"))
								.description(OptionDescription.of(
										Component.literal("If the Waypoint is not in Rainbow mode, selects the Waypoint color.")))
								.binding(defaults.uiAndVisuals.sharedPositionWaypoint.color,
										() -> current.uiAndVisuals.sharedPositionWaypoint.color,
										newValue -> current.uiAndVisuals.sharedPositionWaypoint.color = newValue)
								.controller(ColorControllerBuilder::create)
								.build())
						.build())
				.group(OptionGroup.createBuilder()
						.name(Component.literal("Overlay").withStyle(ChatFormatting.BOLD))
						.collapsed(false)
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Show Etherwarp Teleport Target"))
								.description(OptionDescription.of(
										Component.literal("Shows the block you will teleport to with the Etherwarp Transmission ability.")))
								.binding(defaults.uiAndVisuals.overlay.etherWarp,
										() -> current.uiAndVisuals.overlay.etherWarp,
										newValue -> current.uiAndVisuals.overlay.etherWarp = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Show Gyrokinetic Wand Radius"))
								.description(OptionDescription.of(
										Component.literal("Display an Overlay for the Gyrokinetic Wand radius.")))
								.binding(defaults.uiAndVisuals.overlay.gyrokineticWand,
										() -> current.uiAndVisuals.overlay.gyrokineticWand,
										newValue -> current.uiAndVisuals.overlay.gyrokineticWand = newValue)
								.controller(this::createBooleanController)
								.build())
						.build())
				.group(OptionGroup.createBuilder()
						.name(Component.literal("Party Chat").withStyle(ChatFormatting.BOLD))
						.description(OptionDescription.of(
								Component.literal("Party chat options.")))
						.collapsed(false)
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Enable Party Chat coloring"))
								.description(OptionDescription.of(
										Component.literal("Change the color of messages in Party chat.")))
								.binding(defaults.chat.chatParty.chatPartyColored,
										() -> current.chat.chatParty.chatPartyColored,
										newValue -> current.chat.chatParty.chatPartyColored = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Color>createBuilder()
								.name(Component.literal("Color of party messages"))
								.description(OptionDescription.of(
										Component.literal("Color to be applied to Party messages.")))
								.binding(defaults.chat.chatParty.chatPartyColor,
										() -> current.chat.chatParty.chatPartyColor,
										newValue -> current.chat.chatParty.chatPartyColor = newValue)
								.controller(ColorControllerBuilder::create)
								.build())
						.build())
				.group(OptionGroup.createBuilder()
						.name(Component.literal("Guild Chat").withStyle(ChatFormatting.BOLD))
						.description(OptionDescription.of(
								Component.literal("Guild chat options.")))
						.collapsed(false)
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Enable Guild Chat coloring"))
								.description(OptionDescription.of(
										Component.literal("Change the color of messages in Guild chat.")))
								.binding(defaults.chat.chatGuild.chatGuildColored,
										() -> current.chat.chatGuild.chatGuildColored,
										newValue -> current.chat.chatGuild.chatGuildColored = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Color>createBuilder()
								.name(Component.literal("Color of guild messages"))
								.description(OptionDescription.of(
										Component.literal("Color to be applied to Guild messages.")))
								.binding(defaults.chat.chatGuild.chatGuildColor,
										() -> current.chat.chatGuild.chatGuildColor,
										newValue -> current.chat.chatGuild.chatGuildColor = newValue)
								.controller(ColorControllerBuilder::create)
								.build())
						.build())
				.group(OptionGroup.createBuilder()
						.name(Component.literal("HUD").withStyle(ChatFormatting.BOLD))
						.description(OptionDescription.of(
								Component.literal("HUD on the screen")))
						.collapsed(false)
						.option(ButtonOption.createBuilder()
								.name(Component.literal("Changing HUD positions"))
								.text(Component.literal("Open"))
								.action((screen, opt) -> Minecraft.getInstance().setScreen(HudConfigScreen.create(screen)))
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Ping"))
								.description(OptionDescription.of(
										Component.literal("Display Ping in a Hud.")))
								.binding(defaults.uiAndVisuals.pingHud.enabled,
										() -> current.uiAndVisuals.pingHud.enabled,
										newValue -> current.uiAndVisuals.pingHud.enabled = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Fps"))
								.description(OptionDescription.of(
										Component.literal("Display FPS in a Hud.")))
								.binding(defaults.uiAndVisuals.fpsHud.enabled,
										() -> current.uiAndVisuals.fpsHud.enabled,
										newValue -> current.uiAndVisuals.fpsHud.enabled = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Tps"))
								.description(OptionDescription.of(
										Component.literal("Display TPS in a Hud.")))
								.binding(defaults.uiAndVisuals.tpsHud.enabled,
										() -> current.uiAndVisuals.tpsHud.enabled,
										newValue -> current.uiAndVisuals.tpsHud.enabled = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Day"))
								.description(OptionDescription.of(
										Component.literal("Display the current Day of the World in a Hud.")))
								.binding(defaults.uiAndVisuals.dayHud.enabled,
										() -> current.uiAndVisuals.dayHud.enabled,
										newValue -> current.uiAndVisuals.dayHud.enabled = newValue)
								.controller(this::createBooleanController)
								.build())
						.build())
				.build();
	}
}
