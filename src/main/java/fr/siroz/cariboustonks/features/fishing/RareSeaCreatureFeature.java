package fr.siroz.cariboustonks.features.fishing;

import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.core.infrastructure.scheduler.TickScheduler;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.events.ChatEvents;
import fr.siroz.cariboustonks.events.EventHandler;
import fr.siroz.cariboustonks.platform.context.PlayerContext;
import fr.siroz.cariboustonks.util.StonksUtils;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.NonNull;

public class RareSeaCreatureFeature extends Feature {

	private static final Pattern DOUBLE_HOOK_PATTERN = Pattern.compile("Double Hook!(?: Woot woot!)?");

	private boolean foundCreature = false;
	private boolean doubleHook = false;

	public RareSeaCreatureFeature() {
		ChatEvents.MESSAGE_RECEIVE_EVENT.register(this::onChatMessage);
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock() && this.config().fishing.rareSeaCreatureWarning;
	}

	@Override
	protected void onClientJoinServer() {
		doubleHook = false;
		foundCreature = false;
	}

	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	public boolean hasFoundCreature() {
		return foundCreature;
	}

	@EventHandler(event = "ChatEvents.MESSAGE_RECEIVE_EVENT")
	private void onChatMessage(@NonNull Component text) {
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
			TickScheduler.getInstance().runLater(() -> foundCreature = false, 2, TimeUnit.SECONDS);
		}
		doubleHook = false;
	}

	private void showNotification(@NonNull RareSeaCreature seaCreature, boolean doubleHook) {
		Component seaCreatureText = Component.empty()
				.append(Component.literal("[iL ").withStyle(seaCreature.getColor(), ChatFormatting.OBFUSCATED))
				.append(seaCreature.getText())
				.append(Component.literal(" Li]").withStyle(seaCreature.getColor(), ChatFormatting.OBFUSCATED));
		Component doubleHookText = doubleHook ? Component.literal("Double Hook").withStyle(ChatFormatting.GREEN) : Component.empty();
		PlayerContext.showTitleAndSubtitle(seaCreatureText, doubleHookText, 0, 40, 5);

		if (this.config().fishing.rareSeaCreatureSound) {
			if (doubleHook) {
				PlayerContext.playSound(SoundEvents.WARDEN_SONIC_BOOM, 2.5f, 1f);
			} else {
				PlayerContext.playSound(SoundEvents.ARMOR_EQUIP_NETHERITE.value(), 2.5f, 1f);
			}
		}
	}

	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	private boolean hasFishingRod() {
		ItemStack item = PlayerContext.getHeldItem();
		return item != null && !item.isEmpty() && item.is(Items.FISHING_ROD);
	}
}
