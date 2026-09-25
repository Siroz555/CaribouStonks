package fr.siroz.cariboustonks.core.skyblock;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.core.infrastructure.scheduler.TickScheduler;
import fr.siroz.cariboustonks.core.skyblock.data.external.ExternalDataSource;
import fr.siroz.cariboustonks.core.skyblock.data.hypixel.HypixelDataSource;
import fr.siroz.cariboustonks.core.skyblock.data.hypixel.HypixelPartyManager;
import fr.siroz.cariboustonks.core.skyblock.data.hypixel.election.ElectionResult;
import fr.siroz.cariboustonks.core.skyblock.data.hypixel.election.Mayor;
import fr.siroz.cariboustonks.core.skyblock.data.hypixel.election.Perk;
import fr.siroz.cariboustonks.core.skyblock.data.repository.RepositoryDataSource;
import fr.siroz.cariboustonks.core.skyblock.dungeon.DungeonManager;
import fr.siroz.cariboustonks.core.skyblock.slayer.SlayerManager;
import fr.siroz.cariboustonks.core.skyblock.tablist.TabListManager;
import fr.siroz.cariboustonks.events.SkyBlockEvents;
import fr.siroz.cariboustonks.platform.context.ClientContext;
import fr.siroz.cariboustonks.util.DeveloperTools;
import fr.siroz.cariboustonks.util.StonksUtils;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import net.azureaaron.hmapi.events.HypixelPacketEvents;
import net.azureaaron.hmapi.network.HypixelNetworking;
import net.azureaaron.hmapi.network.packet.s2c.ErrorS2CPacket;
import net.azureaaron.hmapi.network.packet.s2c.HypixelS2CPacket;
import net.azureaaron.hmapi.network.packet.v1.s2c.LocationUpdateS2CPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * The {@code SkyBlockManager} class serves as the core manager for all SkyBlock related-content.
 */
public final class SkyBlockManager {

	private final RepositoryDataSource repositoryDataSource;
	private final HypixelDataSource hypixelDataSource;
	private final ExternalDataSource externalDataSource;

	private final DungeonManager dungeonManager;
	private final SlayerManager slayerManager;
	private final TabListManager tabListManager;
	private final HypixelPartyManager partyManager;

	private volatile SkyBlockLocation currentLocation = SkyBlockLocation.NONE;
	private volatile SkyBlockTime currentTime = SkyBlockTime.DEFAULT;

