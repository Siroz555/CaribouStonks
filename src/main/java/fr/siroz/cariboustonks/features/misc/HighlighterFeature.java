package fr.siroz.cariboustonks.features.misc;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import fr.siroz.cariboustonks.core.component.CommandComponent;
import fr.siroz.cariboustonks.core.component.EntityGlowComponent;
import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.core.module.cooldown.Cooldown;
import fr.siroz.cariboustonks.core.skyblock.IslandType;
import fr.siroz.cariboustonks.events.ClientEvents;
import fr.siroz.cariboustonks.events.EventHandler;
import fr.siroz.cariboustonks.platform.context.PlayerContext;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.jspecify.annotations.NonNull;

public class HighlighterFeature extends Feature {
	private static final Cooldown COOLDOWN = Cooldown.of(1, TimeUnit.SECONDS);
	private static final Set<String> BLACKLIST = Set.of("dinnerbone", "armorstand");

	private final Set<EntityType<?>> entityTypes = new HashSet<>();
	private final Set<String> entityNames = new HashSet<>();

	public HighlighterFeature() {
		ClientEvents.MIDDLE_CLICK_AIR_EVENT.register(this::onMiddleClick);

		this.addComponent(EntityGlowComponent.class, EntityGlowComponent.builder()
				.when(entity -> entityTypes.contains(entity.getType()),
						this.config().misc.highlighterColor.getRGB())
				.when(entity -> !(entity instanceof ArmorStand)
								&& !entity.getName().getString().isEmpty()
								&& entityNames.contains(entity.getName().getString().toLowerCase(Locale.ENGLISH)),
						this.config().misc.highlighterColor.getRGB())
				.build());

		this.addComponent(CommandComponent.class, CommandComponent.builder()
				.custom(d -> d.register(ClientCommands.literal("highlighter")
						.executes(this::commandHelp)
						.then(ClientCommands.literal("clear")
								.executes(this::commandClear))
						.then(ClientCommands.literal("addEntity")
								.then(ClientCommands.argument("entity", EntityIdArgumentType.entityType())
										.executes(this::commandMinecraftEntity)))
						.then(ClientCommands.literal("addName")
								.then(ClientCommands.argument("name", StringArgumentType.greedyString())
										.executes(this::commandCustomName)))
				))
				.build());
	}

	@Override
	public boolean isEnabled() {
		return this.skyBlock().location().onSkyBlock()
				&& this.skyBlock().location().island() != IslandType.DUNGEON
				&& (!entityNames.isEmpty() || !entityTypes.isEmpty());
	}

	@EventHandler(event = "ClientEvents.MIDDLE_CLICK_AIR_EVENT")
	private void onMiddleClick() {
		if (!this.config().misc.highlighterMiddleClick) return;
		if (!COOLDOWN.test()) return;

		HitResult hitResult = MINECRAFT.hitResult;
		if (hitResult == null) return;
		if (hitResult.getType() != HitResult.Type.ENTITY) return;
		if (!(hitResult instanceof EntityHitResult entityHitResult)) return;

		handleName(entityHitResult.getEntity().getName());
	}

	private void handleName(Component entityName) {
		// SIROZ-NOTE : Le truc c'est que c'est limité, par exemple le "Ent" dans Galatea a un vrai nom,
		//  alors que le "Bogged" non, il a un nom Vanilla. Sam dans la Garden c'est une sorte de UUID. etc, etc.
		//  Les mobs "anciens" sont mal géré, alors que les nouveaux comme le "Littlefoot" porte un vrai nom.
		//  Il y a des cas particulier mais généralement les mobs avant < 2026 ont des noms en sorte de UUID
		//  ou leur nom Minecraft Vanilla, comme "Slime" etc.
		//  Mais les PLAYER en EntityType ont généralement un vrai nom, mais ceux < 2026 non la plus part du temps.

		String skyBlockName = entityName.getString();
		if (skyBlockName.isBlank()) return;

		skyBlockName = skyBlockName.toLowerCase(Locale.ENGLISH);

		String skyBlockNameChecker = skyBlockName.replace(" ", "");
		if (BLACKLIST.contains(skyBlockNameChecker)) {
			PlayerContext.sendMessageWithPrefix(Component.literal(entityName.getString() + " cannot have the Glowing effect because of its name.").withStyle(ChatFormatting.RED));
			return;
		}

		boolean hadGlowing = entityNames.remove(skyBlockName) || removeEntityTypeUsingMiddleClick(entityName.getString());
		if (!hadGlowing) entityNames.add(skyBlockName);

		PlayerContext.sendMessageWithPrefix(Component.empty()
				.append(entityName).append(Component.literal(hadGlowing
						? " no longer has the Glowing effect."
						: " now has the Glowing effect."
				).withStyle(hadGlowing ? ChatFormatting.RED : ChatFormatting.GREEN))
		);
		if (!hadGlowing) sendTip(true);
	}

