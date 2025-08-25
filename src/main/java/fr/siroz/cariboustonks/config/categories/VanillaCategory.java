package fr.siroz.cariboustonks.config.categories;

import dev.isxander.yacl3.api.ButtonOption;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import fr.siroz.cariboustonks.config.Config;
import fr.siroz.cariboustonks.screen.HeldItemViewConfigScreen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;

@SuppressWarnings("checkstyle:linelength")
public class VanillaCategory extends AbstractCategory {

	public VanillaCategory(Config defaults, Config current) {
		super(defaults, current);
	}

	@Override
	public ConfigCategory create() {
		return ConfigCategory.createBuilder()
				.name(Text.literal("Vanilla"))
				.tooltip(Text.literal("Minecraft Vanilla-related settings"))
				.option(Option.<Boolean>createBuilder()
						.name(Text.literal("Hide world loading screen"))
						.description(OptionDescription.of(
								Text.literal("Hides the screen displayed when loading in worlds.")))
						.binding(defaults.vanilla.hideWorldLoadingScreen,
								() -> current.vanilla.hideWorldLoadingScreen,
								newValue -> current.vanilla.hideWorldLoadingScreen = newValue)
						.controller(this::createBooleanController)
						.build())
				.option(Option.<Boolean>createBuilder()
						.name(Text.literal("Hide Toast Tutorial"))
						.description(OptionDescription.of(
								Text.literal("Hides Popup Tutorials that appear at the top of the screen as Toast.")))
						.binding(defaults.vanilla.hideTutorialsToast,
								() -> current.vanilla.hideTutorialsToast,
								newValue -> current.vanilla.hideTutorialsToast = newValue)
						.controller(this::createBooleanController)
						.build())
				.option(Option.<Boolean>createBuilder()
						.name(Text.literal("Stop the cursor reset positions"))
						.description(OptionDescription.of(
								Text.literal("Prevents the mouse cursor position from being reset between different GUIs, such as chests or other screens.")))
						.binding(defaults.vanilla.stopCursorResetPosition,
								() -> current.vanilla.stopCursorResetPosition,
								newValue -> current.vanilla.stopCursorResetPosition = newValue)
						.controller(this::createBooleanController)
						.build())
				.option(Option.<Boolean>createBuilder()
						.name(Text.literal("Display your own Nametag in F5"))
						.description(OptionDescription.of(
								Text.literal("Allows you to display your Nametag in F5 above your player.")))
						.binding(defaults.vanilla.displayOwnNametagUsername,
								() -> current.vanilla.displayOwnNametagUsername,
								newValue -> current.vanilla.displayOwnNametagUsername = newValue)
						.controller(this::createBooleanController)
						.build())
				.group(OptionGroup.createBuilder()
						.name(Text.literal("Overlay").formatted(Formatting.BOLD))
						.description(OptionDescription.of(
								Text.literal("Overlay-related settings")))
						.collapsed(false)
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Hide fire on screen"))
								.description(OptionDescription.of(
										Text.literal("Prevents the overlay of fire from being visible while you're burning.")))
								.binding(defaults.vanilla.overlay.hideFireOverlay,
										() -> current.vanilla.overlay.hideFireOverlay,
										newValue -> current.vanilla.overlay.hideFireOverlay = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Hide armor above inventory"))
								.description(OptionDescription.of(
										Text.literal("Hides the armor level displayed above the inventory, above the health.")))
								.binding(defaults.vanilla.overlay.hideArmorOverlay,
										() -> current.vanilla.overlay.hideArmorOverlay,
										newValue -> current.vanilla.overlay.hideArmorOverlay = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Hide food above inventory"))
								.description(OptionDescription.of(
										Text.literal("Hides the food level displayed above the inventory.")))
								.binding(defaults.vanilla.overlay.hideFoodOverlay,
										() -> current.vanilla.overlay.hideFoodOverlay,
										newValue -> current.vanilla.overlay.hideFoodOverlay = newValue)
								.controller(this::createBooleanController)
								.build())
						.build())
				.group(OptionGroup.createBuilder()
						.name(Text.literal("Mobs").formatted(Formatting.BOLD))
						.description(OptionDescription.of(
								Text.literal("Mobs-related settings")))
						.collapsed(false)
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Hide fire on entities"))
								.description(OptionDescription.of(
										Text.literal("Prevents fire on entities from being rendered. For example, for the Flaming Spiders in Crimson Isle.")))
								.binding(defaults.vanilla.mob.hideFireOnEntities,
										() -> current.vanilla.mob.hideFireOnEntities,
										newValue -> current.vanilla.mob.hideFireOnEntities = newValue)
								.controller(this::createBooleanController)
								.build())
						.build())
				.group(OptionGroup.createBuilder()
						.name(Text.literal("Customize Held Item Appearance").formatted(Formatting.BOLD))
						.description(OptionDescription.of(
								Text.literal("Change the appearance of items in the hand and apply transformations.")))
						.collapsed(false)
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Enable Held Item customization"))
								.description(OptionDescription.of(
										Text.literal("Modify the appearance of items when held in the first person.")))
								.binding(defaults.vanilla.itemModelCustomization.enabled,
										() -> current.vanilla.itemModelCustomization.enabled,
										newValue -> current.vanilla.itemModelCustomization.enabled = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(ButtonOption.createBuilder()
								.name(Text.literal("Customize > Main Hand"))
								.text(Text.literal("Open customization screen"))
								.action((screen, opt) -> openScreen(HeldItemViewConfigScreen.create(screen, Hand.MAIN_HAND)))
								.build())
						.option(ButtonOption.createBuilder()
								.name(Text.literal("Customize > Off Hand"))
								.text(Text.literal("Open customization screen"))
								.action((screen, opt) -> openScreen(HeldItemViewConfigScreen.create(screen, Hand.OFF_HAND)))
								.build())
						.option(Option.<Integer>createBuilder()
								.name(Text.literal("Duration of Swing Animation"))
								.description(OptionDescription.of(
										Text.literal("Duration of item swing animation. 6 is the default/vanilla time.")))
								.binding(defaults.vanilla.itemModelCustomization.swingDuration,
										() -> current.vanilla.itemModelCustomization.swingDuration,
										newValue -> current.vanilla.itemModelCustomization.swingDuration = newValue)
								.controller(opt -> IntegerSliderControllerBuilder.create(opt).range(1, 20).step(1))
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Ignore Mining Effects"))
								.description(OptionDescription.of(
										Text.literal("Ignores effects such as Haste and Mining Fatigue when Swing animation duration is modified.")))
								.binding(defaults.vanilla.itemModelCustomization.ignoreMiningEffects,
										() -> current.vanilla.itemModelCustomization.ignoreMiningEffects,
										newValue -> current.vanilla.itemModelCustomization.ignoreMiningEffects = newValue)
								.controller(this::createBooleanController)
								.build())
						.build())
				.group(OptionGroup.createBuilder()
						.name(Text.literal("Sound").formatted(Formatting.BOLD))
						.description(OptionDescription.of(Text.literal("Mutes Vanilla Sounds.")))
						.collapsed(false)
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Mute Player Fall"))
								.description(OptionDescription.of(
										Text.literal("Mute sounds from player fall.")))
								.binding(defaults.vanilla.sound.mutePlayerFall,
										() -> current.vanilla.sound.mutePlayerFall,
										newValue -> current.vanilla.sound.mutePlayerFall = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Mute Enderman"))
								.description(OptionDescription.of(
										Text.literal("Mute the Scream & Stare sounds from Enderman.")))
								.binding(defaults.vanilla.sound.muteEnderman,
										() -> current.vanilla.sound.muteEnderman,
										newValue -> current.vanilla.sound.muteEnderman = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Mute Phantom"))
								.description(OptionDescription.of(
										Text.literal("Mute sounds from Phantoms.")))
								.binding(defaults.vanilla.sound.mutePhantom,
										() -> current.vanilla.sound.mutePhantom,
										newValue -> current.vanilla.sound.mutePhantom = newValue)
								.controller(this::createBooleanController)
								.build())
						.build())
				.build();
	}
}
