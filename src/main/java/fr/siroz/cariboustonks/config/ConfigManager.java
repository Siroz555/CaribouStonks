package fr.siroz.cariboustonks.config;

import com.google.gson.FieldNamingPolicy;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.config.categories.DungeonsCategory;
import fr.siroz.cariboustonks.config.categories.GeneralCategory;
import fr.siroz.cariboustonks.config.categories.MiscCategory;
import fr.siroz.cariboustonks.config.categories.SkillsCategory;
import fr.siroz.cariboustonks.config.categories.SlayerCategory;
import fr.siroz.cariboustonks.config.categories.UIAndVisualsCategory;
import fr.siroz.cariboustonks.config.categories.VanillaCategory;
import fr.siroz.cariboustonks.core.service.json.adapters.CodecTypeAdapter;
import fr.siroz.cariboustonks.util.Client;
import fr.siroz.cariboustonks.util.colors.ColorUtils;
import java.awt.Color;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

/**
 * Manages the CaribouStonks Config.
 * The config uses YetAnotherConfigLib (<a href="https://github.com/isXander/YetAnotherConfigLib">GitHub YACL</a>)
 */
public final class ConfigManager {

	public static final int CONFIG_VERSION = 1;

	private static final Path CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("cariboustonks.json");
	private static final ConfigClassHandler<Config> HANDLER = ConfigClassHandler.createBuilder(Config.class)
			.serializer(handler -> new GsonConfigSerializer<>(handler, CONFIG_FILE, builder -> builder
					.setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
					.setPrettyPrinting()
					.serializeNulls()
					.registerTypeHierarchyAdapter(Identifier.class, new CodecTypeAdapter<>(Identifier.CODEC))
					.registerTypeHierarchyAdapter(Component.class, new CodecTypeAdapter<>(ComponentSerialization.CODEC))
					.registerTypeHierarchyAdapter(Style.class, new CodecTypeAdapter<>(Style.Serializer.CODEC))
					.registerTypeHierarchyAdapter(Color.class, new CodecTypeAdapter<>(ColorUtils.COLOR_CODEC))))
			.build();

	private ConfigManager() {
	}

	public static void loadConfig() {
		if (StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass() != CaribouStonks.class) {
			throw new RuntimeException("Noo noo and noo");
		}

		try {
			Files.createDirectories(CaribouStonks.CONFIG_DIR);
		} catch (IOException ex) {
			CaribouStonks.LOGGER.error("[ConfigManager] Unable to create the CaribouStonks folder", ex);
		}

		HANDLER.load();
	}

	/**
	 * Save the config
	 */
	public static void saveConfig() {
		HANDLER.save();
	}

	/**
	 * Returns the config class instance
	 *
	 * @return the {@link Config}
	 */
	public static Config getConfig() {
		return HANDLER.instance();
	}

	/**
	 * Create the config GUI
	 *
	 * @param parent the parent GUI
	 * @return the {@link Screen} config
	 */
	public static @Nullable Screen createConfigGUI(@Nullable Screen parent) {
		try {
			return YetAnotherConfigLib.create(HANDLER, (defaults, config, builder) -> {
				builder.title(Component.nullToEmpty("CaribouStonks"))
						.category(new GeneralCategory(defaults, config).create())
						.category(new UIAndVisualsCategory(defaults, config).create())
						.category(new SkillsCategory(defaults, config).create())
						.category(new SlayerCategory(defaults, config).create())
						.category(new DungeonsCategory(defaults, config).create())
						.category(new MiscCategory(defaults, config).create())
						.category(new VanillaCategory(defaults, config).create());
				return builder;
			}).generateScreen(parent);
		} catch (Throwable throwable) {
			CaribouStonks.LOGGER.error("[ConfigManager] Unable to generate CaribouStonks Screen for YACL Screen.", throwable);
			Client.sendErrorMessage("Unable to open the Configuration Menu! This may be due to a Snapshot or Pre-Release. If this is not the case, please report the issue!", true);
			return null;
		}
	}
}
