package fr.siroz.cariboustonks.mixin.accessors;

import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerTabOverlay.class)
public interface PlayerTabOverlayAccessor {

	@Accessor("visible")
	boolean isVisible();

	@Accessor("footer")
    Component getFooter();
}
