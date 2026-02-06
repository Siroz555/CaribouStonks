package fr.siroz.cariboustonks;

import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.core.feature.FeatureManager;
import fr.siroz.cariboustonks.core.mod.CaribouManager;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockManager;
import fr.siroz.cariboustonks.core.system.SystemManager;
import fr.siroz.cariboustonks.rendering.CaribouRenderer;
import fr.siroz.cariboustonks.util.StonksUtils;
import java.nio.file.Path;
import java.util.function.Supplier;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.Version;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Main entrypoint of the CaribouStonks Mod
 */
public final class CaribouStonks implements ClientModInitializer {

	public static final Logger LOGGER = LoggerFactory.getLogger("CaribouStonks");

	public static final String NAMESPACE = "cariboustonks";
	public static final ModContainer MOD_CONTAINER = FabricLoader.getInstance().getModContainer(NAMESPACE).orElseThrow();
	public static final Version VERSION = MOD_CONTAINER.getMetadata().getVersion();
	public static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir().resolve(NAMESPACE);

	private static CaribouStonks instance;

	private CaribouManager modManager;
	private SkyBlockManager skyBlockManager;
	private SystemManager systemManager;
	private FeatureManager featureManager;

	public CaribouStonks() {
		instance = this;
	}

	@Override
	public void onInitializeClient() {
		// Mod Configuration
		ConfigManager.loadConfig();
		// Utilities
		StonksUtils.initUtilities();
		// Rendering
		CaribouRenderer.init();
		this.modManager = new CaribouManager();
		this.skyBlockManager = new SkyBlockManager();
		this.systemManager = new SystemManager();
		this.featureManager = new FeatureManager();
	}

	/**
	 * Returns the {@link CaribouManager} instance of the mod.
	 * This instance is used to retrieve and manage all internal mod functionalities.
	 *
	 * @return the {@link CaribouManager} instance
	 */
	public static @NonNull CaribouManager mod() {
		return instance.modManager;
	}

	/**
	 * Returns the {@link SkyBlockManager} instance of the mod.
	 * This instance is used to retrieve and manage all SkyBlock managers.
	 *
	 * @return the {@link SkyBlockManager} instance
	 */
	public static @NonNull SkyBlockManager skyBlock() {
		return instance.skyBlockManager;
	}

	/**
	 * Returns the {@link SystemManager} instance of the mod.
	 * This instance is used to retrieve and manage all registered systems components.
	 *
	 * @return the {@link SystemManager} instance
	 */
	public static @NonNull SystemManager systems() {
		return instance.systemManager;
	}

	/**
	 * Returns the {@link FeatureManager} instance of the mod.
	 * This instance is used to retrieve and manage all registered features.
	 *
	 * @return the {@link FeatureManager} instance
	 */
	public static @NonNull FeatureManager features() {
		return instance.featureManager;
	}

	/**
	 * Returns an {@link Identifier} from the Mod assets.
	 *
	 * @param path the path
	 * @return the {@link Identifier}
	 */
	public static @NonNull Identifier identifier(@NonNull String path) {
		return Identifier.fromNamespaceAndPath(NAMESPACE, path);
	}

	/**
	 * Returns a {@link MutableComponent} colored text prefix of the Mod.
	 * <p>
	 * Symbol: "" ("\ue820")
	 *
	 * @return the text prefix
	 */
	public static @NonNull Supplier<MutableComponent> prefix() {
		return () -> Component.empty()
				.append(Component.literal("[").withStyle(ChatFormatting.DARK_GRAY))
				.append(Component.literal("C").withColor(0xe13333))
				.append(Component.literal("a").withColor(0xdc3030))
				.append(Component.literal("r").withColor(0xd82e2e))
				.append(Component.literal("i").withColor(0xd42c2c))
				.append(Component.literal("b").withColor(0xd02a2a))
				.append(Component.literal("o").withColor(0xcc2828))
				.append(Component.literal("u").withColor(0xc82626))
				.append(Component.literal("S").withColor(0xc42424))
				.append(Component.literal("t").withColor(0xc02222))
				.append(Component.literal("o").withColor(0xbc2020))
				.append(Component.literal("n").withColor(0xb81e1e))
				.append(Component.literal("k").withColor(0xb41c1c))
				.append(Component.literal("s").withColor(0xb01a1a)) // 13
				.append(Component.literal("] ").withStyle(ChatFormatting.DARK_GRAY));
	}
}
