package fr.siroz.cariboustonks.features.reminders;

import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.core.service.scheduler.TickScheduler;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.util.Client;
import fr.siroz.cariboustonks.util.render.AnimationUtils;
import java.util.concurrent.TimeUnit;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class BoosterCookieReminderFeature extends Feature /*implements Reminder*/ {

	private static final ItemStack ICON = new ItemStack(Items.COOKIE);

	private boolean notified = false;

	public BoosterCookieReminderFeature() {
		TickScheduler.getInstance().runRepeating(this::update, 5, TimeUnit.SECONDS);
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock() && this.config().general.reminders.boosterCookie;
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

			Component message = Component.literal("You don't have a").withStyle(ChatFormatting.RED, ChatFormatting.BOLD)
					.append(Component.literal(" Booster Cookie ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
					.append(Component.literal("active!").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));

			Client.sendMessageWithPrefix(message);

			Client.showNotification(message.copy(), ICON);

			AnimationUtils.showSpecialEffect(ICON, ParticleTypes.OMINOUS_SPAWNING, 10,
					SoundEvents.BLAZE_DEATH, 1f, 0.75f);
		}
	}
}
