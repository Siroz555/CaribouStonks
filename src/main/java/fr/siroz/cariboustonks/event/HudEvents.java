package fr.siroz.cariboustonks.event;

import java.util.List;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import org.jetbrains.annotations.NotNull;

public final class HudEvents {

	private HudEvents() {
	}

	/**
	 * Called when the {@code TabList} is updated.
	 * <p>
	 * The update is triggered every second
	 */
	@OnlySkyBlock
	public static final Event<@NotNull TabListUpdate> TAB_LIST_UPDATE = EventFactory.createArrayBacked(TabListUpdate.class, listeners -> lines -> {
		for (TabListUpdate listener : listeners) {
			listener.onUpdate(lines);
		}
	});

	/**
	 * Called when the {@code Scoreboard} is updated.
	 * <p>
	 * The update is triggered every second
	 */
	@OnlySkyBlock
	public static final Event<@NotNull ScoreboardUpdate> SCOREBOARD_UPDATE = EventFactory.createArrayBacked(ScoreboardUpdate.class, listeners -> lines -> {
		for (ScoreboardUpdate listener : listeners) {
			listener.onUpdate(lines);
		}
	});

	@FunctionalInterface
	public interface TabListUpdate {
		void onUpdate(@NotNull List<String> lines);
	}

	@FunctionalInterface
	public interface ScoreboardUpdate {
		void onUpdate(@NotNull List<String> lines);
	}
}
