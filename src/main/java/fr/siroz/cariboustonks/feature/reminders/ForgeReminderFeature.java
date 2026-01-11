package fr.siroz.cariboustonks.feature.reminders;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.manager.container.ContainerMatcherTrait;
import fr.siroz.cariboustonks.manager.container.overlay.ContainerOverlay;
import fr.siroz.cariboustonks.manager.reminder.Reminder;
import fr.siroz.cariboustonks.manager.reminder.ReminderDisplay;
import fr.siroz.cariboustonks.manager.reminder.ReminderManager;
import fr.siroz.cariboustonks.manager.reminder.TimedObject;
import fr.siroz.cariboustonks.feature.Feature;
import fr.siroz.cariboustonks.util.Client;
import fr.siroz.cariboustonks.util.ItemUtils;
import fr.siroz.cariboustonks.util.StonksUtils;
import fr.siroz.cariboustonks.util.render.gui.ColorHighlight;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public final class ForgeReminderFeature extends Feature implements ContainerMatcherTrait, Reminder, ContainerOverlay {

	private static final Pattern TITLE_PATTERN = Pattern.compile("^The Forge");
    private static final ItemStack ICON = new ItemStack(Items.FURNACE);

    @Override
    public boolean isEnabled() {
        return SkyBlockAPI.isOnSkyBlock() && ConfigManager.getConfig().general.reminders.forge;
    }

	@Override
	public Pattern getTitlePattern() {
		return TITLE_PATTERN;
	}

	@Override
    public @NotNull String reminderType() {
        return "FORGE_ITEM";
    }

    @Override
    public @NotNull ReminderDisplay display() {
        return ReminderDisplay.of(
                Component.literal("Forge").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD, ChatFormatting.UNDERLINE),
                null,
                ICON
        );
    }

    @Override
    public void onExpire(@NotNull TimedObject timedObject) {
        Component text = StonksUtils.jsonToText(timedObject.message()).orElse(Component.literal(timedObject.message()));
		MutableComponent message = Component.empty()
				.append(Component.literal("[Forge] ").withStyle(ChatFormatting.GOLD))
				.append(text)
				.append(Component.literal(" was ended!").withStyle(ChatFormatting.GREEN));
		MutableComponent notification = Component.empty()
				.append(Component.literal("Forge !").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
				.append(Component.literal("\n"))
				.append(text)
				.append(Component.literal("\n"))
				.append(Component.literal(" was ended!").withStyle(ChatFormatting.GREEN));

        Client.sendMessageWithPrefix(message );
        Client.showNotification(notification, ICON);
		if (ConfigManager.getConfig().general.reminders.playSound) {
			Client.playSound(SoundEvents.NOTE_BLOCK_PLING.value(), 1f, 1f);
		}
    }

    @Override
    public @NotNull List<ColorHighlight> content(@NotNull Int2ObjectMap<ItemStack> slots) {
        List<ColorHighlight> highlights = new ArrayList<>();
        for (Int2ObjectMap.Entry<ItemStack> entry : slots.int2ObjectEntrySet()) {

            ItemStack itemStack = entry.getValue();
            if (itemStack.is(Items.FURNACE)) {
                highlights.add(ColorHighlight.green(entry.getIntKey(), 0.5f));
            } else {
                String lore = ItemUtils.getLoreLineIf(itemStack, s -> s.contains("Time"));
                if (lore == null) {
					continue;
				}

                String timeRemaining = lore.replace("Time Remaining: ", "");
                int[] time = extractTime(timeRemaining);
                int hours = time[0];
                int minutes = time[1];
                int seconds = time[2];
                if (hours == 0 && minutes == 0 && seconds == 0) {
					continue;
				}

                String text = StonksUtils.textToJson(itemStack.getHoverName()).orElse(itemStack.getHoverName().getString());
                Duration duration = Duration.ofHours(hours).plusMinutes(minutes).plusSeconds(seconds);
                Instant expirationTime = Instant.now().plus(duration);

                TimedObject timedObject = new TimedObject(
                        "forge::" + entry.getIntKey(),
                        text,
                        expirationTime,
                        reminderType());

                CaribouStonks.managers()
                        .getManager(ReminderManager.class)
                        .addTimedObject(timedObject, true);
            }
        }

        return highlights;
    }

    @Contract("_ -> new")
    private int @NotNull [] extractTime(@NotNull String input) {
        int hours = 0;
		int minutes = 0;
		int seconds = 0;

        try {
            String[] parts = input.split(" ");
            for (String part : parts) {
                if (part.endsWith("h")) {
                    hours = Integer.parseInt(part.replace("h", ""));
                } else if (part.endsWith("m")) {
                    minutes = Integer.parseInt(part.replace("m", ""));
                } else if (part.endsWith("s")) {
                    seconds = Integer.parseInt(part.replace("s", ""));
                }
            }
        } catch (NumberFormatException ignored) {
        }

        return new int[]{hours, minutes, seconds};
    }
}
