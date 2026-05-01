package fr.siroz.cariboustonks.platform;

import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.platform.api.ClientContext;
import fr.siroz.cariboustonks.platform.api.WorldContext;
import fr.siroz.cariboustonks.platform.impl.VanillaClientContext;
import fr.siroz.cariboustonks.platform.impl.VanillaWorldContext;
import org.jspecify.annotations.NonNull;

/**
 * Central access point for Minecraft-facing runtime contexts.
 * <p>
 * {@code MinecraftService} is a singleton facade initialized once during
 * client startup. It exposes the two foundational contexts; client and world.
 * <p>
 * The intended usage pattern within features is through the shorthand
 * methods inherited from {@link Feature}, not through direct calls to this
 * class. Direct access is reserved for infrastructure code that operates
 * outside the feature lifecycle.
 *
 * @see ClientContext
 * @see WorldContext
 * @see Feature
 */
public final class MinecraftService {
	private static volatile MinecraftService INSTANCE;

	private final ClientContext playerContext;
	private final WorldContext worldContext;

	private MinecraftService(ClientContext playerContext, WorldContext worldContext) {
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
		MinecraftService instance = INSTANCE;
		if (instance == null) throw new IllegalStateException("MinecraftService not bootstrapped yet");
		return instance;
	}

	/**
	 * Init
	 */
	public static synchronized void bootstrap() {
		if (INSTANCE != null) throw new IllegalStateException("Already bootstrapped");
		INSTANCE = new MinecraftService(new VanillaClientContext(), new VanillaWorldContext());
	}

	/**
	 * Returns the {@link ClientContext} to provides a view of the local player's state.
	 *
	 * @return the context
	 */
	public ClientContext client() {
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
