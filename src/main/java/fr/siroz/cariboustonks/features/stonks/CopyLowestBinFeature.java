package fr.siroz.cariboustonks.features.stonks;

import fr.siroz.cariboustonks.core.component.ContainerOverlayComponent;
import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.core.module.gui.MatcherTrait;
import fr.siroz.cariboustonks.core.skyblock.item.SkyBlockItems;
import fr.siroz.cariboustonks.platform.context.ClientContext;
import fr.siroz.cariboustonks.util.ItemLookupKey;
import java.util.Collections;
import java.util.Optional;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.Nullable;

public class CopyLowestBinFeature extends Feature {
	private static final Pattern CREATE_BIN_AUCTION_PATTERN = Pattern.compile("^Create BIN Auction$");
	private static final int AUCTION_ITEM_SLOT = 13;

	public CopyLowestBinFeature() {
		this.addComponent(ContainerOverlayComponent.class, ContainerOverlayComponent.builder()
				.trait(MatcherTrait.pattern(CREATE_BIN_AUCTION_PATTERN))
				.content(slots -> {
					String copyLowestBin = getCopyLowestBinValue(slots.getOrDefault(AUCTION_ITEM_SLOT, null));
					if (copyLowestBin != null) ClientContext.setToClipboard(copyLowestBin);
					return Collections.emptyList();
				})
				.build());
	}

	@Override
	public boolean isEnabled() {
		return this.skyBlock().location().onSkyBlock() && this.config().general.stonks.autoCopyLowestBinPrice;
	}

	private @Nullable String getCopyLowestBinValue(@Nullable ItemStack itemStack) {
		if (itemStack == null || itemStack.isEmpty() || itemStack.is(Items.STONE_BUTTON)) return null;

		ItemLookupKey key = ItemLookupKey.ofNeuId(SkyBlockItems.getNeuId(itemStack));
		if (!this.skyBlock().getGenericDataSource().hasLowestBin(key)) return null;

		Optional<Double> lowestBin = this.skyBlock().getGenericDataSource().getLowestBin(key);
		if (lowestBin.isEmpty() || lowestBin.get() <= 1) return null;

		int price = (int) (lowestBin.get() - 1);
		return price <= 0 ? null : String.valueOf(price);
	}
}
