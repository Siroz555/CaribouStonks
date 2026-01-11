package fr.siroz.cariboustonks.feature.reminders;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.core.skyblock.IslandType;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.event.ChatEvents;
import fr.siroz.cariboustonks.feature.Feature;
import fr.siroz.cariboustonks.manager.reminder.Reminder;
import fr.siroz.cariboustonks.manager.reminder.ReminderDisplay;
import fr.siroz.cariboustonks.manager.reminder.ReminderManager;
import fr.siroz.cariboustonks.manager.reminder.TimedObject;
import fr.siroz.cariboustonks.util.Client;
import fr.siroz.cariboustonks.util.HeadTextures;
import fr.siroz.cariboustonks.util.ItemUtils;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.Instant;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class UbikCubeReminderFeature extends Feature implements Reminder {

    // You earned 45,000 Motes in this match!
    private static final Pattern UBIK_CUBE_MESSAGE = Pattern.compile("You earned ([\\d,]+) Motes in this match!");

    private static final Component SPLIT_OR_STEAL_TEXT = Component.literal("Split").withStyle(ChatFormatting.GREEN)
            .append(Component.literal(" or ").withStyle(ChatFormatting.WHITE))
            .append(Component.literal("Steal").withStyle(ChatFormatting.RED));

	private final ItemStack ubikCube;

    public UbikCubeReminderFeature() {
		this.ubikCube = ItemUtils.createSkull(HeadTextures.UBIK_CUBE);
        ChatEvents.MESSAGE_RECEIVED.register(this::onChatMessage);
    }

    @Override
    public boolean isEnabled() {
        return SkyBlockAPI.isOnSkyBlock()
                && SkyBlockAPI.getIsland() == IslandType.THE_RIFT
                && ConfigManager.getConfig().general.reminders.ubikCube;
    }

    @Override
    public @NotNull String reminderType() {
        return "RIFT_UBIK_CUBE";
    }

    @Override
    public @NotNull ReminderDisplay display() {
        return ReminderDisplay.of(
                Component.literal("Ubik's Cube").withStyle(ChatFormatting.RED, ChatFormatting.BOLD, ChatFormatting.UNDERLINE),
                SPLIT_OR_STEAL_TEXT,
				ubikCube
        );
    }

    @Override
    public void onExpire(@NotNull TimedObject timedObject) {
		MutableComponent message = Component.empty()
				.append(Component.literal("[Ubik's Cube] ").withStyle(ChatFormatting.GOLD))
				.append(Component.literal("Ready to play ").withStyle(ChatFormatting.GREEN))
				.append(SPLIT_OR_STEAL_TEXT);
		MutableComponent notification = Component.empty()
				.append(Component.literal("Ubik's Cube !").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
				.append(Component.literal("\n"))
				.append(Component.literal("Ready to play ").withStyle(ChatFormatting.GREEN))
				.append(SPLIT_OR_STEAL_TEXT);

        Client.sendMessageWithPrefix(message);
        Client.showNotification(notification, ubikCube);
		if (ConfigManager.getConfig().general.reminders.playSound) {
			Client.playSound(SoundEvents.NOTE_BLOCK_PLING.value(), 1f, 1f);
		}
    }

    private void onChatMessage(@NotNull Component text) {
        if (!isEnabled()) {
			return;
		}

        Matcher ubikCubeMessageMatcher = UBIK_CUBE_MESSAGE.matcher(text.getString());
        if (ubikCubeMessageMatcher.find()) {

            Duration duration = Duration.ofHours(2);
            Instant expirationTime = Instant.now().plus(duration);

            TimedObject timedObject = new TimedObject(
                    "rift::ubikCube",
                    "empty",
                    expirationTime,
                    reminderType());

            CaribouStonks.managers()
                    .getManager(ReminderManager.class)
                    .addTimedObject(timedObject);
        }
    }
}
