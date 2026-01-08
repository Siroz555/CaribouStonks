package fr.siroz.cariboustonks.core;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.core.changelog.ChangelogManager;
import fr.siroz.cariboustonks.core.crash.CrashManager;
import fr.siroz.cariboustonks.core.data.generic.GenericDataSource;
import fr.siroz.cariboustonks.core.data.hypixel.HypixelDataSource;
import fr.siroz.cariboustonks.core.data.mod.ModDataSource;
import fr.siroz.cariboustonks.core.dev.DeveloperManager;
import fr.siroz.cariboustonks.core.json.JsonFileService;
import fr.siroz.cariboustonks.core.scheduler.TickScheduler;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.event.SkyBlockEvents;
import fr.siroz.cariboustonks.core.skyblock.IslandType;
import fr.siroz.cariboustonks.util.DeveloperTools;
import java.util.concurrent.TimeUnit;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.hypixel.modapi.HypixelModAPI;
import net.hypixel.modapi.error.BuiltinErrorReason;
import net.hypixel.modapi.error.ErrorReason;
import net.hypixel.modapi.packet.impl.clientbound.ClientboundHelloPacket;
import net.hypixel.modapi.packet.impl.clientbound.event.ClientboundLocationPacket;
import org.jetbrains.annotations.ApiStatus;

/**
 * The {@code CaribouStonksCore} class serves as the core manager for the mod.
 */
public final class CaribouCore {

	private final CrashManager crashManager;
	private final JsonFileService jsonFileService;

	private final ModDataSource modDataSource;
	private final HypixelDataSource hypixelDataSource;
	private final GenericDataSource genericDataSource;

	@ApiStatus.Internal
	public CaribouCore() {
		// "Main" core components
		this.crashManager = new CrashManager();
		this.jsonFileService = new JsonFileService();

		// "Secondary" core components
		new UpdateChecker();
		new ChangelogManager();
		new WelcomeMessage();

		// Data sources
		this.modDataSource = new ModDataSource();
		this.hypixelDataSource = new HypixelDataSource(this.modDataSource);
		this.genericDataSource = new GenericDataSource();

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

	@ApiStatus.Internal
	public void initDeveloperMode() {
		// Developer Mode
		if (DeveloperTools.isInDevelopment()) {
			new DeveloperManager();
		}
	}

	/**
	 * Retrieves the {@link CrashManager} instance.
	 *
	 * @return the {@link CrashManager} instance.
	 */
	public CrashManager getCrashManager() {
		return crashManager;
	}

	/**
	 * Retrieves the {@link JsonFileService} instance.
	 *
	 * @return the {@link JsonFileService} instance
	 */
	public JsonFileService getJsonFileService() {
		return jsonFileService;
	}

	/**
	 * Retrieves the {@link ModDataSource} instance.
	 *
	 * @return the {@link ModDataSource} instance
	 */
	public ModDataSource getModDataSource() {
		return modDataSource;
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

	@EventHandler(event = "ClientPlayConnectionEvents.DISCONNECT")
	private void onDisconnect() {
		if (SkyBlockAPI.isOnSkyBlock()) {
			SkyBlockEvents.LEAVE.invoker().onLeave();
		}

		SkyBlockAPI.handleInternalLocationUpdate(null, false, "", IslandType.UNKNOWN);
	}

	@ApiStatus.Internal
	private void handleHelloPacket(ClientboundHelloPacket helloPacket) {
		if (helloPacket == null) return;

		if (DeveloperTools.isInDevelopment()) {
			CaribouStonks.LOGGER.info("[HypixelModAPI] Hello o/ ({})", helloPacket.getEnvironment());
		}
		SkyBlockAPI.handleInternalLocationUpdate(true, null, null, null);
	}

	@ApiStatus.Internal
	private void handleLocationPacket(ClientboundLocationPacket locationPacket) {
		if (locationPacket == null) return;

		String previousServerType = SkyBlockAPI.getGameType();
		String gameType = locationPacket.getServerType().isPresent() ? locationPacket.getServerType().get().name() : "";
		IslandType islandType = IslandType.getById(locationPacket.getMode().orElse(""));

		SkyBlockAPI.handleInternalLocationUpdate(null, null, gameType, islandType);
		SkyBlockEvents.ISLAND_CHANGE.invoker().onIslandChange(islandType);

		if (gameType.equals("SKYBLOCK")) {
			SkyBlockAPI.handleInternalLocationUpdate(null, true, null, null);
			if (!previousServerType.equals("SKYBLOCK")) {
				SkyBlockEvents.JOIN.invoker().onJoin(locationPacket.getServerName());
			}
		} else if (previousServerType.equals("SKYBLOCK")) {
			SkyBlockAPI.handleInternalLocationUpdate(null, false, null, null);
			SkyBlockEvents.LEAVE.invoker().onLeave();
		}
	}

	@ApiStatus.Internal
	private void handleErrorPacket(ErrorReason reason) {
		if (reason instanceof BuiltinErrorReason error) {
			CaribouStonks.LOGGER.warn("[HypixelModAPI] [ERROR] [{}] {}", error.getId(), error.name());
		} else {
			CaribouStonks.LOGGER.warn("[HypixelModAPI] [ERROR] [{}]", reason.getId());
		}
	}
}
