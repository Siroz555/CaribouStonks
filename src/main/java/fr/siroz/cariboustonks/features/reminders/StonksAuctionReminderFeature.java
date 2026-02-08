package fr.siroz.cariboustonks.features.reminders;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.core.component.ContainerMatcherComponent;
import fr.siroz.cariboustonks.core.component.ContainerOverlayComponent;
import fr.siroz.cariboustonks.core.component.ReminderComponent;
import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.core.model.TimedObjectModel;
import fr.siroz.cariboustonks.core.module.gui.ColorHighlight;
import fr.siroz.cariboustonks.core.module.reminder.ReminderDisplay;
import fr.siroz.cariboustonks.core.skyblock.IslandType;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.system.ReminderSystem;
import fr.siroz.cariboustonks.util.Client;
import fr.siroz.cariboustonks.util.ItemUtils;
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

public final class StonksAuctionReminderFeature extends Feature {

	private static final Pattern TITLE_PATTERN = Pattern.compile("^Stonks Auction$");

	private static final Pattern NEXT_AUCTION_PATTERN = Pattern.compile("Auction ends in\\s*(?:(\\d+)\\s*h)?\\s*(?:(\\d+)\\s*m)?\\s*(?:(\\d+)\\s*s)?");
	private static final int BID_SLOT = 11;
	private static final String REMINDER_TYPE = "NEXT_STONKS_AUCTION";
	private static final ItemStack ICON = new ItemStack(Items.PAPER);

	public StonksAuctionReminderFeature() {
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
				&& SkyBlockAPI.getIsland() == IslandType.HUB
				&& this.config().general.reminders.stonksAuction;
	}

	private @NonNull List<ColorHighlight> contentAnalyzer(@NonNull Int2ObjectMap<ItemStack> slots) {
		String bidItem = ItemUtils.getConcatenatedLore(slots.get(BID_SLOT));
		Matcher bidItemMatcher = NEXT_AUCTION_PATTERN.matcher(bidItem);
		if (bidItemMatcher.find()) {
			String hoursStr = bidItemMatcher.group(1);
			String minutesStr = bidItemMatcher.group(2);
			String secondsStr = bidItemMatcher.group(3);

			if (hoursStr == null && minutesStr == null && secondsStr == null) {
				return List.of();
			}

			long totalSeconds = 0L;
			if (hoursStr != null) totalSeconds += Long.parseLong(hoursStr) * 3600L;
			if (minutesStr != null) totalSeconds += Long.parseLong(minutesStr) * 60L;
			if (secondsStr != null) totalSeconds += Long.parseLong(secondsStr);

			Instant nextStage = Instant.now().plusSeconds(totalSeconds);

			TimedObjectModel timedObject = new TimedObjectModel(
					"stonksauctions::next",
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
				Component.literal("Stonks Auction").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD, ChatFormatting.UNDERLINE),
				Component.literal("Diaz Stonks Auction").withStyle(ChatFormatting.DARK_PURPLE),
				ICON
		);
	}

	private void onReminderExpire(@NonNull TimedObjectModel timedObject) {
		MutableComponent message = Component.empty()
				.append(Component.literal("[Stonks Auction] ").withStyle(ChatFormatting.LIGHT_PURPLE))
				.append(Component.literal("Diaz Stonks Auction available!").withStyle(ChatFormatting.DARK_PURPLE));
		MutableComponent notification = Component.empty()
				.append(Component.literal("Stonks Auction").withStyle(ChatFormatting.LIGHT_PURPLE))
				.append(Component.literal("\n"))
				.append(Component.literal("Diaz Stonks Auction available!").withStyle(ChatFormatting.DARK_PURPLE));

		Client.sendMessageWithPrefix(message);
		Client.showNotification(notification, ICON);
		if (this.config().general.reminders.playSound) {
			Client.playSound(SoundEvents.NOTE_BLOCK_PLING.value(), 1f, 1f);
		}
	}
}
