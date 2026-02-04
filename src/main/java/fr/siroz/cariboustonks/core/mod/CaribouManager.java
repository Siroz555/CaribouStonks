package fr.siroz.cariboustonks.core.mod;

import fr.siroz.cariboustonks.core.mod.changelog.ChangelogManager;
import fr.siroz.cariboustonks.core.mod.crash.CrashManager;
import fr.siroz.cariboustonks.core.mod.dev.DeveloperManager;
import fr.siroz.cariboustonks.util.DeveloperTools;

/**
 * The {@code CaribouManager} class serves as the core manager for the mod.
 */
public final class CaribouManager {

	private final CrashManager crashManager;
	private final ModDataSource modDataSource;

	public CaribouManager() {
		// "Main" core components
		this.crashManager = new CrashManager();
		this.modDataSource = new ModDataSource();

		// "Secondary" core components
		new UpdateChecker();
		new ChangelogManager();
		new WelcomeMessage();

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
	 * Retrieves the {@link ModDataSource} instance.
	 *
	 * @return the {@link ModDataSource} instance
	 */
	public ModDataSource getModDataSource() {
		return modDataSource;
	}
}
