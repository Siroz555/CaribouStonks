package fr.siroz.cariboustonks.config.categories;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.controller.ColorControllerBuilder;
import fr.siroz.cariboustonks.config.Config;
import java.awt.Color;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

@SuppressWarnings("checkstyle:linelength")
public class DungeonsCategory extends AbstractCategory {

	public DungeonsCategory(Config defaults, Config current) {
		super(defaults, current);
	}

	@Override
	public ConfigCategory create() {
		return ConfigCategory.createBuilder()
				.name(Component.literal("Dungeons"))
				.tooltip(Component.literal("Instances-related Settings"))
				.group(OptionGroup.createBuilder()
						.name(Component.literal("The Catacombs").withStyle(ChatFormatting.BOLD))
						.description(OptionDescription.of(
								Component.literal("Settings for The Catacombs Dungeon.")))
						.collapsed(false)
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Thorn - F4/M4 - Spirit Bear State"))
								.description(OptionDescription.of(
										Component.literal("Displays the current state of the Spirit Bear. Showing Kills, Spawn Timer and Alive state."),
										Component.literal(SPACE + "Adapted according to the server's TPS.").withStyle(ChatFormatting.AQUA)))
								.binding(defaults.instance.theCatacombs.bossThornSpiritBearTimers,
										() -> current.instance.theCatacombs.bossThornSpiritBearTimers,
										newValue -> current.instance.theCatacombs.bossThornSpiritBearTimers = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Sadan - F6/M6 - Terracotta Timers"))
								.description(OptionDescription.of(
										Component.literal("Displays timers for Terracotta spawn and respawn."),
										Component.literal(SPACE + "Adapted according to the server's TPS.").withStyle(ChatFormatting.AQUA)))
								.binding(defaults.instance.theCatacombs.bossSadanTerracottaTimers,
										() -> current.instance.theCatacombs.bossSadanTerracottaTimers,
										newValue -> current.instance.theCatacombs.bossSadanTerracottaTimers = newValue)
								.controller(this::createBooleanController)
								.build())
						.build())
				.group(OptionGroup.createBuilder()
						.name(Component.literal("Wither King - M7").withStyle(ChatFormatting.BOLD))
						.description(OptionDescription.of(
								Component.literal("Settings for the Wither King phase (P5) in the Master Mode Catacombs Floor 7. (M7)")))
						.collapsed(false)
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Wither King Dragons - Spawn Title"))
								.description(OptionDescription.of(
										Component.literal("Displays a Title and plays a sound when a Dragon spawns.")))
								.binding(defaults.instance.theCatacombs.witherKing.showSpawnTitle,
										() -> current.instance.theCatacombs.witherKing.showSpawnTitle,
										newValue -> current.instance.theCatacombs.witherKing.showSpawnTitle = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Wither King Dragons - Spawn Message"))
								.description(OptionDescription.of(
										Component.literal("Displays a message in the chat when a Dragon spawns.")))
								.binding(defaults.instance.theCatacombs.witherKing.showSpawnMessage,
										() -> current.instance.theCatacombs.witherKing.showSpawnMessage,
										newValue -> current.instance.theCatacombs.witherKing.showSpawnMessage = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Wither King Dragons - Spawn Time"))
								.description(OptionDescription.of(
										Component.literal("Displays timers to warn of a Dragon spawn."),
										Component.literal(SPACE + "Adapted according to the server's TPS.").withStyle(ChatFormatting.AQUA)))
								.binding(defaults.instance.theCatacombs.witherKing.showSpawnTime,
										() -> current.instance.theCatacombs.witherKing.showSpawnTime,
										newValue -> current.instance.theCatacombs.witherKing.showSpawnTime = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Wither King Dragons - Bounding Boxes"))
								.description(OptionDescription.of(
										Component.literal("Displays the Dragons' colored boxes")))
								.binding(defaults.instance.theCatacombs.witherKing.showDragBoundingBox,
										() -> current.instance.theCatacombs.witherKing.showDragBoundingBox,
										newValue -> current.instance.theCatacombs.witherKing.showDragBoundingBox = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Wither King Dragons - Target Line"))
								.description(OptionDescription.of(
										Component.literal("Displays a line from your cursor to the dragon that will spawn.")))
								.binding(defaults.instance.theCatacombs.witherKing.showDragTargetLine,
										() -> current.instance.theCatacombs.witherKing.showDragTargetLine,
										newValue -> current.instance.theCatacombs.witherKing.showDragTargetLine = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Wither King Dragons - Last Breath Target"))
								.description(OptionDescription.of(
										Component.literal("Displays a circle that allows you to aim at the dragon that will spawn, having a Last Breath."),
										Component.literal(SPACE + "Considering that you are focused on the Obsidian block.").withStyle(ChatFormatting.ITALIC)))
								.binding(defaults.instance.theCatacombs.witherKing.showLastBreathTarget,
										() -> current.instance.theCatacombs.witherKing.showLastBreathTarget,
										newValue -> current.instance.theCatacombs.witherKing.showLastBreathTarget = newValue)
								.controller(this::createBooleanController)
								.build())
						.build())
				.group(OptionGroup.createBuilder()
						.name(Component.literal("Croesus").withStyle(ChatFormatting.BOLD))
						.description(OptionDescription.of(
								Component.literal("Croesus Helpers")))
						.collapsed(false)
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Croesus - Highlight Opened Chests"))
								.description(OptionDescription.of(
										Component.literal("Highlights the chests you opened in the Croesus.")))
								.binding(defaults.instance.croesus.mainMenuOpenedChest,
										() -> current.instance.croesus.mainMenuOpenedChest,
										newValue -> current.instance.croesus.mainMenuOpenedChest = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Color>createBuilder()
								.name(Component.literal("Croesus - Highlight Opened Chests - color"))
								.description(OptionDescription.of(
										Component.literal("Change the highlight color of the chests you opened in the Croesus.")))
								.binding(defaults.instance.croesus.mainMenuOpenedChestColor,
										() -> current.instance.croesus.mainMenuOpenedChestColor,
										newValue -> current.instance.croesus.mainMenuOpenedChestColor = newValue)
								.controller(ColorControllerBuilder::create)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Croesus - Highlight Kismet Feather Available"))
								.description(OptionDescription.of(
										Component.literal("Highlight where Kismet Feathers can be used in Croesus."),
										Component.literal(SPACE + "If “Opened Chest” is enabled, Kismet Feathers will have priority.").withStyle(ChatFormatting.YELLOW)))
								.binding(defaults.instance.croesus.mainMenuKismetAvailable,
										() -> current.instance.croesus.mainMenuKismetAvailable,
										newValue -> current.instance.croesus.mainMenuKismetAvailable = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Color>createBuilder()
								.name(Component.literal("Croesus - Highlight Kismet Feather Available - color"))
								.description(OptionDescription.of(
										Component.literal("Change the highlight color of where Kismet Feathers can be used in Croesus.")))
								.binding(defaults.instance.croesus.mainMenuKismetAvailableColor,
										() -> current.instance.croesus.mainMenuKismetAvailableColor,
										newValue -> current.instance.croesus.mainMenuKismetAvailableColor = newValue)
								.controller(ColorControllerBuilder::create)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Croesus - Highlight No more Chests"))
								.description(OptionDescription.of(
										Component.literal("Highlights the chests you opened in the Croesus.")))
								.binding(defaults.instance.croesus.mainMenuNoMoreChest,
										() -> current.instance.croesus.mainMenuNoMoreChest,
										newValue -> current.instance.croesus.mainMenuNoMoreChest = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Color>createBuilder()
								.name(Component.literal("Croesus - Highlight No more Chests - color"))
								.description(OptionDescription.of(
										Component.literal("Change the highlight color of the chests you opened in the Croesus.")))
								.binding(defaults.instance.croesus.mainMenuNoMoreChestColor,
										() -> current.instance.croesus.mainMenuNoMoreChestColor,
										newValue -> current.instance.croesus.mainMenuNoMoreChestColor = newValue)
								.controller(ColorControllerBuilder::create)
								.build())
						.build())
				.build();
	}
}
