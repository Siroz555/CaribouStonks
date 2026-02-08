package fr.siroz.cariboustonks.features.stonks.tooltips.bazaar;

public enum BazaarTooltipPriceType {
    NORMAL,
    AVERAGE,
    ALL,
    ;

    @Override
    public String toString() {
        return name();
    }
}
