package fr.siroz.cariboustonks.manager;

import fr.siroz.cariboustonks.feature.Feature;
import fr.siroz.cariboustonks.manager.command.CommandManager;
import fr.siroz.cariboustonks.manager.container.overlay.ContainerOverlayManager;
import fr.siroz.cariboustonks.manager.container.tooltip.ContainerTooltipAppenderManager;
import fr.siroz.cariboustonks.manager.dungeon.DungeonManager;
import fr.siroz.cariboustonks.manager.glowing.GlowingManager;
import fr.siroz.cariboustonks.manager.hud.HudManager;
import fr.siroz.cariboustonks.manager.keybinds.KeyBindManager;
import fr.siroz.cariboustonks.manager.network.NetworkManager;
import fr.siroz.cariboustonks.manager.reminder.ReminderManager;
import fr.siroz.cariboustonks.manager.slayer.SlayerManager;
import fr.siroz.cariboustonks.manager.waypoint.WaypointManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The {@code Managers} class serves as a centralized registry and lifecycle manager
 * for manager components used throughout the mod.
 */
public final class Managers {

    private static final Map<Class<? extends Manager>, Manager> MANAGER_INSTANCES = new ConcurrentHashMap<>();

    @ApiStatus.Internal
    public Managers() {
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> onStop());
        register(new CommandManager());
        register(new KeyBindManager());
        register(new WaypointManager());
        register(new ReminderManager());
        register(new NetworkManager());
        register(new ContainerOverlayManager());
        register(new ContainerTooltipAppenderManager());
        register(new HudManager());
		register(new GlowingManager());
		register(new SlayerManager());
		register(new DungeonManager());
    }

    /**
     * Retrieves a registered {@link Manager} by its class.
     * <p>
     * This method returns the instance associated with the provided manager class.
     *
     * @param managerClass the class of the manager to retrieve
     * @param <T>          the type of the manager
     * @return the registered manager instance of type T
     * @throws IllegalArgumentException if no manager of the given class is registered
     */
    @SuppressWarnings("unchecked")
    public <T extends Manager> @NotNull T getManager(@NotNull Class<T> managerClass) {
        T manager = (T) MANAGER_INSTANCES.get(managerClass);
        if (manager == null) {
			throw new IllegalArgumentException("Manager not found: " + managerClass.getSimpleName());
		}

        return manager;
    }

    /**
     * Registers the provided {@link Feature} in all relevant managers.
     * This method sequentially registers the feature in various managers.
     *
     * @param feature the feature to be registered in all applicable managers
     */
    @ApiStatus.Internal
    public void handleFeatureRegistration(@NotNull Feature feature) {
        for (Manager manager : MANAGER_INSTANCES.values()) {
            manager.register(feature);
        }
    }

    /**
     * Registers a {@link Manager} instance by adding it to the {@code MANAGER_INSTANCES} map.
     *
     * @param manager an instance of a class that implements the {@link Manager} interface to be registered
     */
    private void register(@NotNull Manager manager) {
        MANAGER_INSTANCES.put(manager.getClass(), manager);

        manager.onInit();
    }

    private void onStop() {
        for (Manager manager : MANAGER_INSTANCES.values()) {
            manager.onShutdown();
        }
    }
}
