package fr.siroz.cariboustonks.core.mod.integration;

import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.gui.config.SearchFieldLocation;
import me.shedaniel.rei.api.client.gui.widgets.TextField;
import net.fabricmc.loader.api.FabricLoader;
import org.jspecify.annotations.Nullable;

public final class RoughlyEnoughItemsIntegration {

	private RoughlyEnoughItemsIntegration() {
	}

	public static boolean isModLoaded() {
		return FabricLoader.getInstance().isModLoaded("roughlyenoughitems");
	}

	@Nullable
	public static String getSearchBarText() {
		TextField searchBarField = REIRuntime.getInstance().getSearchTextField();
		if (searchBarField == null) {
			return null;
		}

		return searchBarField.getText();
	}

	public static boolean isSearchBarAtBottomSide() {
		return REIRuntime.getInstance().getContextualSearchFieldLocation() == SearchFieldLocation.BOTTOM_SIDE;
	}
}
