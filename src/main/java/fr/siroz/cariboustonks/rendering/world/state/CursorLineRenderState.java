package fr.siroz.cariboustonks.rendering.world.state;

import fr.siroz.cariboustonks.util.colors.Color;
import net.minecraft.util.math.Vec3d;

public record CursorLineRenderState(
		Vec3d point,
		Color color,
		float lineWidth
) {
}
