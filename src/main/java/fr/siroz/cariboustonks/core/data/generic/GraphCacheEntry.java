package fr.siroz.cariboustonks.core.data.generic;

import it.unimi.dsi.fastutil.objects.ObjectList;
import java.time.Instant;

public record GraphCacheEntry(ObjectList<ItemPrice> data, Instant timestamp) {

    public boolean isValid() {
        return Instant.now().isBefore(timestamp.plus(GenericDataSource.CACHE_EXPIRATION_PRICE_HISTORY));
    }
}
