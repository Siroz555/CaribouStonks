package fr.siroz.cariboustonks.platform.rendering.world.state;

import fr.siroz.cariboustonks.core.module.color.Color;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

public record BeamRenderState(
		@NonNull Vec3 pos,
		@NonNull Color color,
		float height,
		float widthScale,
		boolean throughBlocks
) {
}
