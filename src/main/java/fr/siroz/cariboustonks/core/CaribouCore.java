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
		CaribouStonks.LOGGER.info("[CaribouStonksCore] Loading..");

		// "Main" core components
		this.crashManager = new CrashManager();
		this.jsonFileService = new JsonFileService();

		// "Secondary" core components
		new UpdateChecker();
		new ChangelogManager();
		new HypixelModAPI();
		new WelcomeMessage();

		// Data sources
		this.modDataSource = new ModDataSource();
		this.hypixelDataSource = new HypixelDataSource(this.modDataSource);
		this.genericDataSource = new GenericDataSource();

		// General Tick Scheduler for the SkyBlock API
		TickScheduler.getInstance().runRepeating(SkyBlockAPI::handleInternalUpdate, 3, TimeUnit.SECONDS);

		// Event listeners
		ClientPlayConnectionEvents.DISCONNECT.register((_handler, _client) -> this.onDisconnect());

		CaribouStonks.LOGGER.info("[CaribouStonksCore] Loaded!");
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
}
