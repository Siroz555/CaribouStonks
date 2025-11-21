package fr.siroz.cariboustonks.util.render;

import net.minecraft.resources.Identifier;

public enum Texture {
    NETHERITE_SWORD(Identifier.withDefaultNamespace("textures/item/netherite_sword.png")),
    ;

    private final Identifier identifier;

    Texture(Identifier identifier) {
        this.identifier = identifier;
    }

    public Identifier getIdentifier() {
        return identifier;
    }
}
