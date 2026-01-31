package fr.siroz.cariboustonks.system;

import fr.siroz.cariboustonks.feature.Feature;
import org.jetbrains.annotations.NotNull;

/**
 * Implementations of this interface represent systems that provide common functionality
 * related to a feature and can be registered and retrieved via the {@link Systems} registry.
 */
public interface System {

    /**
     * Registers the provided {@link Feature} with this system.
     * This allows the system to initialize or otherwise interact with the feature
     * following its registration. Individual systems may implement specific
     * behavior for handling feature registration.
     *
     * @param feature the feature to be registered with this system
     */
    default void register(@NotNull Feature feature) {
    }

    /**
     * Invoked when the system is initialized. This method is intended to be overridden
     * to provide initialization logic specific to the implementing system.
     * <p>
     * It is typically called during the registration of the system within the
     * {@link Systems} registry to ensure that the system is properly prepared before being used.
     */
    default void onInit() {
    }

    /**
     * Called to perform cleanup or shutdown operations when the system is no longer needed.
     * This method is intended to be overridden by implementing classes to define
     * specific logic necessary to gracefully terminate or release resources held by the system.
     * <p>
     * This method is invoked during the lifecycle management handled by the {@link Systems} class.
     */
    default void onShutdown() {
    }
}
