package fr.siroz.cariboustonks.util.render;

import fr.siroz.cariboustonks.mixin.accessors.FrustumInvoker;
import fr.siroz.cariboustonks.mixin.accessors.WorldRendererAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Frustum;
import net.minecraft.util.math.Box;
import org.joml.FrustumIntersection;

public final class FrustumUtils {

	private FrustumUtils() {
	}

	public static Frustum getFrustum() {
        return ((WorldRendererAccessor) MinecraftClient.getInstance().worldRenderer).getFrustum();
    }

    public static boolean isVisible(Box box) {
        return getFrustum().isVisible(box);
    }

    public static boolean isVisible(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        int visible = ((FrustumInvoker) getFrustum()).invokeIntersectAab(minX, minY, minZ, maxX, maxY, maxZ);

		return visible == FrustumIntersection.INSIDE || visible == FrustumIntersection.INTERSECT;
    }
}
