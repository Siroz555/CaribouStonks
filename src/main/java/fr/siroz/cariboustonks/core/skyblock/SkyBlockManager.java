package fr.siroz.cariboustonks.core.skyblock;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.core.service.scheduler.TickScheduler;
import fr.siroz.cariboustonks.core.skyblock.data.generic.GenericDataSource;
import fr.siroz.cariboustonks.core.skyblock.data.hypixel.HypixelDataSource;
import fr.siroz.cariboustonks.core.skyblock.dungeon.DungeonManager;
import fr.siroz.cariboustonks.core.skyblock.slayer.SlayerManager;
import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.event.SkyBlockEvents;
import fr.siroz.cariboustonks.util.DeveloperTools;
import java.util.concurrent.TimeUnit;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.hypixel.modapi.HypixelModAPI;
import net.hypixel.modapi.error.BuiltinErrorReason;
import net.hypixel.modapi.error.ErrorReason;
import net.hypixel.modapi.packet.impl.clientbound.ClientboundHelloPacket;
import net.hypixel.modapi.packet.impl.clientbound.event.ClientboundLocationPacket;

/**
 * The {@code SkyBlockManager} class serves as the core manager for all SkyBlock related-content.
 */
public final class SkyBlockManager {

	private final HypixelDataSource hypixelDataSource;
	private final GenericDataSource genericDataSource;

	private final DungeonManager dungeonManager;
	private final SlayerManager slayerManager;

	public SkyBlockManager() {
		// Data Sources
		this.hypixelDataSource = new HypixelDataSource();
		this.genericDataSource = new GenericDataSource();

		// SkyBlock Managers
		this.dungeonManager = new DungeonManager();
		this.slayerManager = new SlayerManager();

		// General Tick Scheduler for the SkyBlock API
		TickScheduler.getInstance().runRepeating(SkyBlockAPI::handleInternalUpdate, 3, TimeUnit.SECONDS);

		// Event listeners
		ClientPlayConnectionEvents.DISCONNECT.register((_handler, _client) -> this.onDisconnect());

		// Official Hypixel Mod API
		try {
			HypixelModAPI.getInstance().subscribeToEventPacket(ClientboundLocationPacket.class);
			HypixelModAPI.getInstance().createHandler(ClientboundHelloPacket.class, this::handleHelloPacket).onError(this::handleErrorPacket);
			HypixelModAPI.getInstance().createHandler(ClientboundLocationPacket.class, this::handleLocationPacket).onError(this::handleErrorPacket);
		} catch (Exception ex) {
			CaribouStonks.LOGGER.error("[HypixelModAPI] Unable to register Hypixel Mod API", ex);
		}
	}

	/**
	 * Retrieves the {@link HypixelDataSource} instance.
	 *
	 * @return the {@link HypixelDataSource} instance
	 */
	public HypixelDataSource getHypixelDataSource() {
		return hypixelDataSource;
	}

	/**
	 * Retrieves the {@link GenericDataSource} instance.
	 *
	 * @return the {@link GenericDataSource} instance
	 */
	public GenericDataSource getGenericDataSource() {
		return genericDataSource;
	}

	/**
	 * Retrieves the {@link DungeonManager} instance.
	 *
	 * @return the {@link DungeonManager} instance
	 */
	public DungeonManager getDungeonManager() {
		return dungeonManager;
	}

	/**
	 * Retrieves the {@link SlayerManager} instance.
	 *
	 * @return the {@link SlayerManager} instance
	 */
	public SlayerManager getSlayerManager() {
		return slayerManager;
	}

	@EventHandler(event = "ClientPlayConnectionEvents.DISCONNECT")
	private void onDisconnect() {
		if (SkyBlockAPI.isOnSkyBlock()) {
			SkyBlockEvents.LEAVE_EVENT.invoker().onLeave();
		}

		SkyBlockAPI.handleInternalLocationUpdate(null, false, "", IslandType.UNKNOWN);
	}

	private void handleHelloPacket(ClientboundHelloPacket helloPacket) {
		if (helloPacket == null) return;

		if (DeveloperTools.isInDevelopment()) {
			CaribouStonks.LOGGER.info("[HypixelModAPI] Hello o/ ({})", helloPacket.getEnvironment());
		}
		SkyBlockAPI.handleInternalLocationUpdate(true, null, null, null);
	}

	private void handleLocationPacket(ClientboundLocationPacket locationPacket) {
		if (locationPacket == null) return;

		String previousServerType = SkyBlockAPI.getGameType();
		String gameType = locationPacket.getServerType().isPresent() ? locationPacket.getServerType().get().name() : "";
		IslandType islandType = IslandType.getById(locationPacket.getMode().orElse(""));

		SkyBlockAPI.handleInternalLocationUpdate(null, null, gameType, islandType);
		SkyBlockEvents.ISLAND_CHANGE_EVENT.invoker().onIslandChange(islandType);

		if (gameType.equals("SKYBLOCK")) {
			SkyBlockAPI.handleInternalLocationUpdate(null, true, null, null);
			if (!previousServerType.equals("SKYBLOCK")) {
				SkyBlockEvents.JOIN_EVENT.invoker().onJoin(locationPacket.getServerName());
			}
		} else if (previousServerType.equals("SKYBLOCK")) {
			SkyBlockAPI.handleInternalLocationUpdate(null, false, null, null);
			SkyBlockEvents.LEAVE_EVENT.invoker().onLeave();
		}
	}

	private void handleErrorPacket(ErrorReason reason) {
		if (reason instanceof BuiltinErrorReason error) {
			CaribouStonks.LOGGER.warn("[HypixelModAPI] [ERROR] [{}] {}", error.getId(), error.name());
		} else {
			CaribouStonks.LOGGER.warn("[HypixelModAPI] [ERROR] [{}]", reason.getId());
		}
	}
}
