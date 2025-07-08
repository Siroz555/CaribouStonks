package fr.siroz.cariboustonks.feature.diana;

import fr.siroz.cariboustonks.util.colors.Color;
import fr.siroz.cariboustonks.util.colors.Colors;
import net.minecraft.util.math.BlockPos;

class GriffinBurrow {

    private final BlockPos pos;
    private int type;
    private boolean found;
    public int critParticle;
    public int enchantParticle;
    public int critMagicParticle;

    public GriffinBurrow(BlockPos pos) {
        this(pos, -1, false, 0, 0, 0);
    }

    public GriffinBurrow(BlockPos pos, int type, boolean found, int critParticle, int enchantParticle, int critMagicParticle) {
        this.pos = pos;
        this.type = type;
        this.found = found;
        this.critParticle = critParticle;
        this.enchantParticle = enchantParticle;
        this.critMagicParticle = critMagicParticle;
    }

    public BlockPos getPos() {
        return pos;
    }

    public boolean isFound() {
        return found;
    }

    public int getType() {
        return type;
    }

    public BurrowType getBurrowType() {
        return switch (this.type) {
            case 0 -> BurrowType.START;
            case 1 -> BurrowType.MOB;
            case 2 -> BurrowType.TREASURE;
            default -> BurrowType.UNKNOWN;
        };
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setFound(boolean found) {
        this.found = found;
    }

    public enum BurrowType {
        START("Start", Colors.GREEN),
        MOB("Mob", Colors.RED),
        TREASURE("Treasure", Colors.ORANGE),
        UNKNOWN("Unknown", Colors.WHITE),
        ;

        private final String text;
        private final Color color;

        BurrowType(String text, Color color) {
            this.text = text;
            this.color = color;
        }

        public String getText() {
            return text;
        }

        public Color getColor() {
            return color;
        }
    }
}
