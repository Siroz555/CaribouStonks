package fr.siroz.cariboustonks.core;

import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.core.scheduler.TickScheduler;
import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.event.SkyBlockEvents;
import fr.siroz.cariboustonks.util.Client;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

final class WelcomeMessage {

	private static final Locale LOCALE = Locale.getDefault();
	private static final String SEPARATOR = "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬";

	WelcomeMessage() {
		if (ConfigManager.getConfig().general.firstTimeWithTheMod) {
			SkyBlockEvents.JOIN.register(_s -> onJoin());
		}
	}

	@EventHandler(event = "SkyBlockEvents.JOIN")
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
		Client.sendMessage(Text.literal(SEPARATOR).formatted(Formatting.RED));

		Client.sendMessage(Text.literal(" Thank you for using").formatted(Formatting.GREEN)
				.append(Text.literal(" CaribouStonks").formatted(Formatting.RED, Formatting.BOLD))
				.append(Text.literal(" !").formatted(Formatting.GREEN)));

		Client.sendMessage(Text.empty());

		Client.sendMessage(Text.literal("Use ").formatted(Formatting.GREEN)
				.append(Text.literal("/cariboustonks").formatted(Formatting.YELLOW, Formatting.BOLD))
				.append(Text.literal(" to open the main mod menu.").formatted(Formatting.GREEN)));

		Client.sendMessage(Text.literal(SEPARATOR).formatted(Formatting.RED));
		Client.playSound(SoundEvents.ENTITY_PARROT_AMBIENT, 1f, 1.2f);
	}

	private void sendWelcomeMessageForMyFrenchUsers() {
		Client.sendMessage(Text.literal(SEPARATOR).formatted(Formatting.RED));

		Client.sendMessage(Text.literal(" Merci d'utiliser").formatted(Formatting.GREEN)
				.append(Text.literal(" CaribouStonks").formatted(Formatting.RED, Formatting.BOLD))
				.append(Text.literal(" !").formatted(Formatting.GREEN)));

		Client.sendMessage(Text.empty());

		Client.sendMessage(Text.literal("Utilisez ").formatted(Formatting.GREEN)
				.append(Text.literal("/cariboustonks").formatted(Formatting.YELLOW, Formatting.BOLD))
				.append(Text.literal(" pour ouvrir le menu principal du mod.").formatted(Formatting.GREEN)));

		Client.sendMessage(Text.literal(SEPARATOR).formatted(Formatting.RED));
		Client.playSound(SoundEvents.ENTITY_PARROT_AMBIENT, 1f, 1.2f);
	}
}
