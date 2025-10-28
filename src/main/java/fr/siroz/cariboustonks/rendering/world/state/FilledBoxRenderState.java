package fr.siroz.cariboustonks.rendering.world.state;

import fr.siroz.cariboustonks.util.colors.Color;

public record FilledBoxRenderState(
		double minX,
		double minY,
		double minZ,
		double maxX,
		double maxY,
		double maxZ,
		Color color,
		boolean throughBlocks
) {
}
