package fr.siroz.cariboustonks.manager.waypoint.options;

import net.minecraft.text.Text;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.Optional;

public final class TextOption {

    private Text text;
    private final boolean withDistance;
    private final int offsetY;
    private final boolean throughBlocks;

    public TextOption() {
        this(null, false, -1, true);
    }

    private TextOption(Text text, boolean withDistance, int offsetY, boolean throughBlocks) {
        this.text = text;
        this.withDistance = withDistance;
        this.offsetY = offsetY;
        this.throughBlocks = throughBlocks;
    }

    /**
     * Récupère le {@link Text} du Waypoint.
     *
     * @return le {@link Text} ou {@code null}
     */
    @Contract(pure = true)
    public @NotNull Optional<Text> getText() {
        return Optional.ofNullable(text);
    }

    /**
     * Changer le {@link Text} du Waypoint.
     *
     * @param text le {@link Text} ou {@code null} pour retirer le Text actuel
     */
    public void updateText(@Nullable Text text) {
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

        protected Text text = null;
        protected boolean withDistance = false;
        protected int offsetY = -1;
        protected boolean throughBlocks = true;

        public TextOption.Builder withText(@Nullable Text text) {
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
