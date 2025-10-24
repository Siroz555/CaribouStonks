package fr.siroz.cariboustonks.config.categories;

import dev.isxander.yacl3.api.ConfigCategory;
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
								.binding(defaults.dungeon.croesus.mainMenuOpenedChest,
										() -> current.dungeon.croesus.mainMenuOpenedChest,
										newValue -> current.dungeon.croesus.mainMenuOpenedChest = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Color>createBuilder()
								.name(Text.literal("Highlight Opened Chests - color"))
								.description(OptionDescription.of(
										Text.literal("Change the highlight color of the chests you opened in the Croesus.")))
								.binding(defaults.dungeon.croesus.mainMenuOpenedChestColor,
										() -> current.dungeon.croesus.mainMenuOpenedChestColor,
										newValue -> current.dungeon.croesus.mainMenuOpenedChestColor = newValue)
								.controller(ColorControllerBuilder::create)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Highlight Kismet Feather Available"))
								.description(OptionDescription.of(
										Text.literal("Highlight where Kismet Feathers can be used in Croesus."),
										Text.literal(SPACE + "If “Opened Chest” is enabled, Kismet Feathers will have priority.").formatted(Formatting.YELLOW)))
								.binding(defaults.dungeon.croesus.mainMenuKismetAvailable,
										() -> current.dungeon.croesus.mainMenuKismetAvailable,
										newValue -> current.dungeon.croesus.mainMenuKismetAvailable = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Color>createBuilder()
								.name(Text.literal("Highlight Kismet Feather Available - color"))
								.description(OptionDescription.of(
										Text.literal("Change the highlight color of where Kismet Feathers can be used in Croesus.")))
								.binding(defaults.dungeon.croesus.mainMenuKismetAvailableColor,
										() -> current.dungeon.croesus.mainMenuKismetAvailableColor,
										newValue -> current.dungeon.croesus.mainMenuKismetAvailableColor = newValue)
								.controller(ColorControllerBuilder::create)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Highlight No more Chests"))
								.description(OptionDescription.of(
										Text.literal("Highlights the chests you opened in the Croesus.")))
								.binding(defaults.dungeon.croesus.mainMenuNoMoreChest,
										() -> current.dungeon.croesus.mainMenuNoMoreChest,
										newValue -> current.dungeon.croesus.mainMenuNoMoreChest = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Color>createBuilder()
								.name(Text.literal("Highlight No more Chests - color"))
								.description(OptionDescription.of(
										Text.literal("Change the highlight color of the chests you opened in the Croesus.")))
								.binding(defaults.dungeon.croesus.mainMenuNoMoreChestColor,
										() -> current.dungeon.croesus.mainMenuNoMoreChestColor,
										newValue -> current.dungeon.croesus.mainMenuNoMoreChestColor = newValue)
								.controller(ColorControllerBuilder::create)
								.build())
						.build())
				.build();
	}
}
