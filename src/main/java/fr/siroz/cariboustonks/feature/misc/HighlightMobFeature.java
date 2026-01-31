package fr.siroz.cariboustonks.feature.misc;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.skyblock.IslandType;
import fr.siroz.cariboustonks.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.feature.Feature;
import fr.siroz.cariboustonks.system.command.CommandComponent;
import fr.siroz.cariboustonks.system.command.argument.EntityIdArgumentType;
import fr.siroz.cariboustonks.system.glowing.EntityGlowProvider;
import fr.siroz.cariboustonks.util.Client;
import java.util.Optional;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

// SIROZ-NOTE : on voit à travers les blocks : avec un Mixin c'est possible d'éviter ça,
//  mais cela a un cout assez élevé coté performance, je trouve. Sachant que Skyblocker le
//  fait déjà, et que la plupart on le Mod, mais à voir...
public class HighlightMobFeature extends Feature implements EntityGlowProvider {

	private EntityType<?> currentEntityTypeGlow = null;

	public HighlightMobFeature() {
		addComponent(CommandComponent.class, d -> d.register(ClientCommandManager.literal("highlighter")
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
		));
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

	@Override
	public int getEntityGlowColor(@NotNull Entity entity) {
		if (currentEntityTypeGlow != null && currentEntityTypeGlow == entity.getType()) {
			return ConfigManager.getConfig().misc.highlighterColor.getRGB();
		}

		return DEFAULT;
	}
}
