package fr.siroz.cariboustonks.features.dungeon;

import fr.siroz.cariboustonks.core.module.color.Color;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;

enum WitherKingDragon {

	POWER("Red",
			3, 3,
			new BlockPos(28, 18, 59), // Text
			new BlockPos(32, 22, 59), // Statue
			new BlockPos(28, 17, 58), // LB
			AABB.encapsulatingFullBlocks(new BlockPos(13, 5, 45), new BlockPos(41, 34, 72)),
			Color.fromHexString("#FF5555"),
			24.0, 30.0, 56.0, 62.0),
	FLAME("Orange",
			1, 5,
			new BlockPos(84, 19, 56), // Text
			new BlockPos(80, 23, 56), // Statue
			new BlockPos(84, 19, 58), // LB
			AABB.encapsulatingFullBlocks(new BlockPos(71, 5, 45), new BlockPos(102, 34, 72)),
			Color.fromHexString("#FFAA00"),
			82.0, 88.0, 53.0, 59.0),
	ICE("Blue",
			4, 2,
			new BlockPos(83, 19, 94), // Text
			new BlockPos(79, 23, 94), // Statue
			new BlockPos(84, 17, 95), // LB
			AABB.encapsulatingFullBlocks(new BlockPos(71, 5, 80), new BlockPos(102, 34, 107)),
			Color.fromHexString("#55FFFF"),
			82.0, 88.0, 91.0, 97.0),
	SOUL("Purple",
			5, 1,
			new BlockPos(56, 18, 123), // Text
			new BlockPos(56, 22, 120), // Statue
			new BlockPos(56, 17, 123), // LB
			AABB.encapsulatingFullBlocks(new BlockPos(41, 5, 112), new BlockPos(71, 34, 145)),
			Color.fromHexString("#AA00AA"),
			53.0, 59.0, 122.0, 128.0),
	APEX("Green",
			2, 4,
			new BlockPos(28, 19, 94), // Text
			new BlockPos(32, 23, 94), // Statue
			new BlockPos(27, 16, 93), // LB
			AABB.encapsulatingFullBlocks(new BlockPos(13, 5, 80), new BlockPos(41, 34, 107)),
			Color.fromHexString("#55FF55"),
			23.0, 29.0, 91.0, 97.0),
	;

	public static final WitherKingDragon[] VALUES = WitherKingDragon.values();
	public static final int SPAWN_COOLDOWN_TICKS = 100; // 5s

	public enum State {
		SPAWNING,
		ALIVE,
		DEAD
	}

	private final String name;
	private final int archPriority;
	private final int bersPriority;
	private final BlockPos text;
	private final BlockPos statuePos;
	private final BlockPos lbPos;
	private final Color color;
	private final AABB box;
	private final double xMin;
	private final double xMax;
	private final double zMin;
	private final double zMax;

	private int timeToSpawn = SPAWN_COOLDOWN_TICKS;
	private int timesSpawned = 0;
	private State state = State.DEAD;

	WitherKingDragon(
			String name,
			int archPriority,
			int bersPriority,
			BlockPos text,
			BlockPos statuePos,
			BlockPos lbPos,
			AABB box,
			Color color,
			double xMin,
			double xMax,
			double zMin,
			double zMax
	) {
		this.name = name;
		this.archPriority = archPriority;
		this.bersPriority = bersPriority;
		this.text = text;
		this.statuePos = statuePos;
		this.lbPos = lbPos;
		this.box = box;
		this.color = color;
		this.xMin = xMin;
		this.xMax = xMax;
		this.zMin = zMin;
		this.zMax = zMax;
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

	public BlockPos getStatuePos() {
		return statuePos;
	}

	public BlockPos getLbPos() {
		return lbPos;
	}

	public AABB getBox() {
		return box;
	}

	public Color getColor() {
		return color;
	}

	public void tick() {
		timeToSpawn--;
	}

	public int getTimeToSpawn() {
		return timeToSpawn;
	}

	public void setTimeToSpawn(int timeToSpawn) {
		this.timeToSpawn = timeToSpawn;
	}

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}

	public int getTimesSpawned() {
		return timesSpawned;
	}

	public boolean isInXRange(double x) {
		return x >= xMin && x <= xMax;
	}

	public boolean isInZRange(double z) {
		return z >= zMin && z <= zMax;
	}

	public void setAlive() {
		if (state == State.ALIVE || state != State.SPAWNING) return;
		state = State.ALIVE;
		timesSpawned++;
	}

	public static void resetAll() {
		for (WitherKingDragon dragon : VALUES) {
			dragon.timeToSpawn = 0;
			dragon.timesSpawned = 0;
			dragon.state = State.DEAD;
		}
	}
}
