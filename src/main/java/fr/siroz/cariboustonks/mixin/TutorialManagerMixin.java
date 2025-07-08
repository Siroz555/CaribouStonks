package fr.siroz.cariboustonks.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import fr.siroz.cariboustonks.config.ConfigManager;
import net.minecraft.client.tutorial.TutorialManager;
import net.minecraft.client.tutorial.TutorialStepHandler;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(TutorialManager.class)
public abstract class TutorialManagerMixin {

    @WrapOperation(method = { "createHandler", "setStep" }, at = @At(value = "FIELD", target = "Lnet/minecraft/client/tutorial/TutorialManager;currentHandler:Lnet/minecraft/client/tutorial/TutorialStepHandler;", opcode = Opcodes.PUTFIELD), require = 2)
    private void cariboustonks$hideTutorials(TutorialManager manager, TutorialStepHandler stepHandler, Operation<Void> operation) {
        if (ConfigManager.getConfig().vanilla.hideTutorialsToast) {
            operation.call(manager, null);
        } else {
            operation.call(manager, stepHandler);
        }
    }
}
