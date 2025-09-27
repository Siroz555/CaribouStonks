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
	 * Called after all features have been initialized.
	 *
	 * @param features the feature manager instance
	 */
	protected void postInitialize(@NotNull Features features) {
	}

	/**
	 * Attaches a {@link Component} to this feature.
	 * <p>
	 * Usage:
	 * <pre>{@code
	 * addComponent(CommandComponent.class, dispatcher -> {
	 *   dispatcher.register(ClientCommandManager.literal("hello").executes(ctx -> 1));
	 * }));
	 *
	 * addComponent(KeyBindComponent.class, () -> List.of(
	 * 	 new KeyBind("Test", GLFW.GLFW_KEY_MINUS, true, this::updateState)
	 * ));
	 * }</pre>
	 * Contract:
	 * <ul>
	 *   <li>Must be called at construction time of the feature, before managers register it.</li>
	 *   <li>Only a single instance per {@code type} is allowed per feature.</li>
	 *   <li>Intended for registration-only capabilities.</li>
	 * </ul>
	 *
	 * @param <C>       the component subtype to attach
	 * @param type      the concrete class token of the component interface (capability)
	 * @param component the component instance to attach
	 * @throws IllegalStateException if a component of the same {@code type} is already attached
	 */
	protected <C extends Component> void addComponent(@NotNull Class<C> type, @NotNull C component) {
		if (components.containsKey(type)) {
			throw new IllegalStateException("Component of type " + type.getSimpleName() + " already registered");
		}

		components.put(type, component);
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
