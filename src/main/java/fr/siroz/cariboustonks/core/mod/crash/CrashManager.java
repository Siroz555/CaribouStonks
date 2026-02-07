package fr.siroz.cariboustonks.core.mod.crash;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.util.Client;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.jspecify.annotations.NonNull;

/**
 * Manages and reports crashes within the mod.
 */
public final class CrashManager {

	private final Set<String> reportedCrashes;

	public CrashManager() {
		this.reportedCrashes = ConcurrentHashMap.newKeySet();
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
			@NonNull CrashType type,
			@NonNull String niceName,
			@NonNull String fullName,
			@NonNull String reason,
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
			@NonNull CrashType type,
			@NonNull String niceName,
			@NonNull String fullName,
			@NonNull String reason,
			boolean shouldSendChat,
			boolean shouldSendNotification,
			Throwable throwable
	) {
		String crashSignature = fullName + "|" + reason;

		if (shouldSendChat) {
			if (!reportedCrashes.contains(crashSignature)) { // Vérifier si ce crash a déjà été reporté
				Client.sendErrorMessage("CaribouStonks error: "
						+ type.getName() + " '" + niceName + "' was crashed in " + reason, shouldSendNotification);
			}
		}

		reportedCrashes.add(crashSignature);

		CaribouStonks.LOGGER.warn("---------------- STONKS CRASH ----------------");
		CaribouStonks.LOGGER.warn(
				"CrashType of {} {} due to {}", type.toString().toLowerCase(Locale.ROOT), niceName, reason);
		CaribouStonks.LOGGER.error("Exception thrown by {}", fullName, throwable);
		CaribouStonks.LOGGER.warn("----------------------------------------------");
	}
}
