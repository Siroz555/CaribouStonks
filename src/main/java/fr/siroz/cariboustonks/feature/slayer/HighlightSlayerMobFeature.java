package fr.siroz.cariboustonks.feature.slayer;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.core.component.EntityGlowComponent;
import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.core.skyblock.slayer.SlayerManager;

public class HighlightSlayerMobFeature extends Feature {

	private final SlayerManager slayerManager;

	public HighlightSlayerMobFeature() {
		this.slayerManager = CaribouStonks.skyBlock().getSlayerManager();

		this.addComponent(EntityGlowComponent.class, EntityGlowComponent.builder()
				.when(entity -> this.config().slayer.highlightMiniboss
								&& slayerManager.getMinibosses().contains(entity),
						this.config().slayer.highlightMinibossColor.getRGB())

				.when(entity -> this.config().slayer.highlightBoss
						&& slayerManager.getBossEntity() != null
						&& slayerManager.getBossEntity() == entity,
						this.config().slayer.highlightBossColor.getRGB())
				.build());
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock()
				&& slayerManager.isInQuest()
				&& (this.config().slayer.highlightBoss || this.config().slayer.highlightMiniboss);
	}
}
