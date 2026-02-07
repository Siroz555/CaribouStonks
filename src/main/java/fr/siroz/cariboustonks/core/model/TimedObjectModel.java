package fr.siroz.cariboustonks.core.model;

import java.time.Instant;

/**
 * Represents a time-bound object that expires at a specific date and is associated with a certain type.
 *
 * @param id             unique identifier for the object
 * @param message        a message associated with the object, used to provide additional information
 *                       that can be retrieved for display in {@link fr.siroz.cariboustonks.core.module.reminder.ReminderDisplay#description()}
 *                       or during reminder processing in {@link fr.siroz.cariboustonks.core.component.ReminderComponent#handleExpiration(TimedObjectModel)}.
 *                       <p>
 *                       The content of this message can take two forms:
 *                        <ul>
 *                          <li><b>Plain text</b>: if no personalized data is needed.</li>
 *                          <li><b>JSON string</b>: if the object needs to carry custom or structured data,
 *                              enabling more advanced customization of displays or behaviors.</li>
 *                        </ul>
 * @param expirationTime the exact {@link Instant} at which the object expires
 * @param type           the type of the object, used to determine its behavior upon expiration
 */
public record TimedObjectModel(
		String id,
		String message,
		Instant expirationTime,
		String type
) {
}
