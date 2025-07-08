package fr.siroz.cariboustonks.manager.keybinds;

import fr.siroz.cariboustonks.feature.Feature;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * This interface is used to define a contract for registering custom {@link KeyBind} within a {@link Feature}.
 * <p>
 * The purpose of this interface is to provide a structure for features that
 * require the definition of one or more keybindings. Each implementation should
 * supply the desired keybinds using the {@code registerKeyBinds()} method, which
 * returns a list of those keybinds.
 * <h3>Exemple</h3>
 * <pre>{@code
 * @Override
 * public List<KeyBind> registerKeyBinds() {
 *     KeyBind keyBind = new KeyBind("X", GLFW.GLFW_KEY_X, true, () -> {});
 *     return List.of(keyBind);
 * }
 * }</pre>
 *
 * @see KeyBind
 */
public interface KeyBindRegistration {

	@ApiStatus.Internal
	@NotNull
	List<KeyBind> registerKeyBinds();
}
