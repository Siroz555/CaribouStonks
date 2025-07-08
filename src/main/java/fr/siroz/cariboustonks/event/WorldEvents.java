package fr.siroz.cariboustonks.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.world.ClientWorld;
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

	@FunctionalInterface
	public interface Join {
		void onJoinWorld(@NotNull ClientWorld world);
	}
}
