package fr.siroz.cariboustonks.feature;

import fr.siroz.cariboustonks.manager.Component;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.MinecraftClient;
import org.jetbrains.annotations.NotNull;

/**
 * Base class for all features.
 */
public abstract class Feature {

	/**
	 * Shared Minecraft client instance for all features.
	 */
	protected static final MinecraftClient CLIENT = MinecraftClient.getInstance();

	private final Map<Class<? extends Component>, Component> components = new HashMap<>();

	/**
	 * Determines if the feature is globally enabled based on its configuration.
	 * <p>
	 * Implementations should consider:
	 * <ul>
	 *   <li>User configuration settings</li>
	 *   <li>Island checks</li>
	 *   <li>Null checks</li>
	 * </ul>
	 * </p>
	 *
	 * @return {@code true} if the feature should be active
	 */
	public abstract boolean isEnabled();

	/**
	 * Attaches a {@link Component} to the feature
	 * <p>
	 * <b>Contract:</b>
	 * <ul>
	 *   <li>Component type must be unique per feature</li>
	 *   <li>Should be called during constructor initialization</li>
	 * </ul>
	 *
	 * @param <C>       Component subtype to register
	 * @param type      class token of the component type
	 * @param component the Component instance to attach
	 * @throws IllegalStateException if a Component of this type already exists
	 */
	protected <C extends Component> void addComponent(@NotNull Class<C> type, @NotNull C component) {
		if (components.containsKey(type)) {
			throw new IllegalStateException("Component of type " + type.getSimpleName() + " already registered");
		}

		components.put(type, component);
	}

	/**
	 * Retrieves a {@link Component} by its type
	 *
	 * @param <C>  the Component type to retrieve
	 * @param type class token of the component type
	 * @return Optional containing the component, empty if not found
	 */
	public <C extends Component> Optional<C> getComponent(@NotNull Class<C> type) {
		return Optional.ofNullable(type.cast(components.get(type)));
	}

	/**
	 * Provides the feature's canonical short name.
	 * <p>
	 * Automatically strips "Feature" suffix from class name
	 *
	 * @return simplified feature name
	 */
	public String getShortName() {
		return this.getClass().getSimpleName().replace("Feature", "");
	}
}
