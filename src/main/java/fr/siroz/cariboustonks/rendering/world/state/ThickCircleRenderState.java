package fr.siroz.cariboustonks.rendering.world.state;

import fr.siroz.cariboustonks.util.colors.Color;
import net.minecraft.util.math.Vec3d;

public record ThickCircleRenderState(
		Vec3d center,
		double radius,
		double thickness,
		int segments,
		Color color,
		boolean throughBlocks
) {
}
