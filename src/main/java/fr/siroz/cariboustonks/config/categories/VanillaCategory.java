package fr.siroz.cariboustonks.config.categories;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.LabelOption;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.controller.FloatSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import fr.siroz.cariboustonks.config.Config;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

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
						.name(Text.literal("Display your own Nametag in F3"))
						.description(OptionDescription.of(
								Text.literal("Allows you to display your Nametag in F3 above your player.")))
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
								.name(Text.literal("Hiding food above inventory"))
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
						.name(Text.literal("Customize display of items on hand").formatted(Formatting.BOLD))
						.description(OptionDescription.of(
								Text.literal("Change the appearance of items in the hand and apply transformations.")))
						.collapsed(false)
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Enable item display customization"))
								.description(OptionDescription.of(
										Text.literal("Modify the appearance of items when held in the first person."),
										Text.literal(SPACE + "Modifies the position, size, swing animation and rotation of hand-held items."),
										Text.literal(SPACE + "For example: a Size of '0.4' with a Y Position of '0.2' (2.1x smaller, with a height of +2 pixels).")))
								.binding(defaults.vanilla.itemModelCustomization.enabled,
										() -> current.vanilla.itemModelCustomization.enabled,
										newValue -> current.vanilla.itemModelCustomization.enabled = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Integer>createBuilder()
								.name(Text.literal("Duration of Swing animation"))
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
						.option(LabelOption.create(Text.literal("| Main Hand").formatted(Formatting.BOLD)))
						.option(Option.<Float>createBuilder()
								.name(Text.literal("Size"))
								.description(OptionDescription.of(
										Text.literal("Size of item in hand.")))
								.binding(defaults.vanilla.itemModelCustomization.mainHand.scale,
										() -> current.vanilla.itemModelCustomization.mainHand.scale,
										newValue -> current.vanilla.itemModelCustomization.mainHand.scale = newValue)
								.controller(opt -> FloatSliderControllerBuilder.create(opt)
										.range(0.1f, 4f)
										.step(0.1f))
								.build())
						.option(Option.<Float>createBuilder()
								.name(Text.literal("Position X"))
								.description(OptionDescription.of(
										Text.literal("The X position of the item in hand.")))
								.binding(defaults.vanilla.itemModelCustomization.mainHand.x,
										() -> current.vanilla.itemModelCustomization.mainHand.x,
										newValue -> current.vanilla.itemModelCustomization.mainHand.x = newValue)
								.controller(opt -> FloatSliderControllerBuilder.create(opt)
										.range(-1.0f, 1.0f)
										.step(0.01f))
								.build())
						.option(Option.<Float>createBuilder()
								.name(Text.literal("Position Y"))
								.description(OptionDescription.of(
										Text.literal("The Y position of the item in hand.")))
								.binding(defaults.vanilla.itemModelCustomization.mainHand.y,
										() -> current.vanilla.itemModelCustomization.mainHand.y,
										newValue -> current.vanilla.itemModelCustomization.mainHand.y = newValue)
								.controller(opt -> FloatSliderControllerBuilder.create(opt)
										.range(-1.0f, 1.0f)
										.step(0.01f))
								.build())
						.option(Option.<Float>createBuilder()
								.name(Text.literal("Position Z"))
								.description(OptionDescription.of(
										Text.literal("The Z position of the item in hand.")))
								.binding(defaults.vanilla.itemModelCustomization.mainHand.z,
										() -> current.vanilla.itemModelCustomization.mainHand.z,
										newValue -> current.vanilla.itemModelCustomization.mainHand.z = newValue)
								.controller(opt -> FloatSliderControllerBuilder.create(opt)
										.range(-1.0f, 1.0f)
										.step(0.01f))
								.build())
						.option(LabelOption.create(Text.literal("| Off Hand").formatted(Formatting.BOLD)))
						.option(Option.<Float>createBuilder()
								.name(Text.literal("Size"))
								.description(OptionDescription.of(
										Text.literal("Size of item in hand.")))
								.binding(defaults.vanilla.itemModelCustomization.offHand.scale,
										() -> current.vanilla.itemModelCustomization.offHand.scale,
										newValue -> current.vanilla.itemModelCustomization.offHand.scale = newValue)
								.controller(opt -> FloatSliderControllerBuilder.create(opt)
										.range(0.1f, 4f)
										.step(0.1f))
								.build())
						.option(Option.<Float>createBuilder()
								.name(Text.literal("Position X"))
								.description(OptionDescription.of(
										Text.literal("The X position of the item in hand.")))
								.binding(defaults.vanilla.itemModelCustomization.offHand.x,
										() -> current.vanilla.itemModelCustomization.offHand.x,
										newValue -> current.vanilla.itemModelCustomization.offHand.x = newValue)
								.controller(opt -> FloatSliderControllerBuilder.create(opt)
										.range(-1.0f, 1.0f)
										.step(0.01f))
								.build())
						.option(Option.<Float>createBuilder()
								.name(Text.literal("Position Y"))
								.description(OptionDescription.of(
										Text.literal("The Y position of the item in hand.")))
								.binding(defaults.vanilla.itemModelCustomization.offHand.y,
										() -> current.vanilla.itemModelCustomization.offHand.y,
										newValue -> current.vanilla.itemModelCustomization.offHand.y = newValue)
								.controller(opt -> FloatSliderControllerBuilder.create(opt)
										.range(-1.0f, 1.0f)
										.step(0.01f))
								.build())
						.option(Option.<Float>createBuilder()
								.name(Text.literal("Position Z"))
								.description(OptionDescription.of(
										Text.literal("The Z position of the item in hand.")))
								.binding(defaults.vanilla.itemModelCustomization.offHand.z,
										() -> current.vanilla.itemModelCustomization.offHand.z,
										newValue -> current.vanilla.itemModelCustomization.offHand.z = newValue)
								.controller(opt -> FloatSliderControllerBuilder.create(opt)
										.range(-1.0f, 1.0f)
										.step(0.01f))
								.build())
						.build())
				.group(OptionGroup.createBuilder()
						.name(Text.literal("Sound").formatted(Formatting.BOLD))
						.description(OptionDescription.of(Text.literal("Mutes Vanilla Sounds.")))
						.collapsed(false)
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
