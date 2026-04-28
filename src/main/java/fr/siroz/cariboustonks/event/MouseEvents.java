package fr.siroz.cariboustonks.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/**
 * Events related to mouse interactions.
 */
public final class MouseEvents {

	private MouseEvents() {
	}

	/**
	 * Called when the mouse wheel is scrolling
	 */
	public static final Event<AllowMouseScroll> ALLOW_MOUSE_SCROLL = EventFactory.createArrayBacked(AllowMouseScroll.class, listeners -> (horizontal, vertical) -> {
		for (AllowMouseScroll listener : listeners) {
			if (!listener.allowMouseScroll(horizontal, vertical)) {
				return false;
			}
		}
		return true;
	});

	/**
	 * Called when the Middle Click is handled out of a Screen.
	 */
	public static final Event<MiddleClickAir> MIDDLE_CLICK_AIR_EVENT = EventFactory.createArrayBacked(MiddleClickAir.class, listeners -> () -> {
		for (MiddleClickAir listener : listeners) {
			listener.onMiddleClickAir();
		}
	});

	@FunctionalInterface
	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	public interface AllowMouseScroll {
		boolean allowMouseScroll(double horizontal, double vertical);
	}

	@FunctionalInterface
	public interface MiddleClickAir {
		void onMiddleClickAir();
	}
}
