package fr.siroz.cariboustonks.core.system;

import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.system.CommandSystem;
import fr.siroz.cariboustonks.system.ContainerOverlaySystem;
import fr.siroz.cariboustonks.system.GlowingSystem;
import fr.siroz.cariboustonks.system.HudSystem;
import fr.siroz.cariboustonks.system.KeyBindSystem;
import fr.siroz.cariboustonks.system.NetworkSystem;
import fr.siroz.cariboustonks.system.ReminderSystem;
import fr.siroz.cariboustonks.system.TooltipAppenderSystem;
import fr.siroz.cariboustonks.system.WaypointSystem;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import org.jspecify.annotations.NonNull;

/**
 * The {@link SystemManager} class serves as a centralized registry and lifecycle manager
 * for system components used throughout the mod.
 */
public final class SystemManager {

    private static final Map<Class<? extends System>, System> SYSTEM_INSTANCES = new ConcurrentHashMap<>();

    public SystemManager() {
        register(new CommandSystem());
        register(new KeyBindSystem());
        register(new WaypointSystem());
        register(new ReminderSystem());
        register(new NetworkSystem());
        register(new ContainerOverlaySystem());
        register(new TooltipAppenderSystem());
        register(new HudSystem());
		register(new GlowingSystem());

		ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
			for (System system : SYSTEM_INSTANCES.values()) {
				system.onShutdown();
			}
		});
    }

    /**
     * Retrieves a registered {@link System} by its class.
     * <p>
     * This method returns the instance associated with the provided system class.
     *
     * @param systemClass the class of the system to retrieve
     * @param <T>          the type of the system
     * @return the registered system instance of type T
     * @throws IllegalArgumentException if no system of the given class is registered
     */
    @SuppressWarnings("unchecked")
    public <T extends System> @NonNull T getSystem(@NonNull Class<T> systemClass) {
        T system = (T) SYSTEM_INSTANCES.get(systemClass);
        if (system == null) {
			throw new IllegalArgumentException("System not found: " + systemClass.getSimpleName());
		}

        return system;
    }

    /**
     * Registers the provided {@link Feature} in all relevant systems.
     * This method sequentially registers the feature in various systems.
     *
     * @param feature the feature to be registered in all applicable systems
     */
    public void handleFeatureRegistration(@NonNull Feature feature) {
        for (System system : SYSTEM_INSTANCES.values()) {
            system.register(feature);
        }
    }

    private void register(@NonNull System system) {
		SYSTEM_INSTANCES.put(system.getClass(), system);

        system.onInit();
    }
}
