package fr.siroz.cariboustonks.feature.hunting.tracking;

import java.util.Map;

/**
 * Immutable snapshot of computed session statistics.
 */
record ShardSessionStats(
		Map<String, Integer> shardsByType,
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
