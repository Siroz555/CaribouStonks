package fr.siroz.cariboustonks.core.skyblock;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.core.service.scheduler.TickScheduler;
import fr.siroz.cariboustonks.core.skyblock.data.generic.GenericDataSource;
import fr.siroz.cariboustonks.core.skyblock.data.hypixel.HypixelDataSource;
import fr.siroz.cariboustonks.core.skyblock.dungeon.DungeonManager;
import fr.siroz.cariboustonks.core.skyblock.slayer.SlayerManager;
import fr.siroz.cariboustonks.core.skyblock.tablist.TabListManager;
import fr.siroz.cariboustonks.events.EventHandler;
import fr.siroz.cariboustonks.events.SkyBlockEvents;
import fr.siroz.cariboustonks.util.StonksUtils;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.concurrent.TimeUnit;
import net.azureaaron.hmapi.events.HypixelPacketEvents;
import net.azureaaron.hmapi.network.HypixelNetworking;
import net.azureaaron.hmapi.network.packet.s2c.ErrorS2CPacket;
import net.azureaaron.hmapi.network.packet.s2c.HelloS2CPacket;
import net.azureaaron.hmapi.network.packet.s2c.HypixelS2CPacket;
import net.azureaaron.hmapi.network.packet.v1.s2c.LocationUpdateS2CPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import org.jspecify.annotations.NonNull;

/**
 * The {@code SkyBlockManager} class serves as the core manager for all SkyBlock related-content.
 */
public final class SkyBlockManager {

	private final HypixelDataSource hypixelDataSource;
	private final GenericDataSource genericDataSource;

	private final DungeonManager dungeonManager;
	private final SlayerManager slayerManager;
	private final TabListManager tabListManager;

	public SkyBlockManager() {
		// Data Sources
		this.hypixelDataSource = new HypixelDataSource();
		this.genericDataSource = new GenericDataSource();
		// SkyBlock Managers
		this.dungeonManager = new DungeonManager();
		this.slayerManager = new SlayerManager();
		this.tabListManager = new TabListManager();
		// Init
		this.initialize();
	}

	public void initialize() {
		// Bootstrap SkyBlockAPI dep
		var modDataSource = CaribouStonks.mod().getModDataSource();
		SkyBlockAPI.bootstrap(hypixelDataSource::getElection, modDataSource::getAttributeByShardName);

		// General Tick Scheduler for the SkyBlock Manager/API
		TickScheduler.getInstance().runRepeating(() -> {
			SkyBlockAPI.handleInternalUpdate();
			this.updateTimeSystem();
		}, 1, TimeUnit.SECONDS);

		// Event listeners
		ClientPlayConnectionEvents.DISCONNECT.register((_, _) -> this.onDisconnect());

		// Hypixel Mod API
		try {
			HypixelNetworking.registerToEvents(StonksUtils.make(new Object2IntOpenHashMap<>(),
					map -> map.put(LocationUpdateS2CPacket.ID, 1)));
			HypixelPacketEvents.HELLO.register(this::handlePacket);
			HypixelPacketEvents.LOCATION_UPDATE.register(this::handlePacket);
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

	/**
	 * Retrieves the {@link TabListManager} instance.
	 *
	 * @return {@link TabListManager} instance
	 */
	public TabListManager getTabListManager() {
		return tabListManager;
	}

	@EventHandler(event = "ClientPlayConnectionEvents.DISCONNECT")
	private void onDisconnect() {
		if (SkyBlockAPI.isOnSkyBlock()) {
			SkyBlockEvents.LEAVE_EVENT.invoker().onLeave();
		}

		SkyBlockAPI.handleInternalLocationUpdate(false, "", IslandType.UNKNOWN);
	}

	private void handlePacket(@NonNull HypixelS2CPacket packet) {
		switch (packet) {

			case HelloS2CPacket(var ignored) -> SkyBlockAPI.handleInternalLocationUpdate(null, null, null);

			case LocationUpdateS2CPacket(var serverName, var serverType, var ignored, var mode, var ignored1) -> {
				String previousServerType = SkyBlockAPI.getGameType();
				String gameType = serverType.orElse("");
				IslandType islandType = IslandType.getById(mode.orElse(""));

				SkyBlockAPI.handleInternalLocationUpdate(null, gameType, islandType);
				SkyBlockEvents.ISLAND_CHANGE_EVENT.invoker().onIslandChange(islandType, serverName);

				if (gameType.equals("SKYBLOCK")) {
					SkyBlockAPI.handleInternalLocationUpdate(true, null, null);
					if (!previousServerType.equals("SKYBLOCK")) {
						SkyBlockEvents.JOIN_EVENT.invoker().onJoin(serverName);
					}
				} else if (previousServerType.equals("SKYBLOCK")) {
					SkyBlockAPI.handleInternalLocationUpdate(false, null, null);
					SkyBlockEvents.LEAVE_EVENT.invoker().onLeave();
				}
			}

			case ErrorS2CPacket(var id, var error) when id.equals(LocationUpdateS2CPacket.ID) -> {
				SkyBlockAPI.handleInternalLocationUpdate(null, "", IslandType.UNKNOWN);
				CaribouStonks.LOGGER.error("[HypixelModAPI] Failed to update Hypixel location! Error: {}", error);
			}

			default -> {
			}
		}
	}

	private void updateTimeSystem() {
		SkyBlockTime prevTime = SkyBlockAPI.getTime();
		SkyBlockTime nowTime = SkyBlockTime.of(SkyBlockAPI.getSkyBlockMillis());
		SkyBlockSeason nowSeason = SkyBlockSeason.VALUES[nowTime.month() / 3];
		SkyBlockSeason prevSeason = SkyBlockSeason.VALUES[prevTime.month() / 3];
		SkyBlockSeason.Month nowMonth = SkyBlockSeason.Month.VALUES[nowTime.month()];
		SkyBlockSeason.Month prevMonth = SkyBlockSeason.Month.VALUES[prevTime.month()];
		// Update
		SkyBlockAPI.handleInternalTimeUpdate(nowTime, nowSeason, nowMonth);
		// Events
		if (nowTime.year() != prevTime.year()) SkyBlockEvents.YEAR_CHANGE_EVENT.invoker().onYearChange(nowTime.year());
		if (nowSeason != prevSeason) SkyBlockEvents.SEASON_CHANGE_EVENT.invoker().onSeasonChange(nowSeason);
		if (nowMonth != prevMonth) SkyBlockEvents.MONTH_CHANGE_EVENT.invoker().onMonthChange(nowMonth);
		if (nowTime.day() != prevTime.day()) SkyBlockEvents.DAY_CHANGE_EVENT.invoker().onDayChange(nowTime.day());
		if (nowTime.hour() != prevTime.hour()) SkyBlockEvents.HOUR_CHANGE_EVENT.invoker().onHourChange(nowTime.hour());
	}
}
