package fr.siroz.cariboustonks.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Events related to global world interactions.
 */
public final class WorldEvents {

	private WorldEvents() {
	}

	/**
	 * Called when a block state is updated.
	 */
	public static final Event<BlockStateUpdate> BLOCK_STATE_UPDATE_EVENT = EventFactory.createArrayBacked(BlockStateUpdate.class, listeners -> (pos, oldState, newState) -> {
		for (BlockStateUpdate listener : listeners) {
			listener.onBlockStateUpdate(pos, oldState, newState);
		}
	});

	/**
	 * Called when the client receives a Sound
	 */
	public static final Event<AllowSound> ALLOW_SOUND_EVENT = EventFactory.createArrayBacked(AllowSound.class, listeners -> sound -> {
		boolean allowSound = true;
		for (AllowSound listener : listeners) {
			allowSound &= listener.allowSound(sound);
		}
		return allowSound;
	});

	public static final Event<ArmorStandRemoved> ARMORSTAND_REMOVE_EVENT = EventFactory.createArrayBacked(ArmorStandRemoved.class, listeners -> (armorStand) -> {
		for (ArmorStandRemoved listener : listeners) {
			listener.onRemove(armorStand);
		}
	});

	@FunctionalInterface
	public interface BlockStateUpdate {
		void onBlockStateUpdate(@NonNull BlockPos pos, @Nullable BlockState oldState, @NonNull BlockState newState);
	}

	@FunctionalInterface
	public interface AllowSound {
		boolean allowSound(@NonNull SoundEvent sound);
	}

	@FunctionalInterface
	public interface ArmorStandRemoved {
		void onRemove(@NonNull ArmorStand armorStand);
	}
}
