package fr.siroz.cariboustonks.feature.slayer;

import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.event.ChatEvents;
import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.feature.Feature;
import fr.siroz.cariboustonks.manager.slayer.SlayerManager;
import fr.siroz.cariboustonks.util.Client;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Experimental
public class SlayerCocoonedWarningFeature extends Feature {

	private static final Pattern COCOONED_BOSS_PATTERN = Pattern.compile("YOU COCOONED YOUR SLAYER BOSS");

	private final SlayerManager slayerManager;

	public SlayerCocoonedWarningFeature(SlayerManager slayerManager) {
		this.slayerManager = slayerManager;
		ChatEvents.MESSAGE_RECEIVED.register(this::onMessage);
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock()
				&& slayerManager.isInQuest()
				&& ConfigManager.getConfig().slayer.slayerBossCocoonedWarning;
	}

	@EventHandler(event = "ChatEvents.MESSAGE_RECEIVED")
	private void onMessage(@NotNull Text text) {
		if (!isEnabled()) return;

		String message = text.getString();
		message = message.replaceFirst("^\\s+", "");

		Matcher cocoonBossMatcher = COCOONED_BOSS_PATTERN.matcher(message);
		if (cocoonBossMatcher.matches()) {
			Client.showTitle(Text.literal("Slayer Boss Cocooned!").formatted(Formatting.RED, Formatting.BOLD), 1, 60, 1);
			Client.playSound(SoundEvents.ENTITY_ELDER_GUARDIAN_CURSE, 1f, 1f);
		}
	}
}
