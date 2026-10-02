package fr.siroz.cariboustonks.features.dungeon;

import fr.siroz.cariboustonks.core.module.color.Color;
import fr.siroz.cariboustonks.core.module.color.Colors;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

enum WitherKingDragon {
	POWER("Red",
			new BlockPos(27, 14, 59), // Spawn
			new BlockPos(32, 22, 59), // Statue
			new BlockPos(28, 17, 58), // LB
			new AABB(14.5, 13.0, 45.5, 39.5, 28.0, 70.5),
			Colors.RED,
			24.0, 30.0, 56.0, 62.0),
	FLAME("Orange",
			new BlockPos(85, 14, 56), // Spawn
			new BlockPos(80, 23, 56), // Statue
			new BlockPos(84, 19, 58), // LB
			new AABB(72.0, 8.0, 47.0, 102.0, 28.0, 77.0),
			Colors.GOLD,
			82.0, 88.0, 53.0, 59.0),
	ICE("Blue",
			new BlockPos(84, 14, 94), // Spawn
			new BlockPos(79, 23, 94), // Statue
			new BlockPos(84, 17, 95), // LB
			new AABB(71.5, 13.0, 82.5, 96.5, 26.0, 107.5),
			Colors.BLUE,
			82.0, 88.0, 91.0, 97.0),
	SOUL("Purple",
			new BlockPos(56, 14, 125), // Spawn
			new BlockPos(56, 22, 120), // Statue
			new BlockPos(56, 17, 123), // LB
			new AABB(45.5, 13.0, 113.5, 68.5, 23.0, 136.5),
			Colors.DARK_PURPLE,
			53.0, 59.0, 122.0, 128.0),
	APEX("Green",
			new BlockPos(27, 14, 94), // Spawn
			new BlockPos(32, 23, 94), // Statue
			new BlockPos(27, 16, 93), // LB
			new AABB(7.0, 8.0, 80.0, 37.0, 28.0, 110.0),
			Colors.GREEN,
			23.0, 29.0, 91.0, 97.0),
	;

	public static final WitherKingDragon[] VALUES = WitherKingDragon.values();

	private final String name;
	private final BlockPos spawnPos;
	private final BlockPos statuePos;
	private final BlockPos lbPos;
	private final Color color;
	private final AABB box;
	private final double xMin;
	private final double xMax;
	private final double zMin;
	private final double zMax;

	WitherKingDragon(
			String name,
			BlockPos spawnPos, BlockPos statuePos, BlockPos lbPos,
			AABB box,
			Color color,
			double xMin, double xMax,
			double zMin, double zMax
	) {
		this.name = name;
		this.spawnPos = spawnPos;
		this.statuePos = statuePos;
		this.lbPos = lbPos;
		this.box = box;
		this.color = color;
		this.xMin = xMin;
		this.xMax = xMax;
		this.zMin = zMin;
		this.zMax = zMax;
	}

	static @Nullable WitherKingDragon bySpawnArea(double x, double z) {
		for (WitherKingDragon dragon : VALUES) if (dragon.isInSpawnArea(x, z)) return dragon;
		return null;
	}

	public String getName() {
		return name;
	}

	public BlockPos getSpawnPos() {
		return spawnPos;
	}

	public BlockPos getStatuePos() {
		return statuePos;
	}

	public BlockPos getLbPos() {
		return lbPos;
	}

	public Color getColor() {
		return color;
	}

	public AABB getBox() {
		return box;
	}

	private boolean isInSpawnArea(double x, double z) {
		return x >= xMin && x <= xMax && z >= zMin && z <= zMax;
	}
}
