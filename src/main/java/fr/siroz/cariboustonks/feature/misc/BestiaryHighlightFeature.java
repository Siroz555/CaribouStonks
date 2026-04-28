package fr.siroz.cariboustonks.feature.misc;

import com.mojang.brigadier.arguments.StringArgumentType;
import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.core.skyblock.IslandType;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.event.MouseEvents;
import fr.siroz.cariboustonks.feature.Feature;
import fr.siroz.cariboustonks.manager.command.CommandComponent;
import fr.siroz.cariboustonks.manager.glowing.EntityGlowProvider;
import fr.siroz.cariboustonks.util.Client;
import fr.siroz.cariboustonks.util.cooldown.Cooldown;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import org.jetbrains.annotations.NotNull;

public class BestiaryHighlightFeature extends Feature implements EntityGlowProvider {

	private static final Cooldown COOLDOWN = Cooldown.of(1500, TimeUnit.MILLISECONDS);

	private final Set<String> entityNames = new HashSet<>();
	private final List<String> blacklist = List.of("dinnerbone", "armorstand");

	public BestiaryHighlightFeature() {
		MouseEvents.MIDDLE_CLICK_AIR_EVENT.register(this::onMiddleClick);

		this.addComponent(CommandComponent.class, dispatcher -> {
			dispatcher.register(ClientCommandManager.literal("bestiaryHighlight")
					.then(ClientCommandManager.literal("clear").executes(ctx -> {
						if (!entityNames.isEmpty()) {
							Iterator<String> nameIterator = entityNames.iterator();
							while (nameIterator.hasNext()) {
								String nextName = nameIterator.next();
								Client.sendMessageWithPrefix(Text.literal("Glowing effect removed to " + nextName).formatted(Formatting.YELLOW));
								nameIterator.remove();
							}
						}
						return 1;
					}))
					.then(ClientCommandManager.literal("add").then(ClientCommandManager.argument("add", StringArgumentType.string()).executes(context -> {
						String custom = StringArgumentType.getString(context, "name");
						if (custom != null && !custom.isBlank() && custom.length() >= 3) {
							entityNames.add(custom);
							Client.sendMessageWithPrefix(Text.literal(custom + " can now have the Glowing effect.").formatted(Formatting.GREEN));
							Client.sendMessage(Text.literal(" | MIDDLE-CLICK on the target to disable!").formatted(Formatting.DARK_GRAY));
						} else {
							Client.sendMessageWithPrefix(Text.literal("Unable to add this entity name. (null or already registered)").formatted(Formatting.RED));
						}
						return 1;
					})))
			);
		});
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock()
				&& SkyBlockAPI.getIsland() != IslandType.DUNGEON
				&& SkyBlockAPI.getIsland() != IslandType.KUUDRA_HOLLOW
				&& ConfigManager.getConfig().misc.bestiaryHighlight;
	}

	@Override
	public int getEntityGlowColor(@NotNull Entity entity) {
		if (!(entity instanceof ArmorStandEntity)
				&& !entity.getName().getString().isEmpty()
				&& entityNames.contains(entity.getName().getString().toLowerCase(Locale.ENGLISH)
		)) {
			return ConfigManager.getConfig().misc.highlighterColor.getRGB();
		} else {
			return EntityGlowProvider.DEFAULT;
		}
	}

	@EventHandler(event = "ClientEvents.MIDDLE_CLICK_AIR_EVENT")
	private void onMiddleClick() {
		if (!isEnabled()) return;
		if (!COOLDOWN.test()) return;

		HitResult hitResult = CLIENT.crosshairTarget;
		if (hitResult == null) return;
		if (hitResult.getType() != HitResult.Type.ENTITY) return;
		if (!(hitResult instanceof EntityHitResult entityHitResult)) return;

		handleName(entityHitResult.getEntity().getName());
	}

	private void handleName(Text entityName) {
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
		if (blacklist.contains(skyBlockNameChecker)) {
			Client.sendMessageWithPrefix(Text.literal(skyBlockName + " cannot have the Glowing effect because of its name.").formatted(Formatting.RED));
			return;
		}

		boolean hadGlowing = entityNames.remove(skyBlockName);
		if (!hadGlowing) entityNames.add(skyBlockName);

		Client.sendMessageWithPrefix(Text.empty().append(entityName).append(Text.literal(hadGlowing
						? " no longer has the Glowing effect."
						: " now has the Glowing effect.")
				.formatted(hadGlowing ? Formatting.RED : Formatting.GREEN))
		);
		Client.sendMessage(Text.literal(" | You can disable MIDDLE-CLICKING in Misc Category.").formatted(Formatting.DARK_GRAY));
	}
}
