package fr.siroz.cariboustonks.feature.slayer;

import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.config.configs.SlayerConfig;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.feature.Feature;
import fr.siroz.cariboustonks.manager.glowing.EntityGlowProvider;
import fr.siroz.cariboustonks.manager.slayer.SlayerManager;
import net.minecraft.entity.Entity;
import org.jetbrains.annotations.NotNull;

public class HighlightSlayerMobFeature extends Feature implements EntityGlowProvider {

	private final SlayerManager slayerManager;

	public HighlightSlayerMobFeature(SlayerManager slayerManager) {
		this.slayerManager = slayerManager;
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock()
				&& slayerManager.isInQuest()
				&& (ConfigManager.getConfig().slayer.highlightBoss || ConfigManager.getConfig().slayer.highlightMiniboss);
	}

	@Override
	public int getEntityGlowColor(@NotNull Entity entity) {
		SlayerConfig slayerConfig = ConfigManager.getConfig().slayer;
		// Minibosses
		if (slayerManager.getMinibosses().contains(entity) && slayerConfig.highlightMiniboss) {
			return slayerConfig.highlightMinibossColor.getRGB();
		}

		// Boss
		if (slayerManager.getBossEntity() != null && slayerManager.getBossEntity() == entity && slayerConfig.highlightBoss) {
			return slayerConfig.highlightBossColor.getRGB();
		}

		return DEFAULT;
	}
}
