package fr.siroz.cariboustonks.platform;

import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.platform.api.context.ClientContext;
import fr.siroz.cariboustonks.platform.api.context.PlayerContext;
import fr.siroz.cariboustonks.platform.api.context.WorldContext;
import fr.siroz.cariboustonks.platform.impl.context.VanillaClientContext;
import fr.siroz.cariboustonks.platform.impl.context.VanillaPlayerContext;
import fr.siroz.cariboustonks.platform.impl.context.VanillaWorldContext;
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
 * @see ClientContext
 * @see PlayerContext
 * @see WorldContext
 * @see Feature
 */
public final class MinecraftService {
	private static MinecraftService INSTANCE;

	private final ClientContext clientContext;
	private final PlayerContext playerContext;
	private final WorldContext worldContext;

	private MinecraftService(ClientContext clientContext, PlayerContext playerContext, WorldContext worldContext) {
		this.clientContext = clientContext;
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
		INSTANCE = new MinecraftService(new VanillaClientContext(), new VanillaPlayerContext(), new VanillaWorldContext());
	}

	/**
	 * Returns the {@link ClientContext} to provides a view of the client state.
	 *
	 * @return the context
	 */
	public ClientContext client() {
		return clientContext;
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
