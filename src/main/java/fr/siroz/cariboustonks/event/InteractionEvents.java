package fr.siroz.cariboustonks.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public final class InteractionEvents {

	private InteractionEvents() {
	}

	/**
	 * Invoked whenever a player left-clicks the air
	 */
	public static final Event<@NotNull LeftClickAir> LEFT_CLICK_AIR = EventFactory.createArrayBacked(LeftClickAir.class, listeners -> (player, hand) -> {
		for (LeftClickAir listener : listeners) {
			listener.onClick(player, hand);
		}
	});

	/**
	 * Invoked whenever the player interact on a block.
	 */
	public static final Event<AllowInteractBlock> ALLOW_INTERACT_BLOCK = EventFactory.createArrayBacked(AllowInteractBlock.class, listeners -> (heldItem) -> {
		for (AllowInteractBlock listener : listeners) {
			if (!listener.onInteract(heldItem)) {
				return false;
			}
		}
		return true;
	});

	@FunctionalInterface
	public interface LeftClickAir {
		void onClick(Player player, InteractionHand hand);
	}

	@FunctionalInterface
	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	public interface AllowInteractBlock {
		boolean onInteract(@NotNull ItemStack heldItem);
	}
}