	public SkyBlockManager() {
		// Data Sources
		this.repositoryDataSource = new RepositoryDataSource();
		this.hypixelDataSource = new HypixelDataSource();
		this.externalDataSource = new ExternalDataSource();
		// SkyBlock Managers
		this.dungeonManager = new DungeonManager(this);
		this.slayerManager = new SlayerManager(this);
		this.tabListManager = new TabListManager(this);
		this.partyManager = new HypixelPartyManager();

		TickScheduler.getInstance().runRepeating(this::onSecond, 1, TimeUnit.SECONDS);
		ClientPlayConnectionEvents.DISCONNECT.register((_, _) -> this.onDisconnect());

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
	 * Returns the current {@link SkyBlockLocation}
	 *
	 * @return the {@code SkyBlockLocation}
	 */
	public SkyBlockLocation location() {
		return currentLocation;
	}

	/**
	 * Returns the current {@link SkyBlockTime}
	 *
	 * @return the {@code SkyBlockTime}
	 */
	public SkyBlockTime time() {
		return currentTime;
	}

	/**
	 * Returns the current {@link SkyBlockSeason}
	 *
	 * @return the {@code SkyBlockSeason}
	 */
	public SkyBlockSeason season() {
		return seasonOf(currentTime);
	}

	/**
	 * Returns whether the given {@link Mayor} currently holds the mayor or minister role,
	 * and (optionally) whether the specified {@link Perk} is present for that role.
	 *
	 * @param mayor the {@link Mayor} to check
	 * @param perk  optional {@link Perk} to verify for the given role; if {@code null} only the role is checked
	 * @return {@code true} if the given {@code mayor} matches the current mayor or minister and,
	 * when {@code perk} is provided, the requested perk is present for that role
	 */
	public boolean isMayorOrMinister(@NonNull Mayor mayor, @Nullable Perk perk) {
		ElectionResult result = hypixelDataSource.getElection();
		return result != null && result.hasMayorOrMinister(mayor, perk);
	}

	/**
	 * Retrieves the current SkyBlock Area where the player is from the Scoreboard.
	 *
	 * @return an {@link Optional} containing the area name
	 */
	public @NonNull Optional<String> getArea() {
		for (String line : ClientContext.getScoreboard()) {
			if (line.contains(SkyBlockConstants.SCOREBOARD_AREA_ICON) || line.contains(SkyBlockConstants.SCOREBOARD_RIFT_AREA_ICON)) {
				return Optional.of(line.strip());
			}
		}
		return Optional.empty();
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
	 * Retrieves the {@link ExternalDataSource} instance.
	 *
	 * @return the {@link ExternalDataSource} instance
	 */
	public ExternalDataSource getExternalDataSource() {
		return externalDataSource;
	}

	/**
	 * Retrieves the {@link RepositoryDataSource} instance.
	 *
	 * @return the {@link RepositoryDataSource} instance
	 */
	public RepositoryDataSource getRepositoryDataSource() {
		return repositoryDataSource;
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

	/**
	 * Retrieves the {@link HypixelPartyManager} instance.
	 *
	 * @return {@link HypixelPartyManager} instance
	 */
	public HypixelPartyManager getPartyManager() {
		return partyManager;
	}

	private void onDisconnect() {
		resetLocation();
	}

	private void onSecond() {
		updateTime();
		// Development
		if (DeveloperTools.isInDevelopment() && ClientContext.isLocalServer()) forceOnSkyBlock();
	}

	private void handlePacket(@NonNull HypixelS2CPacket packet) {
		switch (packet) {
			case LocationUpdateS2CPacket(var serverName, var serverType, var ignored, var mode, var ignored1) -> {
				SkyBlockLocation location = SkyBlockLocation.of(serverType.orElse(""), IslandType.getById(mode.orElse("")));
				updateLocation(serverName, location);
				if (DeveloperTools.isInDevelopment()) CaribouStonks.LOGGER.info("[HypixelModAPI] Location: {}", location);
			}
			case ErrorS2CPacket(var id, var error) when id.equals(LocationUpdateS2CPacket.ID) -> {
				resetLocation();
				if (DeveloperTools.isInDevelopment()) CaribouStonks.LOGGER.error("[HypixelModAPI] Failed to update Hypixel location! Error: {}", error);
			}
			default -> {
			}
		}
	}

	private void updateTime() {
		SkyBlockTime previous = currentTime;
		SkyBlockTime now = SkyBlockTime.now();
		currentTime = now;

		if (now.year() != previous.year()) SkyBlockEvents.YEAR_CHANGE_EVENT.invoker().onYearChange(now.year());
		if (seasonOf(now) != seasonOf(previous)) SkyBlockEvents.SEASON_CHANGE_EVENT.invoker().onSeasonChange(seasonOf(now));
		if (now.month() != previous.month()) SkyBlockEvents.MONTH_CHANGE_EVENT.invoker().onMonthChange(SkyBlockSeason.Month.VALUES[now.month()]);
		if (now.day() != previous.day()) SkyBlockEvents.DAY_CHANGE_EVENT.invoker().onDayChange(now.day());
		if (now.hour() != previous.hour()) SkyBlockEvents.HOUR_CHANGE_EVENT.invoker().onHourChange(now.hour());
	}

	private SkyBlockSeason seasonOf(@NonNull SkyBlockTime time) {
		return SkyBlockSeason.VALUES[time.month() / 3];
	}

	private void updateLocation(String serverName, @NonNull SkyBlockLocation next) {
		SkyBlockLocation previous = currentLocation;
		currentLocation = next;

		SkyBlockEvents.ISLAND_CHANGE_EVENT.invoker().onIslandChange(next.island(), serverName);
		if (!previous.onSkyBlock() && next.onSkyBlock()) {
			SkyBlockEvents.JOIN_EVENT.invoker().onJoin(serverName);
		} else if (previous.onSkyBlock() && !next.onSkyBlock()) {
			SkyBlockEvents.LEAVE_EVENT.invoker().onLeave();
		}
	}

	private void resetLocation() {
		boolean wasOnSkyBlock = currentLocation.onSkyBlock();
		currentLocation = SkyBlockLocation.NONE;
		if (wasOnSkyBlock) {
			SkyBlockEvents.LEAVE_EVENT.invoker().onLeave();
		}
	}

	private void forceOnSkyBlock() {
		SkyBlockLocation location = currentLocation;
		if (!location.onSkyBlock()) {
			currentLocation = new SkyBlockLocation(true, location.gameType(), location.island());
		}
	}
}
