package fr.siroz.cariboustonks.core.feature;

import fr.siroz.cariboustonks.config.Config;
import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.core.component.Component;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.NotNull;

/**
 * Base class for all feature implementations.
 * <p>
 * Features are modular units of functionality that can be enabled/disabled
 * and composed using {@link Component}s.
 *
 * @see Component
 * @see FeatureManager
 */
public abstract class Feature {

	/**
	 * Shared Minecraft client instance for all features.
	 */
	protected static final Minecraft CLIENT = Minecraft.getInstance();

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
	 * Attaches a {@link Component} to this feature.
	 * <p>
	 * Contract:
	 * <ul>
	 *   <li>Must be called at construction time of the feature, before managers register it.</li>
	 *   <li>Only a single instance per {@code type} is allowed per feature.</li>
	 *   <li>Intended for registration-only capabilities.</li>
	 * </ul>
	 *
	 * @param <C>       the component subtype to attach
	 * @param type      the concrete class token of the component
	 * @param component the component instance to attach
	 * @throws IllegalStateException if a component of the same {@code type} is already attached
	 */
	protected <C extends Component> void addComponent(@NotNull Class<C> type, @NotNull C component) {
		Component current = components.putIfAbsent(type, component);
		if (current != null) {
			throw new IllegalStateException("Component of type %s already registered for feature %s"
					.formatted(type.getSimpleName(), getShortName()));
		}
	}

	/**
	 * Retrieves an attached {@link Component} by its capability type.
	 * <p>
	 * Example:
	 * <pre>{@code
	 * feature.getComponent(CommandComponent.class)
	 *        .ifPresent(c ->  {});
	 * }</pre>
	 *
	 * @param <C>  the component type to retrieve
	 * @param type the component interface class token
	 * @return an {@link Optional} containing the component, or empty if not present
	 */
	public <C extends Component> Optional<C> getComponent(@NotNull Class<C> type) {
		return Optional.ofNullable(type.cast(components.get(type)));
	}

	/**
	 * Gets the root configuration.
	 *
	 * @return the root configuration
	 */
	protected final Config config() {
		return ConfigManager.getConfig();
	}

	/**
	 * Called after all features have been initialized.
	 *
	 * @param features the feature manager instance
	 */
	protected void postInitialize(@NotNull FeatureManager features) {
	}

	/**
	 * Called every tick by the client.
	 */
	protected void onClientTick() {
	}

	/**
	 * Called when the client joins a server.
	 */
	protected void onClientJoinServer() {
	}

	/**
	 * Provides the feature's canonical short name.
	 * <p>
	 * Automatically strips the "Feature" suffix from the class name
	 *
	 * @return simplified feature name
	 */
	public String getShortName() {
		return this.getClass().getSimpleName().replace("Feature", "");
	}
}
