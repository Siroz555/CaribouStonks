package fr.siroz.cariboustonks.core.module.gui;

import fr.siroz.cariboustonks.core.module.color.Color;
import fr.siroz.cariboustonks.core.module.color.Colors;
import org.jspecify.annotations.NonNull;

public record ColorHighlight(
		int slot,
		@NonNull Color color
) {

    private static final Color RED = Colors.RED;
    private static final Color YELLOW = Colors.YELLOW;
    private static final Color GREEN = Colors.GREEN;

    public static @NonNull ColorHighlight red(int slot) {
        return new ColorHighlight(slot, RED);
    }

    public static @NonNull ColorHighlight red(int slot, float alpha) {
        return new ColorHighlight(slot, RED.withAlpha(alpha));
    }

    public static @NonNull ColorHighlight yellow(int slot) {
        return new ColorHighlight(slot, YELLOW);
    }

    public static @NonNull ColorHighlight yellow(int slot, float alpha) {
        return new ColorHighlight(slot, YELLOW.withAlpha(alpha));
    }

    public static @NonNull ColorHighlight green(int slot) {
        return new ColorHighlight(slot, GREEN);
    }

    public static @NonNull ColorHighlight green(int slot, float alpha) {
        return new ColorHighlight(slot, GREEN.withAlpha(alpha));
    }
}
