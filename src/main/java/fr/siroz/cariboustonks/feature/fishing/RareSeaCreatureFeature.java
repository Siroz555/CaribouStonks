package fr.siroz.cariboustonks.feature.fishing;

import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.core.scheduler.TickScheduler;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.event.ChatEvents;
import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.feature.Feature;
import fr.siroz.cariboustonks.util.Client;
import fr.siroz.cariboustonks.util.InventoryUtils;
import fr.siroz.cariboustonks.util.StonksUtils;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

public class RareSeaCreatureFeature extends Feature {

	private static final Pattern DOUBLE_HOOK_PATTERN = Pattern.compile("Double Hook!(?: Woot woot!)?");

	private boolean foundCreature = false;
	private boolean doubleHook = false;

	public RareSeaCreatureFeature() {
		ClientPlayConnectionEvents.JOIN.register((_handler, _ps, _client) -> this.reset());
		ChatEvents.MESSAGE_RECEIVED.register(this::onChatMessage);
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock() && ConfigManager.getConfig().fishing.rareSeaCreatureWarning;
	}

	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	public boolean hasFoundCreature() {
		return foundCreature;
	}

	@EventHandler(event = "ChatEvents.MESSAGE_RECEIVED")
	private void onChatMessage(@NotNull Text text) {
		if (!isEnabled()) return;
		if (!hasFishingRod()) return;

		String message = StonksUtils.stripColor(text.getString());

		// Le message du Double Hook est avant le message d'un Rare Sea Creature.
		// S'il est présent, il faut attendre le prochain message, d'où le return.
		if (DOUBLE_HOOK_PATTERN.matcher(message).find()) {
			doubleHook = true;
			return;
		}

		// Un rare sea creature est trouvé, la notification se fait avec ou sans le Double Hook.
		// Si une creature est trouvé ou non, dans tous les cas le Double Hook est reset juste après.
		// #foundCreature est control ici pour permettre la priorité de la notification
		// par rapport aux autres features de fishing.
		RareSeaCreature seaCreature = RareSeaCreature.fromChatMessage(message);
		if (seaCreature != null && !foundCreature) {
			foundCreature = true;
			showNotification(seaCreature, doubleHook);
			TickScheduler.getInstance().runLater(() -> foundCreature = false, 5, TimeUnit.SECONDS);
		}
		doubleHook = false;
	}

	private void showNotification(@NotNull RareSeaCreature seaCreature, boolean doubleHook) {
		Text seaCreatureText = Text.empty()
				.append(Text.literal("[iL ").formatted(seaCreature.getColor(), Formatting.OBFUSCATED))
				.append(seaCreature.getText())
				.append(Text.literal(" Li]").formatted(seaCreature.getColor(), Formatting.OBFUSCATED));
		Text doubleHookText = doubleHook ? Text.literal("Double Hook").formatted(Formatting.GREEN) : Text.empty();
		Client.showTitleAndSubtitle(seaCreatureText, doubleHookText, 0, 40, 5);

		if (ConfigManager.getConfig().fishing.rareSeaCreatureSound) {
			if (doubleHook) {
				Client.playSound(SoundEvents.ENTITY_ENDER_DRAGON_GROWL, 2.5f, 1f);
			} else {
				Client.playSound(SoundEvents.ITEM_ARMOR_EQUIP_NETHERITE.value(), 2.5f, 1f);
			}
		}
	}

	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	private boolean hasFishingRod() {
		ItemStack item = InventoryUtils.getHeldItem();
		return item != null && !item.isEmpty() && item.isOf(Items.FISHING_ROD);
	}

	private void reset() {
		doubleHook = false;
		foundCreature = false;
	}
}
