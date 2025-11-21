package fr.siroz.cariboustonks.manager.reminder;

import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Optional;

/**
 * Represents a contract for managing time-based reminders, including their identification,
 * display information, expiration logic, and optional pre-expiration notifications.
 * <p>
 * Implementations of this interface define how different types of reminders are handled
 * within the mod, allowing for customizable behaviors at the moment a reminder
 * expires or when a pre-notification is required.
 *
 * <h2>Reminder Data and Handling</h2>
 * Each reminder is associated with a {@link TimedObject} that contains contextual information,
 * including a message field. The {@code message} may be either:
 * <ul>
 *   <li>A simple plain text (e.g., "Ubik's Cube"), which can be directly used in notifications</li>
 *   <li>A JSON-serialized {@link net.minecraft.network.chat.Component}, allowing for dynamic and rich descriptions</li>
 * </ul>
 * The {@link #onExpire(TimedObject)} method must properly handle both serialization formats
 * depending on the reminder's requirements.
 * <p>
 * Example usage for registering a simple reminder:
 * <pre>{@code
 * TimedObject timedObject = new TimedObject(
 *     "rift::ubikCube",
 *     "Ubik's Cube",
 *     expirationTime,
 *     reminderType()
 * );
 * reminderManager.addTimedObject(timedObject);
 * }</pre>
 * Example usage handling a simple reminder:
 * <pre>{@code
 * @Override
 * public void onExpire(@NotNull TimedObject timedObject) {
 *     // Custom handling...
 * }
 * }</pre>
 * Example usage for registering a reminder with a JSON-formatted message:
 * <pre>{@code
 * TimedObject timedObject = new TimedObject(
 * 	    "FORGE::itemName",
 * 	    StonksUtils.textToJson(itemName).orElse(...),
 * 	    expirationTime,
 * 	    reminderType()
 * );
 * reminderManager.addTimedObject(timedObject);
 * }</pre>
 * Example usage handling a JSON-formatted message:
 * <pre>{@code
 * @Override
 * public void onExpire(@NotNull TimedObject timedObject) {
 *     Text text = StonksUtils.jsonToText(timedObject.message())
 *         .orElse(Text.literal(timedObject.message()));
 *     // Custom handling...
 * }
 * }</pre>
 */
public interface Reminder {

    /**
     * Returns the unique type identifier of this reminder (e.g., {@code "MAX_CHOCOLATE"}, {@code "RIFT_UBIK_CUBE"}).
     *
     * @return a string representing the type of the reminder
     */
    @NotNull
    String reminderType();

    /**
     * Provides the display information for this reminder, such as title, description, and icon.
     * <p>
     * Used in the {@code Reminder Screen} to render the reminder's visual representation.
     *
     * @return a {@link ReminderDisplay} containing all visual display attributes
     */
    @NotNull
    ReminderDisplay display();

    /**
     * Called when a {@link TimedObject} associated with this reminder expires.
     * <p>
     * The {@link TimedObject#message()} field may contain either a plain text value or
     * a JSON representation of a {@link net.minecraft.network.chat.Component}.
     * Implementations should handle both scenarios accordingly, e.g., by attempting to
     * parse the message as JSON first and falling back to plain text if parsing fails.
     *
     * @param timedObject the expired timed object triggering this reminder
     */
    void onExpire(@NotNull TimedObject timedObject);

    /**
     * Optionally specifies a duration before the expiration time at which a pre-notification
     * should be triggered. If present, {@link #onPreExpire(TimedObject)} will be invoked at that time.
     * The default implementation does not request a pre-notification.
     *
     * @return an optional pre-notification {@link Duration}, or {@link Optional#empty()} if none
     */
    default Optional<Duration> preNotifyDuration() {
        return Optional.empty();
    }

    /**
     * Optionally called before actual expiration, at the instant determined by {@link #preNotifyDuration()}.
     * Use this for early warnings or preparation logic before the main {@link #onExpire(TimedObject)}.
     *
     * @param timedObject the timed object approaching expiration
     */

    default void onPreExpire(@NotNull TimedObject timedObject) {
    }
}
