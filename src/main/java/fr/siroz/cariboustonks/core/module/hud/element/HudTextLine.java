package fr.siroz.cariboustonks.core.module.hud.element;

import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

public record HudTextLine(
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
