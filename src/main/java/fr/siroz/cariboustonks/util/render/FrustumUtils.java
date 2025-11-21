package fr.siroz.cariboustonks.util.render;

import fr.siroz.cariboustonks.mixin.accessors.FrustumAccessor;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;
import org.joml.FrustumIntersection;

public final class FrustumUtils {

	private FrustumUtils() {
	}

	public static boolean isVisible(@Nullable Frustum frustum, AABB box) {
		if (frustum == null) return false;
		return frustum.isVisible(box);
	}

	public static boolean isVisible(@Nullable Frustum frustum, double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
		if (frustum == null) return false;
		int visible = ((FrustumAccessor) frustum).invokeIntersectAab(minX, minY, minZ, maxX, maxY, maxZ);
		return visible == FrustumIntersection.INSIDE || visible == FrustumIntersection.INTERSECT;
	}
}
