package fr.siroz.cariboustonks;

import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.core.CaribouManager;
import fr.siroz.cariboustonks.feature.Features;
import fr.siroz.cariboustonks.skyblock.SkyBlockManager;
import fr.siroz.cariboustonks.system.Systems;
import fr.siroz.cariboustonks.rendering.CaribouRenderer;
import fr.siroz.cariboustonks.util.StonksUtils;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.Version;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.function.Supplier;

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

	private CaribouManager caribouManager;
	private SkyBlockManager skyBlockManager;
	private Systems systems;
	private Features features;

	@ApiStatus.Internal
	public CaribouStonks() {
		instance = this;
	}

	@Override
	public void onInitializeClient() {
		// Mod Configuration
		ConfigManager.loadConfig();
		// Mod components
		this.caribouManager = new CaribouManager();
		// SkyBlock related-content
		this.skyBlockManager = new SkyBlockManager();
		// Features
		this.systems = new Systems();
		this.features = new Features();
		// Utilities
		StonksUtils.initUtilities();
		// Rendering
		CaribouRenderer.init();
		// Developer Mode
		this.caribouManager.initDeveloperMode();
	}

	/**
	 * Returns the {@link CaribouManager} instance of the mod.
	 * This instance is used to retrieve and manage all internal mod functionalities.
	 *
	 * @return the {@link CaribouManager} instance
	 */
	@Contract(pure = true)
	public static CaribouManager core() {
		return instance.caribouManager;
	}

	/**
	 * Returns the {@link SkyBlockManager} instance of the mod.
	 * This instance is used to retrieve and manage all SkyBlock managers.
	 *
	 * @return the {@link SkyBlockManager} instance
	 */
	@Contract(pure = true)
	public static SkyBlockManager skyBlock() {
		return instance.skyBlockManager;
	}

	/**
	 * Returns the {@link Systems} instance of the mod.
	 * This instance is used to retrieve and manage all registered systems components.
	 *
	 * @return the {@link Systems} instance
	 */
	@Contract(pure = true)
	public static Systems systems() {
		return instance.systems;
	}

	/**
	 * Returns the {@link Features} instance of the mod.
	 * This instance is used to retrieve and manage all registered features.
	 *
	 * @return the {@link Features} instance
	 */
	@Contract(pure = true)
	public static Features features() {
		return instance.features;
	}

	/**
	 * Returns an {@link Identifier} from the Mod assets.
	 *
	 * @param path the path
	 * @return the {@link Identifier}
	 */
	@Contract("_ -> new")
	public static @NotNull Identifier identifier(@NotNull String path) {
		return Identifier.fromNamespaceAndPath(NAMESPACE, path);
	}

	/**
	 * Returns a {@link MutableComponent} colored text prefix of the Mod.
	 * <p>
	 * Symbol: "" ("\ue820")
	 *
	 * @return the text prefix
	 */
	@Contract(pure = true)
	public static @NotNull Supplier<MutableComponent> prefix() {
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
