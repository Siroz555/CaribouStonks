package fr.siroz.cariboustonks.feature.misc;

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
		WorldEvents.SOUND_CANCELLABLE.register(this::onSound);
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock();
	}

	@EventHandler(event = "WorldEvents.SOUND_CANCELLABLE")
	private boolean onSound(@NotNull SoundEvent soundEvent) {
		if (!isEnabled()) return false;

		if ((soundEvent.id().equals(SoundEvents.ENTITY_ENDERMAN_SCREAM.id())
				|| soundEvent.id().equals(SoundEvents.ENTITY_ENDERMAN_STARE.id()))
				&& ConfigManager.getConfig().vanilla.sound.muteEnderman) {
			return true;
		}

		if (soundEvent.id().getPath().startsWith("entity.phantom")
				&& ConfigManager.getConfig().vanilla.sound.mutePhantom) {
			return true;
		}

		return false;
	}
}
