package fr.siroz.cariboustonks.core;

import fr.siroz.cariboustonks.core.changelog.ChangelogManager;
import fr.siroz.cariboustonks.core.crash.CrashManager;
import fr.siroz.cariboustonks.core.dev.DeveloperManager;
import fr.siroz.cariboustonks.core.json.JsonFileService;
import fr.siroz.cariboustonks.util.DeveloperTools;
import org.jetbrains.annotations.ApiStatus;

/**
 * The {@code CaribouManager} class serves as the core manager for the mod.
 */
public final class CaribouManager {

	private final CrashManager crashManager;
	private final JsonFileService jsonFileService;
	private final ModDataSource modDataSource;

	@ApiStatus.Internal
	public CaribouManager() {
		// "Main" core components
		this.crashManager = new CrashManager();
		this.jsonFileService = new JsonFileService();
		this.modDataSource = new ModDataSource();

		// "Secondary" core components
		new UpdateChecker();
		new ChangelogManager();
		new WelcomeMessage();
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
}
