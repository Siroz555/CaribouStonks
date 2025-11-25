package fr.siroz.cariboustonks.rendering.world.state;

import net.minecraft.client.gui.Font;
import net.minecraft.world.phys.Vec3;

public record TextRenderState(
        Font.PreparedText preparedText,
        Vec3 pos,
        float scale,
        float offsetY,
        boolean throughBlocks
) {
}
