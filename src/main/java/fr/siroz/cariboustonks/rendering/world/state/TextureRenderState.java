package fr.siroz.cariboustonks.rendering.world.state;

import fr.siroz.cariboustonks.util.colors.Color;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

public record TextureRenderState(
		Vec3d pos,
		float width,
		float height,
		float textureWidth,
		float textureHeight,
		Vec3d renderOffset,
		Identifier texture,
		Color color,
		float alpha,
		boolean throughBlocks
) {
}
