package fr.siroz.cariboustonks.core.skyblock.slayer;

import fr.siroz.cariboustonks.event.SkyBlockEvents;
import java.time.Instant;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.jspecify.annotations.Nullable;

class SlayerBossFight {

	private final SlayerManager slayerManager;
	private final Instant bossSpawnTime;
	private boolean slain = false;
	@Nullable
	private Entity boss = null;
	@Nullable
	private ArmorStand bossArmorStand = null;

	SlayerBossFight(SlayerManager slayerManager, @Nullable ArmorStand bossArmorStand) {
		this.slayerManager = slayerManager;
		this.bossSpawnTime = Instant.now();
		tryToFindBoss(bossArmorStand);
	}

	public void tryToFindBoss(@Nullable ArmorStand armorStand) {
		this.bossArmorStand = armorStand;

		SlayerQuest slayerQuest = slayerManager.getQuest();
		if (armorStand != null && slayerQuest != null) {
			EntityType<? extends Entity> entityType = slayerQuest.getSlayerType().getEntityType();
			this.boss = slayerManager.findClosestEntity(entityType, armorStand);
		}

		if (slayerQuest != null) {
			SkyBlockEvents.SLAYER_BOSS_SPAWN.invoker().onSpawn(slayerQuest.getSlayerType(), slayerQuest.getSlayerTier());
		}
	}

	public Instant getBossSpawnTime() {
		return bossSpawnTime;
	}

	public boolean isSlain() {
		return slain;
	}

	public void setSlain(boolean slain) {
		this.slain = slain;
	}

	@Nullable
	public Entity getBossEntity() {
		return boss;
	}

	@Nullable
	public ArmorStand getBossArmorStand() {
		return bossArmorStand;
	}
}
