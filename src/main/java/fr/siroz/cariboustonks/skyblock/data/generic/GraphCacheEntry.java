package fr.siroz.cariboustonks.skyblock.data.generic;

import java.time.Instant;
import java.util.List;

public record GraphCacheEntry(List<ItemPrice> data, Instant timestamp) {

    public boolean isValid() {
        return Instant.now().isBefore(timestamp.plus(GenericDataSource.CACHE_EXPIRATION_PRICE_HISTORY));
    }
}
