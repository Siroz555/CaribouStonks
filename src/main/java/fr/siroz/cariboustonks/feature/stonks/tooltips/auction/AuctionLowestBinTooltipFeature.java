package fr.siroz.cariboustonks.feature.stonks.tooltips.auction;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.core.data.generic.GenericDataSource;
import fr.siroz.cariboustonks.util.ItemLookupKey;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.feature.Feature;
import fr.siroz.cariboustonks.feature.stonks.tooltips.TooltipPriceDisplayType;
import fr.siroz.cariboustonks.manager.container.ContainerMatcherTrait;
import fr.siroz.cariboustonks.manager.container.tooltip.ContainerTooltipAppender;
import fr.siroz.cariboustonks.util.NotEnoughUpdatesUtils;
import fr.siroz.cariboustonks.util.StonksUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class AuctionLowestBinTooltipFeature extends Feature implements ContainerMatcherTrait, ContainerTooltipAppender {

    private final GenericDataSource genericDataSource;
    private final int priority;

    public AuctionLowestBinTooltipFeature(int priority) {
        this.priority = priority;
        this.genericDataSource = CaribouStonks.core().getGenericDataSource();
    }

    @Override
    public boolean isEnabled() {
        return SkyBlockAPI.isOnSkyBlock() && ConfigManager.getConfig().general.stonks.auctionTooltipPrice;
    }

	@Override
	public @Nullable Pattern getTitlePattern() {
		return null;
	}

	@Override
    public void appendToTooltip(@Nullable Slot focusedSlot, @NotNull ItemStack item, @NotNull List<Text> lines) {
        if (genericDataSource.isLowestBinsInUpdate()) {
            lines.add(Text.literal("Auction is currently updating...").formatted(Formatting.RED));
            return;
        }

        String neuId = NotEnoughUpdatesUtils.getNeuId(item);
        ItemLookupKey key = ItemLookupKey.ofNeuId(neuId);
        if (genericDataSource.hasLowestBin(key)) {

            Optional<Double> lowestBin = genericDataSource.getLowestBin(key);
            if (lowestBin.isEmpty() || lowestBin.get() <= 0) {
                lines.add(Text.literal("Auction API error.").formatted(Formatting.RED));
                return;
            }

			int count = item.getCount();
			double price = lowestBin.get();
			if (StonksUtils.hasShiftDown() && count > 1) price *= count;

            TooltipPriceDisplayType displayType = ConfigManager.getConfig().general.stonks.auctionTooltipPriceDisplayType;
            switch (displayType) {
                case ALL -> {
                    String lowestBinPriceDisplay = StonksUtils.INTEGER_NUMBERS.format(price);
                    String lowestBinPriceShortDisplay = StonksUtils.SHORT_FLOAT_NUMBERS.format(price);
                    lines.add(Text.literal("Auction Lowest BIN: ").formatted(Formatting.YELLOW)
                            .append(Text.literal(lowestBinPriceDisplay + " Coins").formatted(Formatting.GOLD))
                            .append(Text.literal(" (").formatted(Formatting.GRAY))
                            .append(Text.literal(lowestBinPriceShortDisplay).formatted(Formatting.GOLD))
                            .append(Text.literal(")").formatted(Formatting.GRAY)));
                }
                case SHORT -> {
                    String lowestBinPriceShortDisplay = StonksUtils.SHORT_FLOAT_NUMBERS.format(price);
                    lines.add(Text.literal("Auction Lowest BIN: ").formatted(Formatting.YELLOW)
                            .append(Text.literal(lowestBinPriceShortDisplay + " Coins").formatted(Formatting.GOLD)));
                }
                case FULL -> {
                    String lowestBinPriceDisplay = StonksUtils.INTEGER_NUMBERS.format(price);
                    lines.add(Text.literal("Auction Lowest BIN: ").formatted(Formatting.YELLOW)
                            .append(Text.literal(lowestBinPriceDisplay + " Coins").formatted(Formatting.GOLD)));
                }
                case null, default -> {
                }
            }

			if (!StonksUtils.hasShiftDown() && count > 1) {
				lines.add(Text.literal("[Press SHIFT for x" + count + "]").formatted(Formatting.DARK_GRAY));
			}
        }
    }

    @Override
    public int getPriority() {
        return priority;
    }
}
