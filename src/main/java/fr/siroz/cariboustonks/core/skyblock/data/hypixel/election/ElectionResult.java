package fr.siroz.cariboustonks.core.skyblock.data.hypixel.election;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import org.jspecify.annotations.NonNull;

public record ElectionResult(
		@NonNull Mayor mayor,
		@NonNull Mayor minister,
		@NonNull Set<Perk> mayorPerks,
		@NonNull Optional<Perk> ministerPerk,
		@NonNull Instant timestamp
) {
}
