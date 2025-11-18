package fr.siroz.cariboustonks.feature.dungeon;

import fr.siroz.cariboustonks.core.scheduler.TickScheduler;
import fr.siroz.cariboustonks.util.colors.Color;
import java.util.concurrent.TimeUnit;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

enum WitherKingDragon {

	POWER("Red",
			3, 3,
			new BlockPos(28, 18, 59), // Text
			new BlockPos(28, 17, 58), // LB
			new BlockPos(13, 5, 45), new BlockPos(41, 34, 72), // Bounding Box
			Color.fromHexString("#FF5555")),
	FLAME("Orange",
			1, 5,
			new BlockPos(84, 19, 56), // Text
			new BlockPos(84, 19, 58), // LB
			new BlockPos(71, 5, 45), new BlockPos(102, 34, 72), // Bounding Box
			Color.fromHexString("#FFAA00")),
	ICE("Blue",
			4, 2,
			new BlockPos(83, 19, 94), // Text
			new BlockPos(84, 17, 95), // LB
			new BlockPos(71, 5, 80), new BlockPos(102, 34, 107), // Bounding Box
			Color.fromHexString("#55FFFF")),
	SOUL("Purple",
			5, 1,
			new BlockPos(56, 18, 123), // Text
			new BlockPos(56, 17, 123), // LB
			new BlockPos(41, 5, 112), new BlockPos(71, 34, 145), // Bounding Box
			Color.fromHexString("#AA00AA")),
	APEX("Green",
			2, 4,
			new BlockPos(28, 19, 94), // Text
			new BlockPos(27, 16, 93), // LB
			new BlockPos(13, 5, 80), new BlockPos(41, 34, 107), // Bounding Box
			Color.fromHexString("#55FF55")),
	;

	private static final int SPAWN_COOLDOWN_TICKS = 100; // 5s

	private final String name;
	private final int archPriority;
	private final int bersPriority;
	private final BlockPos text;
	private final BlockPos lbPos;
	private final BlockPos pos1;
	private final BlockPos pos2;
	private final Color color;
	private final Box box;

	private int spawnTime = 0;
	private boolean spawned = false;

	public static final WitherKingDragon[] VALUES = WitherKingDragon.values();

	WitherKingDragon(String name, int archPriority, int bersPriority, BlockPos text, BlockPos lbPos, BlockPos pos1, BlockPos pos2, Color color) {
		this.name = name;
		this.archPriority = archPriority;
		this.bersPriority = bersPriority;
		this.text = text;
		this.lbPos = lbPos;
		this.pos1 = pos1;
		this.pos2 = pos2;
		this.box = Box.enclosing(pos1, pos2);
		this.color = color;
	}

	public String getName() {
		return name;
	}

	public int getArchPriority() {
		return archPriority;
	}

	public int getBersPriority() {
		return bersPriority;
	}

	public BlockPos getText() {
		return text;
	}

	public BlockPos getLbPos() {
		return lbPos;
	}

	public BlockPos getPos1() {
		return pos1;
	}

	public BlockPos getPos2() {
		return pos2;
	}

	public Box getBox() {
		return box;
	}

	public Color getColor() {
		return color;
	}

	public int getSpawnTime() {
		return spawnTime;
	}

	public void spawn() {
		spawned = true;
		spawnTime = SPAWN_COOLDOWN_TICKS;
		TickScheduler.getInstance().runLater(() -> spawned = false, 4900, TimeUnit.MILLISECONDS);
	}

	public void tick() {
		spawnTime--;
	}

	public boolean isSpawned() {
		return spawned;
	}

	public static void resetAll() {
		for (WitherKingDragon dragon : VALUES) {
			dragon.spawned = false;
			dragon.spawnTime = 0;
		}
	}
}
