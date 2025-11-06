package fr.siroz.cariboustonks.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.event.MouseEvents;
import fr.siroz.cariboustonks.feature.garden.MouseLockFeature;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import org.lwjgl.glfw.GLFW;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public abstract class MouseMixin {

	@Shadow
	private double x;
	@Shadow
	private double y;
	@Unique
	private double guiX;
	@Unique
	private double guiY;

	@Unique
	private final MouseLockFeature mouseLockFeature = CaribouStonks.features().getFeature(MouseLockFeature.class);

	/**
	 * MouseLockFeature logic {@link MouseLockFeature}
	 */
	@WrapWithCondition(method = "updateMouse", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;changeLookDirection(DD)V"))
	private boolean cariboustonks$lockOrUnlockMouse(ClientPlayerEntity instance, double deltaX, double deltaY) {
		return !mouseLockFeature.isLocked();
	}

	@Inject(method = "lockCursor", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Mouse;cursorLocked:Z", opcode = Opcodes.PUTFIELD))
	private void cariboustonks$setUpCursorPosition(CallbackInfo ci) {
		this.guiX = this.x;
		this.guiY = this.y;
	}

	@Inject(method = "unlockCursor", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/InputUtil;setCursorParameters(Lnet/minecraft/client/util/Window;IDD)V", shift = At.Shift.AFTER))
	private void cariboustonks$unlockCursorPosition(CallbackInfo ci) {
		if (ConfigManager.getConfig().vanilla.stopCursorResetPosition && MinecraftClient.getInstance().currentScreen instanceof GenericContainerScreen) {
			this.x = this.guiX;
			this.y = this.guiY;
			GLFW.glfwSetCursorPos(MinecraftClient.getInstance().getWindow().getHandle(), this.x, this.y);
		}
	}

	@Inject(method = "onMouseScroll", at = @At("HEAD"), cancellable = true)
	private void cariboustonks$trackWheel(long window, double horizontal, double vertical, CallbackInfo ci) {
		if (!MouseEvents.ALLOW_MOUSE_SCROLL.invoker().allowMouseScroll(horizontal, vertical)) {
			ci.cancel();
		}
	}
}
