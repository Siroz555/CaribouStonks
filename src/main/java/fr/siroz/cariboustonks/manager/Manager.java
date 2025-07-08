package fr.siroz.cariboustonks.manager;

import fr.siroz.cariboustonks.feature.Feature;
import org.jetbrains.annotations.NotNull;

/**
 * Implementations of this interface represent managers that provide common functionality
 * related to a feature and can be registered and retrieved via the {@link Managers} registry.
 */
public interface Manager {

    /**
     * Registers the provided {@link Feature} with this manager.
     * This allows the manager to initialize or otherwise interact with the feature
     * following its registration. Individual managers may implement specific
     * behavior for handling feature registration.
     *
     * @param feature the feature to be registered with this manager
     */
    default void register(@NotNull Feature feature) {
    }

    /**
     * Invoked when the manager is initialized. This method is intended to be overridden
     * to provide initialization logic specific to the implementing manager.
     * <p>
     * It is typically called during the registration of the manager within the
     * {@code Managers} registry to ensure that the manager is properly prepared before being used.
     */
    default void onInit() {
    }

    /**
     * Called to perform cleanup or shutdown operations when the manager is no longer needed.
     * This method is intended to be overridden by implementing classes to define
     * specific logic necessary to gracefully terminate or release resources held by the manager.
     * <p>
     * This method is invoked during the lifecycle management handled by the {@code Managers} class.
     */
    default void onShutdown() {
    }
}
