package fr.siroz.cariboustonks.config.categories;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.LabelOption;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.controller.ColorControllerBuilder;
import fr.siroz.cariboustonks.config.Config;
import java.awt.Color;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

@SuppressWarnings("checkstyle:linelength")
public class DungeonsCategory extends AbstractCategory {

	public DungeonsCategory(Config defaults, Config current) {
		super(defaults, current);
	}

	@Override
	public ConfigCategory create() {
		return ConfigCategory.createBuilder()
				.name(Text.literal("Dungeons"))
				.tooltip(Text.literal("Dungeons-related Settings"))
				.group(OptionGroup.createBuilder()
						.name(Text.literal("Croesus").formatted(Formatting.BOLD))
						.description(OptionDescription.of(
								Text.literal("Croesus Helpers")))
						.collapsed(false)
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Highlight Opened Chests"))
								.description(OptionDescription.of(
										Text.literal("Highlights the chests you opened in the Croesus.")))
								.binding(defaults.instance.croesus.mainMenuOpenedChest,
										() -> current.instance.croesus.mainMenuOpenedChest,
										newValue -> current.instance.croesus.mainMenuOpenedChest = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Color>createBuilder()
								.name(Text.literal("Highlight Opened Chests - color"))
								.description(OptionDescription.of(
										Text.literal("Change the highlight color of the chests you opened in the Croesus.")))
								.binding(defaults.instance.croesus.mainMenuOpenedChestColor,
										() -> current.instance.croesus.mainMenuOpenedChestColor,
										newValue -> current.instance.croesus.mainMenuOpenedChestColor = newValue)
								.controller(ColorControllerBuilder::create)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Highlight Kismet Feather Available"))
								.description(OptionDescription.of(
										Text.literal("Highlight where Kismet Feathers can be used in Croesus."),
										Text.literal(SPACE + "If “Opened Chest” is enabled, Kismet Feathers will have priority.").formatted(Formatting.YELLOW)))
								.binding(defaults.instance.croesus.mainMenuKismetAvailable,
										() -> current.instance.croesus.mainMenuKismetAvailable,
										newValue -> current.instance.croesus.mainMenuKismetAvailable = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Color>createBuilder()
								.name(Text.literal("Highlight Kismet Feather Available - color"))
								.description(OptionDescription.of(
										Text.literal("Change the highlight color of where Kismet Feathers can be used in Croesus.")))
								.binding(defaults.instance.croesus.mainMenuKismetAvailableColor,
										() -> current.instance.croesus.mainMenuKismetAvailableColor,
										newValue -> current.instance.croesus.mainMenuKismetAvailableColor = newValue)
								.controller(ColorControllerBuilder::create)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Highlight No more Chests"))
								.description(OptionDescription.of(
										Text.literal("Highlights the chests you opened in the Croesus.")))
								.binding(defaults.instance.croesus.mainMenuNoMoreChest,
										() -> current.instance.croesus.mainMenuNoMoreChest,
										newValue -> current.instance.croesus.mainMenuNoMoreChest = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Color>createBuilder()
								.name(Text.literal("Highlight No more Chests - color"))
								.description(OptionDescription.of(
										Text.literal("Change the highlight color of the chests you opened in the Croesus.")))
								.binding(defaults.instance.croesus.mainMenuNoMoreChestColor,
										() -> current.instance.croesus.mainMenuNoMoreChestColor,
										newValue -> current.instance.croesus.mainMenuNoMoreChestColor = newValue)
								.controller(ColorControllerBuilder::create)
								.build())
						.build())
				.group(OptionGroup.createBuilder()
						.name(Text.literal("The Catacombs").formatted(Formatting.BOLD))
						.description(OptionDescription.of(
								Text.literal("Settings for the dungeon The Catacombs.")))
						.collapsed(false)
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Thorn - F4/M4 - Spirit Bear Timer"))
								.description(OptionDescription.of(
										Text.literal("Displays a timer to warn of the spawn of a Spirit Bear."),
										Text.literal(SPACE + "Adapted according to the server's TPS.").formatted(Formatting.AQUA)))
								.binding(defaults.instance.theCatacombs.bossThornSpiritBearTimers,
										() -> current.instance.theCatacombs.bossThornSpiritBearTimers,
										newValue -> current.instance.theCatacombs.bossThornSpiritBearTimers = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Sadan - F6/M6 - Terracotta Timers"))
								.description(OptionDescription.of(
										Text.literal("Displays timers for Terracotta spawn and respawn."),
										Text.literal(SPACE + "Adapted according to the server's TPS.").formatted(Formatting.AQUA)))
								.binding(defaults.instance.theCatacombs.bossSadanTerracottaTimers,
										() -> current.instance.theCatacombs.bossSadanTerracottaTimers,
										newValue -> current.instance.theCatacombs.bossSadanTerracottaTimers = newValue)
								.controller(this::createBooleanController)
								.build())
						.build())
				.group(OptionGroup.createBuilder()
						.name(Text.literal("Wither King - M7").formatted(Formatting.BOLD))
						.description(OptionDescription.of(
								Text.literal("Settings for the Wither King phase (P5) in the Master Mode Catacombs Floor 7. (M7)")))
						.collapsed(false)
						.option(LabelOption.create(Text.literal("| Dragon Priority & Spawn").formatted(Formatting.BOLD)))
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Enable Dragon Priority & Spawn alerts"))
								.description(OptionDescription.of(
										Text.literal("Tells you which dragon will spawn and you must kill."),
										Text.literal(SPACE + "During the Split phase, this alerts you to which Dragon you must kill based on your Dungeon Class."),
										Text.literal("Split Priority: §6B§7/§bM §7-> §6O§aG§cR§bB§5P §7<- §cA§7/§5H§7/§aT"),
										Text.literal("(Version 1.8 from DragPrio)").formatted(Formatting.GRAY, Formatting.ITALIC),
										Text.literal(SPACE + "After the Split phase, an alert will be sent for each dragon that spawns.")))
								.binding(defaults.instance.theCatacombs.witherKing.dragPrio,
										() -> current.instance.theCatacombs.witherKing.dragPrio,
										newValue -> current.instance.theCatacombs.witherKing.dragPrio = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Dragon Priority & Spawn - Title"))
								.description(OptionDescription.of(
										Text.literal("Displays a Title and plays a sound when a Dragon spawns.")))
								.binding(defaults.instance.theCatacombs.witherKing.dragPrioTitle,
										() -> current.instance.theCatacombs.witherKing.dragPrioTitle,
										newValue -> current.instance.theCatacombs.witherKing.dragPrioTitle = newValue)
								.controller(this::createYesNoController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Dragon Priority & Spawn - Message"))
								.description(OptionDescription.of(
										Text.literal("Displays a message in the chat when a Dragon spawns.")))
								.binding(defaults.instance.theCatacombs.witherKing.dragPrioMessage,
										() -> current.instance.theCatacombs.witherKing.dragPrioMessage,
										newValue -> current.instance.theCatacombs.witherKing.dragPrioMessage = newValue)
								.controller(this::createYesNoController)
								.build())
						.option(LabelOption.create(Text.literal("| Dragon Helpers").formatted(Formatting.BOLD)))
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Show Spawn Time"))
								.description(OptionDescription.of(
										Text.literal("Displays timers to warn of a Dragon spawn."),
										Text.literal(SPACE + "Adapted according to the server's TPS.").formatted(Formatting.AQUA)))
								.binding(defaults.instance.theCatacombs.witherKing.showSpawnTime,
										() -> current.instance.theCatacombs.witherKing.showSpawnTime,
										newValue -> current.instance.theCatacombs.witherKing.showSpawnTime = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Show Bounding Boxes"))
								.description(OptionDescription.of(
										Text.literal("Displays the Dragons' colored boxes")))
								.binding(defaults.instance.theCatacombs.witherKing.showDragBoundingBox,
										() -> current.instance.theCatacombs.witherKing.showDragBoundingBox,
										newValue -> current.instance.theCatacombs.witherKing.showDragBoundingBox = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Show Target Line"))
								.description(OptionDescription.of(
										Text.literal("Displays a line from your cursor to the dragon that will spawn.")))
								.binding(defaults.instance.theCatacombs.witherKing.showDragTargetLine,
										() -> current.instance.theCatacombs.witherKing.showDragTargetLine,
										newValue -> current.instance.theCatacombs.witherKing.showDragTargetLine = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Show Last Breath Target"))
								.description(OptionDescription.of(
										Text.literal("Displays a circle that allows you to aim at the dragon that will spawn, having a Last Breath."),
										Text.literal(SPACE + "Considering that you are focused on the Obsidian block.").formatted(Formatting.ITALIC)))
								.binding(defaults.instance.theCatacombs.witherKing.showLastBreathTarget,
										() -> current.instance.theCatacombs.witherKing.showLastBreathTarget,
										newValue -> current.instance.theCatacombs.witherKing.showLastBreathTarget = newValue)
								.controller(this::createBooleanController)
								.build())
						.build())
				.build();
	}
}
