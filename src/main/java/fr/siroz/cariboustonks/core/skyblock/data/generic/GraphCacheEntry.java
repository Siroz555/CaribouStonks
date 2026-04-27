package fr.siroz.cariboustonks.core.skyblock.data.generic;

import java.time.Instant;

public record GraphCacheEntry(
		GraphParseResult data,
		Instant timestamp
) {

    public boolean isValid() {
        return Instant.now().isBefore(timestamp.plus(GenericDataSource.CACHE_EXPIRATION_PRICE_HISTORY));
    }
}
