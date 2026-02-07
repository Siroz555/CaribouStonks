package fr.siroz.cariboustonks.core.module.reminder;

import fr.siroz.cariboustonks.core.model.TimedObjectModel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Represents the display details of a reminder in the {@code Reminder Screen}.
 *
 * @param title the title of the reminder
 * @param description an optional description providing additional details about the reminder.
 *                    If the description is {@code null}, the reminder description is provided
 *                    by the {@link TimedObjectModel#message()}.
 * @param icon a visual representation associated with the reminder, represented as an {@link ItemStack}
 */
public record ReminderDisplay(
		@NonNull Component title,
		@Nullable Component description,
		@NonNull ItemStack icon
) {

    public static @NonNull ReminderDisplay of(
			@NonNull Component title,
			@Nullable Component description,
			@NonNull ItemStack icon
	) {
        return new ReminderDisplay(title, description, icon);
    }
}
