package fr.siroz.cariboustonks.core.mod;

import fr.siroz.cariboustonks.core.mod.changelog.ChangelogManager;
import fr.siroz.cariboustonks.core.mod.crash.CrashManager;
import fr.siroz.cariboustonks.core.mod.dev.DeveloperManager;
import fr.siroz.cariboustonks.util.DeveloperTools;
import org.jspecify.annotations.NonNull;

/**
 * The {@code ModManager} class serves as the core manager for the mod.
 */
public final class ModManager {

	private final CrashManager crashManager;
	private final ModDataSource modDataSource;
	private final SecretModFeatures secretModFeatures;

	public ModManager() {
		this.crashManager = new CrashManager();
		this.modDataSource = new ModDataSource();
		this.secretModFeatures = new SecretModFeatures();

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
	public @NonNull CrashManager getCrashManager() {
		return crashManager;
	}

	/**
	 * Retrieves the {@link ModDataSource} instance.
	 *
	 * @return the {@link ModDataSource} instance
	 */
	public @NonNull ModDataSource getModDataSource() {
		return modDataSource;
	}

	/**
	 * Retrieves the {@link SecretModFeatures} instance.
	 *
	 * @return the {@link SecretModFeatures} instance
	 */
	public SecretModFeatures getSecretModFeatures() {
		return secretModFeatures;
	}
}
