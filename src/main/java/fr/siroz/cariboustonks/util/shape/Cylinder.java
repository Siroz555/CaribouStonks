package fr.siroz.cariboustonks.util.shape;

import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

@Deprecated
public record Cylinder(@NotNull BlockPos center, double height, double radius) {

	public boolean isInRegion(@NotNull BlockPos pos) {
		return center.isWithinDistance(pos, radius);
	}

	public boolean isInRegionWithMarge(@NotNull BlockPos pos, double marge) {
		return center.isWithinDistance(pos, radius + marge);
	}
}
