package fr.siroz.cariboustonks.core.mod;

import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.core.service.scheduler.TickScheduler;
import fr.siroz.cariboustonks.events.EventHandler;
import fr.siroz.cariboustonks.events.SkyBlockEvents;
import fr.siroz.cariboustonks.util.Client;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

final class WelcomeMessage {

	private static final Locale LOCALE = Locale.getDefault();
	private static final String SEPARATOR = "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬";

	WelcomeMessage() {
		if (ConfigManager.getConfig().general.firstTimeWithTheMod) {
			SkyBlockEvents.JOIN_EVENT.register(_s -> onJoin());
		}
	}

	@EventHandler(event = "SkyBlockEvents.JOIN_EVENT")
	private void onJoin() {
		TickScheduler.getInstance().runLater(() -> {
			if (LOCALE.getLanguage().equalsIgnoreCase("fr")) {
				sendWelcomeMessageForMyFrenchUsers();
			} else {
				sendWelcomeMessageForImposters();
			}

			ConfigManager.getConfig().general.firstTimeWithTheMod = false;
			ConfigManager.saveConfig();
		}, 1, TimeUnit.SECONDS);
	}

	private void sendWelcomeMessageForImposters() {
		Client.sendMessage(Component.literal(SEPARATOR).withStyle(ChatFormatting.RED));

		Client.sendMessage(Component.literal(" Thank you for using").withStyle(ChatFormatting.GREEN)
				.append(Component.literal(" CaribouStonks").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
				.append(Component.literal(" !").withStyle(ChatFormatting.GREEN)));

		Client.sendMessage(Component.empty());

		Client.sendMessage(Component.literal("Use ").withStyle(ChatFormatting.GREEN)
				.append(Component.literal("/cariboustonks").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
				.append(Component.literal(" to open the main mod menu.").withStyle(ChatFormatting.GREEN)));

		Client.sendMessage(Component.literal(SEPARATOR).withStyle(ChatFormatting.RED));
		Client.playSound(SoundEvents.PARROT_AMBIENT, 1f, 1.2f);
	}

	private void sendWelcomeMessageForMyFrenchUsers() {
		Client.sendMessage(Component.literal(SEPARATOR).withStyle(ChatFormatting.RED));

		Client.sendMessage(Component.literal(" Merci d'utiliser").withStyle(ChatFormatting.GREEN)
				.append(Component.literal(" CaribouStonks").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
				.append(Component.literal(" !").withStyle(ChatFormatting.GREEN)));

		Client.sendMessage(Component.empty());

		Client.sendMessage(Component.literal("Utilisez ").withStyle(ChatFormatting.GREEN)
				.append(Component.literal("/cariboustonks").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
				.append(Component.literal(" pour ouvrir le menu principal du mod.").withStyle(ChatFormatting.GREEN)));

		Client.sendMessage(Component.literal(SEPARATOR).withStyle(ChatFormatting.RED));
		Client.playSound(SoundEvents.PARROT_AMBIENT, 1f, 1.2f);
	}
}
