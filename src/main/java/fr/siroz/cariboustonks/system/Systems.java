package fr.siroz.cariboustonks.system;

import fr.siroz.cariboustonks.feature.Feature;
import fr.siroz.cariboustonks.system.command.CommandSystem;
import fr.siroz.cariboustonks.system.container.overlay.ContainerOverlaySystem;
import fr.siroz.cariboustonks.system.container.tooltip.ContainerTooltipAppenderSystem;
import fr.siroz.cariboustonks.system.glowing.GlowingSystem;
import fr.siroz.cariboustonks.system.hud.HudSystem;
import fr.siroz.cariboustonks.system.keybinds.KeyBindSystem;
import fr.siroz.cariboustonks.system.network.NetworkSystem;
import fr.siroz.cariboustonks.system.reminder.ReminderSystem;
import fr.siroz.cariboustonks.system.waypoint.WaypointSystem;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The {@link Systems} class serves as a centralized registry and lifecycle manager
 * for system components used throughout the mod.
 */
public final class Systems {

    private static final Map<Class<? extends System>, System> SYSTEM_INSTANCES = new ConcurrentHashMap<>();

    @ApiStatus.Internal
    public Systems() {
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> onStop());
        register(new CommandSystem());
        register(new KeyBindSystem());
        register(new WaypointSystem());
        register(new ReminderSystem());
        register(new NetworkSystem());
        register(new ContainerOverlaySystem());
        register(new ContainerTooltipAppenderSystem());
        register(new HudSystem());
		register(new GlowingSystem());
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
    public <T extends System> @NotNull T getSystem(@NotNull Class<T> systemClass) {
        T system = (T) SYSTEM_INSTANCES.get(systemClass);
        if (system == null) {
			throw new IllegalArgumentException("Manager not found: " + systemClass.getSimpleName());
		}

        return system;
    }

    /**
     * Registers the provided {@link Feature} in all relevant systems.
     * This method sequentially registers the feature in various systems.
     *
     * @param feature the feature to be registered in all applicable systems
     */
    @ApiStatus.Internal
    public void handleFeatureRegistration(@NotNull Feature feature) {
        for (System system : SYSTEM_INSTANCES.values()) {
            system.register(feature);
        }
    }

    /**
     * Registers a {@link System} instance by adding it to the {@code MANAGER_INSTANCES} map.
     *
     * @param system an instance of a class that implements the {@link System} interface to be registered
     */
    private void register(@NotNull System system) {
		SYSTEM_INSTANCES.put(system.getClass(), system);

        system.onInit();
    }

    private void onStop() {
        for (System system : SYSTEM_INSTANCES.values()) {
            system.onShutdown();
        }
    }
}
