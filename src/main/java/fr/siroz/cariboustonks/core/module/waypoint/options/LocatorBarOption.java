package fr.siroz.cariboustonks.core.module.waypoint.options;

import fr.siroz.cariboustonks.core.module.color.Color;
import fr.siroz.cariboustonks.core.module.color.Colors;
import org.jspecify.annotations.NonNull;

@SuppressWarnings("ALL")
@Deprecated
public final class LocatorBarOption { // SIROZ-NOTE: Il faudra l'implémenté un jour nan siroz ?

    private final boolean enabled;
    private final Color color;
    private final boolean showInBossBar;
    private final int maxDistanceToNextIcon;

    public LocatorBarOption() {
        this(false, Colors.RED, false, 0);
    }

    public LocatorBarOption(
            boolean enabled,
            @NonNull Color color,
            boolean showInBossBar,
            int maxDistanceToNextIcon
    ) {
        this.enabled = enabled;
        this.color = color;
        this.showInBossBar = showInBossBar;
        this.maxDistanceToNextIcon = maxDistanceToNextIcon;
    }

	@NonNull
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        protected Color color = Colors.RED;
        protected boolean showInBossBar = false;
        protected int maxDistanceToNextIcon = 0;
    }
}
