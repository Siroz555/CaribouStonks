package fr.siroz.cariboustonks.config.categories;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.controller.ColorControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import fr.siroz.cariboustonks.config.Config;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.awt.Color;

@SuppressWarnings("checkstyle:linelength")
public class ChatCategory extends AbstractCategory {

    public ChatCategory(Config defaults, Config current) {
        super(defaults, current);
    }

    @Override
    public ConfigCategory create() {
        return ConfigCategory.createBuilder()
                .name(Text.literal("Chat"))
                .tooltip(Text.literal("Chat-related Settings"))
                .option(Option.<Boolean>createBuilder()
                        .name(Text.literal("Copy a message from the chat"))
                        .description(OptionDescription.of(
                                Text.literal("Copy a message from the chat by clicking on it using"),
								Text.literal(SPACE + "CTRL + CLICK").formatted(Formatting.AQUA, Formatting.BOLD)))
                        .binding(defaults.chat.copyChat,
                                () -> current.chat.copyChat,
                                newValue -> current.chat.copyChat = newValue)
                        .controller(this::createBooleanController)
                        .build())
				.option(Option.<Integer>createBuilder()
						.name(Text.literal("Chat history length"))
						.description(OptionDescription.of(
								Text.literal("Modify the maximum length of the chat history")))
						.binding(defaults.chat.chatHistoryLength,
								() -> current.chat.chatHistoryLength,
								newValue -> current.chat.chatHistoryLength = Math.max(100, newValue))
						.controller(opt -> IntegerSliderControllerBuilder.create(opt).range(100, 5000).step(10))
						.build())
				.group(OptionGroup.createBuilder()
						.name(Text.literal("Party Chat").formatted(Formatting.BOLD))
						.description(OptionDescription.of(
								Text.literal("Party chat options.")))
						.collapsed(false)
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Enable Party Chat coloring"))
								.description(OptionDescription.of(
										Text.literal("Change the color of messages in Party chat.")))
								.binding(defaults.chat.chatParty.chatPartyColored,
										() -> current.chat.chatParty.chatPartyColored,
										newValue -> current.chat.chatParty.chatPartyColored = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Color>createBuilder()
								.name(Text.literal("Color of party messages"))
								.description(OptionDescription.of(
										Text.literal("Color to be applied to Party messages.")))
								.binding(defaults.chat.chatParty.chatPartyColor,
										() -> current.chat.chatParty.chatPartyColor,
										newValue -> current.chat.chatParty.chatPartyColor = newValue)
								.controller(ColorControllerBuilder::create)
								.build())
						.build())
				.group(OptionGroup.createBuilder()
						.name(Text.literal("Guild Chat").formatted(Formatting.BOLD))
						.description(OptionDescription.of(
								Text.literal("Guild chat options.")))
						.collapsed(false)
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Enable Guild Chat coloring"))
								.description(OptionDescription.of(
										Text.literal("Change the color of messages in Guild chat.")))
								.binding(defaults.chat.chatGuild.chatGuildColored,
										() -> current.chat.chatGuild.chatGuildColored,
										newValue -> current.chat.chatGuild.chatGuildColored = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Color>createBuilder()
								.name(Text.literal("Color of guild messages"))
								.description(OptionDescription.of(
										Text.literal("Color to be applied to Guild messages.")))
								.binding(defaults.chat.chatGuild.chatGuildColor,
										() -> current.chat.chatGuild.chatGuildColor,
										newValue -> current.chat.chatGuild.chatGuildColor = newValue)
								.controller(ColorControllerBuilder::create)
								.build())
						.build())
                .build();
    }
}
