package fr.siroz.cariboustonks.core.module.hud.element;

import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

public record HudTableRow(
		Component[] cells,
		boolean spaceAfter
) implements HudElement {

	public HudTableRow(
			@NonNull Component cell1,
			@NonNull Component cell2,
			@NonNull Component cell3,
			boolean spaceAfter
	) {
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
