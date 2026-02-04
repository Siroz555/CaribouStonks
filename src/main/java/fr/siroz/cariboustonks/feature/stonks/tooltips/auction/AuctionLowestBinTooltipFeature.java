package fr.siroz.cariboustonks.feature.stonks.tooltips.auction;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.core.component.ContainerMatcherComponent;
import fr.siroz.cariboustonks.core.component.TooltipAppenderComponent;
import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.core.skyblock.data.generic.GenericDataSource;
import fr.siroz.cariboustonks.feature.stonks.tooltips.TooltipPriceDisplayType;
import fr.siroz.cariboustonks.util.Client;
import fr.siroz.cariboustonks.util.ItemLookupKey;
import fr.siroz.cariboustonks.util.NotEnoughUpdatesUtils;
import fr.siroz.cariboustonks.util.StonksUtils;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AuctionLowestBinTooltipFeature extends Feature {

    private final GenericDataSource genericDataSource;

    public AuctionLowestBinTooltipFeature(int priority) {
        this.genericDataSource = CaribouStonks.skyBlock().getGenericDataSource();

		this.addComponent(ContainerMatcherComponent.class, ContainerMatcherComponent.empty());
		this.addComponent(TooltipAppenderComponent.class, TooltipAppenderComponent.builder()
				.priority(priority)
				.appender(this::appendToTooltip)
				.build());
    }

    @Override
    public boolean isEnabled() {
        return SkyBlockAPI.isOnSkyBlock() && ConfigManager.getConfig().general.stonks.auctionTooltipPrice;
    }

    private void appendToTooltip(@Nullable Slot focusedSlot, @NotNull ItemStack item, @NotNull List<Component> lines) {
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
}
