package fr.siroz.cariboustonks.core.data.hypixel.election;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

public record ElectionResult(
		@NotNull Mayor mayor,
		@NotNull Mayor minister,
		@NotNull Set<Perk> mayorPerks,
		@NotNull Optional<Perk> ministerPerk,
		@NotNull Instant timestamp
) {
}
