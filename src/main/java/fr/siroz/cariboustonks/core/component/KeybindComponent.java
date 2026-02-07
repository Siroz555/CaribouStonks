package fr.siroz.cariboustonks.core.component;

import fr.siroz.cariboustonks.core.module.input.KeyBind;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.NonNull;

/**
 * {@code Component} that provides Keybinds registration for features.
 *
 * <h2>Usage Examples</h2>
 * With {@link KeybindComponent}:
 * <h3>Simple</h3>
 * <pre>{@code
 * this.addComponent(KeybindComponent.class, KeybindComponent.builder()
 * 		.add(new KeyBind("Hello", GLFW.GLFW_KEY_A, true, () -> {}))
 * 		.build());
 * }</pre>
 *
 * <h3>With Screen</h3>
 * <pre>{@code
 * this.addComponent(KeybindComponent.class, KeybindComponent.builder()
 * 		.add(new KeyBind("Hello", GLFW.GLFW_KEY_A, (screen, keyCode, scanCode, slot) -> {}))
 * 		.build());
 * }</pre>
 * */
public final class KeybindComponent implements Component { // SIROZ-NOTE: documentation
	private final List<KeyBind> keybinds;

	private KeybindComponent(List<KeyBind> keybinds) {
		this.keybinds = List.copyOf(keybinds);
	}

	public List<KeyBind> getKeybinds() {
		return keybinds;
	}

	@NonNull
	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private final List<KeyBind> keybinds = new ArrayList<>();

		public Builder add(@NonNull KeyBind keybind) {
			Objects.requireNonNull(keybind, "keybind cannot be null");
			this.keybinds.add(keybind);
			return this;
		}

		public KeybindComponent build() {
			if (keybinds.isEmpty()) {
				throw new IllegalStateException("At least one keyBind must be added");
			}
			return new KeybindComponent(keybinds);
		}
	}
}
