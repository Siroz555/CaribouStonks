package fr.siroz.cariboustonks.manager.dungeon;

/**
 * Enum representing the different Dungeon classes. <a href="https://wiki.hypixel.net/Classes">Hypixel Wiki Classes</a>
 */
public enum DungeonClass {

	HEALER("Healer"),
	MAGE("Mage"),
	BERSERK("Berserk"),
	ARCHER("Archer"),
	TANK("Tank"),
	UNKNOWN("Unknown");

	private final String name;

	DungeonClass(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
