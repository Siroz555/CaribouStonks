package fr.siroz.cariboustonks.core.mod.integration;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.util.DeveloperTools;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import net.fabricmc.loader.api.FabricLoader;
import org.jspecify.annotations.Nullable;

@SuppressWarnings("checkstyle:linelength")
public final class JustEnoughItemsIntegration {
	private static final boolean MOD_LOADED = FabricLoader.getInstance().isModLoaded("jei");
	private static MethodHandle getJeiRuntime;
	private static MethodHandle getIngredientFilter;
	private static MethodHandle getFilterText;
	private static MethodHandle getJeiClientConfigs;
	private static MethodHandle getClientConfig;
	private static MethodHandle isCenterSearchBarEnabled;

	private static boolean initialized = false;
	private static boolean available = false;

	private JustEnoughItemsIntegration() {
	}

	public static boolean isModLoaded() {
		return MOD_LOADED;
	}

	private static void ensureInitialized() {
		if (initialized) return;
		initialized = true;
		if (!MOD_LOADED) return;

		try {
			MethodHandles.Lookup lookup = MethodHandles.publicLookup();

			Class<?> internalClass = Class.forName("mezz.jei.common.Internal");
			Class<?> jeiRuntimeClass = Class.forName("mezz.jei.api.runtime.IJeiRuntime");
			Class<?> ingredientFilterClass = Class.forName("mezz.jei.api.runtime.IIngredientFilter");
			Class<?> jeiClientConfigsClass = Class.forName("mezz.jei.api.runtime.IJeiClientConfigs");
			Class<?> clientConfigClass = Class.forName("mezz.jei.api.runtime.IClientConfig");

			getJeiRuntime = lookup.findStatic(internalClass, "getJeiRuntime", MethodType.methodType(jeiRuntimeClass));
			getIngredientFilter = lookup.findVirtual(jeiRuntimeClass, "getIngredientFilter", MethodType.methodType(ingredientFilterClass));
			getFilterText = lookup.findVirtual(ingredientFilterClass, "getFilterText", MethodType.methodType(String.class));

			getJeiClientConfigs = lookup.findStatic(internalClass, "getJeiClientConfigs", MethodType.methodType(jeiClientConfigsClass));
			getClientConfig = lookup.findVirtual(jeiClientConfigsClass, "getClientConfig", MethodType.methodType(clientConfigClass));
			isCenterSearchBarEnabled = lookup.findVirtual(clientConfigClass, "isCenterSearchBarEnabled", MethodType.methodType(boolean.class));

			available = true;
		} catch (Throwable t) {
			available = false;
			if (DeveloperTools.isInDevelopment()) {
				CaribouStonks.LOGGER.error("[JustEnoughItemsIntegration] Reflection init failed", t);
			}
		}
	}

	@Nullable
	public static String getSearchBarText() {
		ensureInitialized();
		if (!available) return null;

		try {
			// Internal.getJeiRuntime().getIngredientFilter().getFilterText();
			Object runtime = getJeiRuntime.invoke();
			Object filter = getIngredientFilter.invoke(runtime);
			return (String) getFilterText.invoke(filter);
		} catch (Throwable t) {
			if (DeveloperTools.isInDevelopment()) {
				CaribouStonks.LOGGER.error("[JustEnoughItemsIntegration] getSearchBarText failed", t);
			}
			return null;
		}
	}

	public static boolean isSearchBarAtCenter() {
		ensureInitialized();
		if (!available) return false;

		try {
			// Internal.getJeiClientConfigs().getClientConfig().isCenterSearchBarEnabled();
			Object configs = getJeiClientConfigs.invoke();
			Object config = getClientConfig.invoke(configs);
			return (boolean) isCenterSearchBarEnabled.invoke(config);
		} catch (Throwable t) {
			if (DeveloperTools.isInDevelopment()) {
				CaribouStonks.LOGGER.error("[JustEnoughItemsIntegration] isSearchBarAtCenter failed", t);
			}
			return false;
		}
	}
}
