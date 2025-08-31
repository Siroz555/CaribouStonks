package fr.siroz.cariboustonks.core.skyblock;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.util.DeveloperTools;
import fr.siroz.cariboustonks.util.ScoreboardUtils;
import fr.siroz.cariboustonks.util.StonksUtils;
import java.util.Optional;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The {@code SkyBlockAPI} class provides a utility layer to interact with SkyBlock-related states and contents.
 */
public final class SkyBlockAPI {

	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();

	private static boolean onHypixelState = false;
	private static boolean onSkyBlockState = false;
	private static IslandType islandType = IslandType.UNKNOWN;
	private static String gameType = "";

	private SkyBlockAPI() {
	}

	/**
	 * Checks if the player is currently on the SkyBlock.
	 *
	 * @return {@code true} if the player is on SkyBlock
	 */
	public static boolean isOnSkyBlock() {
		return onSkyBlockState;
	}

	/**
	 * Retrieves the current {@link IslandType} the player is on in SkyBlock.
	 *
	 * @return the {@code IslandType} representing the player's current island.
	 */
	public static IslandType getIsland() {
		return islandType;
	}

	/**
	 * Checks if the current {@link IslandType} matches any of the specified island types.
	 *
	 * @param islandTypes an array of {@link IslandType} values to check against the current island type
	 * @return {@code true} if the current island type matches any of the specified types
	 */
	public static boolean isOnIslands(IslandType @NotNull ... islandTypes) {
		if (islandTypes.length == 0) {
			return false;
		}

		if (islandTypes.length == 1) {
			return islandTypes[0] == islandType;
		}

		for (IslandType type : islandTypes) {
			if (type == islandType) return true;
		}

		return false;
	}

	/**
	 * Retrieves the current SkyBlock Area where the player is from the Scoreboard.
	 *
	 * @return an {@link Optional} containing the area name
	 */
	public static @NotNull Optional<String> getArea() {
		return Optional.ofNullable(ScoreboardUtils.getIslandArea());
	}

	@ApiStatus.Internal
	public static String getGameType() {
		return gameType;
	}

	@ApiStatus.Internal
	public static void update() {
		FabricLoader fabricLoader = FabricLoader.getInstance();

		if (CLIENT.world == null || CLIENT.isInSingleplayer()) {
			if (fabricLoader.isDevelopmentEnvironment()) {
				onSkyBlockState = true;
			}
		}

		if (fabricLoader.isDevelopmentEnvironment() || StonksUtils.isConnectedToHypixel()) {
			if (!StonksUtils.isConnectedToHypixel()) {
				onHypixelState = true;
			}
		} else if (onHypixelState) {
			onHypixelState = false;
		}
	}

	@ApiStatus.Internal
	public static void handleLocationUpdate(
			@Nullable Boolean onHypixel,
			@Nullable Boolean onSkyBlock,
			@Nullable String gameTypeFromServer,
			@Nullable IslandType islandTypeFromMode
	) {
		if (onHypixel != null) onHypixelState = onHypixel;
		if (onSkyBlock != null) onSkyBlockState = onSkyBlock;
		if (gameTypeFromServer != null) gameType = gameTypeFromServer;
		if (islandTypeFromMode != null) islandType = islandTypeFromMode;

		if (DeveloperTools.isInDevelopment()) {
			CaribouStonks.LOGGER.info("[SkyBlockAPI] Updated: {}, {}, {}, {}",
					onHypixelState, onSkyBlockState, gameType, islandType.name());
		}
	}
}
