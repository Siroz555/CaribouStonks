package fr.siroz.cariboustonks.manager.waypoint;

import fr.siroz.cariboustonks.core.scheduler.TickScheduler;
import fr.siroz.cariboustonks.event.RenderEvents;
import fr.siroz.cariboustonks.event.WorldEvents;
import fr.siroz.cariboustonks.manager.Manager;
import fr.siroz.cariboustonks.rendering.world.WorldRenderer;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The {@code WaypointManager} class is a central management system for all waypoints in the game.
 * It implements the {@link Manager} interface and provides functionality for waypoint lifecycle
 * management, rendering coordination, and timeout handling.
 * <h2>Usage Examples:</h2>
 * <pre>{@code
 * // Accessing the manager
 * WaypointManager wpManager = CaribouStonks.managers().getManager(WaypointManager.class);
 *
 * // Creating and registering a waypoint
 * Waypoint waypoint = Waypoint.builder(position)
 *     .timeout(30, TimeUnit.SECONDS)
 *     .build();
 * wpManager.addWaypoint(waypoint);
 *
 * // Removing a waypoint
 * wpManager.removeWaypoint(waypoint);
 * }</pre>
 *
 * @see Waypoint
 * @see Manager
 * @see TickScheduler
 */
public final class WaypointManager implements Manager {

    private static final Minecraft CLIENT = Minecraft.getInstance();

    private final Map<UUID, Waypoint> waypoints = new ConcurrentHashMap<>();

    @ApiStatus.Internal
    public WaypointManager() {
		WorldEvents.JOIN.register(world -> this.resetWaypoints());
        RenderEvents.WORLD_RENDER.register(this::render);
        TickScheduler.getInstance().runRepeating(this::onTick, 1);
    }

	/**
	 * Adds a waypoint to the collection if it is not already present.
	 *
	 * @param waypoint the waypoint to add
	 */
	public void addWaypoint(@NotNull Waypoint waypoint) {
        waypoints.putIfAbsent(waypoint.getUuid(), waypoint);
    }

    /**
	 * Removes the specified waypoint from the collection of waypoints.
	 *
	 * @param waypoint the waypoint to be removed
	 */
	public void removeWaypoint(@NotNull Waypoint waypoint) {
        waypoints.remove(waypoint.getUuid());
    }

    private void render(WorldRenderer renderer) {
        if (CLIENT.player == null || CLIENT.level == null || waypoints.isEmpty()) {
			return;
		}

        for (Map.Entry<UUID, Waypoint> waypoint : waypoints.entrySet()) {
            waypoint.getValue().getRenderer().render(renderer);
        }
    }

    private void onTick() {
        if (waypoints.isEmpty()) {
			return;
		}

        for (Map.Entry<UUID, Waypoint> waypoint : waypoints.entrySet()) {
            if (waypoint.getValue().getTimeoutTicks() == -1) {
				continue;
			}

            waypoint.getValue().decreaseTimeout();
            if (waypoint.getValue().getTimeoutTicks() == 0) {
                //waypoints.remove(waypoint.getKey());
                waypoint.getValue().destroy();
            }
        }
    }

	private void resetWaypoints() {
		for (Map.Entry<UUID, Waypoint> waypoint : waypoints.entrySet()) {
			if (waypoint.getValue().isResetBetweenWorlds()) {
				waypoint.getValue().destroy();
			}
		}
	}
}
