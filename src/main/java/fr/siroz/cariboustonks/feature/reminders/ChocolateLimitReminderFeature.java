package fr.siroz.cariboustonks.feature.reminders;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.core.component.ContainerMatcherComponent;
import fr.siroz.cariboustonks.core.component.ContainerOverlayComponent;
import fr.siroz.cariboustonks.core.component.ReminderComponent;
import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.core.module.reminder.ReminderDisplay;
import fr.siroz.cariboustonks.core.module.reminder.TimedObject;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.system.ReminderSystem;
import fr.siroz.cariboustonks.util.Client;
import fr.siroz.cariboustonks.util.ItemUtils;
import fr.siroz.cariboustonks.util.TimeUtils;
import fr.siroz.cariboustonks.util.colors.Colors;
import fr.siroz.cariboustonks.util.render.gui.ColorHighlight;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.NonNull;

public final class ChocolateLimitReminderFeature extends Feature {

	private static final Pattern TITLE_PATTERN = Pattern.compile("^Chocolate Factory$");
	private static final String REMINDER_TYPE = "MAX_CHOCOLATE";

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
		this.addComponent(ReminderComponent.class, ReminderComponent.builder(REMINDER_TYPE)
				.display(getReminderDisplay())
				.onExpire(this::onReminderExpire)
				.preNotify(Duration.ofHours(24), this::onReminderPreNotify)
				.build());

		this.addComponent(ContainerMatcherComponent.class, ContainerMatcherComponent.of(TITLE_PATTERN));
		this.addComponent(ContainerOverlayComponent.class, ContainerOverlayComponent.builder()
				.content(this::contentAnalyzer)
				.render(this::render)
				.onReset(() -> {
					totalChocolate = -1;
					chocolatePerSeconds = -1;
					limitTime = null;
				})
				.build());
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock()
				&& ConfigManager.getConfig().general.reminders.chocolateFactoryMaxChocolates;
	}

	private @NonNull List<ColorHighlight> contentAnalyzer(@NonNull Int2ObjectMap<ItemStack> slots) {
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
					REMINDER_TYPE);

			CaribouStonks.systems()
					.getSystem(ReminderSystem.class)
					.addTimedObject(timedObject, true);
		}

		return List.of();
	}

	private void render(@NonNull GuiGraphics context, int screenWidth, int screenHeight, int x, int y) {
		if (limitTime != null) {
			Component text = Component.empty()
					.append(Component.literal("Chocolate will be reached: ")
							.withStyle(ChatFormatting.GOLD))
					.append(Component.literal(TimeUtils.formatInstant(limitTime, TimeUtils.DATE_TIME_FULL))
							.withStyle(ChatFormatting.YELLOW));
			context.drawCenteredString(
					Minecraft.getInstance().font,
					text,
					screenWidth >> 1,
					20,
					Colors.WHITE.asInt());
		}
	}

	private ReminderDisplay getReminderDisplay() {
		return ReminderDisplay.of(
				Component.literal("Max Chocolate Factory").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD, ChatFormatting.UNDERLINE),
				Component.literal("The chocolate limit is reached!").withStyle(ChatFormatting.RED),
				ICON);
	}

	private void onReminderExpire(TimedObject timedObject) {
		Component text = Component.literal("The chocolate limit is reached!").withStyle(ChatFormatting.RESET, ChatFormatting.RED);

		Client.sendMessageWithPrefix(Component.literal("[Chocolate Factory] ").withStyle(ChatFormatting.GOLD)
				.append(text));

		Client.showNotification(Component.literal("Chocolate Factory\n").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)
				.append(text), ICON);

		if (ConfigManager.getConfig().general.reminders.playSound) {
			Client.playSound(SoundEvents.NOTE_BLOCK_PLING.value(), 1f, 1f);
		}
	}

	private void onReminderPreNotify(TimedObject timedObject) {
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
}
