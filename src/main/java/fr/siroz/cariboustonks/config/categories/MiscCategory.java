package fr.siroz.cariboustonks.config.categories;

import dev.isxander.yacl3.api.ButtonOption;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.controller.ColorControllerBuilder;
import fr.siroz.cariboustonks.config.Config;
import fr.siroz.cariboustonks.platform.context.PlayerContext;
import java.awt.Color;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

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
						.action((screen, _) -> {
							PlayerContext.sendMessageWithPrefix(Component.literal("Use /highlighter <mob>").withStyle(ChatFormatting.GREEN));
							screen.onClose();
						})
						.build())
				.option(ButtonOption.createBuilder()
						.name(Component.literal("Bestiary Highlight"))
						.text(Component.literal("/bestiaryHighlight add <name>"))
						.action((screen, _) -> {
							PlayerContext.sendMessageWithPrefix(Component.literal("Use /bestiaryHighlight <add|clear> <name>").withStyle(ChatFormatting.GREEN));
							screen.onClose();
						})
						.build())
				.option(Option.<Boolean>createBuilder()
						.name(Component.literal("Bestiary Highlight Middle Click"))
						.description(OptionDescription.of(
								Component.literal("Highlights entities with the same name by middle-clicking on an entity.")))
						.binding(defaults.misc.bestiaryHighlight,
								() -> current.misc.bestiaryHighlight,
								newValue -> current.misc.bestiaryHighlight = newValue)
						.controller(this::createBooleanController)
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
						.name(Component.literal("Show Hex Color on Items"))
						.description(OptionDescription.of(
								Component.literal("Add the #HEX on TOP of all Dyed Item tooltip everywhere.")))
						.binding(defaults.misc.showHexOnDyedItemEverywhere,
								() -> current.misc.showHexOnDyedItemEverywhere,
								newValue -> current.misc.showHexOnDyedItemEverywhere = newValue)
						.controller(this::createBooleanController)
						.build())
				.option(Option.<Boolean>createBuilder()
						.name(Component.literal("Server Visit History"))
						.description(OptionDescription.of(
								Component.literal("Allows you to display a message in the chat indicating when you were last on the server or that you've just arrived.")))
						.binding(defaults.misc.serverTracker,
								() -> current.misc.serverTracker,
								newValue -> current.misc.serverTracker = newValue)
						.controller(this::createBooleanController)
						.build())
				.option(Option.<Boolean>createBuilder()
						.name(Component.literal("Disable Abiphone placement"))
						.description(OptionDescription.of(
								Component.literal("If enabled, disables the placement of all Abiphone on the ground.")))
						.binding(defaults.misc.disableAbiphonePlacement,
								() -> current.misc.disableAbiphonePlacement,
								newValue -> current.misc.disableAbiphonePlacement = newValue)
						.controller(this::createBooleanController)
						.build())
				.group(OptionGroup.createBuilder()
						.name(Component.literal("Events - Hoppity").withStyle(ChatFormatting.BOLD))
						.collapsed(false)
						.description(OptionDescription.of(
								Component.literal("Events-related settings")))
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Hoppity Hunt - Eggs Finder Guess"))
								.description(OptionDescription.of(
										Component.literal("Locate Hoppity Eggs with your Egg Locator, creating a Guess Waypoint.")))
								.binding(defaults.events.hoppityHunt.eggFinderGuess,
										() -> current.events.hoppityHunt.eggFinderGuess,
										newValue -> current.events.hoppityHunt.eggFinderGuess = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Hoppity Hunt - Unclaimed Eggs HUD"))
								.description(OptionDescription.of(
										Component.literal("Display the status of Hoppity's Eggs that were found or not found during the last day of SkyBlock in the form of a HUD, along with their respawn times.")))
								.binding(defaults.events.hoppityHunt.huntHud.showHud,
										() -> current.events.hoppityHunt.huntHud.showHud,
										newValue -> current.events.hoppityHunt.huntHud.showHud = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Hoppity Hunt - Warn When Unclaimed"))
								.description(OptionDescription.of(
										Component.literal("Warn when all 6 Eggs are ready to be found.")))
								.binding(defaults.events.hoppityHunt.huntNotification,
										() -> current.events.hoppityHunt.huntNotification,
										newValue -> current.events.hoppityHunt.huntNotification = newValue)
								.controller(this::createBooleanController)
								.build())
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
