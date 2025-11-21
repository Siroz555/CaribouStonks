package fr.siroz.cariboustonks.rendering.world.state;

import fr.siroz.cariboustonks.util.colors.Color;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

public record CircleRenderState(
        Vec3 center,
        double radius,
        int segments,
        float thicknessPercent,
        Color color,
        Direction.Axis axis,
        boolean throughBlocks
) {
}
