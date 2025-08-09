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
public class SlayerCategory extends AbstractCategory {

	public SlayerCategory(Config defaults, Config current) {
		super(defaults, current);
	}

	@Override
	public ConfigCategory create() {
		return ConfigCategory.createBuilder()
				.name(Text.literal("Slayers"))
				.tooltip(Text.literal("Slayers-related Settings"))
				.option(Option.<Boolean>createBuilder()
						.name(Text.literal("Slayer Boss Cocooned Warning").append(BETA))
						.description(OptionDescription.of(
								Text.literal("Show a Title and play a Sound when your Slayer Boss is cocooned!"),
								Text.literal(SPACE + "This is a BETA feature, can only work with Slayer Boss for now.").formatted(Formatting.GOLD)))
						.binding(defaults.slayer.slayerBossCocoonedWarning,
								() -> current.slayer.slayerBossCocoonedWarning,
								newValue -> current.slayer.slayerBossCocoonedWarning = newValue)
						.controller(this::createBooleanController)
						.build())
				.group(OptionGroup.createBuilder()
						.name(Text.literal("Spawn Alerts").formatted(Formatting.BOLD))
						.description(OptionDescription.of(
								Text.literal("Spawn Alerts settings")))
						.collapsed(false)
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Boss spawn Alert"))
								.description(OptionDescription.of(
										Text.literal("Show a Title when your Boss spawned.")))
								.binding(defaults.slayer.bossSpawnAlert,
										() -> current.slayer.bossSpawnAlert,
										newValue -> current.slayer.bossSpawnAlert = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Miniboss spawn Alert"))
								.description(OptionDescription.of(
										Text.literal("Show a Title when your Miniboss spawned.")))
								.binding(defaults.slayer.minibossSpawnAlert,
										() -> current.slayer.minibossSpawnAlert,
										newValue -> current.slayer.minibossSpawnAlert = newValue)
								.controller(this::createBooleanController)
								.build())
						.build())
				.group(OptionGroup.createBuilder()
						.name(Text.literal("Highlights").formatted(Formatting.BOLD))
						.description(OptionDescription.of(
								Text.literal("Control entities highlight settings")))
						.collapsed(false)
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Highlight Boss"))
								.description(OptionDescription.of(
										Text.literal("If enabled, highlight your Boss with a custom glow color.")))
								.binding(defaults.slayer.highlightBoss,
										() -> current.slayer.highlightBoss,
										newValue -> current.slayer.highlightBoss = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Color>createBuilder()
								.name(Text.literal("Highlight Boss Color"))
								.description(OptionDescription.of(
										Text.literal("If Highlight Boss is enabled, set your Boss glow color.")))
								.binding(defaults.slayer.highlightBossColor,
										() -> current.slayer.highlightBossColor,
										newValue -> current.slayer.highlightBossColor = newValue)
								.controller(ColorControllerBuilder::create)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Highlight Miniboss"))
								.description(OptionDescription.of(
										Text.literal("If enabled, highlight your Miniboss with a custom glow color.")))
								.binding(defaults.slayer.highlightMiniboss,
										() -> current.slayer.highlightMiniboss,
										newValue -> current.slayer.highlightMiniboss = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Color>createBuilder()
								.name(Text.literal("Highlight Miniboss Color"))
								.description(OptionDescription.of(
										Text.literal("If Highlight Miniboss is enabled, set your Miniboss glow color.")))
								.binding(defaults.slayer.highlightMinibossColor,
										() -> current.slayer.highlightMinibossColor,
										newValue -> current.slayer.highlightMinibossColor = newValue)
								.controller(ColorControllerBuilder::create)
								.build())
						.build())
				.group(OptionGroup.createBuilder()
						.name(Text.literal("Statistics").formatted(Formatting.BOLD))
						.description(OptionDescription.of(
								Text.literal("Control different statistics during Quest/Boss Fight")))
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Show Statistics Breakdown"))
								.description(OptionDescription.of(
										Text.literal("When you kill your Boss, show a message with statistics:"),
										Text.literal(SPACE),
										Text.literal("- Spawn Time"),
										Text.literal("- Kill Time"),
										Text.literal("- Total Time")))
								.binding(defaults.slayer.showStatsBreakdown,
										() -> current.slayer.showStatsBreakdown,
										newValue -> current.slayer.showStatsBreakdown = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Show Statistics Average").append(BETA))
								.description(OptionDescription.of(
										Text.literal("When you kill your Boss, show a message with average statistics:"),
										Text.literal(SPACE),
										Text.literal("- Spawn Avg"),
										Text.literal("- Kill Avg"),
										Text.literal("- Boss/h"),
										Text.literal("- EXP/h"),
										Text.literal("- RNGMeter/h"),
										Text.literal(SPACE + "[!] Subject to change").formatted(Formatting.GOLD)))
								.binding(defaults.slayer.showStatsInChat,
										() -> current.slayer.showStatsInChat,
										newValue -> current.slayer.showStatsInChat = newValue)
								.controller(this::createBooleanController)
								.build())
						.build())
				.group(OptionGroup.createBuilder()
						.name(Text.literal("Slayer's Bosses"))
						.description(OptionDescription.of(
								Text.literal("Control different settings during Boss Fight")))
						.collapsed(false)
						.option(LabelOption.create(Text.literal("| Spider - Tarantula Broodfather").formatted(Formatting.BOLD)))
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Highlight Boss Cocoon Eggs"))
								.description(OptionDescription.of(
										Text.literal("Highlight Cocoon Eggs summoned by the Tarantula Broodfather Tier V with a custom glow color.")))
								.binding(defaults.slayer.tarantulaBoss.highlightBossEggs,
										() -> current.slayer.tarantulaBoss.highlightBossEggs,
										newValue -> current.slayer.tarantulaBoss.highlightBossEggs = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Color>createBuilder()
								.name(Text.literal("Highlight Boss Cocoon Eggs Color"))
								.description(OptionDescription.of(
										Text.literal("Change the glow color of the Highlight Cocoon Eggs.")))
								.binding(defaults.slayer.tarantulaBoss.highlightBossEggsColor,
										() -> current.slayer.tarantulaBoss.highlightBossEggsColor,
										newValue -> current.slayer.tarantulaBoss.highlightBossEggsColor = newValue)
								.controller(ColorControllerBuilder::create)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Show Cursor lines to Cocoon Eggs"))
								.description(OptionDescription.of(
										Text.literal("Show lines from your Cursor to the Cocoon Eggs.")))
								.binding(defaults.slayer.tarantulaBoss.showCursorLineToBossEggs,
										() -> current.slayer.tarantulaBoss.showCursorLineToBossEggs,
										newValue -> current.slayer.tarantulaBoss.showCursorLineToBossEggs = newValue)
								.controller(this::createBooleanController)
								.build())
						.build())
				.build();
	}
}
