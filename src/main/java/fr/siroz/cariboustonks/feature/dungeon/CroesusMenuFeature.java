package fr.siroz.cariboustonks.feature.dungeon;

import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.feature.Feature;
import fr.siroz.cariboustonks.manager.container.ContainerMatcherTrait;
import fr.siroz.cariboustonks.manager.container.overlay.ContainerOverlay;
import fr.siroz.cariboustonks.util.ItemUtils;
import fr.siroz.cariboustonks.util.colors.Color;
import fr.siroz.cariboustonks.util.render.gui.ColorHighlight;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.regex.Pattern;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CroesusMenuFeature extends Feature implements ContainerMatcherTrait, ContainerOverlay {

	private static final Pattern CROESUS_PATTERN = Pattern.compile("^Croesus$");

	private final BooleanSupplier openedChestConfig =
			() -> ConfigManager.getConfig().dungeon.croesus.mainMenuOpenedChest;
	private final BooleanSupplier noMoreChestConfig =
			() -> ConfigManager.getConfig().dungeon.croesus.mainMenuNoMoreChest;
	private final BooleanSupplier kismetAvailableConfig =
			() -> ConfigManager.getConfig().dungeon.croesus.mainMenuKismetAvailable;

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock() && (openedChestConfig.getAsBoolean() || noMoreChestConfig.getAsBoolean() || kismetAvailableConfig.getAsBoolean());
	}

	@Override
	public @Nullable Pattern getTitlePattern() {
		return CROESUS_PATTERN;
	}

	@Override
	public @NotNull List<ColorHighlight> content(@NotNull Int2ObjectMap<ItemStack> slots) {
		List<ColorHighlight> highlights = new ArrayList<>();
		for (Int2ObjectMap.Entry<ItemStack> entry : slots.int2ObjectEntrySet()) {
			List<Text> lore = ItemUtils.getLore(entry.getValue());
			if (lore.isEmpty()) {
				continue;
			}

			boolean notOpenedYet = false;
			boolean opened = false;
			boolean noMoreChest = false;
			boolean kismetAvailable = false;
			for (Text text : lore) {
				String line = text.getString();
				if (line.contains("No chests opened yet!")) notOpenedYet = true;
				if (line.contains("Opened Chest:")) opened = true;
				if (line.contains("No more chests to open!")) noMoreChest = true;
				if (line.contains("Kismet Feather")) {
					List<Text> kismetText = text.getSiblings();
					for (Text kismetLine : kismetText) {
						String kismetLineString = kismetLine.getString();
						if (kismetLineString.contains("Kismet Feather") && !kismetLine.getStyle().isStrikethrough()) {
							kismetAvailable = true;
						}
					}
				}
			}

			ColorHighlight highlight = getHighlight(notOpenedYet, opened, noMoreChest, kismetAvailable, entry);
			if (highlight != null) {
				highlights.add(highlight);
			}

		}
		return highlights;
	}

	@Nullable
	private ColorHighlight getHighlight(boolean notOpenedYet, boolean opened, boolean noMoreChest, boolean kismetAvailable, Int2ObjectMap.Entry<ItemStack> entry) {
		// Priorité des couleurs : Kismet > Opened > NoMoreChest
		// Pas de "else-if", car c'est pour mieux contrôler chaque option dans la config, d'où le "highlight == null"
		ColorHighlight highlight = null;
		if (kismetAvailable && !notOpenedYet && kismetAvailableConfig.getAsBoolean()) {
			highlight = new ColorHighlight(entry.getIntKey(), kismetAvailableColor());
		}

		if (highlight == null && opened && !kismetAvailable && openedChestConfig.getAsBoolean()) {
			highlight = new ColorHighlight(entry.getIntKey(), openedChestColor());
		}

		if (highlight == null && noMoreChest && !kismetAvailable && noMoreChestConfig.getAsBoolean()) {
			highlight = new ColorHighlight(entry.getIntKey(), noMoreChestColor());
		}

		return highlight;
	}

	@Contract(" -> new")
	private @NotNull Color openedChestColor() {
		return Color.fromAwtColor(ConfigManager.getConfig().dungeon.croesus.mainMenuOpenedChestColor);
	}

	@Contract(" -> new")
	private @NotNull Color noMoreChestColor() {
		return Color.fromAwtColor(ConfigManager.getConfig().dungeon.croesus.mainMenuNoMoreChestColor);
	}

	@Contract(" -> new")
	private @NotNull Color kismetAvailableColor() {
		return Color.fromAwtColor(ConfigManager.getConfig().dungeon.croesus.mainMenuKismetAvailableColor);
	}
}
