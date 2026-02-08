package fr.siroz.cariboustonks.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.events.ClientEvents;
import fr.siroz.cariboustonks.features.garden.MouseLockFeature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import org.lwjgl.glfw.GLFW;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class) // Mouse
public abstract class MouseHandlerMixin {

	@Shadow
	private double xpos;
	@Shadow
	private double ypos;
	@Unique
	private double guiX;
	@Unique
	private double guiY;

	@Unique
	private final MouseLockFeature mouseLockFeature = CaribouStonks.features().getFeature(MouseLockFeature.class);

	/**
	 * MouseLockFeature logic {@link MouseLockFeature}
	 */
	@WrapWithCondition(method = "turnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;turn(DD)V"))
	private boolean cariboustonks$lockOrUnlockMouse(LocalPlayer instance, double deltaX, double deltaY) {
		return !mouseLockFeature.isLocked();
	}

	@Inject(method = "grabMouse", at = @At(value = "FIELD", target = "Lnet/minecraft/client/MouseHandler;mouseGrabbed:Z", opcode = Opcodes.PUTFIELD))
	private void cariboustonks$setUpCursorPosition(CallbackInfo ci) {
		this.guiX = this.xpos;
		this.guiY = this.ypos;
	}

	@Inject(method = "releaseMouse", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/InputConstants;grabOrReleaseMouse(Lcom/mojang/blaze3d/platform/Window;IDD)V", shift = At.Shift.AFTER))
	private void cariboustonks$unlockCursorPosition(CallbackInfo ci) {
		if (ConfigManager.getConfig().vanilla.stopCursorResetPosition && Minecraft.getInstance().screen instanceof ContainerScreen) {
			this.xpos = this.guiX;
			this.ypos = this.guiY;
			GLFW.glfwSetCursorPos(Minecraft.getInstance().getWindow().handle(), this.xpos, this.ypos);
		}
	}

	@Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
	private void cariboustonks$trackWheel(long window, double horizontal, double vertical, CallbackInfo ci) {
		if (!ClientEvents.ALLOW_MOUSE_SCROLL_EVENT.invoker().allowMouseScroll(horizontal, vertical)) {
			ci.cancel();
		}
	}
}
