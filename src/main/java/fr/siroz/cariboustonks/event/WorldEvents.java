package fr.siroz.cariboustonks.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.world.ClientWorld;
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
	public static final Event<SoundCancellable> SOUND_CANCELLABLE = EventFactory.createArrayBacked(SoundCancellable.class, listeners -> sound -> {
		for (SoundCancellable listener : listeners) {
			if (listener.onSound(sound)) {
				return true;
			}
		}
		return false;
	});

	@FunctionalInterface
	public interface Join {
		void onJoinWorld(@NotNull ClientWorld world);
	}

	@FunctionalInterface
	public interface SoundCancellable {
		boolean onSound(@NotNull SoundEvent sound);
	}
}
