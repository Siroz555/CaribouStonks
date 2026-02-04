package fr.siroz.cariboustonks.util.position;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Représente une Position immutable dans le monde.
 */
public interface Position extends Comparable<Position> {

    Position ORIGIN = new ImmutablePosition(0, 0, 0);

    @Contract(value = "_, _, _ -> new", pure = true)
    static @NotNull Position of(int x, int y, int z) {
        return new ImmutablePosition(x, y, z);
    }

    @Contract("_ -> new")
    static @NotNull Position of(@NotNull Position position) {
        return of(position.x(), position.y(), position.z());
    }

    @Contract("_ -> new")
    static @NotNull Position of(@NotNull BlockPos pos) {
        return of(pos.getX(), pos.getY(), pos.getZ());
    }

    @Contract("_ -> new")
    static @NotNull Position of(@NotNull Vec3 vec3d) {
        return of((int) vec3d.x(), (int) vec3d.y(), (int) vec3d.z());
    }

    int x();

    int y();

    int z();

    double distanceTo(@NotNull Position other);

    double squaredDistanceTo(@NotNull Position other);

    boolean isInRange(Position other, double radius);

    @NotNull Position offset(int dx, int dy, int dz);

    @NotNull BlockPos toBlockPos();

    @NotNull Vec3 toVec3d();

    @NotNull AABB toBox();

    default @NotNull String asChatCoordinates() {
        return "x: " + x() + ", y: " + y() + ", z: " + z();
    }

    boolean equals(Object o);

    int compareTo(@NotNull Position o);

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
    static @NotNull Position fromString(@NotNull String positionString) {
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
