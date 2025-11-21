package fr.siroz.cariboustonks.rendering.world.state;

import fr.siroz.cariboustonks.util.colors.Color;
import net.minecraft.world.phys.Vec3;

public record QuadRenderState(
        Vec3[] points,
        Color color,
        boolean throughBlocks
) {
}
