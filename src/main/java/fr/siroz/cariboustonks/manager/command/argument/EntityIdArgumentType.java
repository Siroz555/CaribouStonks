package fr.siroz.cariboustonks.manager.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class EntityIdArgumentType implements ArgumentType<String> {

	private static final List<String> ENTITY_ID_LIST = BuiltInRegistries.ENTITY_TYPE.keySet().stream()
			.map(Identifier::getPath)
			.toList();

	@Contract(value = " -> new", pure = true)
	public static @NotNull EntityIdArgumentType entityType() {
		return new EntityIdArgumentType();
	}

	@Override
	public @NotNull String parse(@NotNull StringReader reader) throws CommandSyntaxException {
		String name = reader.readString();
		for (String id : ENTITY_ID_LIST) {
			if (id.equalsIgnoreCase(name)) {
				return id;
			}
		}

		throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().create();
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(@NotNull CommandContext<S> context, SuggestionsBuilder builder) {
		return context.getSource() instanceof SharedSuggestionProvider
				? SharedSuggestionProvider.suggest(ENTITY_ID_LIST.stream().map(String::toLowerCase), builder)
				: Suggestions.empty();
	}

	@Override
	public Collection<String> getExamples() {
		return ENTITY_ID_LIST;
	}
}
