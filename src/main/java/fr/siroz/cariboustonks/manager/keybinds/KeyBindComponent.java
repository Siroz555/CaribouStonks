package fr.siroz.cariboustonks.manager.keybinds;

import fr.siroz.cariboustonks.manager.Component;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;

import java.util.List;

/**
 * Capability component for registering KeyBinds for a {@code Feature}.
 * <p>
 * A {@code KeyBindComponent} is discovered by the {@link KeyBindManager}
 * and wired into Fabric’s {@link KeyBindingHelper}.
 * <p>
 * Registration is one-shot and happens at the client init time.
 *
 * <h3>Example</h3>
 * <pre>{@code
 * addComponent(KeyBindComponent.class, () -> List.of(
 * 		new KeyBind("Test", GLFW.GLFW_KEY_MINUS, true, this::updateState)
 * ));
 * }</pre>
 *
 * @see KeyBind
 */
public interface KeyBindComponent extends Component {

	List<KeyBind> keyBinds();
}
