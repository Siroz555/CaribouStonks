package fr.siroz.cariboustonks.util.shape;

import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class Cuboid {

    private final int xMin;
    private final int xMax;
    private final int yMin;
    private final int yMax;
    private final int zMin;
    private final int zMax;
    private final double xMinCentered;
    private final double xMaxCentered;
    private final double yMinCentered;
    private final double yMaxCentered;
    private final double zMinCentered;
    private final double zMaxCentered;

    public Cuboid(@NotNull BlockPos a, @NotNull BlockPos b) {
        this.xMin = Math.min(a.getX(), b.getX());
        this.xMax = Math.max(a.getX(), b.getX());
        this.yMin = Math.min(a.getY(), b.getY());
        this.yMax = Math.max(a.getY(), b.getY());
        this.zMin = Math.min(a.getZ(), b.getZ());
        this.zMax = Math.max(a.getZ(), b.getZ());
        this.xMinCentered = this.xMin + 0.5;
        this.xMaxCentered = this.xMax + 0.5;
        this.yMinCentered = this.yMin + 0.5;
        this.yMaxCentered = this.yMax + 0.5;
        this.zMinCentered = this.zMin + 0.5;
        this.zMaxCentered = this.zMax + 0.5;
    }

    @Contract(value = " -> new", pure = true)
    public @NotNull BlockPos getCenter() {
        return new BlockPos(
                (xMax - xMin) / 2 + xMin,
                (yMax - yMin) / 2 + yMin,
                (zMax - zMin) / 2 + zMin
        );
    }

    @Contract(value = " -> new", pure = true)
    public @NotNull BlockPos getMin() {
        return new BlockPos(xMin, yMin, zMin);
    }

    @Contract(value = " -> new", pure = true)
    public @NotNull BlockPos getMax() {
        return new BlockPos(xMax, yMax, zMax);
    }

    public int getHeight() {
        return yMax - yMin + 1;
    }

    public int getXWidth() {
        return xMax - xMin + 1;
    }

    public int getZWidth() {
        return zMax - zMin + 1;
    }

    public double getDistanceSquared() {
        return getMin().getSquaredDistance(getMax());
    }

    public int getTotalBlockSize() {
        return getHeight() * getXWidth() * getZWidth();
    }

    public boolean isInRegion(@NotNull BlockPos pos) {
        return pos.getX() >= xMin && pos.getX() <= xMax
                && pos.getY() >= yMin && pos.getY() <= yMax
                && pos.getZ() >= zMin && pos.getZ() <= zMax;
    }

    public boolean isInRegionWithMarge(@NotNull BlockPos pos, double marge) {
        return pos.getX() >= xMinCentered - marge && pos.getX() <= xMaxCentered + marge
                && pos.getY() >= yMinCentered - marge && pos.getY() <= yMaxCentered + marge
                && pos.getZ() >= zMinCentered - marge && pos.getZ() <= zMaxCentered + marge;
    }
}
