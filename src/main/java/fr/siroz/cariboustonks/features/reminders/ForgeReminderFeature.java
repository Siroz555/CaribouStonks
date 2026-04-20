package fr.siroz.cariboustonks.features.reminders;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.core.component.ContainerOverlayComponent;
import fr.siroz.cariboustonks.core.component.ReminderComponent;
import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.core.model.TimedObjectModel;
import fr.siroz.cariboustonks.core.module.gui.ColorHighlight;
import fr.siroz.cariboustonks.core.module.gui.MatcherTrait;
import fr.siroz.cariboustonks.core.module.reminder.ReminderDisplay;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.systems.ReminderSystem;
import fr.siroz.cariboustonks.util.Client;
import fr.siroz.cariboustonks.util.CodecUtils;
import fr.siroz.cariboustonks.util.ItemUtils;
import fr.siroz.cariboustonks.util.TimeUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
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

		this.addComponent(ContainerOverlayComponent.class, ContainerOverlayComponent.builder()
				.trait(MatcherTrait.pattern(TITLE_PATTERN))
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
		Component text = CodecUtils.jsonToText(timedObject.message()).orElse(Component.literal(timedObject.message()));
		MutableComponent message = Component.empty()
				.append(Component.literal("[Forge] ").withStyle(ChatFormatting.GOLD))
				.append(text)
				.append(Component.literal(" was ended!").withStyle(ChatFormatting.GREEN))
				.append(Component.literal(" CLICK").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
				.withStyle(style -> style
						.withHoverEvent(new HoverEvent.ShowText(Component.literal("Click to call Forge").withStyle(ChatFormatting.YELLOW)))
						.withClickEvent(new ClickEvent.RunCommand("/call forge")));
		MutableComponent notification = Component.empty()
				.append(Component.literal("Forge !").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
				.append(Component.literal("\n"))
				.append(text)
				.append(Component.literal(" was ended!").withStyle(ChatFormatting.GREEN));

        Client.sendMessageWithPrefix(message);
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
				if (lore == null) continue;

				Duration duration = TimeUtils.extractDuration(lore.replace("Time Remaining: ", ""));
				if (duration.isZero()) continue;

				String text = CodecUtils.textToJson(itemStack.getHoverName())
						.orElse(itemStack.getHoverName().getString());

				TimedObjectModel timedObject = new TimedObjectModel(
						"forge::" + entry.getIntKey(),
						text,
						Instant.now().plus(duration),
						REMINDER_TYPE
				);

				CaribouStonks.systems()
						.getSystem(ReminderSystem.class)
						.addTimedObject(timedObject, true);
			}
		}

		return highlights;
	}
}
