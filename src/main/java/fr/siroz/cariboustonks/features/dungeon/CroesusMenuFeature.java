package fr.siroz.cariboustonks.features.dungeon;

import fr.siroz.cariboustonks.core.component.ContainerMatcherComponent;
import fr.siroz.cariboustonks.core.component.ContainerOverlayComponent;
import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.core.module.color.Color;
import fr.siroz.cariboustonks.core.module.gui.ColorHighlight;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.util.ItemUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.regex.Pattern;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class CroesusMenuFeature extends Feature {

	private static final Pattern CROESUS_PATTERN = Pattern.compile("^Croesus$");

	private final BooleanSupplier configOpenedChest =
			() -> this.config().instance.croesus.mainMenuOpenedChest;

	private final BooleanSupplier configNoMoreChest =
			() ->  this.config().instance.croesus.mainMenuNoMoreChest;

	private final BooleanSupplier configKismetAvailable =
			() ->  this.config().instance.croesus.mainMenuKismetAvailable;

	public CroesusMenuFeature() {
		this.addComponent(ContainerMatcherComponent.class, ContainerMatcherComponent.builder()
				.titlePattern(CROESUS_PATTERN)
				.build());
		this.addComponent(ContainerOverlayComponent.class, ContainerOverlayComponent.builder()
				.content(this::contentAnalyzer)
				.build());
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock()
				&& (configOpenedChest.getAsBoolean() || configNoMoreChest.getAsBoolean() || configKismetAvailable.getAsBoolean());
	}

	@NonNull
	private List<ColorHighlight> contentAnalyzer(@NonNull Int2ObjectMap<ItemStack> slots) {
		List<ColorHighlight> highlights = new ArrayList<>();
		for (Int2ObjectMap.Entry<ItemStack> entry : slots.int2ObjectEntrySet()) {
			List<Component> lore = ItemUtils.getLore(entry.getValue());
			if (lore.isEmpty()) {
				continue;
			}

			boolean notOpenedYet = false;
			boolean opened = false;
			boolean noMoreChest = false;
			boolean kismetAvailable = false;
			for (Component text : lore) {
				String line = text.getString();
				if (line.contains("No chests opened yet!")) notOpenedYet = true;
				if (line.contains("Opened Chest:")) opened = true;
				if (line.contains("No more chests to open!")) noMoreChest = true; // SIROZ-NOTE: bah c'est plus présent?
				if (line.contains("Kismet Feather")) {
					List<Component> kismetText = text.getSiblings();
					for (Component kismetLine : kismetText) {
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
		if (kismetAvailable && notOpenedYet && configKismetAvailable.getAsBoolean()) {
			highlight = new ColorHighlight(entry.getIntKey(), Color.fromAwtColor(this.config().instance.croesus.mainMenuKismetAvailableColor));
		}

		if (highlight == null && opened && !kismetAvailable && configOpenedChest.getAsBoolean()) {
			highlight = new ColorHighlight(entry.getIntKey(), Color.fromAwtColor(this.config().instance.croesus.mainMenuOpenedChestColor));
		}

		if (highlight == null && noMoreChest && !kismetAvailable && configNoMoreChest.getAsBoolean()) {
			highlight = new ColorHighlight(entry.getIntKey(), Color.fromAwtColor(this.config().instance.croesus.mainMenuNoMoreChestColor));
		}

		return highlight;
	}
}
