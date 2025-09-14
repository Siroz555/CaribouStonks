package fr.siroz.cariboustonks.core;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.core.changelog.ChangelogManager;
import fr.siroz.cariboustonks.core.crash.CrashType;
import fr.siroz.cariboustonks.core.data.generic.GenericDataSource;
import fr.siroz.cariboustonks.core.data.hypixel.HypixelDataSource;
import fr.siroz.cariboustonks.core.data.mod.ModDataSource;
import fr.siroz.cariboustonks.core.json.JsonFileService;
import fr.siroz.cariboustonks.core.scheduler.TickScheduler;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.event.SkyBlockEvents;
import fr.siroz.cariboustonks.util.Client;
import fr.siroz.cariboustonks.core.skyblock.IslandType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * The {@code CaribouStonksCore} class serves as the core manager for the mod.
 * It initializes and provides access to key parts such as data sources and
 * crash reporting utilities, and handles various lifecycle events.
 */
public final class CaribouStonksCore {

	private static final Set<String> REPORTED_CRASHES = ConcurrentHashMap.newKeySet();

	private final JsonFileService jsonFileService;
	private final ModDataSource modDataSource;
	private final HypixelDataSource hypixelDataSource;
	private final GenericDataSource genericDataSource;

	@ApiStatus.Internal
	public CaribouStonksCore() {
		CaribouStonks.LOGGER.info("[CaribouStonksCore] Loading..");

		TickScheduler.getInstance().runRepeating(SkyBlockAPI::update, 3, TimeUnit.SECONDS);

		new UpdateChecker();
		new ChangelogManager();

		ClientPlayConnectionEvents.DISCONNECT.register((_handler, _client) -> onDisconnect());

		this.jsonFileService = new JsonFileService();
		this.modDataSource = new ModDataSource();
		this.hypixelDataSource = new HypixelDataSource(this.modDataSource);
		this.genericDataSource = new GenericDataSource();

		new HypixelModAPI();
		new WelcomeMessage();

		CaribouStonks.LOGGER.info("[CaribouStonksCore] Loaded!");
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

	/**
	 * Reports of a crash occurring within the mod.
	 *
	 * @param type      the specific {@link CrashType} of the crash
	 * @param niceName  a user-friendly name describing the affected component
	 * @param fullName  the fully qualified name of the affected component
	 * @param reason    the reason or cause of the crash
	 * @param throwable the exception or error thrown during the crash
	 */
	public void reportCrash(
			@NotNull CrashType type,
			@NotNull String niceName,
			@NotNull String fullName,
			@NotNull String reason,
			Throwable throwable
	) {
		reportCrash(type, niceName, fullName, reason, true, true, throwable);
	}

	/**
	 * Reports of a crash occurring within the mod.
	 *
	 * @param type                   the specific {@link CrashType} of the crash
	 * @param niceName               a user-friendly name
	 * @param fullName               the fully qualified name
	 * @param reason                 the reason or cause of the crash
	 * @param shouldSendChat         whether an error message should be sent on the chat
	 * @param shouldSendNotification whether a notification should be displayed
	 * @param throwable              the exception
	 */
	public void reportCrash(
			@NotNull CrashType type,
			@NotNull String niceName,
			@NotNull String fullName,
			@NotNull String reason,
			boolean shouldSendChat,
			boolean shouldSendNotification,
			Throwable throwable
	) {
		String crashSignature = fullName + "|" + reason;

		if (shouldSendChat) {
			if (!REPORTED_CRASHES.contains(crashSignature)) { // Vérifier si ce crash a déjà été reporté
				Client.sendErrorMessage("CaribouStonks error: "
						+ type.getName() + " '" + niceName + "' was crashed in " + reason, shouldSendNotification);
			}
		}

		REPORTED_CRASHES.add(crashSignature);

		CaribouStonks.LOGGER.warn("---------------- STONKS CRASH ----------------");
		CaribouStonks.LOGGER.warn(
				"CrashType of {} {} due to {}", type.toString().toLowerCase(Locale.ROOT), niceName, reason);
		CaribouStonks.LOGGER.error("Exception thrown by {}", fullName, throwable);
		CaribouStonks.LOGGER.warn("----------------------------------------------");
	}

	@EventHandler(event = "ClientPlayConnectionEvents.DISCONNECT")
	private void onDisconnect() {
		if (SkyBlockAPI.isOnSkyBlock()) {
			SkyBlockEvents.LEAVE.invoker().onLeave();
		}

		SkyBlockAPI.handleLocationUpdate(null, false, "", IslandType.UNKNOWN);
	}
}
