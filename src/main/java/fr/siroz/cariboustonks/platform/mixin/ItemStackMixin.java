package fr.siroz.cariboustonks.platform.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.events.GuiEvents;
import fr.siroz.cariboustonks.util.DeveloperTools;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.TooltipProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ItemStack.class, priority = 1111)
public abstract class ItemStackMixin {

	@ModifyVariable(method = "addToTooltip", at = @At("STORE"), name = "component")
	private TooltipProvider cariboustonks$appendTooltipEvent(TooltipProvider component) {
		if (component instanceof ItemLore loreComponent) {
			ItemLore lore = GuiEvents.TOOLTIP_APPENDER_EVENT.invoker().lines((ItemStack) (Object) this, loreComponent);
			if (lore != null) {
				return lore;
			}
		}

		return component;
	}

	@Inject(method = "addDetailsToTooltip",
			slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/core/DefaultedRegistry;getKey(Ljava/lang/Object;)Lnet/minecraft/resources/Identifier;")),
			at = @At(value = "INVOKE", target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V", shift = At.Shift.AFTER, ordinal = 0)
	)
	private void cariboustonks$addSkyBlockIdInDevelopment(CallbackInfo ci, @Local(name = "builder", argsOnly = true) Consumer<Component> builder) {
		if (DeveloperTools.isInDevelopment()) {
			String skyblockId = SkyBlockAPI.getSkyBlockItemId((ItemStack) (Object) this);
			if (!skyblockId.isEmpty()) {
				builder.accept(Component.literal("SkyBlockId:" + skyblockId).withStyle(ChatFormatting.DARK_GRAY));
			}
		}
	}
}
