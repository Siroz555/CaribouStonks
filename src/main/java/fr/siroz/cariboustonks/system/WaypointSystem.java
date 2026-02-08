package fr.siroz.cariboustonks.system;

import fr.siroz.cariboustonks.core.module.waypoint.Waypoint;
import fr.siroz.cariboustonks.core.service.scheduler.TickScheduler;
import fr.siroz.cariboustonks.core.system.System;
import fr.siroz.cariboustonks.events.EventHandler;
import fr.siroz.cariboustonks.events.RenderEvents;
import fr.siroz.cariboustonks.rendering.world.WorldRenderer;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents;
import net.minecraft.client.Minecraft;
import org.jspecify.annotations.NonNull;

/**
 * The {@code WaypointSystem} class is a central management system for all waypoints in the game.
 * It implements the {@link System} interface and provides functionality for waypoint lifecycle
 * management, rendering coordination, and timeout handling.
 * <h2>Usage Examples:</h2>
 * <pre>{@code
 * // Accessing the system
 * var ws = CaribouStonks.systems().getSystem(WaypointSystem.class);
 *
 * // Creating and registering a waypoint
 * Waypoint waypoint = Waypoint.builder(position)
 *     .timeout(30, TimeUnit.SECONDS)
 *     .build();
 * ws.addWaypoint(waypoint);
 *
 * // Removing a waypoint
 * ws.removeWaypoint(waypoint);
 * }</pre>
 *
 * @see Waypoint
 * @see System
 * @see TickScheduler
 */
public final class WaypointSystem implements System {

    private static final Minecraft CLIENT = Minecraft.getInstance();

    private final Map<UUID, Waypoint> waypoints = new ConcurrentHashMap<>();

    public WaypointSystem() {
		ClientWorldEvents.AFTER_CLIENT_WORLD_CHANGE.register((event, world) -> this.resetWaypoints());
        RenderEvents.WORLD_RENDER_EVENT.register(this::render);
        TickScheduler.getInstance().runRepeating(this::onTick, 1);
    }

	/**
	 * Adds a waypoint to the collection if it is not already present.
	 *
	 * @param waypoint the waypoint to add
	 */
	public void addWaypoint(@NonNull Waypoint waypoint) {
        waypoints.putIfAbsent(waypoint.getUuid(), waypoint);
    }

    /**
	 * Removes the specified waypoint from the collection of waypoints.
	 *
	 * @param waypoint the waypoint to be removed
	 */
	public void removeWaypoint(@NonNull Waypoint waypoint) {
        waypoints.remove(waypoint.getUuid());
    }

	@EventHandler(event = "RenderEvents.WORLD_RENDER_EVENT")
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
