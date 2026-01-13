package fr.siroz.cariboustonks.feature.garden;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.core.skyblock.IslandType;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.feature.Feature;
import fr.siroz.cariboustonks.manager.container.ContainerMatcherTrait;
import fr.siroz.cariboustonks.manager.container.overlay.ContainerOverlay;
import fr.siroz.cariboustonks.manager.reminder.Reminder;
import fr.siroz.cariboustonks.manager.reminder.ReminderDisplay;
import fr.siroz.cariboustonks.manager.reminder.ReminderManager;
import fr.siroz.cariboustonks.manager.reminder.TimedObject;
import fr.siroz.cariboustonks.util.Client;
import fr.siroz.cariboustonks.util.ItemUtils;
import fr.siroz.cariboustonks.util.render.gui.ColorHighlight;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Experimental // TODO - Une fois que Hypixel aura patch le "bug de 4h", avoir quelque chose de continu.
public class GreenhouseGrowthStageFeature extends Feature implements ContainerMatcherTrait, ContainerOverlay, Reminder {

	private static final Pattern TITLE_PATTERN = Pattern.compile("^Crop Diagnostics$");

	private static final Pattern GROWTH_STAGE_PATTERN = Pattern.compile("Next Stage:\\s*(?:(\\d+)\\s*h)?\\s*(?:(\\d+)\\s*m)?\\s*(?:(\\d+)\\s*s)?");
	private static final int GROWTH_STAGE_SLOT = 20;
	private static final ItemStack ICON = new ItemStack(Items.JUNGLE_SAPLING);

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock()
				&& SkyBlockAPI.getIsland() == IslandType.GARDEN
				&& ConfigManager.getConfig().farming.garden.greenhouseGrowthStageReminder;
	}

	@Override
	public @Nullable Pattern getTitlePattern() {
		return TITLE_PATTERN;
	}

	@Override
	public @NotNull List<ColorHighlight> content(@NotNull Int2ObjectMap<ItemStack> slots) {

		String growthStage = ItemUtils.getConcatenatedLore(slots.get(GROWTH_STAGE_SLOT));
		Matcher growthStageMatcher = GROWTH_STAGE_PATTERN.matcher(growthStage);
		if (growthStageMatcher.find()) {
			String hoursStr = growthStageMatcher.group(1);
			String minutesStr = growthStageMatcher.group(2);
			String secondsStr = growthStageMatcher.group(3);

			// Si aucun groupe n'est présent, genre "FULLY GROWTH"
			if (hoursStr == null && minutesStr == null && secondsStr == null) {
				return List.of();
			}

			long totalSeconds = 0L;
			if (hoursStr != null) totalSeconds += Long.parseLong(hoursStr) * 3600L;
			if (minutesStr != null) totalSeconds += Long.parseLong(minutesStr) * 60L;
			if (secondsStr != null) totalSeconds += Long.parseLong(secondsStr);

			Instant nextStage = Instant.now().plusSeconds(totalSeconds);

			TimedObject timedObject = new TimedObject(
					"greenhouse::next",
					"empty",
					nextStage,
					reminderType());

			CaribouStonks.managers()
					.getManager(ReminderManager.class)
					.addTimedObject(timedObject, true);
		}

		return List.of();
	}

	@Override
	public @NotNull String reminderType() {
		return "GREENHOUSE_GROWTH_STAGE";
	}

	@Override
	public @NotNull ReminderDisplay display() {
		return ReminderDisplay.of(
				Component.literal("Greenhouse").withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.BOLD, ChatFormatting.UNDERLINE),
				Component.literal("Next Growth Stage").withStyle(ChatFormatting.GREEN),
				ICON
		);
	}

	@Override
	public void onExpire(@NotNull TimedObject timedObject) {
		MutableComponent message = Component.empty()
				.append(Component.literal("[Greenhouse] ").withStyle(ChatFormatting.DARK_GREEN))
				.append(Component.literal("Next Growth Stage is reached!").withStyle(ChatFormatting.GREEN));
		MutableComponent notification = Component.empty()
				.append(Component.literal("Greenhouse").withStyle(ChatFormatting.DARK_GREEN))
				.append(Component.literal("\n"))
				.append(Component.literal("Next Growth Stage is reached!").withStyle(ChatFormatting.GREEN));

		Client.sendMessageWithPrefix(message);
		Client.showNotification(notification, ICON);
		if (ConfigManager.getConfig().general.reminders.playSound) {
			Client.playSound(SoundEvents.NOTE_BLOCK_PLING.value(), 1f, 1f);
		}
	}

	@Override
	public Optional<Duration> preNotifyDuration() {
		return Optional.of(Duration.ofMinutes(ConfigManager.getConfig().farming.garden.greenhouseGrowthStagePreReminderTime));
	}

	@Override
	public void onPreExpire(@NotNull TimedObject timedObject) {
		if (!ConfigManager.getConfig().farming.garden.greenhouseGrowthStagePreReminder) return;

		MutableComponent message = Component.empty()
				.append(Component.literal("[Greenhouse] ").withStyle(ChatFormatting.DARK_GREEN))
				.append(Component.literal("The Next Growth Stage will be reached in 5 minutes!").withStyle(ChatFormatting.GREEN));
		MutableComponent notification = Component.empty()
				.append(Component.literal("Greenhouse").withStyle(ChatFormatting.DARK_GREEN))
				.append(Component.literal("\n"))
				.append(Component.literal("The Next Growth Stage will be reached in 5 minutes!").withStyle(ChatFormatting.GREEN));

		Client.sendMessageWithPrefix(message);
		Client.showNotification(notification, ICON);
		if (ConfigManager.getConfig().general.reminders.playSound) {
			Client.playSound(SoundEvents.NOTE_BLOCK_PLING.value(), 1f, 1f);
		}
	}
}
