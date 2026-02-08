package fr.siroz.cariboustonks.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;

public final class InteractionEvents {

	private InteractionEvents() {
	}

	/**
	 * Invoked whenever a player left-clicks the air
	 */
	public static final Event<LeftClickAir> LEFT_CLICK_AIR_EVENT = EventFactory.createArrayBacked(LeftClickAir.class, listeners -> (player, hand) -> {
		for (LeftClickAir listener : listeners) {
			listener.onLeftClick(player, hand);
		}
	});

	/**
	 * Invoked whenever the player interact on a block.
	 */
	public static final Event<AllowInteractBlock> ALLOW_INTERACT_BLOCK_EVENT = EventFactory.createArrayBacked(AllowInteractBlock.class, listeners -> (heldItem) -> {
		for (AllowInteractBlock listener : listeners) {
			if (!listener.allowInteract(heldItem)) {
				return false;
			}
		}
		return true;
	});

	@FunctionalInterface
	public interface LeftClickAir {
		void onLeftClick(Player player, InteractionHand hand);
	}

	@FunctionalInterface
	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	public interface AllowInteractBlock {
		boolean allowInteract(@NonNull ItemStack heldItem);
	}
}
