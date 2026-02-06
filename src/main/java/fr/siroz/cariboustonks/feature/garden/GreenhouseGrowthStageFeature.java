package fr.siroz.cariboustonks.feature.garden;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.core.component.ContainerMatcherComponent;
import fr.siroz.cariboustonks.core.component.ContainerOverlayComponent;
import fr.siroz.cariboustonks.core.component.ReminderComponent;
import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.core.module.reminder.ReminderDisplay;
import fr.siroz.cariboustonks.core.module.reminder.TimedObject;
import fr.siroz.cariboustonks.core.skyblock.IslandType;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.system.ReminderSystem;
import fr.siroz.cariboustonks.util.Client;
import fr.siroz.cariboustonks.util.ItemUtils;
import fr.siroz.cariboustonks.util.render.gui.ColorHighlight;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.time.Instant;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.NonNull;

public class GreenhouseGrowthStageFeature extends Feature {

	private static final Pattern TITLE_PATTERN = Pattern.compile("^Crop Diagnostics$");
	private static final String REMINDER_TYPE = "GREENHOUSE_GROWTH_STAGE";

	private static final Pattern GROWTH_STAGE_PATTERN = Pattern.compile("Next Stage:\\s*(?:(\\d+)\\s*h)?\\s*(?:(\\d+)\\s*m)?\\s*(?:(\\d+)\\s*s)?");
	private static final int GROWTH_STAGE_SLOT = 20;
	private static final ItemStack ICON = new ItemStack(Items.JUNGLE_SAPLING);

	public GreenhouseGrowthStageFeature() {
		this.addComponent(ReminderComponent.class, ReminderComponent.builder(REMINDER_TYPE)
				.display(getReminderDisplay())
				.onExpire(this::onReminderExpire)
				.build());

		this.addComponent(ContainerMatcherComponent.class, ContainerMatcherComponent.of(TITLE_PATTERN));
		this.addComponent(ContainerOverlayComponent.class, ContainerOverlayComponent.builder()
				.content(this::contentAnalyzer)
				.build());
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock()
				&& SkyBlockAPI.getIsland() == IslandType.GARDEN
				&& this.config().farming.garden.greenhouseGrowthStageReminder;
	}

	@NonNull
	private List<ColorHighlight> contentAnalyzer(@NonNull Int2ObjectMap<ItemStack> slots) {
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
					REMINDER_TYPE);

			CaribouStonks.systems()
					.getSystem(ReminderSystem.class)
					.addTimedObject(timedObject, true);
		}

		return List.of();
	}

	private @NonNull ReminderDisplay getReminderDisplay() {
		return ReminderDisplay.of(
				Component.literal("Greenhouse").withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.BOLD, ChatFormatting.UNDERLINE),
				Component.literal("Next Growth Stage").withStyle(ChatFormatting.GREEN),
				ICON
		);
	}

	private void onReminderExpire(@NonNull TimedObject timedObject) {
		MutableComponent message = Component.empty()
				.append(Component.literal("[Greenhouse] ").withStyle(ChatFormatting.DARK_GREEN))
				.append(Component.literal("Next Growth Stage is reached!").withStyle(ChatFormatting.GREEN));
		MutableComponent notification = Component.empty()
				.append(Component.literal("Greenhouse").withStyle(ChatFormatting.DARK_GREEN))
				.append(Component.literal("\n"))
				.append(Component.literal("Next Growth Stage is reached!").withStyle(ChatFormatting.GREEN));

		Client.sendMessageWithPrefix(message);
		Client.showNotification(notification, ICON);

		if (this.config().general.reminders.playSound) {
			Client.playSound(SoundEvents.NOTE_BLOCK_PLING.value(), 1f, 1f);
		}
	}
}
