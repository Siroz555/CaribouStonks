package fr.siroz.cariboustonks.feature.misc;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.feature.Feature;
import fr.siroz.cariboustonks.manager.command.CommandRegistration;
import fr.siroz.cariboustonks.manager.command.argument.EntityIdArgumentType;
import fr.siroz.cariboustonks.util.Client;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Optional;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Experimental
public class HighlightMobFeature extends Feature implements CommandRegistration {

	private final Object2IntMap<Entity> cachedEntities = new Object2IntOpenHashMap<>();
	private EntityType<?> currentEntityTypeGlow = null;

	public HighlightMobFeature() {
		ClientWorldEvents.AFTER_CLIENT_WORLD_CHANGE.register(this::onClientChangeWorld);
		ClientTickEvents.END_WORLD_TICK.register(client -> this.cachedEntities.clear());
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock() && currentEntityTypeGlow != null;
	}

	@EventHandler(event = "ClientWorldEvents.AFTER_CLIENT_WORLD_CHANGE")
	private void onClientChangeWorld(MinecraftClient client, ClientWorld _clientWorld) {
		if (currentEntityTypeGlow != null) {
			Client.sendMessageWithPrefix(Text.literal("Glowing entities are no longer displayed due to a server change.").formatted(Formatting.RED));
		}

		currentEntityTypeGlow = null;
	}

	@Override
	public void register(@NotNull CommandDispatcher<FabricClientCommandSource> dispatcher, @NotNull CommandRegistryAccess registryAccess) {
		dispatcher.register(ClientCommandManager.literal("highlighter")
				.executes(context -> {
					currentEntityTypeGlow = null;
					context.getSource().sendFeedback(CaribouStonks.prefix().get()
							.append(Text.literal("Glowing entities removed.").formatted(Formatting.RED)));
					return Command.SINGLE_SUCCESS;
				})
				.then(ClientCommandManager.argument("mob", EntityIdArgumentType.entityType())
						.executes(context -> {
							String entityArg = context.getArgument("mob", String.class);
							Optional<EntityType<?>> entityType = Registries.ENTITY_TYPE
									.getOptionalValue(Identifier.ofVanilla(entityArg));
							if (entityType.isEmpty()) {
								context.getSource().sendFeedback(CaribouStonks.prefix().get()
										.append(Text.literal("Unable to find this entity type!").formatted(Formatting.RED)));
							} else {
								if (currentEntityTypeGlow == entityType.get()) {
									currentEntityTypeGlow = null;
									context.getSource().sendFeedback(CaribouStonks.prefix().get()
											.append(Text.literal("Glowing entities removed.").formatted(Formatting.RED)));
								} else {
									currentEntityTypeGlow = entityType.get();
									context.getSource().sendFeedback(CaribouStonks.prefix().get()
											.append(Text.literal("Glowing ").formatted(Formatting.GREEN)
													.append(entityType.get().getName()).formatted(Formatting.YELLOW)));
								}
							}

							return Command.SINGLE_SUCCESS;
						})
				)
		);
	}

	public int getGlowColorOrDefault(Entity entity, int defaultColor) {
		if (!isEnabled()) return defaultColor;
		return cachedEntities.getOrDefault(entity, defaultColor);
	}

	public boolean hasOrComputeGlowColor(Entity entity) {
		if (!isEnabled()) return false;
		if (cachedEntities.containsKey(entity)) return true;

		int color = getMobGlow(entity);
		if (color != 0) {
			cachedEntities.put(entity, color);
			return true;
		}

		return false;
	}

	private int getMobGlow(Entity entity) {
		if (currentEntityTypeGlow != null && currentEntityTypeGlow == entity.getType()) {
			return ConfigManager.getConfig().misc.highlighterColor.getRGB();
		}

		return 0;
	}
}
