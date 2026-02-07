package fr.siroz.cariboustonks.core.module.hud.element;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;

public record HudIconLine(
		@NonNull ItemStack stack,
		@NonNull Component text,
		boolean spaceAfter
) implements HudElement {

	@Override
	public Component @NonNull [] getCells() {
		return new Component[]{text};
	}

	@Override
	public boolean hasSpaceAfter() {
		return spaceAfter;
	}
}
