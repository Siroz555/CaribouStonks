package fr.siroz.cariboustonks.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.network.packet.s2c.play.EntityEquipmentUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import org.jetbrains.annotations.NotNull;

/**
 * Events triggered through network packets and interactions.
 */
public final class NetworkEvents {

	private NetworkEvents() {
	}

	/**
	 * Called when the {@code CommonPingS2CPacket} is received
	 */
	public static final Event<ServerTick> SERVER_TICK = EventFactory.createArrayBacked(ServerTick.class, listeners -> () -> {
		for (ServerTick listener : listeners) {
			listener.onServerTick();
		}
	});

	/**
	 * Called when the {@code GameJoinS2CPacket} is received
	 */
	public static final Event<GameJoin> GAME_JOIN_PACKET = EventFactory.createArrayBacked(GameJoin.class, listeners -> () -> {
		for (GameJoin listener : listeners) {
			listener.onGameJoin();
		}
	});

	/**
	 * Called when the {@code WorldTimeUpdateS2CPacket} is received
	 */
	public static final Event<WorldTimeUpdate> WORLD_TIME_UPDATE_PACKET = EventFactory.createArrayBacked(WorldTimeUpdate.class, listeners -> () -> {
		for (WorldTimeUpdate listener : listeners) {
			listener.onWorldTimeUpdate();
		}
	});

	/**
	 * Called when the {@code PingMeasurer} received the ping result
	 */
	public static final Event<PingResult> PING_RESULT = EventFactory.createArrayBacked(PingResult.class, listeners -> ping -> {
		for (PingResult listener : listeners) {
			listener.onPingResult(ping);
		}
	});

	/**
	 * Called when the {@code ParticleS2CPacket} is received. Can be {@code canceled}.
	 */
	public static final Event<ParticlePreReceived> PARTICLE_PRE_RECEIVED_PACKET = EventFactory.createArrayBacked(ParticlePreReceived.class, listeners -> particlePacket -> {
		for (ParticlePreReceived listener : listeners) {
			if (listener.onParticlePreReceived(particlePacket)) {
				return true;
			}
		}
		return false;
	});

	/**
	 * Called when the {@code ParticleS2CPacket} is received
	 */
	public static final Event<ParticleReceived> PARTICLE_RECEIVED_PACKET = EventFactory.createArrayBacked(ParticleReceived.class, listeners -> particlePacket -> {
		for (ParticleReceived listener : listeners) {
			listener.onParticleReceived(particlePacket);
		}
	});

	/**
	 * Called when the {@code PlaySoundS2CPacket} is received
	 */
	public static final Event<PlaySound> PLAY_SOUND_PACKET = EventFactory.createArrayBacked(PlaySound.class, listeners -> soundPacket -> {
		for (PlaySound listener : listeners) {
			listener.onPlaySound(soundPacket);
		}
	});

	/**
	 * Called when an {@link ArmorStandEntity} has been updated
	 * from {@link EntityTrackerUpdateS2CPacket} or {@link EntityEquipmentUpdateS2CPacket}
	 */
	public static final Event<ArmorStandUpdate> ARMORSTAND_UPDATE_PACKET = EventFactory.createArrayBacked(ArmorStandUpdate.class, listeners -> (entity, equipmentUpdate) -> {
		for (ArmorStandUpdate listener : listeners) {
			listener.onArmorStandUpdate(entity, equipmentUpdate);
		}
	});

	@FunctionalInterface
	public interface ServerTick {
		void onServerTick();
	}

	@FunctionalInterface
	public interface GameJoin {
		void onGameJoin();
	}

	@FunctionalInterface
	public interface WorldTimeUpdate {
		void onWorldTimeUpdate();
	}

	@FunctionalInterface
	public interface PingResult {
		void onPingResult(long ping);
	}

	@FunctionalInterface
	public interface ParticlePreReceived {
		boolean onParticlePreReceived(ParticleS2CPacket particlePacket);
	}

	@FunctionalInterface
	public interface ParticleReceived {
		void onParticleReceived(ParticleS2CPacket particlePacket);
	}

	@FunctionalInterface
	public interface PlaySound {
		void onPlaySound(PlaySoundS2CPacket soundPacket);
	}

	@FunctionalInterface
	public interface ArmorStandUpdate {
		void onArmorStandUpdate(@NotNull ArmorStandEntity entity, boolean equipmentUpdate);
	}
}
