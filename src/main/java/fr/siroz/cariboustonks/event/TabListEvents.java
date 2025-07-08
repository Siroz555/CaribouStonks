package fr.siroz.cariboustonks.event;

import it.unimi.dsi.fastutil.objects.ObjectList;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import org.jetbrains.annotations.NotNull;

/**
 * Events related to TabList interactions.
 */
public final class TabListEvents {

	private TabListEvents() {
	}

	/**
	 * Called when the {@code TabList} is updated.
	 * <p>
	 * The update is triggered every {@code 3 seconds}
	 */
	public static final Event<Update> UPDATE = EventFactory.createArrayBacked(Update.class, listeners -> lines -> {
		for (Update listener : listeners) {
			listener.onUpdate(lines);
		}
	});

	@FunctionalInterface
	public interface Update {
		void onUpdate(@NotNull ObjectList<String> lines);
	}
}
