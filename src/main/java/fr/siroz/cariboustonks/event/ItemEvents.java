package fr.siroz.cariboustonks.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public final class ItemEvents {

	private ItemEvents() {
	}

	/**
	 * Called when a {@link ItemStack} is set in the player inventory slot.
	 */
	public static final Event<Pickup> PICKUP = EventFactory.createArrayBacked(Pickup.class, listeners -> (slot, stack) -> {
		for (Pickup listener : listeners) {
			listener.onPickup(slot, stack);
		}
	});

	@FunctionalInterface
	public interface Pickup {
		void onPickup(int slot, @NotNull ItemStack stack);
	}
}
