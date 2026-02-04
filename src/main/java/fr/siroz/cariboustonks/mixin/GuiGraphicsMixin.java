package fr.siroz.cariboustonks.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.event.ItemRenderEvents;
import fr.siroz.cariboustonks.feature.vanilla.ScrollableTooltipFeature;
import fr.siroz.cariboustonks.mixin.accessors.AbstractContainerScreenAccessor;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector2ic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(value = GuiGraphics.class, priority = 1111) // DrawContext
public abstract class GuiGraphicsMixin {

	@Unique
	private int storedTooltipWidth;
	@Unique
	private int storedTooltipHeight;
	@Unique
	private Vector2ic storedPos;

	@Unique
	private final ScrollableTooltipFeature scrollableTooltipFeature = CaribouStonks.features()
			.getFeature(ScrollableTooltipFeature.class);

	// TODO(Ravel): remapper for org.spongepowered.asm.mixin.injection.Group is not implemented
	@Group(name = "storeLocals", min = 1, max = 1)
	@Inject(method = "renderTooltip", at = @At(value = "INVOKE_ASSIGN", target = "Lorg/joml/Vector2ic;x()I", shift = At.Shift.BEFORE, remap = false), locals = LocalCapture.CAPTURE_FAILSOFT)
	private void cariboustonks$onDrawTooltipEventAndStoreLocals(Font font, List<ClientTooltipComponent> components, int x, int y, ClientTooltipPositioner positioner, Identifier resource, CallbackInfo info, int tooltipWidth, int tooltipHeight, int tooltipWidth2, int tooltipHeight2, Vector2ic postPos) {
		ItemRenderEvents.TOOLTIP_TRACKER.invoker().onTooltipTracker(components);
		storedTooltipWidth = tooltipWidth2;
		storedTooltipHeight = tooltipHeight2;
		storedPos = postPos;
	}

	@Inject(method = "renderTooltip", at = @At(value = "TAIL"))
	private void cariboustonks$onDrawTooltipInternalEvent(Font textRenderer, List<ClientTooltipComponent> components, int x, int y, ClientTooltipPositioner positioner, Identifier texture, CallbackInfo ci) {
		ItemStack stack = ItemStack.EMPTY;

		Screen currentScreen = Minecraft.getInstance().screen;
		if (currentScreen instanceof AbstractContainerScreen<?> handledScreen) {
			Slot hovered = ((AbstractContainerScreenAccessor) handledScreen).getFocusedSlot();
			if (hovered != null) {
				stack = hovered.getItem();
			}
		}

		if (!stack.isEmpty() && !components.isEmpty()) {
			ItemRenderEvents.POST_TOOLTIP.invoker().onPostTooltip(
					(GuiGraphics) (Object) this,
					stack,
					storedPos.x() + getXOffset(),
					storedPos.y() + getYOffset(),
					storedTooltipWidth,
					storedTooltipHeight,
					textRenderer,
					components
			);
		}
	}

	@Inject(method = "renderTooltip", at = @At(value = "INVOKE", target = "Lorg/joml/Matrix3x2fStack;pushMatrix()Lorg/joml/Matrix3x2fStack;"))
	private void cariboustonks$scrollableTooltipXYAxis(Font textRenderer, List<ClientTooltipComponent> components, int x, int y, ClientTooltipPositioner positioner, Identifier texture, CallbackInfo info, @Local(ordinal = 6) LocalIntRef refX, @Local(ordinal = 7) LocalIntRef refY) {
		refX.set(refX.get() + getXOffset());
		refY.set(refY.get() + getYOffset());

		if (scrollableTooltipFeature.canStartFromTop()) {
			int currentY = refY.get();
			if (refY.get() < 5) {
				refY.set(5);
				scrollableTooltipFeature.initOffsetY(5 - currentY);
			}
		}
	}

	@Unique
	private int getXOffset() {
		return scrollableTooltipFeature.getXOffset();
	}

	@Unique
	private int getYOffset() {
		return scrollableTooltipFeature.getYOffset();
	}
}
