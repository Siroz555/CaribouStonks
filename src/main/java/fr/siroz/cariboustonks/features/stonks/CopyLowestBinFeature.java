package fr.siroz.cariboustonks.features.stonks;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.core.component.ContainerOverlayComponent;
import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.core.module.gui.MatcherTrait;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.core.skyblock.data.generic.GenericDataSource;
import fr.siroz.cariboustonks.platform.context.ClientContext;
import fr.siroz.cariboustonks.util.ItemLookupKey;
import fr.siroz.cariboustonks.util.NotEnoughUpdatesUtils;
import java.util.Collections;
import java.util.Optional;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.Nullable;

public class CopyLowestBinFeature extends Feature {
	private static final Pattern CREATE_BIN_AUCTION_PATTERN = Pattern.compile("^Create BIN Auction$");
	private static final int AUCTION_ITEM_SLOT = 13;

	private final GenericDataSource genericDataSource;

	public CopyLowestBinFeature() {
		this.genericDataSource = CaribouStonks.skyBlock().getGenericDataSource();

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
		return SkyBlockAPI.isOnSkyBlock() && this.config().general.stonks.autoCopyLowestBinPrice;
	}

	private @Nullable String getCopyLowestBinValue(@Nullable ItemStack itemStack) {
		if (itemStack == null || itemStack.isEmpty() || itemStack.is(Items.STONE_BUTTON)) return null;

		ItemLookupKey key = ItemLookupKey.ofNeuId(NotEnoughUpdatesUtils.getNeuId(itemStack));
		if (!genericDataSource.hasLowestBin(key)) return null;

		Optional<Double> lowestBin = genericDataSource.getLowestBin(key);
		if (lowestBin.isEmpty() || lowestBin.get() <= 1) return null;

		int price = (int) (lowestBin.get() - 1);
		return price <= 0 ? null : String.valueOf(price);
	}
}
