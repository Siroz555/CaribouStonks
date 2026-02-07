package fr.siroz.cariboustonks.event;

import java.util.List;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;

/**
 * Events related to client
 */
public final class ClientEvents {

	private ClientEvents() {
	}

	/**
	 * Called when the {@code TabList} is updated.
	 * <p>
	 * The update is triggered every second
	 */
	public static final Event<TabListUpdate> TAB_LIST_UPDATE_EVENT = EventFactory.createArrayBacked(TabListUpdate.class, listeners -> lines -> {
		for (TabListUpdate listener : listeners) {
			listener.onUpdate(lines);
		}
	});

	/**
	 * Called when the {@code Scoreboard} is updated.
	 * <p>
	 * The update is triggered every second
	 */
	public static final Event<ScoreboardUpdate> SCOREBOARD_UPDATE_EVENT = EventFactory.createArrayBacked(ScoreboardUpdate.class, listeners -> lines -> {
		for (ScoreboardUpdate listener : listeners) {
			listener.onUpdate(lines);
		}
	});

	/**
	 * Called when a {@link ItemStack} is set in the client inventory.
	 */
	@Deprecated // AbstractContainerMenuMixin
	public static final Event<PickupItem> PICKUP_ITEM_EVENT = EventFactory.createArrayBacked(PickupItem.class, listeners -> (slot, stack) -> {
		for (PickupItem listener : listeners) {
			listener.onPickup(slot, stack);
		}
	});

	/**
	 * Called when the mouse wheel is scrolling
	 */
	public static final Event<AllowMouseScroll> ALLOW_MOUSE_SCROLL_EVENT = EventFactory.createArrayBacked(AllowMouseScroll.class, listeners -> (horizontal, vertical) -> {
		for (AllowMouseScroll listener : listeners) {
			if (!listener.allowMouseScroll(horizontal, vertical)) {
				return false;
			}
		}
		return true;
	});

	@FunctionalInterface
	public interface TabListUpdate {
		void onUpdate(@NonNull List<String> lines);
	}

	@FunctionalInterface
	public interface ScoreboardUpdate {
		void onUpdate(@NonNull List<String> lines);
	}

	@FunctionalInterface
	public interface PickupItem {
		void onPickup(int slot, @NonNull ItemStack stack);
	}

	@FunctionalInterface
	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	public interface AllowMouseScroll {
		boolean allowMouseScroll(double horizontal, double vertical);
	}
}
