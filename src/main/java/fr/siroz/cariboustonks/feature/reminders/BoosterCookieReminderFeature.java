package fr.siroz.cariboustonks.feature.reminders;

import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.core.scheduler.TickScheduler;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.feature.Feature;
import fr.siroz.cariboustonks.util.Client;
import fr.siroz.cariboustonks.util.render.animation.AnimationUtils;
import java.util.concurrent.TimeUnit;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public final class BoosterCookieReminderFeature extends Feature /*implements Reminder*/ {

	private static final ItemStack ICON = new ItemStack(Items.COOKIE);

	private boolean notified = false;

	public BoosterCookieReminderFeature() {
		TickScheduler.getInstance().runRepeating(this::update, 5, TimeUnit.SECONDS);
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock() && ConfigManager.getConfig().general.reminders.boosterCookie;
	}

	/*@Override
	public @NotNull String reminderType() {
		return "BOOSTER_COOKIE";
	}

	@Override
	public @NotNull ReminderDisplay display() {
		return ReminderDisplay.of(
				Text.literal("Booster Cookie").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD, Formatting.UNDERLINE),
				null,
				ICON
		);
	}

	@Override
	public void onExpire(@NotNull TimedObject timedObject) {
		AnimationUtils.showSpecialEffect(ICON, ParticleTypes.OMINOUS_SPAWNING, 10,
				SoundEvents.ENTITY_BLAZE_DEATH, 1f, 0.75f);

		Text text = Text.literal("Booster Cookie").formatted(Formatting.LIGHT_PURPLE)
				.append(Text.literal(" expired!").formatted(Formatting.RED));

		Client.sendMessageWithPrefix(text);
		Notification.show(text.copy(), ICON);
	}*/

	private void update() {
		if (!isEnabled()) return;
		if (notified) return;

		String footer = Client.getTabListFooter();
		if (footer == null || !footer.contains("Cookie Buff")) return;

		if (footer.contains("Not active! Obtain booster cookies from the community")) {
			notified = true;

			Text message = Text.literal("You don't have a").formatted(Formatting.RED, Formatting.BOLD)
					.append(Text.literal(" Booster Cookie ").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
					.append(Text.literal("active!").formatted(Formatting.RED, Formatting.BOLD));

			Client.sendMessageWithPrefix(message);

			Client.showNotification(message.copy(), ICON);

			AnimationUtils.showSpecialEffect(ICON, ParticleTypes.OMINOUS_SPAWNING, 10,
					SoundEvents.ENTITY_BLAZE_DEATH, 1f, 0.75f);
		}
	}
}
