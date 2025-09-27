package fr.siroz.cariboustonks.feature.combat;

import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.core.scheduler.TickScheduler;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.event.ChatEvents;
import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.feature.Feature;
import fr.siroz.cariboustonks.util.Client;
import fr.siroz.cariboustonks.util.StonksUtils;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

public class SpiritMaskFeature extends Feature {

	// TODO - Afficher un HUD pour afficher le temps avant le ready.
	//  Maybe avec le Phoenix et le Bonzo Mask? fusionné pour la même "ability"?

	private static final Pattern SPIRIT_MASK_PATTERN = Pattern.compile("Second Wind Activated! Your Spirit Mask saved your life!");

	public SpiritMaskFeature() {
		ChatEvents.MESSAGE_RECEIVED.register(this::onChatMessage);
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock() && ConfigManager.getConfig().combat.secondLife.spiritMaskUsed;
	}

	@EventHandler(event = "ChatEvents.MESSAGE_RECEIVED")
	private void onChatMessage(@NotNull Text text) {
		if (!isEnabled()) return;

		String message = StonksUtils.stripColor(text.getString());
		if (SPIRIT_MASK_PATTERN.matcher(message).matches()) {
			spiritMaskUsed();
		}
	}

	private void spiritMaskUsed() {
		Client.showTitle(Text.literal("Spirit Mask used!").formatted(Formatting.RED), 0, 30, 0);
		// Afficher un message/title quand le Spirit Mask est de nouveau ready.
		if (ConfigManager.getConfig().combat.secondLife.spiritMaskBack) {
			TickScheduler.getInstance().runLater(this::spiritMaskBack, 30, TimeUnit.SECONDS);
		}
	}

	private void spiritMaskBack() {
		Text message = Text.empty()
				.append(Text.literal("Spirit Mask").formatted(Formatting.DARK_PURPLE))
				.append(Text.literal(" is back!").formatted(Formatting.GREEN));
		Client.sendMessageWithPrefix(message);
		Client.showTitle(Text.literal("Spirit Mask ready!").formatted(Formatting.GREEN), 0, 30, 0);
		Client.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
	}
}
