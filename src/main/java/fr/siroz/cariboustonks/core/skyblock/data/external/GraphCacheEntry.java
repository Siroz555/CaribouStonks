package fr.siroz.cariboustonks.core.skyblock.data.external;

import java.time.Duration;
import java.time.Instant;

public record GraphCacheEntry(
		GraphParseResult data,
		Instant timestamp
) {

    public boolean isValid() {
        return Instant.now().isBefore(timestamp.plus(Duration.ofMinutes(15)));
    }
}
