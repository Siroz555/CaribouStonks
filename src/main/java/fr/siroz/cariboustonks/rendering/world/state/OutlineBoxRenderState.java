package fr.siroz.cariboustonks.rendering.world.state;

import fr.siroz.cariboustonks.util.colors.Color;
import net.minecraft.util.math.Box;

public record OutlineBoxRenderState(
		Box box,
		Color color,
		float lineWidth,
		boolean throughBlocks
) {
}
