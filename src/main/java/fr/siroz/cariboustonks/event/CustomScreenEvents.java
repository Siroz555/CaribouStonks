package fr.siroz.cariboustonks.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.world.inventory.Slot;
import org.jspecify.annotations.NonNull;

/**
 * Provides custom screen event hooks.
 */
public final class CustomScreenEvents {

	private CustomScreenEvents() {
	}

	/**
	 * Called when a screen is closed in the game
	 */
	public static final Event<ScreenClose> CLOSE = EventFactory.createArrayBacked(ScreenClose.class, listeners -> (screen) -> {
		for (ScreenClose listener : listeners) {
			listener.onClose(screen);
		}
	});

	/**
	 * Called when a key is pressed within a screen.
	 */
	public static final Event<KeyPressed> KEY_PRESSED = EventFactory.createArrayBacked(KeyPressed.class, listeners -> (screen, keyInput, slot) -> {
		for (KeyPressed listener : listeners) {
			listener.onKeyPressed(screen, keyInput, slot);
		}
	});

	@FunctionalInterface
	public interface ScreenClose {
		void onClose(@NonNull Screen screen);
	}

	@FunctionalInterface
	public interface KeyPressed {
		void onKeyPressed(@NonNull Screen screen, @NonNull KeyEvent input, @NonNull Slot slot);
	}
}
