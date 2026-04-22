package fr.siroz.cariboustonks.features.hunting.tracking;

import java.util.Map;
import org.jspecify.annotations.NonNull;

/**
 * Immutable snapshot of computed session statistics.
 */
record ShardSessionStats(
		@NonNull Map<String, Integer> shardsByType,
		int totalShards,
		double totalCoins,
		double shardsPerHour,
		double coinsPerHour,
		int catchCount
) {

	ShardSessionStats {
		shardsByType = Map.copyOf(shardsByType); // defensive
	}
}
