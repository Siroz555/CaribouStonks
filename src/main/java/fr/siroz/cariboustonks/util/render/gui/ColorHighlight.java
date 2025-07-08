package fr.siroz.cariboustonks.util.render.gui;

import fr.siroz.cariboustonks.util.colors.Color;
import fr.siroz.cariboustonks.util.colors.Colors;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public record ColorHighlight(int slot, Color color) {

    private static final Color RED = Colors.RED;
    private static final Color YELLOW = Colors.YELLOW;
    private static final Color GREEN = Colors.GREEN;

    @Contract("_ -> new")
    public static @NotNull ColorHighlight red(int slot) {
        return new ColorHighlight(slot, RED);
    }

    @Contract("_, _ -> new")
    public static @NotNull ColorHighlight red(int slot, float alpha) {
        return new ColorHighlight(slot, RED.withAlpha(alpha));
    }

    @Contract("_ -> new")
    public static @NotNull ColorHighlight yellow(int slot) {
        return new ColorHighlight(slot, YELLOW);
    }

    @Contract("_, _ -> new")
    public static @NotNull ColorHighlight yellow(int slot, float alpha) {
        return new ColorHighlight(slot, YELLOW.withAlpha(alpha));
    }

    @Contract("_ -> new")
    public static @NotNull ColorHighlight green(int slot) {
        return new ColorHighlight(slot, GREEN);
    }

    @Contract("_, _ -> new")
    public static @NotNull ColorHighlight green(int slot, float alpha) {
        return new ColorHighlight(slot, GREEN.withAlpha(alpha));
    }
}
