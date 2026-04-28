package fr.siroz.cariboustonks.feature.reminders;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.core.scheduler.TickScheduler;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.feature.Feature;
import fr.siroz.cariboustonks.manager.reminder.Reminder;
import fr.siroz.cariboustonks.manager.reminder.ReminderDisplay;
import fr.siroz.cariboustonks.manager.reminder.ReminderManager;
import fr.siroz.cariboustonks.manager.reminder.TimedObject;
import fr.siroz.cariboustonks.util.Client;
import fr.siroz.cariboustonks.util.TimeUtils;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

public final class BoosterCookieReminderFeature extends Feature implements Reminder {

	private static final String REMINDER_TYPE = "BOOSTER_COOKIE";
	private static final ItemStack ICON = new ItemStack(Items.COOKIE);
	private static final Text MESSAGE = Text.literal("You don't have a Booster Cookie active!")
			.formatted(Formatting.RED, Formatting.BOLD);

	private boolean notified = false;

	public BoosterCookieReminderFeature() {
		TickScheduler.getInstance().runRepeating(this::updateBoosterCookieStatus, 10, TimeUnit.SECONDS);
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock() && ConfigManager.getConfig().general.reminders.boosterCookie;
	}

	@Override
	public @NotNull String reminderType() {
		return REMINDER_TYPE;
	}

	@Override
	public @NotNull ReminderDisplay display() {
		return ReminderDisplay.of(
				Text.literal("Booster Cookie").formatted(Formatting.GOLD, Formatting.BOLD, Formatting.UNDERLINE),
				Text.literal("Booster Cookie Inactive!").formatted(Formatting.RED),
				ICON
		);
	}

	@Override
	public void onExpire(@NotNull TimedObject timedObject) {
		warn();
	}

	private void updateBoosterCookieStatus() {
		if (CLIENT.player == null || CLIENT.world == null) return;
		if (!isEnabled()) return;

		// --- TabList ---
		// Widget "Effects" -> Cookie Buff
		Optional<String> cookieBuffOpt = CaribouStonks.core()
				.getTabListManager()
				.findLine("Active Effects", "Cookie Buff:");

		if (cookieBuffOpt.isPresent()) {
			String cookieBuff = cookieBuffOpt.get()
					.replace("Cookie Buff: ", "")
					.replace(" ", ""); // Duration = PT0S si ce n'est pas remplacé

			if (cookieBuff.equals("INACTIVE")) {
				warn();
				return;
			}

			Duration duration = TimeUtils.extractDuration(cookieBuff); // PT0S
			if (duration.isZero()) return;

			TimedObject timedObject = new TimedObject(
					"boosterCookie::cookieBuff",
					"empty",
					Instant.now().plus(duration),
					REMINDER_TYPE);

			CaribouStonks.managers()
					.getManager(ReminderManager.class)
					.addTimedObject(timedObject, true);

			return;
		}

		// --- TabList Footer ---

		if (!notified) {
			String footer = Client.getTabListFooter();
			if (footer == null || !footer.contains("Cookie Buff")) return;

			if (footer.contains("Not active! Obtain booster cookies from the community")) {
				warn();
			}
		}
	}

	private void warn() {
		if (notified) return;
		notified = true;

		MutableText message = Text.empty()
				.append(Text.literal("[Booster Cookie] ").formatted(Formatting.GOLD))
				.append(MESSAGE);
		MutableText notification = Text.empty()
				.append(Text.literal("Booster Cookie ").formatted(Formatting.GOLD, Formatting.BOLD))
				.append(Text.literal("\n"))
				.append(MESSAGE);

		Client.sendMessageWithPrefix(message);
		Client.showNotification(notification, ICON);
		if (ConfigManager.getConfig().general.reminders.playSound) {
			Client.playSound(SoundEvents.ENTITY_BLAZE_DEATH, 1f, 0.75f);
		}
	}
}
