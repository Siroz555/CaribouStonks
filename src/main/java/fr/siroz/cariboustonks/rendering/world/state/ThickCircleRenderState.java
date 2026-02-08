package fr.siroz.cariboustonks.rendering.world.state;

import fr.siroz.cariboustonks.core.module.color.Color;
import net.minecraft.world.phys.Vec3;

public record ThickCircleRenderState(
        Vec3 center,
        double radius,
        double thickness,
        int segments,
        Color color,
        boolean throughBlocks
) {
}
