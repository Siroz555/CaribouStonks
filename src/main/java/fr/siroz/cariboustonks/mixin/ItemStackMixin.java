package fr.siroz.cariboustonks.mixin;

import fr.siroz.cariboustonks.events.GuiEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.TooltipProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = ItemStack.class, priority = 1111)
public abstract class ItemStackMixin {

	@ModifyVariable(method = "addToTooltip", at = @At("STORE"))
	private TooltipProvider cariboustonks$appendTooltipEvent(TooltipProvider component) {
		if (component instanceof ItemLore loreComponent) {
			ItemLore lore = GuiEvents.TOOLTIP_APPENDER_EVENT.invoker().lines((ItemStack) (Object) this, loreComponent);
			if (lore != null) {
				return lore;
			}
		}

		return component;
	}
}
