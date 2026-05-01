package fr.siroz.cariboustonks.platform;

import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.platform.api.PlayerContext;
import fr.siroz.cariboustonks.platform.api.WorldContext;
import fr.siroz.cariboustonks.platform.impl.VanillaPlayerContext;
import fr.siroz.cariboustonks.platform.impl.VanillaWorldContext;
import org.jspecify.annotations.NonNull;

/**
 * Central access point for Minecraft-facing runtime contexts.
 * <p>
 * {@code MinecraftService} is a singleton facade initialized once during
 * client startup. It exposes foundational contexts; client, player and world.
 * <p>
 * The intended usage pattern within features is through the shorthand
 * methods inherited from {@link Feature}, not through direct calls to this
 * class. Direct access is reserved for infrastructure code that operates
 * outside the feature lifecycle.
 *
 * @see PlayerContext
 * @see WorldContext
 * @see Feature
 */
public final class MinecraftService {
	private static MinecraftService INSTANCE;

	private final PlayerContext playerContext;
	private final WorldContext worldContext;

	private MinecraftService(PlayerContext playerContext, WorldContext worldContext) {
		this.playerContext = playerContext;
		this.worldContext = worldContext;
	}

	/**
	 * Returns the {@link MinecraftService} instance
	 *
	 * @return the instance
	 * @throws IllegalStateException if not bootstrapped yet
	 */
	public static @NonNull MinecraftService getInstance() {
		if (INSTANCE == null) throw new IllegalStateException("MinecraftService not bootstrapped yet");
		return INSTANCE;
	}

	/**
	 * Init
	 */
	public static void bootstrap() {
		if (INSTANCE != null) throw new IllegalStateException("Already bootstrapped");
		INSTANCE = new MinecraftService(new VanillaPlayerContext(), new VanillaWorldContext());
	}

	/**
	 * Returns the {@link PlayerContext} to provides a view of the local player's state.
	 *
	 * @return the context
	 */
	public PlayerContext player() {
		return playerContext;
	}

	/**
	 * Returns the {@link WorldContext} to provides a view of the current client-side world.
	 *
	 * @return the context
	 */
	public WorldContext world() {
		return worldContext;
	}
}
