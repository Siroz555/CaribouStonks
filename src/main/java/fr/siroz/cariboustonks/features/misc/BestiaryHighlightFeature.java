package fr.siroz.cariboustonks.features.misc;

import com.mojang.brigadier.arguments.StringArgumentType;
import fr.siroz.cariboustonks.core.component.CommandComponent;
import fr.siroz.cariboustonks.core.component.EntityGlowComponent;
import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.core.module.cooldown.Cooldown;
import fr.siroz.cariboustonks.core.skyblock.IslandType;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.events.ClientEvents;
import fr.siroz.cariboustonks.events.EventHandler;
import fr.siroz.cariboustonks.util.Client;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class BestiaryHighlightFeature extends Feature {

	private static final Cooldown COOLDOWN = Cooldown.of(1500, TimeUnit.MILLISECONDS);

	private final Set<String> entityNames = new HashSet<>();
	private final List<String> blacklist = List.of("dinnerbone", "armorstand");

	public BestiaryHighlightFeature() {
		ClientEvents.MIDDLE_CLICK_AIR_EVENT.register(this::onMiddleClick);

		this.addComponent(EntityGlowComponent.class, EntityGlowComponent.builder()
				.when(entity -> !(entity instanceof ArmorStand)
						&& !entity.getName().getString().isEmpty()
						&& entityNames.contains(entity.getName().getString().toLowerCase(Locale.ENGLISH)
				), this.config().misc.highlighterColor.getRGB())
				.build());

		this.addComponent(CommandComponent.class, CommandComponent.builder().standalone("bestiaryHighlight", builder -> {
			builder.then(ClientCommands.literal("clear").executes(_ -> {
				if (!entityNames.isEmpty()) {
					Iterator<String> nameIterator = entityNames.iterator();
					while (nameIterator.hasNext()) {
						String nextName = nameIterator.next();
						Client.sendMessageWithPrefix(Component.literal("Glowing effect removed to " + nextName).withStyle(ChatFormatting.YELLOW));
						nameIterator.remove();
					}
				}
				return 1;
			}));
			builder.then(ClientCommands.literal("add").then(ClientCommands.argument("name", StringArgumentType.string()).executes(ctx -> {
				String custom = StringArgumentType.getString(ctx, "name");
				if (custom != null && !custom.isBlank() && custom.length() >= 3) {
					entityNames.add(custom);
					Client.sendMessageWithPrefix(Component.literal(custom + " can now have the Glowing effect.").withStyle(ChatFormatting.GREEN));
					Client.sendMessage(Component.literal(" | MIDDLE-CLICK on the target to disable!").withStyle(ChatFormatting.DARK_GRAY));
				} else {
					Client.sendMessageWithPrefix(Component.literal("Unable to add this entity name. (null or already registered)").withStyle(ChatFormatting.RED));
				}
				return 1;
			})));
		}).build());
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock()
				&& SkyBlockAPI.getIsland() != IslandType.DUNGEON
				&& SkyBlockAPI.getIsland() != IslandType.KUUDRA_HOLLOW
				&& this.config().misc.bestiaryHighlight;
	}

	@EventHandler(event = "ClientEvents.MIDDLE_CLICK_AIR_EVENT")
	private void onMiddleClick() {
		if (!isEnabled()) return;
		if (!COOLDOWN.test()) return;

		HitResult hitResult = CLIENT.hitResult;
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
		if (blacklist.contains(skyBlockNameChecker)) {
			Client.sendMessageWithPrefix(Component.literal(skyBlockName + " cannot have the Glowing effect because of its name.").withStyle(ChatFormatting.RED));
			return;
		}

		boolean hadGlowing = entityNames.remove(skyBlockName);
		if (!hadGlowing) entityNames.add(skyBlockName);

		Client.sendMessageWithPrefix(Component.empty().append(entityName).append(Component.literal(hadGlowing
						? " no longer has the Glowing effect."
						: " now has the Glowing effect.")
				.withStyle(hadGlowing ? ChatFormatting.RED : ChatFormatting.GREEN))
		);
		Client.sendMessage(Component.literal(" | You can disable MIDDLE-CLICKING in Misc Category.").withStyle(ChatFormatting.DARK_GRAY));
	}
}
