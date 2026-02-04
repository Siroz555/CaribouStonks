package fr.siroz.cariboustonks.feature.slayer;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.core.component.EntityGlowComponent;
import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.core.skyblock.slayer.SlayerManager;

public class HighlightSlayerMobFeature extends Feature {

	private final SlayerManager slayerManager;

	public HighlightSlayerMobFeature() {
		this.slayerManager = CaribouStonks.skyBlock().getSlayerManager();

		this.addComponent(EntityGlowComponent.class, EntityGlowComponent.builder()
				.when(entity -> ConfigManager.getConfig().slayer.highlightMiniboss
								&& slayerManager.getMinibosses().contains(entity),
						ConfigManager.getConfig().slayer.highlightMinibossColor.getRGB())

				.when(entity -> ConfigManager.getConfig().slayer.highlightBoss
						&& slayerManager.getBossEntity() != null
						&& slayerManager.getBossEntity() == entity,
						ConfigManager.getConfig().slayer.highlightBossColor.getRGB())
				.build());
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock()
				&& slayerManager.isInQuest()
				&& (ConfigManager.getConfig().slayer.highlightBoss || ConfigManager.getConfig().slayer.highlightMiniboss);
	}
}
