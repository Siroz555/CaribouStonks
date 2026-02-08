package fr.siroz.cariboustonks.core.module.position;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

/**
 * Représente une Position immutable dans le monde.
 */
public interface Position extends Comparable<Position> {

    Position ORIGIN = new ImmutablePosition(0, 0, 0);

    static @NonNull Position of(int x, int y, int z) {
        return new ImmutablePosition(x, y, z);
    }

    static @NonNull Position of(@NonNull Position position) {
        return of(position.x(), position.y(), position.z());
    }

    static @NonNull Position of(@NonNull BlockPos pos) {
        return of(pos.getX(), pos.getY(), pos.getZ());
    }

    static @NonNull Position of(@NonNull Vec3 vec3d) {
        return of((int) vec3d.x(), (int) vec3d.y(), (int) vec3d.z());
    }

    int x();

    int y();

    int z();

    double distanceTo(@NonNull Position other);

    double squaredDistanceTo(@NonNull Position other);

    boolean isInRange(Position other, double radius);

    @NonNull Position offset(int dx, int dy, int dz);

    @NonNull BlockPos toBlockPos();

    @NonNull Vec3 toVec3d();

    @NonNull AABB toBox();

    default @NonNull String asChatCoordinates() {
        return "x: " + x() + ", y: " + y() + ", z: " + z();
    }

    boolean equals(Object o);

    int compareTo(@NonNull Position o);

    default String asString() {
        return "x:" + x() + "," + "y:" + y() + "," + "z:" + z();
    }

    /**
     * Parses a {@code String} from the format {@code "x:<value>,y:<value>,z:<value>"} with a Position object.
     *
     * @param positionString the String to parse
     * @return the Position object
     * @throws IllegalArgumentException if the String is not in the expected format
     */
    static @NonNull Position fromString(@NonNull String positionString) throws IllegalArgumentException {
        String[] parts = positionString.split(",");
        if (parts.length != 3) {
			throw new IllegalArgumentException(
					"Invalid format: expected 'x:<value>,y:<value>,z:<value>' but got '" + positionString + "' instead");
		}

        Map<String, Integer> values = Stream.of(parts)
                .map(String::trim)
                .map(part -> part.split(":", 2))
                .peek(kv -> {
                    if (kv.length != 2) {
                        throw new IllegalArgumentException(
								"Invalid segment: '" + String.join(":", kv) + "', expected format 'key:value'");
                    }
                })
                .collect(Collectors.toMap(
                        kv -> kv[0].trim(),
                        kv -> {
                            try {
                                return Integer.parseInt(kv[1].trim());
                            } catch (NumberFormatException ex) {
                                throw new IllegalArgumentException(
										"Invalid value for key '" + kv[0] + "' : '" + kv[1] + "'", ex);
                            }
                        }
                ));

        if (!values.containsKey("x") || !values.containsKey("y") || !values.containsKey("z")) {
			throw new IllegalArgumentException(
					"Missing keys: expected 'x', 'y' and 'z', found " + values.keySet());
		}

        return Position.of(values.get("x"), values.get("y"), values.get("z"));
    }
}
