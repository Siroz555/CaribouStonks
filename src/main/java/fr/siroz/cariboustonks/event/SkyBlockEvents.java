package fr.siroz.cariboustonks.event;

import fr.siroz.cariboustonks.core.skyblock.IslandType;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import org.jetbrains.annotations.NotNull;

/**
 * Events related to SkyBlock "gameplay" interactions.
 */
public final class SkyBlockEvents {

	private SkyBlockEvents() {
	}

	/**
	 * Called when the client joins the SkyBlock
	 */
	public static final Event<Join> JOIN = EventFactory.createArrayBacked(Join.class, listeners -> (serverName) -> {
		for (Join listener : listeners) {
			listener.onJoin(serverName);
		}
	});

	/**
	 * Called when the client leaves the SkyBlock
	 */
	public static final Event<Leave> LEAVE = EventFactory.createArrayBacked(Leave.class, listeners -> () -> {
		for (Leave listener : listeners) {
			listener.onLeave();
		}
	});

	/**
	 * Called when the client changes of SkyBlock Island
	 */
	public static final Event<IslandChange> ISLAND_CHANGE = EventFactory.createArrayBacked(IslandChange.class, listeners -> (location) -> {
		for (IslandChange listener : listeners) {
			listener.onIslandChange(location);
		}
	});

	@FunctionalInterface
	public interface Join {
		void onJoin(@NotNull String serverName);
	}

	@FunctionalInterface
	public interface Leave {
		void onLeave();
	}

	@FunctionalInterface
	public interface IslandChange {
		void onIslandChange(@NotNull IslandType islandType);
	}
}
