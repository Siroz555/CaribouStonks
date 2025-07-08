package fr.siroz.cariboustonks.manager.reminder;

import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents the display details of a reminder in the {@code Reminder Screen}.
 *
 * @param title the title of the reminder
 * @param description an optional description providing additional details about the reminder.
 *                    If the description is {@code null}, the reminder description is provided
 *                    by the {@link TimedObject#message()}.
 * @param icon a visual representation associated with the reminder, represented as an {@link ItemStack}
 */
public record ReminderDisplay(@NotNull Text title, @Nullable Text description, @NotNull ItemStack icon) {

    @Contract(value = "_, _, _ -> new", pure = true)
    public static @NotNull ReminderDisplay of(@NotNull Text title, @Nullable Text description, @NotNull ItemStack icon) {
        return new ReminderDisplay(title, description, icon);
    }
}
