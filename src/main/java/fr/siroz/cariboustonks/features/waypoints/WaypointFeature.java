package fr.siroz.cariboustonks.features.waypoints;

import com.google.common.reflect.TypeToken;
import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.core.component.CommandComponent;
import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.core.module.waypoint.Waypoint;
import fr.siroz.cariboustonks.core.service.json.JsonFileService;
import fr.siroz.cariboustonks.core.service.json.JsonProcessingException;
import fr.siroz.cariboustonks.core.skyblock.IslandType;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.events.EventHandler;
import fr.siroz.cariboustonks.events.RenderEvents;
import fr.siroz.cariboustonks.rendering.world.WorldRenderer;
import fr.siroz.cariboustonks.screens.waypoints.WaypointScreen;
import fr.siroz.cariboustonks.util.Client;
import fr.siroz.cariboustonks.util.DeveloperTools;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.client.Minecraft;
import org.jspecify.annotations.NonNull;

public final class WaypointFeature extends Feature {

    private static final Path WAYPOINT_PATH = CaribouStonks.CONFIG_DIR.resolve("waypoints.json");

    private Map<IslandType, List<Waypoint>> waypoints = Arrays.stream(IslandType.VALUES)
            .collect(Collectors.toMap(
                    Function.identity(),
                    l -> new ArrayList<>(),
                    (a, b) -> a,
                    () -> new EnumMap<>(IslandType.class)
            ));

    public WaypointFeature() {
        ClientLifecycleEvents.CLIENT_STARTED.register(this::onClientStarted);
        ClientLifecycleEvents.CLIENT_STOPPING.register(this::onClientStopping);
        RenderEvents.WORLD_RENDER_EVENT.register(this::render);

		this.addComponent(CommandComponent.class, CommandComponent.builder()
				.namespaced("waypoints", ctx -> {
					ctx.executes(Client.openScreen(() -> WaypointScreen.create(null)));
				})
				.build());
    }

    @Override
    public boolean isEnabled() {
        return SkyBlockAPI.isOnSkyBlock() && !waypoints.isEmpty();
    }

    public @NonNull Map<IslandType, List<Waypoint>> getWaypointsSnapshot() {
        return new EnumMap<>(waypoints);
    }

    public void updateWaypoints(Map<IslandType, List<Waypoint>> newWaypoints) {
        if (newWaypoints == null || newWaypoints.isEmpty()) return;

        Map<IslandType, List<Waypoint>> copy = new EnumMap<>(IslandType.class);
        for (IslandType islandType : IslandType.values()) {
            List<Waypoint> waypointsList = newWaypoints.get(islandType);
            copy.put(islandType, waypointsList != null ? new ArrayList<>(waypointsList) : new ArrayList<>());
        }

        waypoints = copy;
    }

	public void saveWaypoints() {
		try {
			JsonFileService.get().save(WAYPOINT_PATH, waypoints);
		} catch (JsonProcessingException ex) {
			CaribouStonks.LOGGER.error("[WaypointFeature] Unable to save waypoints", ex);
		}
	}

	@EventHandler(event = "ClientLifecycleEvents.CLIENT_STARTED")
    private void onClientStarted(Minecraft client) {
        loadWaypoints().thenAccept(this::loadExistingWaypoints);
    }

	@EventHandler(event = "ClientLifecycleEvents.CLIENT_STOPPING")
	private void onClientStopping(Minecraft client) {
		saveWaypoints();
	}

	@EventHandler(event = "RenderEvents.WORLD_RENDER_EVENT")
	private void render(WorldRenderer renderer) {
		if (!isEnabled()) return;

		// SIROZ-NOTE: au lieu de créer des ArrayList a chaque frame, juste clear et addAll
		List<Waypoint> currentWaypoints = new ArrayList<>(waypoints.get(SkyBlockAPI.getIsland()));
		currentWaypoints.addAll(new ArrayList<>(waypoints.get(IslandType.ANY)));
		if (currentWaypoints.isEmpty()) {
			return;
		}

		for (Waypoint waypoint : currentWaypoints) {
			waypoint.getRenderer().render(renderer);
		}
	}

    private CompletableFuture<Map<IslandType, List<Waypoint>>> loadWaypoints() {
        if (!Files.exists(WAYPOINT_PATH)) {
			return CompletableFuture.completedFuture(Map.of());
		}

        return CompletableFuture.supplyAsync(() -> {
            Type mapType = new TypeToken<Map<IslandType, List<Waypoint>>>() {}.getType();
			try {
				return JsonFileService.get().loadMap(WAYPOINT_PATH, mapType);
			} catch (JsonProcessingException ex) {
				CaribouStonks.LOGGER.error("[WaypointFeature] Unable to load waypoints", ex);
				return Collections.emptyMap();
			}
		});
    }

    private void loadExistingWaypoints(@NonNull Map<IslandType, List<Waypoint>> waypointMap) {
        int loaded = 0;
        for (Map.Entry<IslandType, List<Waypoint>> entry : waypointMap.entrySet()) {
            // S'assurer qu'il n'y a pas de vide dans le Json, même si useless. Mettre + de sécu cotée Json
            if (entry.getValue() == null) {
				waypointMap.put(entry.getKey(), new ArrayList<>());
			}

            loaded += entry.getValue().size();
        }

        if (DeveloperTools.isInDevelopment()) {
			CaribouStonks.LOGGER.info("[Waypoints] Loaded {} waypoints", loaded);
		}

        waypoints.putAll(waypointMap);
    }
}
