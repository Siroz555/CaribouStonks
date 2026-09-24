package fr.siroz.cariboustonks.core.skyblock;

import org.jspecify.annotations.NonNull;

/**
 * Immuable current player Location
 *
 * @param onSkyBlock if the player is currently on the SkyBlock
 * @param gameType   the type of server the player has connected to if available such as SKYBLOCK
 * @param island     the current {@link IslandType} the player is on.
 */
public record SkyBlockLocation(boolean onSkyBlock, String gameType, IslandType island) {
	public static final SkyBlockLocation NONE = new SkyBlockLocation(false, "", IslandType.UNKNOWN);

	static @NonNull SkyBlockLocation of(String gameType, IslandType island) {
		return new SkyBlockLocation("SKYBLOCK".equals(gameType), gameType, island);
	}

	/**
	 * Checks if the current {@link IslandType} matches any of the specified island types.
	 *
	 * @param islands an array of {@link IslandType} values to check against the current island type
	 * @return {@code true} if the current island type matches any of the specified types
	 */
	public boolean isOn(IslandType @NonNull ... islands) {
		if (islands.length == 0) return false;
		if (islands.length == 1) return islands[0] == island;

		for (IslandType type : islands) {
			if (type == island) return true;
		}

		return false;
	}
}
