package fr.siroz.cariboustonks.core.mod.integration;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.util.DeveloperTools;
import mezz.jei.common.Internal;
import net.fabricmc.loader.api.FabricLoader;
import org.jspecify.annotations.Nullable;

public final class JustEnoughItemsIntegration {

	private JustEnoughItemsIntegration() {
	}

	public static boolean isModLoaded() {
		return FabricLoader.getInstance().isModLoaded("jei");
	}

	@Nullable
	public static String getSearchBarText() {
		try {
			return Internal.getJeiRuntime().getIngredientFilter().getFilterText();
		} catch (Exception ex) {
			if (DeveloperTools.isInDevelopment()) {
				CaribouStonks.LOGGER.error("[JustEnoughItemsIntegration] getSearchBarText failed", ex);
			}
			return null;
		}
	}

	public static boolean isSearchBarAtCenter() {
		try {
			return Internal.getJeiClientConfigs().getClientConfig().isCenterSearchBarEnabled();
		} catch (Exception ex) {
			if (DeveloperTools.isInDevelopment()) {
				CaribouStonks.LOGGER.error("[JustEnoughItemsIntegration] isSearchBarAtCenter failed", ex);
			}
			return false;
		}
	}
}
