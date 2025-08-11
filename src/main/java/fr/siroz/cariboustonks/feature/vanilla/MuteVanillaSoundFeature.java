package fr.siroz.cariboustonks.feature.vanilla;

import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.event.WorldEvents;
import fr.siroz.cariboustonks.feature.Feature;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import org.jetbrains.annotations.NotNull;

public class MuteVanillaSoundFeature extends Feature {

	public MuteVanillaSoundFeature() {
		WorldEvents.ALLOW_SOUND.register(this::allowSound);
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock();
	}

	@EventHandler(event = "WorldEvents.ALLOW_SOUND_CANCELLABLE")
	private boolean allowSound(@NotNull SoundEvent soundEvent) {
		if (!isEnabled()) return true;

		if ((soundEvent.id().equals(SoundEvents.ENTITY_ENDERMAN_SCREAM.id())
				|| soundEvent.id().equals(SoundEvents.ENTITY_ENDERMAN_STARE.id()))
				&& ConfigManager.getConfig().vanilla.sound.muteEnderman) {
			return false;
		}

		if (soundEvent.id().getPath().startsWith("entity.phantom")
				&& ConfigManager.getConfig().vanilla.sound.mutePhantom) {
			return false;
		}

		return true;
	}
}
