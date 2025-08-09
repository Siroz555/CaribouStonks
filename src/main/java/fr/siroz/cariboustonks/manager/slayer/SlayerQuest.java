package fr.siroz.cariboustonks.manager.slayer;

import fr.siroz.cariboustonks.event.SkyBlockEvents;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.ArmorStandEntity;
import org.jetbrains.annotations.NotNull;

class SlayerQuest {

	private final SlayerManager slayerManager;
	private SlayerType slayerType;
	private SlayerTier slayerTier;

	private final List<ArmorStandEntity> minibossesArmorStand = new ArrayList<>();
	private final List<Entity> minibosses = new ArrayList<>();

	SlayerQuest(SlayerManager slayerManager) {
		this.slayerManager = slayerManager;
		this.slayerType = SlayerType.UNKNOWN;
		this.slayerTier = SlayerTier.UNKNOWN;
	}

	public void onMinibossSpawn(@NotNull ArmorStandEntity armorStand, @NotNull SlayerType type) {
		if (!minibossesArmorStand.contains(armorStand)) {
			minibossesArmorStand.add(armorStand);
			// Cas particulier :
			// Depuis l'ajout du Spider T5, les Miniboss ne sont plus la même EntityType que le boss lui-même
			if (slayerTier == SlayerTier.V && !type.getMinibossEntityTypes().isEmpty()) {
				for (EntityType<? extends Entity> minibossEntityType : type.getMinibossEntityTypes()) {
					Entity closestEntity = slayerManager.findClosestEntity(minibossEntityType, armorStand);
					// true sera toujours retourné avec le add même si la closestEntity est null
					if (closestEntity != null) {
						minibosses.add(closestEntity);
						break;
					}
				}
			} else {
				minibosses.add(slayerManager.findClosestEntity(type.getEntityType(), armorStand));
			}

			SkyBlockEvents.SLAYER_MINIBOSS_SPAWN.invoker().onSpawn(type, slayerTier);
		}
	}

	public SlayerType getSlayerType() {
		return slayerType;
	}

	public void setSlayerType(SlayerType slayerType) {
		this.slayerType = slayerType;
	}

	public SlayerTier getSlayerTier() {
		return slayerTier;
	}

	public void setSlayerTier(SlayerTier slayerTier) {
		this.slayerTier = slayerTier;
	}

	public List<Entity> getMinibosses() {
		return minibosses;
	}
}
