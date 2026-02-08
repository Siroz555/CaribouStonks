package fr.siroz.cariboustonks.features.reminders;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.core.component.ContainerMatcherComponent;
import fr.siroz.cariboustonks.core.component.ContainerOverlayComponent;
import fr.siroz.cariboustonks.core.component.ReminderComponent;
import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.core.model.TimedObjectModel;
import fr.siroz.cariboustonks.core.module.gui.ColorHighlight;
import fr.siroz.cariboustonks.core.module.reminder.ReminderDisplay;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.system.ReminderSystem;
import fr.siroz.cariboustonks.util.Client;
import fr.siroz.cariboustonks.util.CodecUtil;
import fr.siroz.cariboustonks.util.ItemUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.NonNull;

public final class ForgeReminderFeature extends Feature {

	private static final Pattern TITLE_PATTERN = Pattern.compile("^The Forge");
	private static final String REMINDER_TYPE = "FORGE_ITEM";
    private static final ItemStack ICON = new ItemStack(Items.FURNACE);

	public ForgeReminderFeature() {
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
        return SkyBlockAPI.isOnSkyBlock() && this.config().general.reminders.forge;
    }

    private @NonNull ReminderDisplay getReminderDisplay() {
        return ReminderDisplay.of(
                Component.literal("Forge").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD, ChatFormatting.UNDERLINE),
                null,
                ICON
        );
    }

    private void onReminderExpire(@NonNull TimedObjectModel timedObject) {
        Component text = CodecUtil.jsonToText(timedObject.message()).orElse(Component.literal(timedObject.message()));
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
		if (this.config().general.reminders.playSound) {
			Client.playSound(SoundEvents.NOTE_BLOCK_PLING.value(), 1f, 1f);
		}
    }

    private @NonNull List<ColorHighlight> contentAnalyzer(@NonNull Int2ObjectMap<ItemStack> slots) {
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

                String text = CodecUtil.textToJson(itemStack.getHoverName()).orElse(itemStack.getHoverName().getString());
                Duration duration = Duration.ofHours(hours).plusMinutes(minutes).plusSeconds(seconds);
                Instant expirationTime = Instant.now().plus(duration);

                TimedObjectModel timedObject = new TimedObjectModel(
                        "forge::" + entry.getIntKey(),
                        text,
                        expirationTime,
                       REMINDER_TYPE);

                CaribouStonks.systems()
                        .getSystem(ReminderSystem.class)
                        .addTimedObject(timedObject, true);
            }
        }

        return highlights;
    }

    private int @NonNull [] extractTime(@NonNull String input) {
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
