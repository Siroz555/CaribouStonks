package fr.siroz.cariboustonks.core.module.input;

import fr.siroz.cariboustonks.core.component.KeybindComponent;
import fr.siroz.cariboustonks.system.KeyBindSystem;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.inventory.Slot;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Represents a custom {@link KeyMapping} that can be registered in the game control settings.
 * The KeyBinding can handle key presses both globally and within specific screens.
 * <p>
 * {@code Fabric API} does not provide an API in this way, hence the existence of this internal KeyBind API.
 *
 * @see KeybindComponent
 */
public class KeyBind {

	private final String name;
	@Nullable
	private final Runnable onPress;
	@Nullable
	private final SlotKeyHandler onScreenPress;
	private final boolean firstPress;
	private final KeyMapping keyBinding;
	private boolean isPressed = false;

	/**
	 * Creates a new {@link KeyBind}.
	 *
	 * @param name       the name of the KeyBind, which will be displayed in the game's Control Menu
	 * @param keyCode    the keyCode of the KeyBind ({@link org.lwjgl.glfw.GLFW})
	 * @param firstPress whether {@code onPress} should be executed only once
	 *                   (prevents detecting a hold and ensures a single click is registered)
	 */
	public KeyBind(@NonNull String name, int keyCode, boolean firstPress) {
		this(name, keyCode, firstPress, null, null);
	}

	/**
	 * Creates a new {@link KeyBind}.
	 *
	 * @param name       the name of the KeyBind, which will be displayed in the game's Control Menu
	 * @param keyCode    the keyCode of the KeyBind ({@link org.lwjgl.glfw.GLFW})
	 * @param firstPress whether {@code onPress} should be executed only once
	 *                   (prevents detecting a hold and ensures a single click is registered)
	 * @param onPress    the code to execute when the key is pressed, or null
	 */
	public KeyBind(@NonNull String name, int keyCode, boolean firstPress, @Nullable Runnable onPress) {
		this(name, keyCode, firstPress, onPress, null);
	}

	/**
	 * Creates a new {@link KeyBind}, dedicated exclusively to {@link Screen}s.
	 *
	 * @param name          the name of the KeyBind, which will be displayed in the game's Control Menu
	 * @param keyCode       the keyCode of the KeyBind ({@link org.lwjgl.glfw.GLFW})
	 * @param onScreenPress the handler that will be executed when the key is pressed on a screen, or null
	 */
	public KeyBind(@NonNull String name, int keyCode, @Nullable SlotKeyHandler onScreenPress) {
		this(name, keyCode, true, null, onScreenPress);
	}

	private KeyBind(
			@NonNull String name,
			int keyCode,
			boolean firstPress,
			@Nullable Runnable onPress,
			@Nullable SlotKeyHandler onScreenPress
	) {
		this.name = name;
		this.firstPress = firstPress;
		this.onPress = onPress;
		this.onScreenPress = onScreenPress;
		this.keyBinding = new KeyMapping(name, keyCode, KeyBindSystem.CATEGORY);
	}

	public String getName() {
		return name;
	}

	public boolean isFirstPress() {
		return firstPress;
	}

	public boolean isPressed() {
		return isPressed;
	}

	public boolean hasScreenPressHandler() {
		return onScreenPress != null;
	}

	public void setPressed(boolean pressed) {
		isPressed = pressed;
	}

	public KeyMapping getKeyBinding() {
		return keyBinding;
	}

	public void onPress() {
		if (onPress != null) {
			onPress.run();
		}
	}

	public void onScreenPress(Screen screen, Slot slot) {
		if (onScreenPress != null) {
			onScreenPress.onKeyPressed(screen, slot);
		}
	}
}
