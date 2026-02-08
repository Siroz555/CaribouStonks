package fr.siroz.cariboustonks.rendering.world.state;

import fr.siroz.cariboustonks.core.module.color.Color;
import net.minecraft.world.phys.Vec3;

public record CuboidOutlineRenderState(
        Vec3 center,
        int depth,
        int size,
        int minY,
        int maxY,
        float lineWidth,
        Color mainColor,
        Color secondColor
) {
}
