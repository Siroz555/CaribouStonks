package fr.siroz.cariboustonks.features.ui.tracking;

import net.minecraft.world.entity.decoration.ArmorStand;
import org.jspecify.annotations.NonNull;

/**
 * Wrapper immutable pour une entity trackée avec sa priorité
 */
public record TrackedEntity(
		@NonNull ArmorStand armorStand,
		int priority,
		long addedTime,
		boolean isSlayerBoss
) implements Comparable<TrackedEntity> {

	public TrackedEntity(ArmorStand armorStand, int priority) {
		this(armorStand, priority, false);
	}

	public TrackedEntity(ArmorStand armorStand, int priority, boolean isSlayerBoss) {
		this(armorStand, isSlayerBoss ? Integer.MAX_VALUE : priority, System.currentTimeMillis(), isSlayerBoss);
	}

	@Override
	public int compareTo(@NonNull TrackedEntity other) {
		// Tri par priorité décroissante (plus haute priorité en premier).
		int priorityCompare = Integer.compare(other.priority(), this.priority);
		if (priorityCompare != 0) {
			return priorityCompare;
		}

		// En cas d'égalité, le plus récent apparaît en premier.
		// Normalement, c'est un FIFO, à voir si j'inverse.
		return Long.compare(other.addedTime(), this.addedTime);
	}

	public boolean isValid() {
		return !armorStand.isRemoved()
				&& !armorStand.isDeadOrDying()
				&& armorStand.isAlive();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		TrackedEntity that = (TrackedEntity) o;
		return armorStand.getId() == that.armorStand.getId();
	}

	@Override
	public int hashCode() {
		return armorStand.getId();
	}
}
