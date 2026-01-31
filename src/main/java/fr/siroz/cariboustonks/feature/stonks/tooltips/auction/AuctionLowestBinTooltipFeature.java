package fr.siroz.cariboustonks.feature.stonks.tooltips.auction;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.skyblock.data.generic.GenericDataSource;
import fr.siroz.cariboustonks.util.Client;
import fr.siroz.cariboustonks.util.ItemLookupKey;
import fr.siroz.cariboustonks.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.feature.Feature;
import fr.siroz.cariboustonks.feature.stonks.tooltips.TooltipPriceDisplayType;
import fr.siroz.cariboustonks.system.container.ContainerMatcherTrait;
import fr.siroz.cariboustonks.system.container.tooltip.ContainerTooltipAppender;
import fr.siroz.cariboustonks.util.NotEnoughUpdatesUtils;
import fr.siroz.cariboustonks.util.StonksUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.Slot;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
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
        this.genericDataSource = CaribouStonks.skyBlock().getGenericDataSource();
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
    public void appendToTooltip(@Nullable Slot focusedSlot, @NotNull ItemStack item, @NotNull List<Component> lines) {
        if (genericDataSource.isLowestBinsInUpdate()) {
            lines.add(Component.literal("Auction is currently updating...").withStyle(ChatFormatting.RED));
            return;
        }

        String neuId = NotEnoughUpdatesUtils.getNeuId(item);
        ItemLookupKey key = ItemLookupKey.ofNeuId(neuId);
        if (genericDataSource.hasLowestBin(key)) {

            Optional<Double> lowestBin = genericDataSource.getLowestBin(key);
            if (lowestBin.isEmpty() || lowestBin.get() <= 0) {
                lines.add(Component.literal("Auction API error.").withStyle(ChatFormatting.RED));
                return;
            }

			int count = item.getCount();
			double price = lowestBin.get();
			if (Client.hasShiftDown() && count > 1) price *= count;

            TooltipPriceDisplayType displayType = ConfigManager.getConfig().general.stonks.auctionTooltipPriceDisplayType;
            switch (displayType) {
                case ALL -> {
                    String lowestBinPriceDisplay = StonksUtils.INTEGER_NUMBERS.format(price);
                    String lowestBinPriceShortDisplay = StonksUtils.SHORT_FLOAT_NUMBERS.format(price);
                    lines.add(Component.literal("Auction Lowest BIN: ").withStyle(ChatFormatting.YELLOW)
                            .append(Component.literal(lowestBinPriceDisplay + " Coins").withStyle(ChatFormatting.GOLD))
                            .append(Component.literal(" (").withStyle(ChatFormatting.GRAY))
                            .append(Component.literal(lowestBinPriceShortDisplay).withStyle(ChatFormatting.GOLD))
                            .append(Component.literal(")").withStyle(ChatFormatting.GRAY)));
                }
                case SHORT -> {
                    String lowestBinPriceShortDisplay = StonksUtils.SHORT_FLOAT_NUMBERS.format(price);
                    lines.add(Component.literal("Auction Lowest BIN: ").withStyle(ChatFormatting.YELLOW)
                            .append(Component.literal(lowestBinPriceShortDisplay + " Coins").withStyle(ChatFormatting.GOLD)));
                }
                case FULL -> {
                    String lowestBinPriceDisplay = StonksUtils.INTEGER_NUMBERS.format(price);
                    lines.add(Component.literal("Auction Lowest BIN: ").withStyle(ChatFormatting.YELLOW)
                            .append(Component.literal(lowestBinPriceDisplay + " Coins").withStyle(ChatFormatting.GOLD)));
                }
                case null, default -> {
                }
            }

			if (!Client.hasShiftDown() && count > 1) {
				lines.add(Component.literal("[Press SHIFT for x" + count + "]").withStyle(ChatFormatting.DARK_GRAY));
			}
        }
    }

    @Override
    public int getPriority() {
        return priority;
    }
}
