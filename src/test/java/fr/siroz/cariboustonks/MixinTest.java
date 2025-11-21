package fr.siroz.cariboustonks;

import net.minecraft.server.Bootstrap;
import net.minecraft.SharedConstants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.transformer.IMixinTransformer;

public class MixinTest {

    @BeforeAll
    public static void setupEnvironment() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    public void auditMixins() {
        Assertions.assertInstanceOf(IMixinTransformer.class, MixinEnvironment.getCurrentEnvironment().getActiveTransformer());
        MixinEnvironment.getCurrentEnvironment().audit();
    }
}
