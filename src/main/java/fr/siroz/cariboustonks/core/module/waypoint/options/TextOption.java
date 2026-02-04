package fr.siroz.cariboustonks.core.module.waypoint.options;

import java.util.Optional;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

public final class TextOption {

    private Component text;
    private final boolean withDistance;
    private final int offsetY;
    private final boolean throughBlocks;

    public TextOption() {
        this(null, false, -1, true);
    }

    private TextOption(Component text, boolean withDistance, int offsetY, boolean throughBlocks) {
        this.text = text;
        this.withDistance = withDistance;
        this.offsetY = offsetY;
        this.throughBlocks = throughBlocks;
    }

    /**
     * Récupère le {@link Component} du Waypoint.
     *
     * @return le {@link Component} ou {@code null}
     */
    @Contract(pure = true)
    public @NotNull Optional<Component> getText() {
        return Optional.ofNullable(text);
    }

    /**
     * Changer le {@link Component} du Waypoint.
     *
     * @param text le {@link Component} ou {@code null} pour retirer le Text actuel
     */
    public void updateText(@Nullable Component text) {
        this.text = text;
    }

    public boolean isWithDistance() {
        return withDistance;
    }

    public int getOffsetY() {
        return offsetY;
    }

    public boolean isThroughBlocks() {
        return throughBlocks;
    }

    @Contract(" -> new")
    public static @NotNull TextOption.Builder builder() {
        return new TextOption.Builder();
    }

    public static class Builder {

        protected Component text = null;
        protected boolean withDistance = false;
        protected int offsetY = -1;
        protected boolean throughBlocks = true;

        public TextOption.Builder withText(@Nullable Component text) {
            this.text = text;
            return this;
        }

        public TextOption.Builder withDistance(boolean withDistance) {
            this.withDistance = withDistance;
            return this;
        }

        public TextOption.Builder withOffsetY(@Range(from = 1, to = 100) int offsetY) {
            this.offsetY = offsetY;
            return this;
        }

        public TextOption.Builder withThroughBlocks(boolean throughBlocks) {
            this.throughBlocks = throughBlocks;
            return this;
        }

        public TextOption build() {
            return new TextOption(text, withDistance, offsetY, throughBlocks);
        }
    }
}
