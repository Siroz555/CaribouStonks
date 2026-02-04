package fr.siroz.cariboustonks.core.module.hud.element;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public record HudTableRow(Component[] cells, boolean spaceAfter) implements HudElement {

	public HudTableRow(@NotNull Component cell1, @NotNull Component cell2, @NotNull Component cell3, boolean spaceAfter) {
		this(new Component[]{cell1, cell2, cell3}, spaceAfter);
	}

	@Override
	public Component[] getCells() {
		return cells;
	}

	@Override
	public boolean hasSpaceAfter() {
		return spaceAfter;
	}
}
