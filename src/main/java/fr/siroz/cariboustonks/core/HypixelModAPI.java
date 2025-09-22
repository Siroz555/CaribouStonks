package fr.siroz.cariboustonks.core;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.event.SkyBlockEvents;
import fr.siroz.cariboustonks.core.skyblock.IslandType;
import fr.siroz.cariboustonks.util.StonksUtils;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.azureaaron.hmapi.events.HypixelPacketEvents;
import net.azureaaron.hmapi.network.HypixelNetworking;
import net.azureaaron.hmapi.network.packet.s2c.ErrorS2CPacket;
import net.azureaaron.hmapi.network.packet.s2c.HelloS2CPacket;
import net.azureaaron.hmapi.network.packet.s2c.HypixelS2CPacket;
import net.azureaaron.hmapi.network.packet.v1.s2c.LocationUpdateS2CPacket;
import org.jetbrains.annotations.NotNull;

/**
 * This version of HypixelModAPI uses 'hm-api' from azureaaron
 * <a href="https://github.com/azureaaron/hm-api">GitHub azureaaron hm-api</a>.
 * <p>
 * Much more than the official Hypixel version, many thanks to him.
 */
final class HypixelModAPI {

	HypixelModAPI() {
		try {
			HypixelNetworking.registerToEvents(StonksUtils.make(new Object2IntOpenHashMap<>(),
					map -> map.put(LocationUpdateS2CPacket.ID, 1)));
			HypixelPacketEvents.HELLO.register(this::handlePacket);
			HypixelPacketEvents.LOCATION_UPDATE.register(this::handlePacket);
		} catch (Exception ex) {
			CaribouStonks.LOGGER.error("[HypixelModAPI] Unable to register ModAPI to HypixelNetworking", ex);
		}
	}

	private void handlePacket(@NotNull HypixelS2CPacket packet) {
		switch (packet) {

			case HelloS2CPacket(var ignored) -> SkyBlockAPI.handleInternalLocationUpdate(true, null, null, null);

			case LocationUpdateS2CPacket(var serverName, var serverType, var ignored, var mode, var ignored1) -> {
				String previousServerType = SkyBlockAPI.getGameType();
				String gameType = serverType.orElse("");
				IslandType islandType = IslandType.getById(mode.orElse(""));

				SkyBlockAPI.handleInternalLocationUpdate(null, null, gameType, islandType);
				SkyBlockEvents.ISLAND_CHANGE.invoker().onIslandChange(islandType);

				if (gameType.equals("SKYBLOCK")) {
					SkyBlockAPI.handleInternalLocationUpdate(null, true, null, null);
					if (!previousServerType.equals("SKYBLOCK")) {
						SkyBlockEvents.JOIN.invoker().onJoin(serverName);
					}
				} else if (previousServerType.equals("SKYBLOCK")) {
					SkyBlockAPI.handleInternalLocationUpdate(null, false, null, null);
					SkyBlockEvents.LEAVE.invoker().onLeave();
				}
			}

			case ErrorS2CPacket(var id, var error) when id.equals(LocationUpdateS2CPacket.ID) -> {
				SkyBlockAPI.handleInternalLocationUpdate(null, null, "", IslandType.UNKNOWN);
				CaribouStonks.LOGGER.error("[HypixelModAPI] Failed to update Hypixel location! Error: {}", error);
			}

			default -> {
			}
		}
	}
}
