package fr.siroz.cariboustonks.config.categories;

import dev.isxander.yacl3.api.ButtonOption;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.controller.ColorControllerBuilder;
import fr.siroz.cariboustonks.config.Config;
import fr.siroz.cariboustonks.util.Client;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.awt.Color;

@SuppressWarnings("checkstyle:linelength")
public class MiscCategory extends AbstractCategory {

    public MiscCategory(Config defaults, Config current) {
        super(defaults, current);
    }

    @Override
    public ConfigCategory create() {
        return ConfigCategory.createBuilder()
                .name(Text.literal("Misc"))
                .tooltip(Text.literal("Miscellaneous Settings"))
				.option(ButtonOption.createBuilder()
						.name(Text.literal("Highlighter Mob"))
						.text(Text.literal("/highlighter <mob>"))
						.action((screen, buttonOption) -> {
							Client.sendMessageWithPrefix(Text.literal("Use /highlighter <mob>").formatted(Formatting.GREEN));
							screen.close();
						})
						.build())
				.option(Option.<Color>createBuilder()
						.name(Text.literal("Highlighter Mob color"))
						.description(OptionDescription.of(
								Text.literal("Change the color of the highlighter mob command (/highlighter <mob>).")))
						.binding(defaults.misc.highlighterColor,
								() -> current.misc.highlighterColor,
								newValue -> current.misc.highlighterColor = newValue)
						.controller(ColorControllerBuilder::create)
						.build())
                .option(Option.<Boolean>createBuilder()
                        .name(Text.literal("Locating Hoppity Eggs"))
                        .description(OptionDescription.of(
								Text.literal("Locate Hoppity Eggs with your Egg Locator, creating a Guess Waypoint.")))
                        .binding(defaults.misc.hoppityEggFinderGuess,
                                () -> current.misc.hoppityEggFinderGuess,
                                newValue -> current.misc.hoppityEggFinderGuess = newValue)
                        .controller(this::createBooleanController)
                        .build())
				.option(Option.<Boolean>createBuilder()
						.name(Text.literal("Show Hex Color on Items"))
						.description(OptionDescription.of(
								Text.literal("Add the #HEX on TOP of all Dyed Item tooltip everywhere.")))
						.binding(defaults.misc.showHexOnDyedItemEverywhere,
								() -> current.misc.showHexOnDyedItemEverywhere,
								newValue -> current.misc.showHexOnDyedItemEverywhere = newValue)
						.controller(this::createBooleanController)
						.build())
				.group(OptionGroup.createBuilder()
						.name(Text.literal("Other Mods").formatted(Formatting.BOLD))
						.collapsed(false)
						.description(OptionDescription.of(
								Text.literal("Features of mods other than CaribouStonks.")))
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("RoughlyEnoughItems Calculator"))
								.description(OptionDescription.of(
										Text.literal("Allows you to have a calculator in the RoughlyEnoughItems Search Bar.")))
								.binding(defaults.misc.compatibility.reiSearchBarCalculator,
										() -> current.misc.compatibility.reiSearchBarCalculator,
										newValue -> current.misc.compatibility.reiSearchBarCalculator = newValue)
								.controller(this::createBooleanController)
								.build())
						.build())
                .group(OptionGroup.createBuilder()
                        .name(Text.literal("Party Commands").formatted(Formatting.BOLD))
                        .collapsed(false)
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Enable Party Commands"))
								.binding(defaults.misc.partyCommands.enabled,
										() -> current.misc.partyCommands.enabled,
										newValue -> current.misc.partyCommands.enabled = newValue)
								.controller(this::createBooleanController)
								.build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.literal("Coords (!coords)"))
                                .description(OptionDescription.of(
                                        Text.literal("'!coords'").formatted(Formatting.AQUA, Formatting.BOLD),
                                        Text.literal(SPACE + "A message sends your current position.")))
                                .binding(defaults.misc.partyCommands.coords,
                                        () -> current.misc.partyCommands.coords,
                                        newValue -> current.misc.partyCommands.coords = newValue)
                                .controller(this::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.literal("Warp (!warp)"))
                                .description(OptionDescription.of(
                                        Text.literal("'!warp'").formatted(Formatting.AQUA, Formatting.BOLD),
                                        Text.literal(SPACE + "The party leader will run /warp, teleporting members to his server.")))
                                .binding(defaults.misc.partyCommands.warp,
                                        () -> current.misc.partyCommands.warp,
                                        newValue -> current.misc.partyCommands.warp = newValue)
                                .controller(this::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.literal("Dice Game (!dice)"))
                                .description(OptionDescription.of(
                                        Text.literal("'!dice'").formatted(Formatting.AQUA, Formatting.BOLD),
                                        Text.literal(SPACE + "Send a message to see if you're lucky..")))
                                .binding(defaults.misc.partyCommands.diceGame,
                                        () -> current.misc.partyCommands.diceGame,
                                        newValue -> current.misc.partyCommands.diceGame = newValue)
                                .controller(this::createBooleanController)
                                .build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Coin Flip Game (!cf)"))
								.description(OptionDescription.of(
										Text.literal("'!cf'").formatted(Formatting.AQUA, Formatting.BOLD),
										Text.literal(SPACE + "Heads or Tails?")))
								.binding(defaults.misc.partyCommands.coinFlip,
										() -> current.misc.partyCommands.coinFlip,
										newValue -> current.misc.partyCommands.coinFlip = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Server Tick (!tps)"))
								.description(OptionDescription.of(
										Text.literal("'!tps'").formatted(Formatting.AQUA, Formatting.BOLD),
										Text.literal(SPACE + "Show the current server TPS.")))
								.binding(defaults.misc.partyCommands.tps,
										() -> current.misc.partyCommands.tps,
										newValue -> current.misc.partyCommands.tps = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Magic 8 Ball (!8ball)"))
								.description(OptionDescription.of(
										Text.literal("'!8ball'").formatted(Formatting.AQUA, Formatting.BOLD),
										Text.literal(SPACE + "Yes? No? Maybe? (or anything else)")))
								.binding(defaults.misc.partyCommands.magic8Ball,
										() -> current.misc.partyCommands.magic8Ball,
										newValue -> current.misc.partyCommands.magic8Ball = newValue)
								.controller(this::createBooleanController)
								.build())
                        .build())
                .build();
    }
}
