package fr.siroz.cariboustonks.core.mod.integration;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.util.DeveloperTools;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import net.fabricmc.loader.api.FabricLoader;
import org.jspecify.annotations.Nullable;

@SuppressWarnings("checkstyle:linelength")
public final class RoughlyEnoughItemsIntegration {
	private static final boolean MOD_LOADED = FabricLoader.getInstance().isModLoaded("roughlyenoughitems");

	private static MethodHandle getInstance;
	private static MethodHandle getSearchTextField;
	private static MethodHandle getText;
	private static MethodHandle getContextualSearchFieldLocation;
	private static Object bottomSideConstant;

	private static boolean initialized = false;
	private static boolean available = false;

	private RoughlyEnoughItemsIntegration() {
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

			Class<?> reiRuntimeClass = Class.forName("me.shedaniel.rei.api.client.REIRuntime");
			Class<?> textFieldClass = Class.forName("me.shedaniel.rei.api.client.gui.widgets.TextField");
			Class<?> searchFieldLocationClass = Class.forName("me.shedaniel.rei.api.client.gui.config.SearchFieldLocation");

			getInstance = lookup.findStatic(reiRuntimeClass, "getInstance", MethodType.methodType(reiRuntimeClass));
			getSearchTextField = lookup.findVirtual(reiRuntimeClass, "getSearchTextField", MethodType.methodType(textFieldClass));
			getText = lookup.findVirtual(textFieldClass, "getText", MethodType.methodType(String.class));
			getContextualSearchFieldLocation = lookup.findVirtual(reiRuntimeClass, "getContextualSearchFieldLocation", MethodType.methodType(searchFieldLocationClass));

			MethodHandle bottomSideGetter = lookup.findStaticGetter(searchFieldLocationClass, "BOTTOM_SIDE", searchFieldLocationClass);
			bottomSideConstant = bottomSideGetter.invoke();

			available = true;
		} catch (Throwable t) {
			available = false;
			if (DeveloperTools.isInDevelopment()) {
				CaribouStonks.LOGGER.error("[RoughlyEnoughItemsIntegration] Reflection init failed", t);
			}
		}
	}

	@Nullable
	public static String getSearchBarText() {
		ensureInitialized();
		if (!available) return null;

		try {
			Object runtime = getInstance.invoke();
			Object searchBarField = getSearchTextField.invoke(runtime);
			if (searchBarField == null) return null;

			return (String) getText.invoke(searchBarField);
		} catch (Throwable t) {
			if (DeveloperTools.isInDevelopment()) {
				CaribouStonks.LOGGER.error("[RoughlyEnoughItemsIntegration] getSearchBarText failed", t);
			}
			return null;
		}
	}

	public static boolean isSearchBarAtBottomSide() {
		ensureInitialized();
		if (!available) return true;

		try {
			Object runtime = getInstance.invoke();
			Object location = getContextualSearchFieldLocation.invoke(runtime);
			return bottomSideConstant.equals(location);
		} catch (Throwable t) {
			if (DeveloperTools.isInDevelopment()) {
				CaribouStonks.LOGGER.error("[RoughlyEnoughItemsIntegration] isSearchBarAtBottomSide failed", t);
			}
			return true;
		}
	}
}
