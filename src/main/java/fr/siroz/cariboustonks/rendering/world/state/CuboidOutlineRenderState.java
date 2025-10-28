package fr.siroz.cariboustonks.rendering.world.state;

import fr.siroz.cariboustonks.util.colors.Color;
import net.minecraft.util.math.Vec3d;

public record CuboidOutlineRenderState(
		Vec3d center,
		int depth,
		int size,
		int minY,
		int maxY,
		float lineWidth,
		Color mainColor,
		Color secondColor
) {
}
