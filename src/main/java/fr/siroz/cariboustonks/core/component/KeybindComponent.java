package fr.siroz.cariboustonks.core.component;

import fr.siroz.cariboustonks.core.module.input.KeyBind;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.NonNull;

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
