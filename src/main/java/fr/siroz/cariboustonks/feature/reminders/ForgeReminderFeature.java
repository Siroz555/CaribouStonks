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
import fr.siroz.cariboustonks.util.TimeUtils;
import fr.siroz.cariboustonks.util.render.gui.ColorHighlight;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
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
                Text.literal("Forge").formatted(Formatting.GOLD, Formatting.BOLD, Formatting.UNDERLINE),
                null,
                ICON
        );
    }

    @Override
    public void onExpire(@NotNull TimedObject timedObject) {
        Text text = StonksUtils.jsonToText(timedObject.message()).orElse(Text.literal(timedObject.message()));
		MutableText message = Text.empty()
				.append(Text.literal("[Forge] ").formatted(Formatting.GOLD))
				.append(text)
				.append(Text.literal(" was ended!").formatted(Formatting.GREEN))
				.append(Text.literal(" CLICK").formatted(Formatting.YELLOW, Formatting.BOLD))
				.styled(style -> style
						.withHoverEvent(new HoverEvent.ShowText(Text.literal("Click to call Forge").formatted(Formatting.YELLOW)))
						.withClickEvent(new ClickEvent.RunCommand("/call forge")));
		MutableText notification = Text.empty()
				.append(Text.literal("Forge !").formatted(Formatting.GOLD, Formatting.BOLD))
				.append(Text.literal("\n"))
				.append(text)
				.append(Text.literal("\n"))
				.append(Text.literal(" was ended!").formatted(Formatting.GREEN));

        Client.sendMessageWithPrefix(message );
        Client.showNotification(notification, ICON);
		if (ConfigManager.getConfig().general.reminders.playSound) {
			Client.playSound(SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), 1f, 1f);
		}
    }

    @Override
    public @NotNull List<ColorHighlight> content(@NotNull Int2ObjectMap<ItemStack> slots) {
        List<ColorHighlight> highlights = new ArrayList<>();
        for (Int2ObjectMap.Entry<ItemStack> entry : slots.int2ObjectEntrySet()) {

            ItemStack itemStack = entry.getValue();
            if (itemStack.isOf(Items.FURNACE)) {
                highlights.add(ColorHighlight.green(entry.getIntKey(), 0.5f));
            } else {
                String lore = ItemUtils.getLoreLineIf(itemStack, s -> s.contains("Time"));
                if (lore == null) continue;

				Duration duration = TimeUtils.extractDuration(lore.replace("Time Remaining: ", ""));
				if (duration.isZero()) continue;

				String text = StonksUtils.textToJson(itemStack.getName()).orElse(itemStack.getName().getString());
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
}
