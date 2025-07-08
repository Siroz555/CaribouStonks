package fr.siroz.cariboustonks.mixin;

import fr.siroz.cariboustonks.event.ItemRenderEvents;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipAppender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = ItemStack.class, priority = 1111)
public abstract class ItemStackMixin {

	@ModifyVariable(method = "appendComponentTooltip", at = @At("STORE"))
	private TooltipAppender cariboustonks$appendTooltipEvent(TooltipAppender component) {
		if (component instanceof LoreComponent loreComponent) {
			LoreComponent lore = ItemRenderEvents.TOOLTIP_APPENDER.invoker().lines((ItemStack) (Object) this, loreComponent);
			if (lore != null) {
				return lore;
			}
		}

		return component;
	}
}
