package fr.siroz.cariboustonks.feature.slayer;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.core.service.scheduler.TickScheduler;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.core.skyblock.slayer.SlayerManager;
import fr.siroz.cariboustonks.event.ChatEvents;
import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.util.Client;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import org.jetbrains.annotations.NotNull;

public class SlayerCocoonedWarningFeature extends Feature {

	private static final Pattern COCOONED_BOSS_PATTERN = Pattern.compile("YOU COCOONED YOUR SLAYER BOSS");

	private final SlayerManager slayerManager;

	private static boolean cocoonedBoss = false;

	public SlayerCocoonedWarningFeature() {
		this.slayerManager = CaribouStonks.skyBlock().getSlayerManager();
		ChatEvents.MESSAGE_RECEIVED.register(this::onMessage);
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock()
				&& slayerManager.isInQuest()
				&& ConfigManager.getConfig().slayer.slayerBossCocoonedWarning;
	}

	@Override
	protected void onClientJoinServer() {
		cocoonedBoss = false;
	}

	public static boolean isCocoonedBoss() {
		return cocoonedBoss;
	}

	@EventHandler(event = "ChatEvents.MESSAGE_RECEIVED")
	private void onMessage(@NotNull Component text) {
		if (!isEnabled()) return;

		String message = text.getString();
		message = message.replaceFirst("^\\s+", "");

		Matcher cocoonBossMatcher = COCOONED_BOSS_PATTERN.matcher(message);
		if (cocoonBossMatcher.matches()) {
			Client.showTitle(Component.literal("Slayer Boss Cocooned!").withStyle(ChatFormatting.RED, ChatFormatting.BOLD), 1, 60, 1);
			Client.playSound(SoundEvents.ELDER_GUARDIAN_CURSE, 1f, 1f);
			cocoonedBoss = true;
			TickScheduler.getInstance().runLater(() -> cocoonedBoss = false, 3, TimeUnit.SECONDS);
		}
	}
}
