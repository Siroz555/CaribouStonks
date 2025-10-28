package fr.siroz.cariboustonks.rendering.world.state;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.util.math.Vec3d;

public record TextRenderState(
		TextRenderer.GlyphDrawable glyphs,
		Vec3d pos,
		float scale,
		float offsetY,
		boolean throughBlocks
) {
}
