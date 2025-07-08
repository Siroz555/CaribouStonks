package fr.siroz.cariboustonks.feature.diana;

public enum TargetBurrowLogic {
    NORMAL,
    NORMAL_OR_GUESS,
    NORMAL_OR_CLOSEST_GUESS,
    ;

    @Override
    public String toString() {
        return name();
    }
}
