package fr.siroz.cariboustonks.rendering.world.state;

import fr.siroz.cariboustonks.util.colors.Color;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public record CircleRenderState(
		Vec3d center,
		double radius,
		int segments,
		float thicknessPercent,
		Color color,
		Direction.Axis axis,
		boolean throughBlocks
) {
}
