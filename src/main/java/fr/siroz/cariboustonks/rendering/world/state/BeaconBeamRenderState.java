package fr.siroz.cariboustonks.rendering.world.state;

import net.minecraft.util.math.BlockPos;

public record BeaconBeamRenderState(
		BlockPos pos,
		int colorInt,
		float scale,
		float beamRotationDegrees
) {
}
