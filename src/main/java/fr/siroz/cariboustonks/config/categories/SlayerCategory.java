package fr.siroz.cariboustonks.config.categories;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.LabelOption;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.controller.ColorControllerBuilder;
import fr.siroz.cariboustonks.config.Config;
import java.awt.Color;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

@SuppressWarnings("checkstyle:linelength")
public class SlayerCategory extends AbstractCategory {

	public SlayerCategory(Config defaults, Config current) {
		super(defaults, current);
	}

	@Override
	public ConfigCategory create() {
		return ConfigCategory.createBuilder()
				.name(Component.literal("Slayers"))
				.tooltip(Component.literal("Slayers-related Settings"))
				.option(Option.<Boolean>createBuilder()
						.name(Component.literal("Slayer Stats HUD"))
						.description(OptionDescription.of(
								Component.literal("Displays a HUD on the screen, which shows statistics for the current session."),
								Component.literal(SPACE + "This displays average spawn/kill times, the number of bosses per hour, and XP per hour.")))
						.binding(defaults.slayer.statsHud.enabled,
								() -> current.slayer.statsHud.enabled,
								newValue -> current.slayer.statsHud.enabled = newValue)
						.controller(this::createYesNoController)
						.build())
				.option(Option.<Boolean>createBuilder()
						.name(Component.literal("Slayer Boss Cocooned Warning"))
						.description(OptionDescription.of(
								Component.literal("Show a Title and play a Sound when your Slayer Boss is cocooned!"),
								Component.literal(SPACE + "[!] For Minibosses, see Skills > Combat > Cocooned Mobs").withStyle(ChatFormatting.YELLOW)))
						.binding(defaults.slayer.slayerBossCocoonedWarning,
								() -> current.slayer.slayerBossCocoonedWarning,
								newValue -> current.slayer.slayerBossCocoonedWarning = newValue)
						.controller(this::createBooleanController)
						.build())
				.group(OptionGroup.createBuilder()
						.name(Component.literal("Spawn Alerts").withStyle(ChatFormatting.BOLD))
						.description(OptionDescription.of(
								Component.literal("Spawn Alerts settings")))
						.collapsed(false)
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Boss spawn Alert"))
								.description(OptionDescription.of(
										Component.literal("Show a Title when your Boss spawned.")))
								.binding(defaults.slayer.bossSpawnAlert,
										() -> current.slayer.bossSpawnAlert,
										newValue -> current.slayer.bossSpawnAlert = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Miniboss spawn Alert"))
								.description(OptionDescription.of(
										Component.literal("Show a Title when your Miniboss spawned.")))
								.binding(defaults.slayer.minibossSpawnAlert,
										() -> current.slayer.minibossSpawnAlert,
										newValue -> current.slayer.minibossSpawnAlert = newValue)
								.controller(this::createBooleanController)
								.build())
						.build())
				.group(OptionGroup.createBuilder()
						.name(Component.literal("Highlights").withStyle(ChatFormatting.BOLD))
						.description(OptionDescription.of(
								Component.literal("Control entities highlight settings")))
						.collapsed(false)
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Highlight Boss"))
								.description(OptionDescription.of(
										Component.literal("If enabled, highlight your Boss with a custom glow color.")))
								.binding(defaults.slayer.highlightBoss,
										() -> current.slayer.highlightBoss,
										newValue -> current.slayer.highlightBoss = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Color>createBuilder()
								.name(Component.literal("Highlight Boss Color"))
								.description(OptionDescription.of(
										Component.literal("If Highlight Boss is enabled, set your Boss glow color.")))
								.binding(defaults.slayer.highlightBossColor,
										() -> current.slayer.highlightBossColor,
										newValue -> current.slayer.highlightBossColor = newValue)
								.controller(ColorControllerBuilder::create)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Highlight Miniboss"))
								.description(OptionDescription.of(
										Component.literal("If enabled, highlight your Miniboss with a custom glow color.")))
								.binding(defaults.slayer.highlightMiniboss,
										() -> current.slayer.highlightMiniboss,
										newValue -> current.slayer.highlightMiniboss = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Color>createBuilder()
								.name(Component.literal("Highlight Miniboss Color"))
								.description(OptionDescription.of(
										Component.literal("If Highlight Miniboss is enabled, set your Miniboss glow color.")))
								.binding(defaults.slayer.highlightMinibossColor,
										() -> current.slayer.highlightMinibossColor,
										newValue -> current.slayer.highlightMinibossColor = newValue)
								.controller(ColorControllerBuilder::create)
								.build())
						.build())
				.group(OptionGroup.createBuilder()
						.name(Component.literal("Statistics").withStyle(ChatFormatting.BOLD))
						.description(OptionDescription.of(
								Component.literal("Control different statistics during Quest/Boss Fight")))
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Show Statistics Breakdown"))
								.description(OptionDescription.of(
										Component.literal("When you kill your Boss, show a message with statistics:"),
										Component.literal(SPACE),
										Component.literal("- Spawn Time"),
										Component.literal("- Kill Time"),
										Component.literal("- Total Time")))
								.binding(defaults.slayer.showStatsBreakdown,
										() -> current.slayer.showStatsBreakdown,
										newValue -> current.slayer.showStatsBreakdown = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Show Statistics Average").append(BETA))
								.description(OptionDescription.of(
										Component.literal("When you kill your Boss, show a message with average statistics:"),
										Component.literal(SPACE),
										Component.literal("- Spawn Avg"),
										Component.literal("- Kill Avg"),
										Component.literal("- Boss/h"),
										Component.literal("- EXP/h"),
										Component.literal("- RNGMeter/h"),
										Component.literal(SPACE + "[!] Subject to change").withStyle(ChatFormatting.GOLD)))
								.binding(defaults.slayer.showStatsInChat,
										() -> current.slayer.showStatsInChat,
										newValue -> current.slayer.showStatsInChat = newValue)
								.controller(this::createBooleanController)
								.build())
						.build())
				.group(OptionGroup.createBuilder()
						.name(Component.literal("Slayer's Bosses").withStyle(ChatFormatting.BOLD))
						.description(OptionDescription.of(
								Component.literal("Control different settings during Boss Fight")))
						.collapsed(false)
						.option(LabelOption.create(Component.literal("| Spider - Tarantula Broodfather").withStyle(ChatFormatting.BOLD)))
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Highlight Boss Cocoon Eggs"))
								.description(OptionDescription.of(
										Component.literal("Highlight Cocoon Eggs summoned by the Tarantula Broodfather Tier V with a custom glow color.")))
								.binding(defaults.slayer.tarantulaBoss.highlightBossEggs,
										() -> current.slayer.tarantulaBoss.highlightBossEggs,
										newValue -> current.slayer.tarantulaBoss.highlightBossEggs = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Color>createBuilder()
								.name(Component.literal("Highlight Boss Cocoon Eggs Color"))
								.description(OptionDescription.of(
										Component.literal("Change the glow color of the Highlight Cocoon Eggs.")))
								.binding(defaults.slayer.tarantulaBoss.highlightBossEggsColor,
										() -> current.slayer.tarantulaBoss.highlightBossEggsColor,
										newValue -> current.slayer.tarantulaBoss.highlightBossEggsColor = newValue)
								.controller(ColorControllerBuilder::create)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Show Cursor lines to Cocoon Eggs"))
								.description(OptionDescription.of(
										Component.literal("Show lines from your Cursor to the Cocoon Eggs.")))
								.binding(defaults.slayer.tarantulaBoss.showCursorLineToBossEggs,
										() -> current.slayer.tarantulaBoss.showCursorLineToBossEggs,
										newValue -> current.slayer.tarantulaBoss.showCursorLineToBossEggs = newValue)
								.controller(this::createBooleanController)
								.build())
						.build())
				.build();
	}
}
