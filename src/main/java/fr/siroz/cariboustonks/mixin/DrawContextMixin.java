package fr.siroz.cariboustonks.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.event.ItemRenderEvents;
import fr.siroz.cariboustonks.feature.item.ScrollableTooltipFeature;
import fr.siroz.cariboustonks.mixin.accessors.HandledScreenAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.gui.tooltip.TooltipPositioner;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;
import org.joml.Vector2ic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(value = DrawContext.class, priority = 1111)
public abstract class DrawContextMixin {

	@Unique
	private int storedTooltipWidth;
	@Unique
	private int storedTooltipHeight;
	@Unique
	private Vector2ic storedPos;

	@Unique
	private final ScrollableTooltipFeature scrollableTooltipFeature = CaribouStonks.features()
			.getFeature(ScrollableTooltipFeature.class);

	@Group(name = "storeLocals", min = 1, max = 1)
	@Inject(method = "drawTooltipImmediately", at = @At(value = "INVOKE_ASSIGN", target = "Lorg/joml/Vector2ic;x()I", shift = At.Shift.BEFORE, remap = false), locals = LocalCapture.CAPTURE_FAILSOFT)
	private void cariboustonks$onDrawTooltipEventAndStoreLocals(TextRenderer font, List<TooltipComponent> components, int x, int y, TooltipPositioner positioner, Identifier resource, CallbackInfo info, int tooltipWidth, int tooltipHeight, int tooltipWidth2, int tooltipHeight2, Vector2ic postPos) {
		ItemRenderEvents.TOOLTIP_TRACKER.invoker().onTooltipTracker(components);
		storedTooltipWidth = tooltipWidth2;
		storedTooltipHeight = tooltipHeight2;
		storedPos = postPos;
	}

	@Inject(method = "drawTooltipImmediately", at = @At(value = "TAIL"))
	private void cariboustonks$onDrawTooltipInternalEvent(TextRenderer textRenderer, List<TooltipComponent> components, int x, int y, TooltipPositioner positioner, Identifier texture, CallbackInfo ci) {
		ItemStack stack = ItemStack.EMPTY;

		Screen currentScreen = MinecraftClient.getInstance().currentScreen;
		if (currentScreen instanceof HandledScreen<?> handledScreen) {
			Slot hovered = ((HandledScreenAccessor) handledScreen).getFocusedSlot();
			if (hovered != null) {
				stack = hovered.getStack();
			}
		}

		if (!stack.isEmpty() && !components.isEmpty()) {
			ItemRenderEvents.POST_TOOLTIP.invoker().onPostTooltip(
					(DrawContext) (Object) this,
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

	@Inject(method = "drawTooltipImmediately", at = @At(value = "INVOKE", target = "Lorg/joml/Matrix3x2fStack;pushMatrix()Lorg/joml/Matrix3x2fStack;"))
	private void cariboustonks$scrollableTooltipXYAxis(TextRenderer textRenderer, List<TooltipComponent> components, int x, int y, TooltipPositioner positioner, Identifier texture, CallbackInfo info, @Local(ordinal = 6) LocalIntRef refX, @Local(ordinal = 7) LocalIntRef refY) {
		refX.set(refX.get() + getXOffset());
		refY.set(refY.get() + getYOffset());
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
