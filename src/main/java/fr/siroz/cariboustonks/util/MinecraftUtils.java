package fr.siroz.cariboustonks.util;

import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.PlayerTeam;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public final class MinecraftUtils {

	private MinecraftUtils() {
	}

	/**
	 * Checks if a {@link Player} as {@code Entity} is a real player, and not an enemy or NPC
	 *
	 * @return {@code true} if is a real player
	 */
	public static boolean isPlayer(@NonNull Entity entity) {
		return entity instanceof Player player && player.getUUID().version() == 4;
	}

	/**
	 * Retrieve the {@code Sound Name} of the given Sound Packet.
	 * <p>
	 * {@code entity.warden.death}
	 *
	 * @param soundPacket the sound packet
	 * @return the sound name of an empty String
	 */
	public static @NonNull String convertSoundPacketToName(@Nullable ClientboundSoundPacket soundPacket) {
		if (soundPacket == null) return "";
		return soundPacket.getSound().value().location().getPath();
	}

	/**
	 * Returns the {@link Component} formatted for the given {@link PlayerInfo}.
	 * <p>
	 * Import depuis PlayerTabOverlay
	 *
	 * @param playerInfo the player
	 * @param profileName the profile name
	 * @return the Component
	 */
	public static Component getNameForDisplay(@NonNull PlayerInfo playerInfo, @NonNull String profileName) {
		return playerInfo.getTabListDisplayName() != null
				? playerInfo.getTabListDisplayName().copy()
				: PlayerTeam.formatNameForTeam(playerInfo.getTeam(), Component.literal(profileName));
	}
}
