package fr.siroz.cariboustonks.feature.waypoints;

import com.google.common.reflect.TypeToken;
import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.core.json.JsonProcessingException;
import fr.siroz.cariboustonks.core.skyblock.IslandType;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.feature.Feature;
import fr.siroz.cariboustonks.manager.waypoint.Waypoint;
import fr.siroz.cariboustonks.util.render.WorldRendererProvider;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class WaypointFeature extends Feature implements WorldRendererProvider {

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
        ClientLifecycleEvents.CLIENT_STOPPING.register(this::saveWaypoints);
        WorldRenderEvents.AFTER_TRANSLUCENT.register(this::render);
    }

    @Override
    public boolean isEnabled() {
        return SkyBlockAPI.isOnSkyBlock();
    }

    @Override
    public void render(WorldRenderContext context) {
        if (!isEnabled()) return;
        if (waypoints.isEmpty()) return;

        List<Waypoint> currentWaypoints = new ArrayList<>(waypoints.get(SkyBlockAPI.getIsland()));
        currentWaypoints.addAll(new ArrayList<>(waypoints.get(IslandType.ANY)));
        if (currentWaypoints.isEmpty()) {
			return;
		}

        for (Waypoint waypoint : currentWaypoints) {
            waypoint.getRenderer().render(context);
        }
    }

    @Contract(" -> new")
    public @NotNull Map<IslandType, List<Waypoint>> getWaypoints() {
        return new EnumMap<>(waypoints);
    }

    public void updateWaypoints(Map<IslandType, List<Waypoint>> newWaypoints) {
        if (newWaypoints == null || newWaypoints.isEmpty()) {
			return;
		}

        Map<IslandType, List<Waypoint>> copy = new EnumMap<>(IslandType.class);
        for (IslandType islandType : IslandType.values()) {
            List<Waypoint> waypointsList = newWaypoints.get(islandType);
            copy.put(islandType, waypointsList != null ? new ArrayList<>(waypointsList) : new ArrayList<>());
        }

        waypoints = copy;
    }

    private void onClientStarted(MinecraftClient client) {
        loadWaypoints().thenAccept(this::loadExistingWaypoints);
    }

    private CompletableFuture<Map<IslandType, List<Waypoint>>> loadWaypoints() {
        if (!Files.exists(WAYPOINT_PATH)) {
			return CompletableFuture.completedFuture(Map.of());
		}

        return CompletableFuture.supplyAsync(() -> {
            Type mapType = new TypeToken<Map<IslandType, List<Waypoint>>>() {}.getType();
			try {
				return CaribouStonks.core().getJsonFileService().loadMap(WAYPOINT_PATH, mapType);
			} catch (JsonProcessingException ex) {
				CaribouStonks.LOGGER.error("[WaypointFeature] Unable to load waypoints", ex);
				return Collections.emptyMap();
			}
		});
    }

    private void loadExistingWaypoints(@NotNull Map<IslandType, List<Waypoint>> waypointMap) {
        int loaded = 0;
        for (Map.Entry<IslandType, List<Waypoint>> entry : waypointMap.entrySet()) {
            // S'assurer qu'il n'y a pas de vide dans le Json, même si useless. Mettre + de sécu cotée Json
            if (entry.getValue() == null) {
				waypointMap.put(entry.getKey(), new ArrayList<>());
			}

            loaded += entry.getValue().size();
        }

        CaribouStonks.LOGGER.info("[Waypoints] Loaded {} waypoints", loaded);

        waypoints.putAll(waypointMap);
    }

    public void saveWaypoints(MinecraftClient client) {
		try {
			CaribouStonks.core().getJsonFileService().save(WAYPOINT_PATH, waypoints);
		} catch (JsonProcessingException ex) {
			CaribouStonks.LOGGER.error("[WaypointFeature] Unable to save waypoints", ex);
		}
	}
}
