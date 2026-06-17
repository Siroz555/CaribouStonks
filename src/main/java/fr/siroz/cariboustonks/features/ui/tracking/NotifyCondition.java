package fr.siroz.cariboustonks.features.ui.tracking;

import net.minecraft.world.entity.Entity;
import org.jspecify.annotations.NonNull;

@FunctionalInterface
public interface NotifyCondition {

	Result evaluate(@NonNull Entity entity, int notifyCount);

	enum Result {
		/**
		 * Ne pas notifier pour l'instant
		 */
		SKIP,
		/**
		 * Notifier et autoriser de futures notifications
		 */
		NOTIFY,
		/**
		 * Notifier une dernière fois et bloquer définitivement
		 */
		NOTIFY_ONCE
	}

	default NotifyCondition and(@NonNull NotifyCondition other) {
		return (entity, count) -> {
			Result r1 = this.evaluate(entity, count);
			if (r1 == Result.SKIP) return Result.SKIP;

			Result r2 = other.evaluate(entity, count);
			if (r2 == Result.SKIP) return Result.SKIP; // Garde le résultat le plus restrictif

			if (r1 == Result.NOTIFY_ONCE || r2 == Result.NOTIFY_ONCE) return Result.NOTIFY_ONCE;

			return Result.NOTIFY;
		};
	}
}
