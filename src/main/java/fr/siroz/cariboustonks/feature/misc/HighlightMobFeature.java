package fr.siroz.cariboustonks.feature.misc;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.core.component.CommandComponent;
import fr.siroz.cariboustonks.core.component.EntityGlowComponent;
import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.core.skyblock.IslandType;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.util.Client;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

// SIROZ-NOTE : on voit à travers les blocks : avec un Mixin c'est possible d'éviter ça,
//  mais cela a un cout assez élevé coté performance, je trouve. Sachant que Skyblocker le
//  fait déjà, et que la plupart on le Mod, mais à voir...
public class HighlightMobFeature extends Feature {

	private EntityType<?> currentEntityTypeGlow = null;

	public HighlightMobFeature() {
		this.addComponent(EntityGlowComponent.class, EntityGlowComponent.of(entity -> {
			if (currentEntityTypeGlow != null && currentEntityTypeGlow == entity.getType()) {
				return ConfigManager.getConfig().misc.highlighterColor.getRGB();
			}
			return EntityGlowComponent.EntityGlowStrategy.DEFAULT;
		}));

		// SIROZ-NOTE: remettre la fluent API
		this.addComponent(CommandComponent.class, CommandComponent.builder()
				.custom(d -> d.register(ClientCommandManager.literal("highlighter")
						.executes(context -> {
							currentEntityTypeGlow = null;
							context.getSource().sendFeedback(CaribouStonks.prefix().get()
									.append(Component.literal("Glowing entities removed.").withStyle(ChatFormatting.RED)));
							return 1;
						})
						.then(ClientCommandManager.argument("mob", EntityIdArgumentType.entityType())
								.executes(context -> {
									String entityArg = context.getArgument("mob", String.class);
									Optional<EntityType<?>> entityType = BuiltInRegistries.ENTITY_TYPE
											.getOptional(Identifier.withDefaultNamespace(entityArg));
									if (entityType.isEmpty()) {
										context.getSource().sendFeedback(CaribouStonks.prefix().get()
												.append(Component.literal("Unable to find this entity type!").withStyle(ChatFormatting.RED)));
									} else {
										if (currentEntityTypeGlow == entityType.get()) {
											currentEntityTypeGlow = null;
											context.getSource().sendFeedback(CaribouStonks.prefix().get()
													.append(Component.literal("Glowing entities removed.").withStyle(ChatFormatting.RED)));
										} else {
											currentEntityTypeGlow = entityType.get();
											context.getSource().sendFeedback(CaribouStonks.prefix().get()
													.append(Component.literal("Glowing ").withStyle(ChatFormatting.GREEN)
															.append(entityType.get().getDescription()).withStyle(ChatFormatting.YELLOW)));
										}
									}
									return 1;
								})
						)
				))
				.build());
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock()
				&& SkyBlockAPI.getIsland() != IslandType.DUNGEON
				&& currentEntityTypeGlow != null;
	}

	@Override
	protected void onClientJoinServer() {
		if (currentEntityTypeGlow != null) {
			Client.sendMessageWithPrefix(Component.literal("Glowing entities are no longer displayed due to a server change.").withStyle(ChatFormatting.RED));
		}

		currentEntityTypeGlow = null;
	}

	private static final class EntityIdArgumentType implements ArgumentType<String> {
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
}
