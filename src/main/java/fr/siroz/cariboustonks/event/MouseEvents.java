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
	public static final Event<MouseScroll> MOUSE_SCROLL = EventFactory.createArrayBacked(MouseScroll.class, listeners -> (horizontal, vertical) -> {
		// Ne retourne pas directement le premier listener.
		for (MouseScroll listener : listeners) {
			if (listener.onMouseScroll(horizontal, vertical)) {
				return true;
			}
		}
		return false;
	});

	@FunctionalInterface
	public interface MouseScroll {
		boolean onMouseScroll(double horizontal, double vertical);
	}
}
