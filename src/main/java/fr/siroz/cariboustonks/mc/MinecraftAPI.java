package fr.siroz.cariboustonks.mc;

import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.mc.api.ClientContext;
import fr.siroz.cariboustonks.mc.api.WorldContext;
import fr.siroz.cariboustonks.mc.impl.VanillaClientContext;
import fr.siroz.cariboustonks.mc.impl.VanillaWorldContext;

/**
 * Central access point for Minecraft-facing runtime contexts.
 * <p>
 * {@code MinecraftAPI} is a singleton facade initialized once during
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
public final class MinecraftAPI {
	private static volatile MinecraftAPI INSTANCE;

	private final ClientContext playerContext;
	private final WorldContext worldContext;

	private MinecraftAPI(ClientContext playerContext, WorldContext worldContext) {
		this.playerContext = playerContext;
		this.worldContext = worldContext;
	}

	/**
	 * Returns the {@link MinecraftAPI} instance
	 *
	 * @return the instance
	 * @throws IllegalStateException if not bootstrapped yet
	 */
	public static MinecraftAPI getInstance() {
		if (INSTANCE == null) throw new IllegalStateException("MinecraftAPI not bootstrapped yet");
		return INSTANCE;
	}

	/**
	 * Init
	 */
	public static void bootstrap() {
		if (INSTANCE != null) throw new IllegalStateException("Already bootstrapped");
		INSTANCE = new MinecraftAPI(new VanillaClientContext(), new VanillaWorldContext());
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
