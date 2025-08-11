package fr.siroz.cariboustonks.feature.vanilla;

import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.event.WorldEvents;
import fr.siroz.cariboustonks.feature.Feature;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public class MuteVanillaSoundFeature extends Feature {

	private static final Map<Predicate<Identifier>, Supplier<Boolean>> SOUND_RULES = Map.of(
			id -> id.equals(SoundEvents.ENTITY_ENDERMAN_SCREAM.id()) || id.equals(SoundEvents.ENTITY_ENDERMAN_STARE.id()),
			() -> ConfigManager.getConfig().vanilla.sound.muteEnderman,

			id -> id.getPath().startsWith("entity.phantom"),
			() -> ConfigManager.getConfig().vanilla.sound.mutePhantom
	);

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

		Identifier id = soundEvent.id();
		for (Map.Entry<Predicate<Identifier>, Supplier<Boolean>> entry : SOUND_RULES.entrySet()) {
			if (entry.getKey().test(id) && entry.getValue().get()) {
				return false;
			}
		}
		return true;
	}
}
