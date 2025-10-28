package fr.siroz.cariboustonks.rendering.world.state;

import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;

/**
 * State of the camera.
 *
 * @param pos the position
 * @param rotation the rotation
 * @param pitch the pitch
 * @param yaw the yaw
 */
public record CameraRenderState(
		Vec3d pos,
		Quaternionf rotation,
		float pitch,
		float yaw
) {
}
