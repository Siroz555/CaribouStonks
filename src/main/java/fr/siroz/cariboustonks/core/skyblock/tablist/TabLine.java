package fr.siroz.cariboustonks.core.skyblock.tablist;

import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

/**
 * Represents a Line from a {@link TabWidget}
 *
 * @param text      the text as {@link String}
 * @param component the text as {@link Component}
 */
public record TabLine(
		@NonNull String text,
		@NonNull Component component
) {

	public static final TabLine EMPTY = new TabLine("", Component.empty());

	public boolean isEmpty() {
		return text.isBlank();
	}

	/**
	 * Checks if the line start with a space -> widget content
	 *
	 * @return {@code true} if is indented
	 */
	public boolean isIndented() {
		return !text.isEmpty() && text.charAt(0) == ' ';
	}

	/**
	 * Non-indented line with ":" -> widget header.
	 * <p>
	 * "Profile: Zucchini", "Jacob's Contest: 11m left"
	 * <p>
	 * "ACTIVE" (en dessous de "Jacob's Contest") n'a pas de " :",
	 * donc ne déclenchera pas un nouveau widget.
	 *
	 * @return {@code true} if is a widget header
	 */
	public boolean isWidgetHeader() {
		return !isEmpty() && !isIndented() && text.contains(":");
	}
}
