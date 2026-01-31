package fr.siroz.cariboustonks.feature.vanilla;

import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.event.WorldEvents;
import fr.siroz.cariboustonks.feature.Feature;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.resources.Identifier;

public class MuteVanillaSoundFeature extends Feature {

	private static final Map<Identifier, BooleanSupplier> EXACT_SOUND_RULES = Map.of(
			SoundEvents.LIGHTNING_BOLT_IMPACT.location(), () -> ConfigManager.getConfig().vanilla.sound.muteLightning,
			SoundEvents.LIGHTNING_BOLT_THUNDER.location(), () -> ConfigManager.getConfig().vanilla.sound.muteLightning,
			SoundEvents.PLAYER_SMALL_FALL.location(), () -> ConfigManager.getConfig().vanilla.sound.mutePlayerFall,
			SoundEvents.PLAYER_BIG_FALL.location(), () -> ConfigManager.getConfig().vanilla.sound.mutePlayerFall,
			SoundEvents.ENDERMAN_SCREAM.location(), () -> ConfigManager.getConfig().vanilla.sound.muteEnderman,
			SoundEvents.ENDERMAN_STARE.location(), () -> ConfigManager.getConfig().vanilla.sound.muteEnderman
	);

	private static final List<PrefixRule> PREFIX_SOUND_RULES = List.of(
			new PrefixRule("entity.phantom", () -> ConfigManager.getConfig().vanilla.sound.mutePhantom)
	);

	public MuteVanillaSoundFeature() {
		WorldEvents.ALLOW_SOUND.register(this::allowSound);
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock();
	}

	@EventHandler(event = "WorldEvents.ALLOW_SOUND")
	private boolean allowSound(SoundEvent soundEvent) {
		if (!isEnabled()) return true;

		Identifier id = soundEvent.location();

		BooleanSupplier exact = EXACT_SOUND_RULES.get(id);
		if (exact != null && exact.getAsBoolean()) {
			return false;
		}

		String path = id.getPath();
		for (PrefixRule pr : PREFIX_SOUND_RULES) {
			if (path.startsWith(pr.prefix()) && pr.enabled().getAsBoolean()) {
				return false;
			}
		}

		return true;
	}

	private record PrefixRule(String prefix, BooleanSupplier enabled) {
	}
}
