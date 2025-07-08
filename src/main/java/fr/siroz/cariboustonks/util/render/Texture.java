package fr.siroz.cariboustonks.util.render;

import net.minecraft.util.Identifier;

public enum Texture {
    NETHERITE_SWORD(Identifier.ofVanilla("textures/item/netherite_sword.png")),
    ;

    private final Identifier identifier;

    Texture(Identifier identifier) {
        this.identifier = identifier;
    }

    public Identifier getIdentifier() {
        return identifier;
    }
}
