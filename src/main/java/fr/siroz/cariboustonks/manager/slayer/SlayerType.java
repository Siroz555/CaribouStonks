package fr.siroz.cariboustonks.manager.slayer;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import org.jetbrains.annotations.NotNull;

public enum SlayerType {

	ZOMBIE(
			EntityType.ZOMBIE,
			"Revenant Horror",
			List.of("Revenant Sycophant", "Revenant Champion", "Deformed Revenant", "Atoned Champion", "Atoned Revenant"),
			List.of(), // Tous des ZOMBIE en T5
			new int[]{5, 25, 100, 500, 1500}
	),
	SPIDER(
			EntityType.SPIDER,
			"Tarantula Broodfather",
			List.of("Tarantula Vermin", "Tarantula Beast", "Mutant Tarantula", "Primordial Jockey", "Primordial Viscount"),
			List.of(EntityType.SKELETON, EntityType.CAVE_SPIDER), // Le T5 pour respectivement ses miniboss
			new int[]{5, 25, 100, 500, 1500}
	),
	WOLF(
			EntityType.WOLF,
			"Sven Packmaster",
			List.of("Pack Enforcer", "Sven Follower", "Sven Alpha"),
			List.of(), // Pas de T5
			new int[]{5, 25, 100, 500, 1500}
	),
	ENDERMAN(
			EntityType.ENDERMAN,
			"Voidgloom Seraph",
			List.of("Voidling Devotee", "Voidling Radical", "Voidcrazed Maniac"),
			List.of(), // Pas de T5
			new int[]{5, 25, 100, 500, 1500}
	),
	BLAZE(
			EntityType.BLAZE,
			"Inferno Demonlord",
			List.of("Flare Demon", "Kindleheart Demon", "Burningsoul Demon"),
			List.of(), // Pas de T5
			new int[]{5, 25, 100, 500, 1500}
	),
	VAMPIRE(
			EntityType.PLAYER,
			"Riftstalker Bloodfiend",
			List.of(),
			List.of(), // Aucun Miniboss
			new int[]{5, 25, 100, 500, 1500}
	),
	UNKNOWN(null, "Unknown", List.of(), List.of(), new int[]{});

	private final EntityType<? extends Entity> entityType;
	private final String bossName;
	private final List<String> minibossNames;
	private final List<EntityType<? extends Entity>> minibossEntityTypes;
	private final int[] expPerTier;

	private static final Map<String, SlayerType> BOSS_NAME_TO_TYPE = new HashMap<>();

	SlayerType(
			EntityType<? extends Entity> entityType,
			String bossName,
			List<String> minibossNames,
			List<EntityType<? extends Entity>> minibossEntityTypes,
			int[] expPerTier
	) {
		this.entityType = entityType;
		this.bossName = bossName;
		this.minibossNames = minibossNames;
		this.minibossEntityTypes = minibossEntityTypes;
		this.expPerTier = expPerTier;
	}

	public static SlayerType fromBossName(@NotNull String bossName) {
		return BOSS_NAME_TO_TYPE.getOrDefault(bossName.toLowerCase(), UNKNOWN);
	}

	public EntityType<? extends Entity> getEntityType() {
		return entityType;
	}

	public String getBossName() {
		return bossName;
	}

	public List<String> getMinibossNames() {
		return minibossNames;
	}

	public List<EntityType<? extends Entity>> getMinibossEntityTypes() {
		return minibossEntityTypes;
	}

	public int[] getExpPerTier() {
		return expPerTier;
	}

	static {
		for (SlayerType type : values()) {
			BOSS_NAME_TO_TYPE.put(type.getBossName().toLowerCase(Locale.ENGLISH), type);
		}
	}
}
