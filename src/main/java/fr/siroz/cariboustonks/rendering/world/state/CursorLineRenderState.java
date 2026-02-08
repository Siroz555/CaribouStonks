package fr.siroz.cariboustonks.rendering.world.state;

import fr.siroz.cariboustonks.core.module.color.Color;
import net.minecraft.world.phys.Vec3;

public record CursorLineRenderState(
        Vec3 point,
        Color color,
        float lineWidth
) {
}
