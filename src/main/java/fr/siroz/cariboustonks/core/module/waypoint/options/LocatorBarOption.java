package fr.siroz.cariboustonks.core.module.waypoint.options;

import fr.siroz.cariboustonks.util.colors.Color;
import fr.siroz.cariboustonks.util.colors.Colors;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@Deprecated
public final class LocatorBarOption {

    private final boolean enabled;
    private final Color color;
    private final boolean showInBossBar;
    private final int maxDistanceToNextIcon;

    public LocatorBarOption() {
        this(false, Colors.RED, false, 0);
    }

    public LocatorBarOption(
            boolean enabled,
            @NotNull Color color,
            boolean showInBossBar,
            int maxDistanceToNextIcon
    ) {
        this.enabled = enabled;
        this.color = color;
        this.showInBossBar = showInBossBar;
        this.maxDistanceToNextIcon = maxDistanceToNextIcon;
    }

    @Contract(value = " -> new", pure = true)
    public static @NotNull Builder builder() {
        return new Builder();
    }

    public static class Builder {

        protected Color color = Colors.RED;
        protected boolean showInBossBar = false;
        protected int maxDistanceToNextIcon = 0;
    }
}
