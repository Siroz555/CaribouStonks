package fr.siroz.cariboustonks.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.sound.SoundEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Events related to global world interactions.
 */
public final class WorldEvents {

	private WorldEvents() {
	}

	/**
	 * Called when the client joins the world or a new world
	 */
	public static final Event<Join> JOIN = EventFactory.createArrayBacked(Join.class, listeners -> world -> {
		for (Join listener : listeners) {
			listener.onJoinWorld(world);
		}
	});

	/**
	 * Called when the client receives a Sound
	 */
	public static final Event<AllowSound> ALLOW_SOUND = EventFactory.createArrayBacked(AllowSound.class, listeners -> sound -> {
		boolean allowSound = true;
		for (AllowSound listener : listeners) {
			allowSound &= listener.allowSound(sound);
		}
		return allowSound;
	});

	public static final Event<ArmorStandRemoved> ARMORSTAND_REMOVED = EventFactory.createArrayBacked(ArmorStandRemoved.class, listeners -> (armorStand) -> {
		for (ArmorStandRemoved listener : listeners) {
			listener.onRemove(armorStand);
		}
	});

	@FunctionalInterface
	public interface Join {
		void onJoinWorld(@NotNull ClientWorld world);
	}

	@FunctionalInterface
	public interface AllowSound {
		boolean allowSound(@NotNull SoundEvent sound);
	}

	@FunctionalInterface
	public interface ArmorStandRemoved {
		void onRemove(@NotNull ArmorStandEntity armorStand);
	}
}
