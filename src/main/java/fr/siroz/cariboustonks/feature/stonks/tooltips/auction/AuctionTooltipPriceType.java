package fr.siroz.cariboustonks.feature.stonks.tooltips.auction;

public enum AuctionTooltipPriceType {
    LOWEST_BIN,
    AVERAGE_3_DAYS,
    LOWEST_BIN_AND_AVERAGE,
    ;

    @Override
    public String toString() {
        return name();
    }
}
