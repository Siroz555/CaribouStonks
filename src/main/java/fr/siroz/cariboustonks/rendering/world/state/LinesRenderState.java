package fr.siroz.cariboustonks.rendering.world.state;

import fr.siroz.cariboustonks.util.colors.Color;
import net.minecraft.util.math.Vec3d;

public record LinesRenderState(
		Vec3d[] points,
		Color color,
		float lineWidth,
		boolean throughBlocks
) {
}
