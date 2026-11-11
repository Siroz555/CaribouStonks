package fr.siroz.cariboustonks.platform.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.events.GuiEvents;
import fr.siroz.cariboustonks.systems.ContainerOverlaySystem;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin<T extends AbstractContainerMenu> extends Screen {

	@Shadow
	@Nullable
	protected Slot hoveredSlot;

	@Shadow
	@Final
	protected T menu;

	@Unique
	private final ContainerOverlaySystem overlayManager = CaribouStonks.systems().getSystem(ContainerOverlaySystem.class);

	protected AbstractContainerScreenMixin(Component title) {
		super(title);
	}

	@SuppressWarnings("unchecked")
	@Inject(method = "extractTooltip", at = @At("HEAD"))
	private void cariboustonks$drawContainerOverlay(CallbackInfo ci, @Local(argsOnly = true, name = "graphics") GuiGraphicsExtractor graphics) {
		overlayManager.draw(graphics, (AbstractContainerScreen<ChestMenu>) (Object) this, this.menu.slots);
	}

	@Inject(method = "keyPressed", at = @At("HEAD"))
	private void cariboustonks$onKeyPressedEvent(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
		if (this.minecraft.player == null) return;

		if (this.hoveredSlot != null && event.input() != 256 && !this.minecraft.options.keyInventory.matches(event)) {
			GuiEvents.SCREEN_KEY_PRESS_EVENT.invoker().onKeyPressed(this, event, this.hoveredSlot);
		}
	}

	@WrapOperation(method = "extractTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;setTooltipForNextFrame(Lnet/minecraft/client/gui/Font;Ljava/util/List;Ljava/util/Optional;IILnet/minecraft/resources/Identifier;Z)V"))
	private void cariboustonks$onExtractMouseOverTooltip(GuiGraphicsExtractor instance, Font font, List<Component> texts, Optional<TooltipComponent> optionalImage, int xo, int yo, @Nullable Identifier style, boolean extraSpaceAfterFirstLine, Operation<Void> original) {
		if (minecraft.player != null && hoveredSlot != null && hoveredSlot.container != minecraft.player.getInventory()) {
			if (!GuiEvents.MOUSE_OVER_TOOLTIP_EVENT.invoker().onMouseOverTooltip(instance, this, hoveredSlot.getContainerSlot(), xo, yo)) {
				return;
			}
		}
		original.call(instance, font, texts, optionalImage, xo, yo, style, extraSpaceAfterFirstLine);
	}
}
