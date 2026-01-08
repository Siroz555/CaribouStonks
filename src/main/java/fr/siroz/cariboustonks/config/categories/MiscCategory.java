package fr.siroz.cariboustonks.config.categories;

import dev.isxander.yacl3.api.ButtonOption;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.controller.ColorControllerBuilder;
import fr.siroz.cariboustonks.config.Config;
import fr.siroz.cariboustonks.util.Client;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.awt.Color;

@SuppressWarnings("checkstyle:linelength")
public class MiscCategory extends AbstractCategory {

    public MiscCategory(Config defaults, Config current) {
        super(defaults, current);
    }

    @Override
    public ConfigCategory create() {
        return ConfigCategory.createBuilder()
                .name(Component.literal("Misc"))
                .tooltip(Component.literal("Miscellaneous Settings"))
				.option(ButtonOption.createBuilder()
						.name(Component.literal("Highlighter Mob"))
						.text(Component.literal("/highlighter <mob>"))
						.action((screen, buttonOption) -> {
							Client.sendMessageWithPrefix(Component.literal("Use /highlighter <mob>").withStyle(ChatFormatting.GREEN));
							screen.onClose();
						})
						.build())
				.option(Option.<Color>createBuilder()
						.name(Component.literal("Highlighter Mob color"))
						.description(OptionDescription.of(
								Component.literal("Change the color of the highlighter mob command (/highlighter <mob>).")))
						.binding(defaults.misc.highlighterColor,
								() -> current.misc.highlighterColor,
								newValue -> current.misc.highlighterColor = newValue)
						.controller(ColorControllerBuilder::create)
						.build())
                .option(Option.<Boolean>createBuilder()
                        .name(Component.literal("Locating Hoppity Eggs"))
                        .available(false)
                        .description(OptionDescription.of(
                                Component.literal("Locate Hoppity Eggs with your Egg Locator, creating a Guess Waypoint.")))
                        .binding(defaults.misc.hoppityEggFinderGuess,
                                () -> current.misc.hoppityEggFinderGuess,
                                newValue -> current.misc.hoppityEggFinderGuess = newValue)
                        .controller(this::createBooleanController)
                        .build())
				.group(OptionGroup.createBuilder()
						.name(Component.literal("Other Mods").withStyle(ChatFormatting.BOLD))
						.collapsed(false)
						.description(OptionDescription.of(
								Component.literal("Features of mods other than CaribouStonks.")))
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("RoughlyEnoughItems Calculator"))
								.description(OptionDescription.of(
										Component.literal("Allows you to have a calculator in the RoughlyEnoughItems Search Bar.")))
								.binding(defaults.misc.compatibility.reiSearchBarCalculator,
										() -> current.misc.compatibility.reiSearchBarCalculator,
										newValue -> current.misc.compatibility.reiSearchBarCalculator = newValue)
								.controller(this::createBooleanController)
								.build())
						.build())
                .group(OptionGroup.createBuilder()
                        .name(Component.literal("Party Commands").withStyle(ChatFormatting.BOLD))
                        .collapsed(false)
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Enable Party Commands"))
								.binding(defaults.misc.partyCommands.enabled,
										() -> current.misc.partyCommands.enabled,
										newValue -> current.misc.partyCommands.enabled = newValue)
								.controller(this::createBooleanController)
								.build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.literal("Coords (!coords)"))
                                .description(OptionDescription.of(
                                        Component.literal("'!coords'").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD),
                                        Component.literal(SPACE + "A message sends your current position.")))
                                .binding(defaults.misc.partyCommands.coords,
                                        () -> current.misc.partyCommands.coords,
                                        newValue -> current.misc.partyCommands.coords = newValue)
                                .controller(this::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.literal("Warp (!warp)"))
                                .description(OptionDescription.of(
                                        Component.literal("'!warp'").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD),
                                        Component.literal(SPACE + "The party leader will run /warp, teleporting members to his server.")))
                                .binding(defaults.misc.partyCommands.warp,
                                        () -> current.misc.partyCommands.warp,
                                        newValue -> current.misc.partyCommands.warp = newValue)
                                .controller(this::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.literal("Dice Game (!dice)"))
                                .description(OptionDescription.of(
                                        Component.literal("'!dice'").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD),
                                        Component.literal(SPACE + "Send a message to see if you're lucky..")))
                                .binding(defaults.misc.partyCommands.diceGame,
                                        () -> current.misc.partyCommands.diceGame,
                                        newValue -> current.misc.partyCommands.diceGame = newValue)
                                .controller(this::createBooleanController)
                                .build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Coin Flip Game (!cf)"))
								.description(OptionDescription.of(
										Component.literal("'!cf'").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD),
										Component.literal(SPACE + "Heads or Tails?")))
								.binding(defaults.misc.partyCommands.coinFlip,
										() -> current.misc.partyCommands.coinFlip,
										newValue -> current.misc.partyCommands.coinFlip = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Server Tick (!tps)"))
								.description(OptionDescription.of(
										Component.literal("'!tps'").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD),
										Component.literal(SPACE + "Show the current server TPS.")))
								.binding(defaults.misc.partyCommands.tps,
										() -> current.misc.partyCommands.tps,
										newValue -> current.misc.partyCommands.tps = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Magic 8 Ball (!8ball)"))
								.description(OptionDescription.of(
										Component.literal("'!8ball'").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD),
										Component.literal(SPACE + "Yes? No? Maybe? (or anything else)")))
								.binding(defaults.misc.partyCommands.magic8Ball,
										() -> current.misc.partyCommands.magic8Ball,
										newValue -> current.misc.partyCommands.magic8Ball = newValue)
								.controller(this::createBooleanController)
								.build())
                        .build())
                .build();
    }
}
