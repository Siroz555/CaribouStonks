package fr.siroz.cariboustonks.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.ConfigField;
import dev.isxander.yacl3.config.v2.api.ConfigSerializer;
import dev.isxander.yacl3.config.v2.api.FieldAccess;
import fr.siroz.cariboustonks.mixin.accessors.ConfigClassHandlerImplAccessor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.function.UnaryOperator;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

/**
 * Credits to AzureAaron in the Skyblocker project
 * (<a href="https://github.com/SkyblockerMod/Skyblocker">GitHub Skyblocker</a>) for this patch.
 * <p>
 * Apparently, there is a "config wipe" from a more recent version in the YetAnotherConfigLib.
 * This patch provides a clean Gson that does not depend on YetAnotherConfigLib config parser.
 * <p>
 * Commit: a3cb1c28
 * <p>YetAnotherConfigLib version before the patch: 3.6.6+1.21.5
 * <p>YetAnotherConfigLib version after the patch: 3.7.1+1.21.5
 */
final class GsonConfigSerializer<T> extends ConfigSerializer<T> {

	private static final Logger LOGGER = LogUtils.getLogger();

	private final Path path;
	private final Gson gson;

	GsonConfigSerializer(
			@NonNull ConfigClassHandler<T> config,
			@NonNull Path path,
			@NonNull UnaryOperator<GsonBuilder> builder
	) {
		super(config);
		this.path = path;
		this.gson = builder.apply(new GsonBuilder()).create();
	}

	@Override
	public void save() {
		T instance = this.config.instance();

		try {
			String json = gson.toJson(instance);
			Files.createDirectories(path.getParent());
			Files.writeString(path, json, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);

			LOGGER.info("[CaribouStonks Config] Successfully saved to {}.", path);
		} catch (Exception ex) {
			LOGGER.error(LogUtils.FATAL_MARKER, "[CaribouStonks Config] Unable to save file to: {}", path, ex);
		}
	}

	@Override
	public LoadResult loadSafely(Map<ConfigField<?>, FieldAccess<?>> bufferAccessMap) {
		try {
			if (!Files.exists(path)) {
				((ConfigClassHandlerImplAccessor) this.config).setInstance(createNewConfigInstance());
				save();
				return LoadResult.NO_CHANGE;
			}
		} catch (Exception ex) {
			LOGGER.error("[CaribouStonks Config] Failed to create the default config file", ex);
		}

		try {
			String config = Files.readString(path);
			T instance = gson.fromJson(JsonParser.parseString(config), this.config.configClass());
			((ConfigClassHandlerImplAccessor) this.config).setInstance(instance);

			LOGGER.info("[CaribouStonks Config] Successfully loaded from {}", path);
		} catch (Exception ex) {
			LOGGER.error(LogUtils.FATAL_MARKER, "[CaribouStonks Config] Failed to load the config :/", ex);
		}

		return LoadResult.NO_CHANGE;
	}

	private @Nullable T createNewConfigInstance() {
		try {
			return this.config.configClass().getDeclaredConstructor().newInstance();
		} catch (Exception ex) {
			LOGGER.error("[CaribouStonks Config] Failed to create a new config instance", ex);
		}

		return null;
	}
}
