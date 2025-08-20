package fr.siroz.cariboustonks.config.categories;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.LabelOption;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.controller.ColorControllerBuilder;
import fr.siroz.cariboustonks.config.Config;
import fr.siroz.cariboustonks.feature.diana.GuessBurrowLogic;
import fr.siroz.cariboustonks.feature.diana.TargetBurrowLogic;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.awt.Color;

@SuppressWarnings("checkstyle:linelength")
public class EventsCategory extends AbstractCategory {

    public EventsCategory(Config defaults, Config current) {
        super(defaults, current);
    }

    @Override
    public ConfigCategory create() {
        return ConfigCategory.createBuilder()
                .name(Text.literal("Events"))
                .tooltip(Text.literal("Event-related settings"))
                .group(OptionGroup.createBuilder()
                        .name(Text.literal("Mythological Ritual").formatted(Formatting.BOLD))
                        .collapsed(false)
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Enable Mythological Ritual"))
								.description(OptionDescription.of(
										Text.literal("Global activation of all Mythological Ritual settings.")))
								.binding(defaults.events.mythologicalRitual.enabled,
										() -> current.events.mythologicalRitual.enabled,
										newValue -> current.events.mythologicalRitual.enabled = newValue)
								.controller(this::createBooleanController)
								.build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.literal("Guess Burrow"))
                                .description(OptionDescription.of(
                                        Text.literal("Display a Waypoint that predicts the location of a Burrow."),
                                        Text.literal(SPACE + "The prediction system is the same as in version 1.8, with some differences")))
                                .binding(defaults.events.mythologicalRitual.guessBurrow,
                                        () -> current.events.mythologicalRitual.guessBurrow,
                                        newValue -> current.events.mythologicalRitual.guessBurrow = newValue)
                                .controller(this::createBooleanController)
                                .build())
                        .option(Option.<Color>createBuilder()
                                .name(Text.literal("Guess Burrow Color"))
                                .description(OptionDescription.of(
                                        Text.literal("Changes the color for the Guess Burrow Waypoint.")))
                                .binding(defaults.events.mythologicalRitual.guessBurrowColor,
                                        () -> current.events.mythologicalRitual.guessBurrowColor,
                                        newValue -> current.events.mythologicalRitual.guessBurrowColor = newValue)
                                .controller(ColorControllerBuilder::create)
                                .build())
                        .option(Option.<GuessBurrowLogic>createBuilder()
								.available(false)
                                .name(Text.literal("Guess Burrow Logic"))
                                .description(OptionDescription.of(
                                        Text.literal("The logic used to predict the location of a Burrow.")))
                                .binding(defaults.events.mythologicalRitual.guessBurrowLogic,
                                        () -> current.events.mythologicalRitual.guessBurrowLogic,
                                        newValue -> current.events.mythologicalRitual.guessBurrowLogic = newValue)
                                .controller(this::createEnumCyclingController)
                                .build())
                        .option(Option.<TargetBurrowLogic>createBuilder()
								.available(false)
                                .name(Text.literal("Target Burrow Logic"))
                                .description(OptionDescription.of(
                                        Text.literal("The logic used to determine the target Burrow of a Burrow.")))
                                .binding(defaults.events.mythologicalRitual.targetBurrowLogic,
                                        () -> current.events.mythologicalRitual.targetBurrowLogic,
                                        newValue -> current.events.mythologicalRitual.targetBurrowLogic = newValue)
                                .controller(this::createEnumCyclingController)
                                .build())
						.option(LabelOption.create(Text.literal("| Burrow Particle Finder").formatted(Formatting.BOLD)))
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.literal("Burrow Particle Finder"))
                                .description(OptionDescription.of(
                                        Text.literal("Display a Waypoint according to the type of Burrow nearby.")))
                                .binding(defaults.events.mythologicalRitual.burrowParticleFinder,
                                        () -> current.events.mythologicalRitual.burrowParticleFinder,
                                        newValue -> current.events.mythologicalRitual.burrowParticleFinder = newValue)
                                .controller(this::createBooleanController)
                                .build())
                        .option(Option.<Color>createBuilder()
                                .name(Text.literal("Burrow Particle Finder - Start"))
                                .description(OptionDescription.of(
                                        Text.literal("Changes the color for the Start Burrow Waypoint.")))
                                .binding(defaults.events.mythologicalRitual.burrowParticleFinderStartColor,
                                        () -> current.events.mythologicalRitual.burrowParticleFinderStartColor,
                                        newValue -> current.events.mythologicalRitual.burrowParticleFinderStartColor = newValue)
                                .controller(ColorControllerBuilder::create)
                                .build())
                        .option(Option.<Color>createBuilder()
                                .name(Text.literal("Burrow Particle Finder - Mob"))
                                .description(OptionDescription.of(
                                        Text.literal("Changes the color for the Mob Burrow Waypoint.")))
                                .binding(defaults.events.mythologicalRitual.burrowParticleFinderMobColor,
                                        () -> current.events.mythologicalRitual.burrowParticleFinderMobColor,
                                        newValue -> current.events.mythologicalRitual.burrowParticleFinderMobColor = newValue)
                                .controller(ColorControllerBuilder::create)
                                .build())
                        .option(Option.<Color>createBuilder()
                                .name(Text.literal("Burrow Particle Finder - Treasure"))
                                .description(OptionDescription.of(
                                        Text.literal("Changes the color for the Treasure Burrow Waypoint.")))
                                .binding(defaults.events.mythologicalRitual.burrowParticleFinderTreasureColor,
                                        () -> current.events.mythologicalRitual.burrowParticleFinderTreasureColor,
                                        newValue -> current.events.mythologicalRitual.burrowParticleFinderTreasureColor = newValue)
                                .controller(ColorControllerBuilder::create)
                                .build())
						.option(LabelOption.create(Text.literal("| Qol").formatted(Formatting.BOLD)))
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.literal("Line to closest Burrow"))
                                .description(OptionDescription.of(
                                        Text.literal("Display a line from the mouse cursor to the nearest Burrow.")))
                                .binding(defaults.events.mythologicalRitual.lineToClosestBurrow,
                                        () -> current.events.mythologicalRitual.lineToClosestBurrow,
                                        newValue -> current.events.mythologicalRitual.lineToClosestBurrow = newValue)
                                .controller(this::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.literal("Nearest Warp"))
                                .description(OptionDescription.of(
                                        Text.literal("Teleport to the Warp nearest to Guess Burrow")))
                                .binding(defaults.events.mythologicalRitual.nearestWarp,
                                        () -> current.events.mythologicalRitual.nearestWarp,
                                        newValue -> current.events.mythologicalRitual.nearestWarp = newValue)
                                .controller(this::createBooleanController)
                                .build())
						.option(LabelOption.create(Text.literal("| Inquisitor").formatted(Formatting.BOLD)))
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.literal("Share Inquisitors"))
                                .description(OptionDescription.of(
                                        Text.literal("When you have an Inquisitor and you're in a Party, allows you to share it with other players.")))
                                .binding(defaults.events.mythologicalRitual.shareInquisitor,
                                        () -> current.events.mythologicalRitual.shareInquisitor,
                                        newValue -> current.events.mythologicalRitual.shareInquisitor = newValue)
                                .controller(this::createBooleanController)
                                .build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Highlight Inquisitors"))
								.description(OptionDescription.of(
										Text.literal("Highlight the Inquisitor entity with a custom glow color.")))
								.binding(defaults.events.mythologicalRitual.highlightInquisitor,
										() -> current.events.mythologicalRitual.highlightInquisitor,
										newValue -> current.events.mythologicalRitual.highlightInquisitor = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Color>createBuilder()
								.name(Text.literal("Highlight Inquisitors Color"))
								.description(OptionDescription.of(
										Text.literal("If Highlight Inquisitors is enabled, set the entity glow color.")))
								.binding(defaults.events.mythologicalRitual.highlightInquisitorColor,
										() -> current.events.mythologicalRitual.highlightInquisitorColor,
										newValue -> current.events.mythologicalRitual.highlightInquisitorColor = newValue)
								.controller(ColorControllerBuilder::create)
								.build())
                        .build())
                .build();
    }
}
