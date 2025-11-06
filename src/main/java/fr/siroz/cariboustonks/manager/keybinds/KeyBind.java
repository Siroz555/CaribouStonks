package fr.siroz.cariboustonks.manager.keybinds;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.screen.slot.Slot;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a custom {@link KeyBinding} that can be registered in the game control settings.
 * The KeyBinding can handle key presses both globally and within specific screens.
 * <p>
 * {@code Fabric API} does not provide an API in this way, hence the existence of this internal KeyBind API.
 *
 * <h2>Usage Examples</h2>
 * With {@link KeyBindComponent} interface:
 * <h3>Simple</h3>
 * <pre>{@code
 * @Override
 * public List<KeyBind> registerKeyBinds() {
 *     KeyBind keyBind = new KeyBind("Hello", GLFW.GLFW_KEY_A, true, () -> {
 *         // Code
 *     });
 *     return List.of(keyBind);
 * }
 * }</pre>
 *
 * <h3>With Screen</h3>
 * <pre>{@code
 * @Override
 * public List<KeyBind> registerKeyBinds() {
 *     KeyBind keyBind = new KeyBind("Hello", GLFW.GLFW_KEY_A, (screen, keyCode, scanCode, slot) -> {
 *         // Code
 *     });
 *     return List.of(keyBind);
 * }
 * }</pre>
 *
 * @see KeyBindComponent
 */
public class KeyBind {

	private final String name;
	@Nullable
	private final Runnable onPress;
	@Nullable
	private final SlotKeyHandler onScreenPress;
	private final boolean firstPress;
	private final KeyBinding keyBinding;
	private boolean isPressed = false;

	/**
	 * Creates a new {@link KeyBind}.
	 *
	 * @param name       the name of the KeyBind, which will be displayed in the game's Control Menu
	 * @param keyCode    the keyCode of the KeyBind ({@link org.lwjgl.glfw.GLFW})
	 * @param firstPress whether {@code onPress} should be executed only once
	 *                   (prevents detecting a hold and ensures a single click is registered)
	 */
	public KeyBind(@NotNull String name, int keyCode, boolean firstPress) {
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
	public KeyBind(@NotNull String name, int keyCode, boolean firstPress, @Nullable Runnable onPress) {
		this(name, keyCode, firstPress, onPress, null);
	}

	/**
	 * Creates a new {@link KeyBind}, dedicated exclusively to {@link Screen}s.
	 *
	 * @param name          the name of the KeyBind, which will be displayed in the game's Control Menu
	 * @param keyCode       the keyCode of the KeyBind ({@link org.lwjgl.glfw.GLFW})
	 * @param onScreenPress the handler that will be executed when the key is pressed on a screen, or null
	 */
	public KeyBind(@NotNull String name, int keyCode, @Nullable SlotKeyHandler onScreenPress) {
		this(name, keyCode, true, null, onScreenPress);
	}

	private KeyBind(
			@NotNull String name,
			int keyCode,
			boolean firstPress,
			@Nullable Runnable onPress,
			@Nullable SlotKeyHandler onScreenPress
	) {
		this.name = name;
		this.firstPress = firstPress;
		this.onPress = onPress;
		this.onScreenPress = onScreenPress;
		this.keyBinding = new KeyBinding(name, keyCode, KeyBindManager.CATEGORY);
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

	@ApiStatus.Internal
	public void setPressed(boolean pressed) {
		isPressed = pressed;
	}

	@ApiStatus.Internal
	public KeyBinding getKeyBinding() {
		return keyBinding;
	}

	@ApiStatus.Internal
	public void onPress() {
		if (onPress != null) {
			onPress.run();
		}
	}

	@ApiStatus.Internal
	public void onScreenPress(Screen screen, Slot slot) {
		if (onScreenPress != null) {
			onScreenPress.onKeyPressed(screen, slot);
		}
	}
}
