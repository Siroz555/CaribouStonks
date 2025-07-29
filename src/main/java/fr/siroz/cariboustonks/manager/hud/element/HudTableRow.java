package fr.siroz.cariboustonks.manager.hud.element;

import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public record HudTableRow(Text[] cells, boolean spaceAfter) implements HudElement {

	public HudTableRow(@NotNull Text cell1, @NotNull Text cell2, @NotNull Text cell3, boolean spaceAfter) {
		this(new Text[]{cell1, cell2, cell3}, spaceAfter);
	}

	@Override
	public Text[] getCells() {
		return cells;
	}

	@Override
	public boolean hasSpaceAfter() {
		return spaceAfter;
	}
}
