package fr.siroz.cariboustonks.features.reminders;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.core.component.ReminderComponent;
import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.core.model.TimedObjectModel;
import fr.siroz.cariboustonks.core.module.reminder.ReminderDisplay;
import fr.siroz.cariboustonks.core.skyblock.IslandType;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.core.skyblock.item.HeadTextures;
import fr.siroz.cariboustonks.events.ChatEvents;
import fr.siroz.cariboustonks.events.EventHandler;
import fr.siroz.cariboustonks.system.ReminderSystem;
import fr.siroz.cariboustonks.util.Client;
import fr.siroz.cariboustonks.util.ItemUtils;
import java.time.Duration;
import java.time.Instant;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;

public final class UbikCubeReminderFeature extends Feature {

    // You earned 45,000 Motes in this match!
    private static final Pattern UBIK_CUBE_MESSAGE = Pattern.compile("You earned ([\\d,]+) Motes in this match!");

    private static final Component SPLIT_OR_STEAL_TEXT = Component.literal("Split").withStyle(ChatFormatting.GREEN)
            .append(Component.literal(" or ").withStyle(ChatFormatting.WHITE))
            .append(Component.literal("Steal").withStyle(ChatFormatting.RED));

	private static final String REMINDER_TYPE = "RIFT_UBIK_CUBE";

	private final ItemStack ubikCube;

    public UbikCubeReminderFeature() {
		this.ubikCube = ItemUtils.createSkull(HeadTextures.UBIK_CUBE);
        ChatEvents.MESSAGE_RECEIVE_EVENT.register(this::onChatMessage);

		this.addComponent(ReminderComponent.class, ReminderComponent.builder(REMINDER_TYPE)
				.display(getReminderDisplay())
				.onExpire(this::onReminderExpire)
				.build());
    }

    @Override
    public boolean isEnabled() {
        return SkyBlockAPI.isOnSkyBlock()
                && SkyBlockAPI.getIsland() == IslandType.THE_RIFT
                && this.config().general.reminders.ubikCube;
    }

    private @NonNull ReminderDisplay getReminderDisplay() {
        return ReminderDisplay.of(
                Component.literal("Ubik's Cube").withStyle(ChatFormatting.RED, ChatFormatting.BOLD, ChatFormatting.UNDERLINE),
                SPLIT_OR_STEAL_TEXT,
				ubikCube
        );
    }

    private void onReminderExpire(@NonNull TimedObjectModel timedObject) {
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
		if (this.config().general.reminders.playSound) {
			Client.playSound(SoundEvents.NOTE_BLOCK_PLING.value(), 1f, 1f);
		}
    }

	@EventHandler(event = "ChatEvents.MESSAGE_RECEIVE_EVENT")
    private void onChatMessage(@NonNull Component text) {
        if (!isEnabled()) {
			return;
		}

        Matcher ubikCubeMessageMatcher = UBIK_CUBE_MESSAGE.matcher(text.getString());
        if (ubikCubeMessageMatcher.find()) {

            Duration duration = Duration.ofHours(2);
            Instant expirationTime = Instant.now().plus(duration);

            TimedObjectModel timedObject = new TimedObjectModel(
                    "rift::ubikCube",
                    "empty",
                    expirationTime,
                    REMINDER_TYPE);

            CaribouStonks.systems()
                    .getSystem(ReminderSystem.class)
                    .addTimedObject(timedObject);
        }
    }
}
