package fr.siroz.cariboustonks.feature.reminders;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.config.ConfigManager;
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
import fr.siroz.cariboustonks.util.TimeUtils;
import fr.siroz.cariboustonks.util.colors.Colors;
import fr.siroz.cariboustonks.util.render.gui.ColorHighlight;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.Instant;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ChocolateLimitReminderFeature
		extends Feature
		implements ContainerMatcherTrait, Reminder, ContainerOverlay {

	private static final Pattern TITLE_PATTERN = Pattern.compile("^Chocolate Factory$");

	private static final Pattern CHOCOLATE_PATTERN = Pattern.compile("^([\\d,]+) Chocolate$");
	private static final Pattern CHOCOLATE_SECOND_PATTERN = Pattern.compile("([\\d,.]+) Chocolate per second");

	private static final int CHOCOLATE_SLOT = 13;
	private static final int COCOA_BEAM_SLOT = 45;
	private static final long MAX_CHOCOLATE = 60_000_000_000L;

	private static final ItemStack ICON = new ItemStack(Items.COCOA_BEANS);

	private long totalChocolate = -1L;
	private double chocolatePerSeconds = -1.0D;
	private Instant limitTime = null;

	public ChocolateLimitReminderFeature() {
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock()
				&& ConfigManager.getConfig().general.reminders.chocolateFactoryMaxChocolates;
	}

	@Override
	public Pattern getTitlePattern() {
		return TITLE_PATTERN;
	}

	@Override
	public @NotNull String reminderType() {
		return "MAX_CHOCOLATE";
	}

	@Override
	public @NotNull ReminderDisplay display() {
		return ReminderDisplay.of(
				Component.literal("Max Chocolate Factory").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD, ChatFormatting.UNDERLINE),
				Component.literal("The chocolate limit is reached!").withStyle(ChatFormatting.RED),
				ICON
		);
	}

	@Override
	public void onExpire(@NotNull TimedObject timedObject) {
		Component text = Component.literal("The chocolate limit is reached!").withStyle(ChatFormatting.RESET, ChatFormatting.RED);

		Client.sendMessageWithPrefix(Component.literal("[Chocolate Factory] ").withStyle(ChatFormatting.GOLD)
				.append(text));

		Client.showNotification(Component.literal("Chocolate Factory\n").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)
				.append(text), ICON);
	}

	@Override
	public Optional<Duration> preNotifyDuration() {
		return Optional.of(Duration.ofHours(24));
	}

	@Override
	public void onPreExpire(@NotNull TimedObject timedObject) {
		MutableComponent text = Component.literal("The chocolate limit will be reached soon!").withStyle(ChatFormatting.RED);
		MutableComponent message = Component.empty()
				.append(Component.literal("[Chocolate Factory] ").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
				.append(text);
		MutableComponent notification = Component.empty()
				.append(Component.literal("Chocolate Factory").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
				.append(Component.literal("\n"))
				.append(text);

		Client.sendMessageWithPrefix(message);
		Client.showNotification(notification, ICON);
	}

	@Override
	public @NotNull List<ColorHighlight> content(@NotNull Int2ObjectMap<ItemStack> slots) {

		String info = slots.get(CHOCOLATE_SLOT).getHoverName().getString();
		Matcher chocolateMatcher = CHOCOLATE_PATTERN.matcher(info);
		if (chocolateMatcher.find()) {
			totalChocolate = Long.parseLong(chocolateMatcher.group(1).replace(",", ""));
		}

		String cocoaBeam = ItemUtils.getConcatenatedLore(slots.get(COCOA_BEAM_SLOT));
		Matcher chocolateSecondMatcher = CHOCOLATE_SECOND_PATTERN.matcher(cocoaBeam);
		if (chocolateSecondMatcher.find()) {
			chocolatePerSeconds = Double.parseDouble(chocolateSecondMatcher.group(1).replace(",", ""));
		}

		if (totalChocolate > 0 && chocolatePerSeconds > 0) {

			long remainingChocolate = MAX_CHOCOLATE - totalChocolate;
			long secondsToReachTotal = (long) Math.ceil(remainingChocolate / chocolatePerSeconds);

			Duration duration = Duration.ofSeconds(secondsToReachTotal);
			Instant expirationTime = Instant.now().plus(duration);
			limitTime = expirationTime;
			TimedObject timedObject = new TimedObject(
					"cf::limit",
					"empty",
					expirationTime,
					reminderType());

			CaribouStonks.managers()
					.getManager(ReminderManager.class)
					.addTimedObject(timedObject, true);
		}

		return List.of();
	}

	@Override
	public void render(@NotNull GuiGraphics context, int screenWidth, int screenHeight, int x, int y) {
		if (limitTime != null) {
			Component limitText = Component.literal("Chocolate will be reached: ").withStyle(ChatFormatting.GOLD)
					.append(Component.literal(TimeUtils.formatInstant(limitTime, TimeUtils.DATE_TIME_FULL)).withStyle(ChatFormatting.YELLOW));
			context.drawCenteredString(
					Minecraft.getInstance().font,
					limitText,
					screenWidth >> 1,
					20,
					Colors.WHITE.asInt());
		}
	}

	@Override
	public void reset() {
		totalChocolate = -1;
		chocolatePerSeconds = -1;
		limitTime = null;
	}
}
