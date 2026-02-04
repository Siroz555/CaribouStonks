package fr.siroz.cariboustonks.core.module.hud.element;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public record HudIconLine(@NotNull ItemStack stack, @NotNull Component text, boolean spaceAfter) implements HudElement {

	@Contract(value = " -> new", pure = true)
	@Override
	public Component @NotNull [] getCells() {
		return new Component[]{text};
	}

	@Override
	public boolean hasSpaceAfter() {
		return spaceAfter;
	}
}
