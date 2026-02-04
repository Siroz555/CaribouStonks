package fr.siroz.cariboustonks.util.position;

import java.util.Comparator;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public record ImmutablePosition(int x, int y, int z) implements Position {

	// le Y est comparé en dernier
	private static final Comparator<Position> POSITION_COMPARATOR = Comparator.comparing(
			Position::x, Integer::compareTo)
			.thenComparing(Position::z, Integer::compareTo)
			.thenComparing(Position::y, Integer::compareTo);

	@Override
	public double distanceTo(@NotNull Position other) {
		double dx = other.x() - x;
		double dy = other.y() - y;
		double dz = other.z() - z;
		return Math.sqrt(dx * dx + dy * dy + dz * dz);
	}

	@Override
	public double squaredDistanceTo(@NotNull Position other) {
		double dx = other.x() - x;
		double dy = other.y() - y;
		double dz = other.z() - z;
		return dx * dx + dy * dy + dz * dz;
	}

	@Override
	public boolean isInRange(@NotNull Position other, double radius) {
		return squaredDistanceTo(other) < radius * radius;
	}

	@Override
	public @NotNull Position offset(int dx, int dy, int dz) {
		return new ImmutablePosition(x + dx, y + dy, z + dz);
	}

	@Override
	public @NotNull BlockPos toBlockPos() {
		return new BlockPos(x, y, z);
	}

	@Override
	public @NotNull Vec3 toVec3d() {
		return new Vec3(x, y, z);
	}

	@Override
	public @NotNull AABB toBox() {
		return new AABB(new BlockPos(x, y, z));
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		ImmutablePosition position = (ImmutablePosition) o;
		return x == position.x() && y == position.y() && z == position.z();
	}

	@Override
	public int hashCode() {
		return Objects.hash(x, y, z);
	}

	@Override
	public int compareTo(@NotNull Position that) {
		return POSITION_COMPARATOR.compare(this, that);
	}
}
