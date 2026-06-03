package fr.siroz.cariboustonks.features.ui.tracking;

import org.jspecify.annotations.NonNull;

public final class NotifyConditions {

	private NotifyConditions() {
	}

	/**
	 * Une seule notification par entité
	 */
	public static final NotifyCondition ONCE =
			(_, _) -> NotifyCondition.Result.NOTIFY_ONCE;

	/**
	 * Notifie à chaque fois, sans restriction
	 */
	public static final NotifyCondition ALWAYS =
			(_, _) -> NotifyCondition.Result.NOTIFY;

	/**
	 * Notifie uniquement si l'entité est SOUS la hauteur Y
	 */
	public static @NonNull NotifyCondition belowY(double maxY) {
		return (entity, _) -> entity.position().y() <= maxY
				? NotifyCondition.Result.NOTIFY
				: NotifyCondition.Result.SKIP;
	}

	/**
	 * Notifie uniquement si l'entité est AU-DESSUS de la hauteur Y
	 */
	@SuppressWarnings("unused")
	public static @NonNull NotifyCondition aboveY(double minY) {
		return (entity, _) -> entity.position().y() >= minY
				? NotifyCondition.Result.NOTIFY
				: NotifyCondition.Result.SKIP;
	}

	/**
	 * Notifie au maximum N fois par entité, puis bloque
	 */
	public static @NonNull NotifyCondition maxTimes(int max) {
		return (_, count) -> {
			if (count >= max) return NotifyCondition.Result.SKIP;
			return count == max - 1 ? NotifyCondition.Result.NOTIFY_ONCE : NotifyCondition.Result.NOTIFY;
		};
	}

	/**
	 * Notifie une seule fois, mais uniquement si la condition Y est remplie
	 */
	public static NotifyCondition onceIfBelowY(double maxY) {
		return belowY(maxY).and(ONCE);
	}
}
