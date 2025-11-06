package fr.siroz.cariboustonks.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.screen.slot.Slot;
import org.jetbrains.annotations.NotNull;

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
		void onClose(Screen screen);
	}

	@FunctionalInterface
	public interface KeyPressed {
		void onKeyPressed(Screen screen, KeyInput input, @NotNull Slot slot);
	}
}
