package fr.siroz.cariboustonks.event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as an event handler that listens to a specific event.
 * <p>
 * Methods annotated with {@code @EventHandler} should conform to the
 * required method signature of the event they handle or without any parameters.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface EventHandler {
	String event() default "";
}

