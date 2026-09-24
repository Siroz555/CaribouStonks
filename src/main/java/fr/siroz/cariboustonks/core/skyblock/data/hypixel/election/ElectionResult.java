package fr.siroz.cariboustonks.core.skyblock.data.hypixel.election;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public record ElectionResult(
		@NonNull Mayor mayor,
		@NonNull Mayor minister,
		@NonNull Set<Perk> mayorPerks,
		@NonNull Optional<Perk> ministerPerk,
		@NonNull Instant timestamp
) {

	/**
	 * Returns whether the given {@link Mayor} currently holds the mayor or minister role,
	 * and (optionally) whether the specified {@link Perk} is present for that role.
	 *
	 * @param mayor the {@link Mayor} to check
	 * @param perk  optional {@link Perk} to verify for the given role; if {@code null} only the role is checked
	 * @return {@code true} if the given {@code mayor} matches the current mayor or minister and,
	 * when {@code perk} is provided, the requested perk is present for that role
	 */
	public boolean hasMayorOrMinister(@NonNull Mayor mayor, @Nullable Perk perk) {
		if (perk == null) return mayor == this.mayor() || mayor == this.minister();

		if (mayor == this.mayor()) {
			final Set<Perk> mayorPerks = this.mayorPerks();
			return !mayorPerks.isEmpty() && mayorPerks.contains(perk);
		}

		if (mayor == this.minister()) {
			final Optional<Perk> opt = this.ministerPerk();
			return opt.isPresent() && opt.get() == perk;
		}

		return false;
	}
}
