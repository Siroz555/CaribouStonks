package fr.siroz.cariboustonks.core.module.cooldown;

import fr.siroz.cariboustonks.util.TimeUtils;
import java.util.OptionalLong;
import java.util.concurrent.TimeUnit;
import org.jspecify.annotations.NonNull;

/**
 * Represents a simple cooldown
 */
public interface Cooldown {

	/**
	 * Creates a cooldown with the given amount if time and the unit.
	 *
	 * @param amount the amount of time
	 * @param unit   the unit of time
	 * @return a new cooldown
	 */
	@NonNull
	static Cooldown of(long amount, @NonNull TimeUnit unit) {
		return new CooldownImpl(amount, unit);
	}

	/**
	 * Returns {@code true} if the cooldown is not active, and then resets the timer.
	 * <p>
	 * If the cooldown is currently active, the timer is not reset.
	 *
	 * @return {@code true} if the cooldown is not active
	 */
	default boolean test() {
		if (!testSilently()) {
			return false;
		}

		reset();
		return true;
	}

	/**
	 * Returns {@code true} if the cooldown is not active.
	 *
	 * @return {@code true} if the cooldown is not active
	 */
	default boolean testSilently() {
		return elapsed() > getTimeout();
	}

	/**
	 * Returns the elapsed time in milliseconds since the cooldown was last reset, or since creation time
	 *
	 * @return the elapsed time
	 */
	default long elapsed() {
		return TimeUtils.nowMillis() - getLastTested().orElse(0);
	}

	/**
	 * Resets the cooldown
	 */
	default void reset() {
		setLastTested(TimeUtils.nowMillis());
	}

	/**
	 * Return the time in milliseconds when this cooldown was last {@link #test()}ed.
	 *
	 * @return the last call time
	 */
	@NonNull
	OptionalLong getLastTested();

	/**
	 * Sets the time in milliseconds when this cooldown was last tested.
	 * <p>
	 * Note: this should only be used when re-constructing a cooldown instance.
	 * Use {@link #test()} otherwise.
	 *
	 * @param time the time
	 */
	void setLastTested(long time);

	/**
	 * Gets the timeout in milliseconds for this cooldown.
	 *
	 * @return the timeout in milliseconds
	 */
	long getTimeout();

	/**
	 * Copies this cooldown to a new instance.
	 *
	 * @return a new cooldown instance
	 */
	@NonNull Cooldown copy();
}
