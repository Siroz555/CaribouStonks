package fr.siroz.cariboustonks.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Events related to global world interactions.
 */
public final class WorldEvents {

	private WorldEvents() {
	}

	/**
	 * Called when a block state is updated.
	 */
	public static final Event<@NotNull BlockStateUpdate> BLOCK_STATE_UPDATE = EventFactory.createArrayBacked(BlockStateUpdate.class, listeners -> (pos, oldState, newState) -> {
		for (BlockStateUpdate listener : listeners) {
			listener.onBlockStateUpdate(pos, oldState, newState);
		}
	});

	/**
	 * Called when the client joins the world or a new world
	 */
	public static final Event<@NotNull Join> JOIN = EventFactory.createArrayBacked(Join.class, listeners -> world -> {
		for (Join listener : listeners) {
			listener.onJoinWorld(world);
		}
	});

	/**
	 * Called when the client receives a Sound
	 */
	@OnlySkyBlock
	public static final Event<@NotNull AllowSound> ALLOW_SOUND = EventFactory.createArrayBacked(AllowSound.class, listeners -> sound -> {
		boolean allowSound = true;
		for (AllowSound listener : listeners) {
			allowSound &= listener.allowSound(sound);
		}
		return allowSound;
	});

	@OnlySkyBlock
	public static final Event<@NotNull ArmorStandRemoved> ARMORSTAND_REMOVED = EventFactory.createArrayBacked(ArmorStandRemoved.class, listeners -> (armorStand) -> {
		for (ArmorStandRemoved listener : listeners) {
			listener.onRemove(armorStand);
		}
	});

	@FunctionalInterface
	public interface BlockStateUpdate {
		void onBlockStateUpdate(@NotNull BlockPos pos, @Nullable BlockState oldState, @NotNull BlockState newState);
	}

	@FunctionalInterface
	public interface Join {
		void onJoinWorld(@NotNull ClientLevel world);
	}

	@FunctionalInterface
	public interface AllowSound {
		boolean allowSound(@NotNull SoundEvent sound);
	}

	@FunctionalInterface
	public interface ArmorStandRemoved {
		void onRemove(@NotNull ArmorStand armorStand);
	}
}
