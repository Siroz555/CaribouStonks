package fr.siroz.cariboustonks.feature.reminders;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.skyblock.IslandType;
import fr.siroz.cariboustonks.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.feature.Feature;
import fr.siroz.cariboustonks.system.container.ContainerMatcherTrait;
import fr.siroz.cariboustonks.system.container.overlay.ContainerOverlay;
import fr.siroz.cariboustonks.system.reminder.Reminder;
import fr.siroz.cariboustonks.system.reminder.ReminderDisplay;
import fr.siroz.cariboustonks.system.reminder.ReminderSystem;
import fr.siroz.cariboustonks.system.reminder.TimedObject;
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
import org.jetbrains.annotations.NotNull;

public final class StonksAuctionReminderFeature extends Feature implements ContainerMatcherTrait, ContainerOverlay, Reminder {

	private static final Pattern TITLE_PATTERN = Pattern.compile("^Stonks Auction$");

	private static final Pattern NEXT_AUCTION_PATTERN = Pattern.compile("Auction ends in\\s*(?:(\\d+)\\s*h)?\\s*(?:(\\d+)\\s*m)?\\s*(?:(\\d+)\\s*s)?");
	private static final int BID_SLOT = 11;
	private static final ItemStack ICON = new ItemStack(Items.PAPER);

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock()
				&& SkyBlockAPI.getIsland() == IslandType.HUB
				&& ConfigManager.getConfig().general.reminders.stonksAuction;
	}

	@Override
	public Pattern getTitlePattern() {
		return TITLE_PATTERN;
	}

	@Override
	public @NotNull List<ColorHighlight> content(@NotNull Int2ObjectMap<ItemStack> slots) {
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

			TimedObject timedObject = new TimedObject(
					"stonksauctions::next",
					"empty",
					nextStage,
					reminderType());

			CaribouStonks.systems()
					.getSystem(ReminderSystem.class)
					.addTimedObject(timedObject, true);
		}

		return List.of();
	}

	@Override
	public @NotNull String reminderType() {
		return "NEXT_STONKS_AUCTION";
	}

	@Override
	public @NotNull ReminderDisplay display() {
		return ReminderDisplay.of(
				Component.literal("Stonks Auction").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD, ChatFormatting.UNDERLINE),
				Component.literal("Diaz Stonks Auction").withStyle(ChatFormatting.DARK_PURPLE),
				ICON
		);
	}

	@Override
	public void onExpire(@NotNull TimedObject timedObject) {
		MutableComponent message = Component.empty()
				.append(Component.literal("[Stonks Auction] ").withStyle(ChatFormatting.LIGHT_PURPLE))
				.append(Component.literal("Diaz Stonks Auction available!").withStyle(ChatFormatting.DARK_PURPLE));
		MutableComponent notification = Component.empty()
				.append(Component.literal("Stonks Auction").withStyle(ChatFormatting.LIGHT_PURPLE))
				.append(Component.literal("\n"))
				.append(Component.literal("Diaz Stonks Auction available!").withStyle(ChatFormatting.DARK_PURPLE));

		Client.sendMessageWithPrefix(message);
		Client.showNotification(notification, ICON);
		if (ConfigManager.getConfig().general.reminders.playSound) {
			Client.playSound(SoundEvents.NOTE_BLOCK_PLING.value(), 1f, 1f);
		}
	}
}
