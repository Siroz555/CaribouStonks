package fr.siroz.cariboustonks.core.skyblock;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.core.data.hypixel.election.ElectionResult;
import fr.siroz.cariboustonks.core.data.hypixel.election.Mayor;
import fr.siroz.cariboustonks.core.data.hypixel.election.Perk;
import fr.siroz.cariboustonks.util.DeveloperTools;
import fr.siroz.cariboustonks.util.ScoreboardUtils;
import fr.siroz.cariboustonks.util.StonksUtils;
import java.util.Optional;
import java.util.Set;
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
	 * Returns whether the given {@link Mayor} is currently the mayor or minister.
	 *
	 * @param mayor the {@link Mayor} to check
	 * @return {@code true} if the given {@code mayor} matches the current mayor or minister
	 * @see #isMayorOrMinister(Mayor, Perk)
	 */
	public static boolean isMayorOrMinister(@NotNull Mayor mayor) {
		return isMayorOrMinister(mayor, null);
	}

	/**
	 * Returns whether the given {@link Mayor} currently holds the mayor or minister role,
	 * and (optionally) whether the specified {@link Perk} is present for that role.
	 *
	 * <p>Behavior:
	 * <ul>
	 *   <li>If the latest {@link ElectionResult} is not available, this method returns {@code false}.</li>
	 *   <li>If {@code perk} is {@code null}, the method returns {@code true} when {@code mayor}
	 *       equals either the current mayor or the current minister.</li>
	 *   <li>If {@code perk} is non-null:
	 *       <ul>
	 *         <li>When {@code mayor} equals the current mayor, the method returns {@code true}
	 *             only if the mayor's perk set contains {@code perk}.</li>
	 *         <li>When {@code mayor} equals the current minister, the method returns {@code true}
	 *             only if the minister has a perk and that perk equals {@code perk}.</li>
	 *       </ul>
	 *   </li>
	 * </ul>
	 *
	 * @param mayor the {@link Mayor} to check
	 * @param perk  optional {@link Perk} to verify for the given role; if {@code null} only the role is checked
	 * @return {@code true} if the given {@code mayor} matches the current mayor or minister and,
	 * when {@code perk} is provided, the requested perk is present for that role
	 * @see #isMayorOrMinister(Mayor)
	 */
	public static boolean isMayorOrMinister(@NotNull Mayor mayor, @Nullable Perk perk) {
		ElectionResult result = CaribouStonks.core().getHypixelDataSource().getElection();
		if (result == null) {
			return false;
		}

		if (perk == null) {
			return mayor == result.mayor() || mayor == result.minister();
		}

		if (mayor == result.mayor()) {
			final Set<Perk> mayorPerks = result.mayorPerks();
			return !mayorPerks.isEmpty() && mayorPerks.contains(perk);
		}

		if (mayor == result.minister()) {
			final Optional<Perk> opt = result.ministerPerk();
			return opt.isPresent() && opt.get() == perk;
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
