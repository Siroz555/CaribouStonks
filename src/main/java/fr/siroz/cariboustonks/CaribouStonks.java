package fr.siroz.cariboustonks;

import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.core.CaribouStonksCore;
import fr.siroz.cariboustonks.util.DeveloperTools;
import fr.siroz.cariboustonks.feature.Features;
import fr.siroz.cariboustonks.manager.Managers;
import fr.siroz.cariboustonks.util.StonksUtils;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
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
	public static final String VERSION = MOD_CONTAINER.getMetadata().getVersion().getFriendlyString();
	public static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir().resolve(NAMESPACE);

	private static CaribouStonks instance;

	private CaribouStonksCore caribouStonksCore;
	private Managers managers;
	private Features features;

	@ApiStatus.Internal
	public CaribouStonks() {
		instance = this;
	}

	@Override
	public void onInitializeClient() {
		// Mod Configuration
		ConfigManager.loadConfig();
		// Utilities
		StonksUtils.initUtilities();
		// Main
		this.caribouStonksCore = new CaribouStonksCore();
		this.managers = new Managers();
		this.features = new Features();
		// Dev
		DeveloperTools.initDeveloperTools();
	}

	/**
	 * Returns the {@link CaribouStonksCore} instance of the mod.
	 * This instance is used to retrieve and manage all core functionalities.
	 *
	 * @return the {@link CaribouStonksCore} instance
	 */
	@Contract(pure = true)
	public static CaribouStonksCore core() {
		return instance.caribouStonksCore;
	}

	/**
	 * Returns the {@link Managers} instance of the mod.
	 * This instance is used to retrieve and manage all registered manager components.
	 *
	 * @return the {@link Managers} instance
	 */
	@Contract(pure = true)
	public static Managers managers() {
		return instance.managers;
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
		return Identifier.of(NAMESPACE, path);
	}

	/**
	 * Returns a {@link MutableText} colored text prefix of the Mod.
	 * <p>
	 * Symbol: "" ("\ue820")
	 *
	 * @return the text prefix
	 */
	@Contract(pure = true)
	public static @NotNull Supplier<MutableText> prefix() {
		return () -> Text.empty()
				.append(Text.literal("[").formatted(Formatting.DARK_GRAY))
				.append(Text.literal("C").withColor(0xe13333))
				.append(Text.literal("a").withColor(0xdc3030))
				.append(Text.literal("r").withColor(0xd82e2e))
				.append(Text.literal("i").withColor(0xd42c2c))
				.append(Text.literal("b").withColor(0xd02a2a))
				.append(Text.literal("o").withColor(0xcc2828))
				.append(Text.literal("u").withColor(0xc82626))
				.append(Text.literal("S").withColor(0xc42424))
				.append(Text.literal("t").withColor(0xc02222))
				.append(Text.literal("o").withColor(0xbc2020))
				.append(Text.literal("n").withColor(0xb81e1e))
				.append(Text.literal("k").withColor(0xb41c1c))
				.append(Text.literal("s").withColor(0xb01a1a)) // 13
				.append(Text.literal("] ").formatted(Formatting.DARK_GRAY));
	}
}
