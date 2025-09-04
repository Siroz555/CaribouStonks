package fr.siroz.cariboustonks.feature.slayer;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.core.scheduler.TickScheduler;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.event.ChatEvents;
import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.feature.Feature;
import fr.siroz.cariboustonks.manager.slayer.SlayerManager;
import fr.siroz.cariboustonks.util.Client;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

public class SlayerCocoonedWarningFeature extends Feature {

	private static final Pattern COCOONED_BOSS_PATTERN = Pattern.compile("YOU COCOONED YOUR SLAYER BOSS");

	private final SlayerManager slayerManager;

	private static boolean cocoonedBoss = false;

	public SlayerCocoonedWarningFeature() {
		this.slayerManager = CaribouStonks.managers().getManager(SlayerManager.class);;
		ClientWorldEvents.AFTER_CLIENT_WORLD_CHANGE.register((_c, _w) -> cocoonedBoss = false);
		ChatEvents.MESSAGE_RECEIVED.register(this::onMessage);
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock()
				&& slayerManager.isInQuest()
				&& ConfigManager.getConfig().slayer.slayerBossCocoonedWarning;
	}

	public static boolean isCocoonedBoss() {
		return cocoonedBoss;
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
			cocoonedBoss = true;
			TickScheduler.getInstance().runLater(() -> cocoonedBoss = false, 3, TimeUnit.SECONDS);
		}
	}
}
