package fr.siroz.cariboustonks.feature.stonks.tooltips;

public enum TooltipPriceDisplayType {
    FULL,
    SHORT,
    ALL,
    ;

    @Override
    public String toString() {
        return name();
    }
}
