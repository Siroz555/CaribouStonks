package fr.siroz.cariboustonks.rendering.world.state;

import fr.siroz.cariboustonks.util.colors.Color;
import net.minecraft.util.math.Vec3d;

public record QuadRenderState(
		Vec3d[] points,
		Color color,
		boolean throughBlocks
) {
}
