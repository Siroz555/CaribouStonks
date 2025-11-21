package fr.siroz.cariboustonks.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import fr.siroz.cariboustonks.config.ConfigManager;
import net.minecraft.client.tutorial.Tutorial;
import net.minecraft.client.tutorial.TutorialStepInstance;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Tutorial.class) // TutorialManager
public abstract class TutorialMixin {

    @WrapOperation(method = {"start", "setStep"}, at = @At(value = "FIELD", target = "Lnet/minecraft/client/tutorial/Tutorial;instance:Lnet/minecraft/client/tutorial/TutorialStepInstance;", opcode = Opcodes.PUTFIELD), require = 2)
    private void cariboustonks$hideTutorials(Tutorial manager, TutorialStepInstance stepHandler, Operation<Void> operation) {
        if (ConfigManager.getConfig().vanilla.hideTutorialsToast) {
            operation.call(manager, null);
        } else {
            operation.call(manager, stepHandler);
        }
    }
}
