package fr.siroz.cariboustonks.core.component;

import fr.siroz.cariboustonks.core.module.reminder.ReminderDisplay;
import fr.siroz.cariboustonks.core.model.TimedObjectModel;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;

/**
 * {@code Component} that define how reminders are handled within the mod,
 * allowing for customizable behaviors at the moment a reminder expires
 * or when a pre-notification is required.
 *
 * <h2>Reminder Data and Handling</h2>
 * Each reminder is associated with a {@link TimedObjectModel} that contains contextual information,
 * including a message field. The {@code message} may be either:
 * <ul>
 *   <li>A simple plain text (e.g., "Ubik's Cube"), which can be directly used in notifications</li>
 *   <li>A JSON-serialized {@link net.minecraft.network.chat.Component}, allowing for dynamic and rich descriptions</li>
 * </ul>
 * The {@link #handleExpiration(TimedObjectModel)} must properly handle both serialization formats
 * depending on the reminder's requirements.
 * <p>
 * Example usage for registering a simple reminder:
 * <pre>{@code
 * TimedObject timedObject = new TimedObject(
 *     "rift::ubikCube",
 *     "Ubik's Cube",
 *     expirationTime,
 *     REMINDER_TYPE
 * );
 *
 * CaribouStonks.systems().getSystem(ReminderSystem.class)
 *     .addTimedObject(timedObject);
 * }</pre>
 * Example usage handling a simple reminder:
 * <pre>{@code
 * private void onExpire(TimedObject timedObject) {
 *     // Custom handling...
 * }
 * }</pre>
 * Example usage for registering a reminder with a JSON-formatted message:
 * <pre>{@code
 * TimedObject timedObject = new TimedObject(
 * 	    "FORGE::itemName",
 * 	    StonksUtils.textToJson(itemName).orElse(...),
 * 	    expirationTime,
 * 	    REMINDER_TYPE
 * );
 *
 * CaribouStonks.systems().getSystem(ReminderSystem.class)
 *     .addTimedObject(timedObject);
 * }</pre>
 * Example usage handling a JSON-formatted message:
 * <pre>{@code
 * private void onExpire(TimedObject timedObject) {
 *     Text text = StonksUtils.jsonToText(timedObject.message())
 *         .orElse(Text.literal(timedObject.message()));
 *     // Custom handling...
 * }
 * }</pre>
 */
public final class ReminderComponent implements Component {
	private final String reminderType;
	private final ReminderDisplay display;
	private final ReminderExpirationHandler onExpireHandler;
	private final Duration preNotifyDuration;
	private final ReminderPreExpirationHandler onPreExpireHandler;

	private ReminderComponent(
			String reminderType,
			ReminderDisplay display,
			ReminderExpirationHandler onExpireHandler,
			Duration preNotifyDuration,
			ReminderPreExpirationHandler onPreExpireHandler
	) {
		this.reminderType = reminderType;
		this.display = display;
		this.onExpireHandler = onExpireHandler;
		this.preNotifyDuration = preNotifyDuration;
		this.onPreExpireHandler = onPreExpireHandler;
	}

	public String getReminderType() {
		return reminderType;
	}

	public ReminderDisplay getDisplay() {
		return display;
	}

	public void handleExpiration(TimedObjectModel timedObject) {
		onExpireHandler.onExpire(timedObject);
	}

	@NonNull
	public Optional<Duration> getPreNotifyDuration() {
		return Optional.ofNullable(preNotifyDuration);
	}

	public void handlePreExpiration(TimedObjectModel timedObject) {
		if (onPreExpireHandler != null) {
			onPreExpireHandler.onPreExpire(timedObject);
		}
	}

	@NonNull
	public static Builder builder(@NonNull String reminderType) {
		Objects.requireNonNull(reminderType);
		return new Builder(reminderType);
	}

	/**
	 * Functional interface for handling reminder expiration.
	 */
	@FunctionalInterface
	public interface ReminderExpirationHandler {
		void onExpire(TimedObjectModel timedObject);
	}

	/**
	 * Functional interface for handling pre-expiration notifications.
	 */
	@FunctionalInterface
	public interface ReminderPreExpirationHandler {
		void onPreExpire(TimedObjectModel timedObject);
	}

	public static class Builder {
		private final String reminderType;
		private ReminderDisplay display;
		private ReminderExpirationHandler onExpireHandler;
		private Duration preNotifyDuration;
		private ReminderPreExpirationHandler onPreExpireHandler;

		private Builder(@NonNull String reminderType) {
			this.reminderType = Objects.requireNonNull(reminderType, "Reminder type cannot be null");
		}

		/**
		 * Sets the display information for this reminder.
		 */
		public Builder display(@NonNull ReminderDisplay display) {
			this.display = display;
			return this;
		}

		/**
		 * Convenient method to set display with inline parameters.
		 */
		public Builder display(net.minecraft.network.chat.Component title, net.minecraft.network.chat.Component description, @NonNull ItemStack icon) {
			this.display = new ReminderDisplay(title, description, icon);
			return this;
		}

		/**
		 * Sets the handler to be called when the reminder expires (required).
		 */
		public Builder onExpire(@NonNull ReminderExpirationHandler handler) {
			this.onExpireHandler = handler;
			return this;
		}

		/**
		 * Sets a pre-notification duration and its handler.
		 */
		public Builder preNotify(@NonNull Duration duration, @NonNull ReminderPreExpirationHandler handler) {
			this.preNotifyDuration = duration;
			this.onPreExpireHandler = handler;
			return this;
		}

		public ReminderComponent build() {
			Objects.requireNonNull(display, "Display must be set");
			Objects.requireNonNull(onExpireHandler, "Expiration handler must be set");

			// Si preNotifyDuration est défini, le handler doit l'être aussi
			if (preNotifyDuration != null && onPreExpireHandler == null) {
				throw new IllegalStateException(
						"Pre-notification handler must be provided when pre-notify duration is set"
				);
			}

			return new ReminderComponent(
					reminderType,
					display,
					onExpireHandler,
					preNotifyDuration,
					onPreExpireHandler
			);
		}
	}
}
