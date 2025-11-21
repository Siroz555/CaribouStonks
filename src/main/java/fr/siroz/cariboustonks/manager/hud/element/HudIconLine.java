package fr.siroz.cariboustonks.manager.hud.element;

import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
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
