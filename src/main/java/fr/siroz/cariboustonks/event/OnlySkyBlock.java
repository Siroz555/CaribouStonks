package fr.siroz.cariboustonks.event;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker annotation indicating that the annotated event is only invoked when
 * the player is already known to be on SkyBlock.
 * <p>
 * When this annotation is present on an {@code Event} field,
 * callers guarantee that the necessary SkyBlock check has already been performed
 * before the event is fired.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.SOURCE)
@Documented
public @interface OnlySkyBlock {
}
