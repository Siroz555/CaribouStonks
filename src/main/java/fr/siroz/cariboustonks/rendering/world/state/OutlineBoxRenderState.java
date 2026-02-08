package fr.siroz.cariboustonks.rendering.world.state;

import fr.siroz.cariboustonks.core.module.color.Color;
import net.minecraft.world.phys.AABB;

public record OutlineBoxRenderState(
        AABB box,
        Color color,
        float lineWidth,
        boolean throughBlocks
) {
}
