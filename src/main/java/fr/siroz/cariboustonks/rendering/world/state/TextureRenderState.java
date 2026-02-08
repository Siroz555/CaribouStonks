package fr.siroz.cariboustonks.rendering.world.state;

import fr.siroz.cariboustonks.core.module.color.Color;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

public record TextureRenderState(
        Vec3 pos,
        float width,
        float height,
        float textureWidth,
        float textureHeight,
        Vec3 renderOffset,
        Identifier texture,
        Color color,
        float alpha,
        boolean throughBlocks
) {
}
