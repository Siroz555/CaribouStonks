package fr.siroz.cariboustonks.manager.hud.element;

import net.minecraft.text.Text;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public record HudTextLine(@NotNull Text text, boolean spaceAfter) implements HudElement {

	@Contract(value = " -> new", pure = true)
	@Override
	public Text @NotNull [] getCells() {
		return new Text[]{text};
	}

	@Override
	public boolean hasSpaceAfter() {
		return spaceAfter;
	}
}
