package fr.siroz.cariboustonks.features.ui;

import fr.siroz.cariboustonks.core.component.ContainerOverlayComponent;
import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.core.module.gui.ColorHighlight;
import fr.siroz.cariboustonks.core.module.gui.MatcherTrait;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.util.ItemUtils;
import java.util.List;
import java.util.regex.Pattern;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class SelectedPetHighlightFeature extends Feature {

	private static final Pattern TITLE_PATTERN = Pattern.compile("^Pets.*");
	private static final Pattern SELECTED_PATTERN = Pattern.compile("Click to despawn!");

	public SelectedPetHighlightFeature() {
		this.addComponent(ContainerOverlayComponent.class, ContainerOverlayComponent.builder()
				.trait(MatcherTrait.pattern(TITLE_PATTERN))
				.content(slots -> slots.int2ObjectEntrySet().stream()
						.filter(itemStackEntry ->  isSelected(itemStackEntry.getValue()))
						.map(e -> ColorHighlight.green(e.getIntKey(), 0.25f))
						.toList())
				.build());
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock() && this.config().uiAndVisuals.highlightSelectedPet;
	}

	private boolean isSelected(ItemStack itemStack) {
		if (itemStack == null || !itemStack.is(Items.PLAYER_HEAD)) return false;

		List<Component> lore = ItemUtils.getLore(itemStack);
		if (lore.isEmpty()) return false;

		String concatenateLore = ItemUtils.concatenateLore(lore);
		return SELECTED_PATTERN.matcher(concatenateLore).find();
	}
}
