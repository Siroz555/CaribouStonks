package fr.siroz.cariboustonks.util.render;

import fr.siroz.cariboustonks.mixin.accessors.FrustumInvoker;
import net.minecraft.client.render.Frustum;
import net.minecraft.util.math.Box;
import org.jetbrains.annotations.Nullable;
import org.joml.FrustumIntersection;

public final class FrustumUtils {

	private FrustumUtils() {
	}

	public static boolean isVisible(@Nullable Frustum frustum, Box box) {
		if (frustum == null) return false;
		return frustum.isVisible(box);
	}

	public static boolean isVisible(@Nullable Frustum frustum, double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
		if (frustum == null) return false;
		int visible = ((FrustumInvoker) frustum).invokeIntersectAab(minX, minY, minZ, maxX, maxY, maxZ);
		return visible == FrustumIntersection.INSIDE || visible == FrustumIntersection.INTERSECT;
	}
}
