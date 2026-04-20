package fr.siroz.cariboustonks.features.reminders;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.core.component.ReminderComponent;
import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.core.model.TimedObjectModel;
import fr.siroz.cariboustonks.core.module.reminder.ReminderDisplay;
import fr.siroz.cariboustonks.core.service.scheduler.TickScheduler;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.systems.ReminderSystem;
import fr.siroz.cariboustonks.util.Client;
import fr.siroz.cariboustonks.util.TimeUtils;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;

public final class BoosterCookieReminderFeature extends Feature {

	private static final String REMINDER_TYPE = "BOOSTER_COOKIE";
	private static final ItemStack ICON = new ItemStack(Items.COOKIE);
	private static final Component MESSAGE = Component.literal("You don't have a Booster Cookie active!")
			.withStyle(ChatFormatting.RED, ChatFormatting.BOLD);

	private boolean notified = false;

	public BoosterCookieReminderFeature() {
		TickScheduler.getInstance().runRepeating(this::updateBoosterCookieStatus, 10, TimeUnit.SECONDS);

		this.addComponent(ReminderComponent.class, ReminderComponent.builder(REMINDER_TYPE)
				.display(ReminderDisplay.of(
						Component.literal("Booster Cookie").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD, ChatFormatting.UNDERLINE),
						Component.literal("Booster Cookie Inactive!").withStyle(ChatFormatting.RED),
						ICON))
				.onExpire(_e -> this.warn())
				.build());
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock() && this.config().general.reminders.boosterCookie;
	}

	private void updateBoosterCookieStatus() {
		if (CLIENT.player == null || CLIENT.level == null) return;
		if (!isEnabled()) return;

		// --- TabList ---
		// Widget "Effects" -> Cookie Buff
		Optional<String> cookieBuffOpt = CaribouStonks.skyBlock()
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

			TimedObjectModel timedObject = new TimedObjectModel(
					"boosterCookie::cookieBuff",
					"empty",
					Instant.now().plus(duration),
					REMINDER_TYPE);

			CaribouStonks.systems()
					.getSystem(ReminderSystem.class)
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

		MutableComponent message = Component.empty()
				.append(Component.literal("[Booster Cookie] ").withStyle(ChatFormatting.GOLD))
				.append(MESSAGE);
		MutableComponent notification = Component.empty()
				.append(Component.literal("Booster Cookie ").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
				.append(Component.literal("\n"))
				.append(MESSAGE);

		Client.sendMessageWithPrefix(message);
		Client.showNotification(notification, ICON);
		if (this.config().general.reminders.playSound) {
			Client.playSound(SoundEvents.BLAZE_DEATH, 1f, 0.75f);
		}
	}
}