	private boolean removeEntityTypeUsingMiddleClick(String entityName) {
		if (entityTypes.isEmpty()) return false;

		Optional<EntityType<?>> entityTypeOpt = entityTypes.stream()
				.filter(entityType -> entityType.getDescription().getString().equalsIgnoreCase(entityName))
				.findFirst();

		if (entityTypeOpt.isPresent()) {
			entityTypes.remove(entityTypeOpt.get());
			return true;
		} else {
			return false;
		}
	}

	private int commandHelp(CommandContext<FabricClientCommandSource> ctx) {
		PlayerContext.sendMessageWithPrefix(Component.literal("&7&l> &e/highlighter clear &7: Clear all highlighter"));
		PlayerContext.sendMessageWithPrefix(Component.literal("&7&l> &e/highlighter addEntity &7: Add a Vanilla entity"));
		PlayerContext.sendMessageWithPrefix(Component.literal("&7&l> &e/highlighter addName &7: Add a custom Mob name"));
		return 1;
	}

	private int commandClear(CommandContext<FabricClientCommandSource> ctx) {
		if (!entityNames.isEmpty()) {
			Iterator<String> nameIterator = entityNames.iterator();
			while (nameIterator.hasNext()) {
				String nextName = nameIterator.next();
				PlayerContext.sendMessageWithPrefix(Component.literal("Glowing effect removed to " + nextName).withStyle(ChatFormatting.YELLOW));
				nameIterator.remove();
			}
		}
		if (!entityTypes.isEmpty()) {
			Iterator<EntityType<?>> entityTypeIterator = entityTypes.iterator();
			while (entityTypeIterator.hasNext()) {
				Component nextName = entityTypeIterator.next().getDescription();
				PlayerContext.sendMessageWithPrefix(Component.literal("Glowing effect removed to ").withStyle(ChatFormatting.YELLOW).append(nextName));
				entityTypeIterator.remove();
			}
		}
		return 1;
	}

	private int commandMinecraftEntity(CommandContext<FabricClientCommandSource> ctx) {
		String entityArg = ctx.getArgument("entity", String.class);
		Optional<EntityType<?>> entityTypeOpt = BuiltInRegistries.ENTITY_TYPE
				.getOptional(Identifier.withDefaultNamespace(entityArg));
		if (entityTypeOpt.isEmpty()) {
			PlayerContext.sendMessageWithPrefix(Component.literal("Unable to find this entity type!").withStyle(ChatFormatting.RED));
		} else {
			EntityType<?> entityType = entityTypeOpt.get();
			if (entityTypes.contains(entityType)) {
				entityTypes.remove(entityType);
				PlayerContext.sendMessageWithPrefix(Component.literal("Removed Glowing to ").withStyle(ChatFormatting.RED).append(entityType.getDescription()));
			} else {
				entityTypes.add(entityType);
				PlayerContext.sendMessageWithPrefix(Component.literal("Added Glowing to ").withStyle(ChatFormatting.GREEN).append(entityType.getDescription()));
				sendTip(false);
			}
		}
		return 1;
	}

	private int commandCustomName(CommandContext<FabricClientCommandSource> ctx) {
		String custom = StringArgumentType.getString(ctx, "name");
		if (custom != null && !custom.isBlank() && custom.length() >= 3) {
			String entityName = custom.toLowerCase(Locale.ENGLISH);
			if (entityNames.contains(entityName)) {
				entityNames.remove(entityName);
				PlayerContext.sendMessageWithPrefix(Component.literal("Removed Glowing to " + custom).withStyle(ChatFormatting.RED));
			} else {
				entityNames.add(entityName);
				PlayerContext.sendMessageWithPrefix(Component.literal(custom + " can now have the Glowing effect.").withStyle(ChatFormatting.GREEN));
				sendTip(false);
			}
		} else {
			PlayerContext.sendMessageWithPrefix(Component.literal("Unable to add this entity name. (null or already registered)").withStyle(ChatFormatting.RED));
		}
		return 1;
	}

	private void sendTip(boolean full) {
		PlayerContext.sendMessage(Component.literal(" | MIDDLE-CLICK on the target to disable!").withStyle(ChatFormatting.DARK_GRAY));
		if (full) PlayerContext.sendMessage(Component.literal(" | You can disable MIDDLE-CLICKING in Misc Category.").withStyle(ChatFormatting.DARK_GRAY));
	}

	private static final class EntityIdArgumentType implements ArgumentType<String> {
		private static final List<String> ENTITY_ID_LIST = BuiltInRegistries.ENTITY_TYPE.keySet().stream()
				.map(Identifier::getPath)
				.toList();

		public static @NonNull EntityIdArgumentType entityType() {
			return new EntityIdArgumentType();
		}

		@Override
		public @NonNull String parse(@NonNull StringReader reader) throws CommandSyntaxException {
			String name = reader.readString();
			for (String id : ENTITY_ID_LIST) {
				if (id.equalsIgnoreCase(name)) {
					return id;
				}
			}

			throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().create();
		}

		@Override
		public <S> CompletableFuture<Suggestions> listSuggestions(@NonNull CommandContext<S> context, SuggestionsBuilder builder) {
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
