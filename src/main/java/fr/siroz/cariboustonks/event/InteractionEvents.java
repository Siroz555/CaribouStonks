package fr.siroz.cariboustonks.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
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
	 * Invoked whenever a player right-clicks the air
	 */
	@Deprecated
	public static final Event<@NotNull RightClickAir> RIGHT_CLICK_AIR = EventFactory.createArrayBacked(RightClickAir.class, listeners -> (player, hand) -> {
		for (RightClickAir listener : listeners) {
			listener.onClick(player, hand);
		}
	});

	@FunctionalInterface
	public interface LeftClickAir {
		void onClick(Player player, InteractionHand hand);
	}

	@Deprecated
	@FunctionalInterface
	public interface RightClickAir {
		void onClick(Player player, InteractionHand hand);
	}
}
