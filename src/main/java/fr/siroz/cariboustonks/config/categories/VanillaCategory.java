package fr.siroz.cariboustonks.config.categories;

import dev.isxander.yacl3.api.ButtonOption;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import fr.siroz.cariboustonks.config.Config;
import fr.siroz.cariboustonks.screens.HeldItemViewConfigScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;

@SuppressWarnings("checkstyle:linelength")
public class VanillaCategory extends AbstractCategory {

	public VanillaCategory(Config defaults, Config current) {
		super(defaults, current);
	}

	@Override
	public ConfigCategory create() {
		return ConfigCategory.createBuilder()
				.name(Component.literal("Vanilla"))
				.tooltip(Component.literal("Minecraft Vanilla-related settings"))
				.option(Option.<Boolean>createBuilder()
						.name(Component.literal("Hide world loading screen"))
						.description(OptionDescription.of(
								Component.literal("Hides the screen displayed when loading in worlds.")))
						.binding(defaults.vanilla.hideWorldLoadingScreen,
								() -> current.vanilla.hideWorldLoadingScreen,
								newValue -> current.vanilla.hideWorldLoadingScreen = newValue)
						.controller(this::createBooleanController)
						.build())
				.option(Option.<Boolean>createBuilder()
						.name(Component.literal("Hide Toast Tutorial"))
						.description(OptionDescription.of(
								Component.literal("Hides Popup Tutorials that appear at the top of the screen as Toast.")))
						.binding(defaults.vanilla.hideTutorialsToast,
								() -> current.vanilla.hideTutorialsToast,
								newValue -> current.vanilla.hideTutorialsToast = newValue)
						.controller(this::createBooleanController)
						.build())
				.option(Option.<Boolean>createBuilder()
						.name(Component.literal("Stop the cursor reset positions"))
						.description(OptionDescription.of(
								Component.literal("Prevents the mouse cursor position from being reset between different GUIs, such as chests or other screens.")))
						.binding(defaults.vanilla.stopCursorResetPosition,
								() -> current.vanilla.stopCursorResetPosition,
								newValue -> current.vanilla.stopCursorResetPosition = newValue)
						.controller(this::createBooleanController)
						.build())
				.option(Option.<Boolean>createBuilder()
						.name(Component.literal("Stop the FPS Limiter"))
						.description(OptionDescription.of(
								Component.literal("Prevents the FPS Limiter from limiting the game's FPS.")))
						.binding(defaults.vanilla.stopFpsLimiter,
								() -> current.vanilla.stopFpsLimiter,
								newValue -> current.vanilla.stopFpsLimiter = newValue)
						.controller(this::createBooleanController)
						.build())
				.option(Option.<Boolean>createBuilder()
						.name(Component.literal("Display your own Nametag in F5"))
						.description(OptionDescription.of(
								Component.literal("Allows you to display your Nametag in F5 above your player.")))
						.binding(defaults.vanilla.displayOwnNametagUsername,
								() -> current.vanilla.displayOwnNametagUsername,
								newValue -> current.vanilla.displayOwnNametagUsername = newValue)
						.controller(this::createBooleanController)
						.build())
				.group(OptionGroup.createBuilder()
						.name(Component.literal("Zoom").withStyle(ChatFormatting.BOLD))
						.description(OptionDescription.of(
								Component.literal("In-game Zoom Options")))
						.collapsed(false)
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Zoom"))
								.description(OptionDescription.of(
										Component.literal("If enabled, hold a key to toggle the zoom in-game. (Default to 'C')"),
										Component.literal(SPACE + "You can change the Key Bind in the Minecraft Options.").withStyle(ChatFormatting.ITALIC)))
								.binding(defaults.vanilla.zoom.enabled,
										() -> current.vanilla.zoom.enabled,
										newValue -> current.vanilla.zoom.enabled = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(this::shortcutToKeybindsOptions)
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Zoom Scrolling"))
								.description(OptionDescription.of(
										Component.literal("If enabled, the mouse wheel can be used to zoom in-game.")))
								.binding(defaults.vanilla.zoom.mouseScrolling,
										() -> current.vanilla.zoom.mouseScrolling,
										newValue -> current.vanilla.zoom.mouseScrolling = newValue)
								.controller(this::createBooleanController)
								.build())
						.build())
				.group(OptionGroup.createBuilder()
						.name(Component.literal("Scrollable Tooltips").withStyle(ChatFormatting.BOLD))
						.description(OptionDescription.of(
								Component.literal("Options for scrolling Tooltips on items")))
						.collapsed(false)
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Enable Tooltip Scrolling"))
								.description(OptionDescription.of(
										Component.literal("When enabled, item tooltips can be scrolled using the mouse wheel."),
										Component.literal(SPACE + "Scrolling up moves the tooltip upwards, scrolling down moves it downwards."),
										Component.literal(SPACE + "Hold SHIFT while scrolling to move the tooltip horizontally (left or right)."),
										Component.literal(SPACE + "[!] Attention: This feature may not work correctly with certain other mods. Please be aware of potential compatibility issues.").withStyle(ChatFormatting.RED)))
								.binding(defaults.vanilla.scrollableTooltip.enabled,
										() -> current.vanilla.scrollableTooltip.enabled,
										newValue -> current.vanilla.scrollableTooltip.enabled = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Reverse Scroll"))
								.description(OptionDescription.of(
										Component.literal("If enabled, the Scrolling is reversed.")))
								.binding(defaults.vanilla.scrollableTooltip.reverseScroll,
										() -> current.vanilla.scrollableTooltip.reverseScroll,
										newValue -> current.vanilla.scrollableTooltip.reverseScroll = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Start displaying Tooltips from the TOP"))
								.description(OptionDescription.of(
										Component.literal("If enabled, allows Tooltips for items to be displayed starting from the TOP.")))
								.binding(defaults.vanilla.scrollableTooltip.startOnTop,
										() -> current.vanilla.scrollableTooltip.startOnTop,
										newValue -> current.vanilla.scrollableTooltip.startOnTop = newValue)
								.controller(this::createBooleanController)
								.build())
						.build())
				.group(OptionGroup.createBuilder()
						.name(Component.literal("Overlay").withStyle(ChatFormatting.BOLD))
						.description(OptionDescription.of(
								Component.literal("Overlay-related settings")))
						.collapsed(false)
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Hide fire on screen"))
								.description(OptionDescription.of(
										Component.literal("Prevents the overlay of fire from being visible while you're burning.")))
								.binding(defaults.vanilla.overlay.hideFireOverlay,
										() -> current.vanilla.overlay.hideFireOverlay,
										newValue -> current.vanilla.overlay.hideFireOverlay = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Hide armor above inventory"))
								.description(OptionDescription.of(
										Component.literal("Hides the armor level displayed above the inventory, above the health.")))
								.binding(defaults.vanilla.overlay.hideArmorOverlay,
										() -> current.vanilla.overlay.hideArmorOverlay,
										newValue -> current.vanilla.overlay.hideArmorOverlay = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Hide food above inventory"))
								.description(OptionDescription.of(
										Component.literal("Hides the food level displayed above the inventory.")))
								.binding(defaults.vanilla.overlay.hideFoodOverlay,
										() -> current.vanilla.overlay.hideFoodOverlay,
										newValue -> current.vanilla.overlay.hideFoodOverlay = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Hide Status Effects (Potions Effects)"))
								.description(OptionDescription.of(
										Component.literal("Hides Status Effects (Potions Effects) displayed next to the inventory, or at the top of the screen.")))
								.binding(defaults.vanilla.overlay.hideStatusEffectsOverlay,
										() -> current.vanilla.overlay.hideStatusEffectsOverlay,
										newValue -> current.vanilla.overlay.hideStatusEffectsOverlay = newValue)
								.controller(this::createBooleanController)
								.build())
						.build())
				.group(OptionGroup.createBuilder()
						.name(Component.literal("Customize Held Item Appearance").withStyle(ChatFormatting.BOLD))
						.description(OptionDescription.of(
								Component.literal("Change the appearance of items in the hand and apply transformations.")))
						.collapsed(false)
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Enable Held Item customization"))
								.description(OptionDescription.of(
										Component.literal("Modify the appearance of items when held in the first person.")))
								.binding(defaults.vanilla.itemModelCustomization.enabled,
										() -> current.vanilla.itemModelCustomization.enabled,
										newValue -> current.vanilla.itemModelCustomization.enabled = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(ButtonOption.createBuilder()
								.name(Component.literal("Customize > Main Hand"))
								.text(Component.literal("Open customization screen"))
								.action((screen, opt) -> openScreen(HeldItemViewConfigScreen.create(screen, InteractionHand.MAIN_HAND)))
								.build())
						.option(ButtonOption.createBuilder()
								.name(Component.literal("Customize > Off Hand"))
								.text(Component.literal("Open customization screen"))
								.action((screen, opt) -> openScreen(HeldItemViewConfigScreen.create(screen, InteractionHand.OFF_HAND)))
								.build())
						.option(Option.<Integer>createBuilder()
								.name(Component.literal("Duration of Swing Animation"))
								.description(OptionDescription.of(
										Component.literal("Duration of item swing animation. 6 is the default/vanilla time.")))
								.binding(defaults.vanilla.itemModelCustomization.swingDuration,
										() -> current.vanilla.itemModelCustomization.swingDuration,
										newValue -> current.vanilla.itemModelCustomization.swingDuration = newValue)
								.controller(opt -> IntegerSliderControllerBuilder.create(opt).range(1, 20).step(1))
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Ignore Mining Effects"))
								.description(OptionDescription.of(
										Component.literal("Ignores effects such as Haste and Mining Fatigue when Swing animation duration is modified.")))
								.binding(defaults.vanilla.itemModelCustomization.ignoreMiningEffects,
										() -> current.vanilla.itemModelCustomization.ignoreMiningEffects,
										newValue -> current.vanilla.itemModelCustomization.ignoreMiningEffects = newValue)
								.controller(this::createBooleanController)
								.build())
						.build())
				.group(OptionGroup.createBuilder()
						.name(Component.literal("Chat").withStyle(ChatFormatting.BOLD))
						.description(OptionDescription.of(Component.literal("Chat-related settings.")))
						.collapsed(false)
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Copy a message from the chat"))
								.description(OptionDescription.of(
										Component.literal("Copy a message from the chat by clicking on it using"),
										Component.literal(SPACE + "CTRL + CLICK").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD)))
								.binding(defaults.chat.copyChat,
										() -> current.chat.copyChat,
										newValue -> current.chat.copyChat = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Integer>createBuilder()
								.name(Component.literal("Chat history length"))
								.description(OptionDescription.of(
										Component.literal("Modify the maximum length of the chat history")))
								.binding(defaults.chat.chatHistoryLength,
										() -> current.chat.chatHistoryLength,
										newValue -> current.chat.chatHistoryLength = Math.max(100, newValue))
								.controller(opt -> IntegerSliderControllerBuilder.create(opt).range(100, 5000).step(10))
								.build())
						.build())
				.group(OptionGroup.createBuilder()
						.name(Component.literal("Mobs").withStyle(ChatFormatting.BOLD))
						.description(OptionDescription.of(
								Component.literal("Mobs-related settings")))
						.collapsed(false)
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Hide fire on entities"))
								.description(OptionDescription.of(
										Component.literal("Prevents fire on entities from being rendered. For example, for the Flaming Spiders in Crimson Isle.")))
								.binding(defaults.vanilla.mob.hideFireOnEntities,
										() -> current.vanilla.mob.hideFireOnEntities,
										newValue -> current.vanilla.mob.hideFireOnEntities = newValue)
								.controller(this::createBooleanController)
								.build())
						.build())
				.group(OptionGroup.createBuilder()
						.name(Component.literal("Sound").withStyle(ChatFormatting.BOLD))
						.description(OptionDescription.of(Component.literal("Mutes Vanilla Sounds.")))
						.collapsed(false)
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Mute Lightning"))
								.description(OptionDescription.of(
										Component.literal("Mute the Lightning Thunder and Lightning Impact sounds.")))
								.binding(defaults.vanilla.sound.muteLightning,
										() -> current.vanilla.sound.muteLightning,
										newValue -> current.vanilla.sound.muteLightning = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Mute Player Fall"))
								.description(OptionDescription.of(
										Component.literal("Mute sounds from player fall.")))
								.binding(defaults.vanilla.sound.mutePlayerFall,
										() -> current.vanilla.sound.mutePlayerFall,
										newValue -> current.vanilla.sound.mutePlayerFall = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Mute Enderman"))
								.description(OptionDescription.of(
										Component.literal("Mute the Scream & Stare sounds from Enderman.")))
								.binding(defaults.vanilla.sound.muteEnderman,
										() -> current.vanilla.sound.muteEnderman,
										newValue -> current.vanilla.sound.muteEnderman = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Mute Phantom"))
								.description(OptionDescription.of(
										Component.literal("Mute sounds from Phantoms.")))
								.binding(defaults.vanilla.sound.mutePhantom,
										() -> current.vanilla.sound.mutePhantom,
										newValue -> current.vanilla.sound.mutePhantom = newValue)
								.controller(this::createBooleanController)
								.build())
						.build())
				.build();
	}
}
