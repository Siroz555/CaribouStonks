package fr.siroz.cariboustonks.manager.hud.builder;

import fr.siroz.cariboustonks.manager.hud.element.HudTextLine;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

/**
 * A builder for assembling a sequence of {@link HudTextLine} to be rendered in the default HUD view.
 */
public class HudElementTextBuilder {

	private final List<HudTextLine> elements = new ArrayList<>();

	/**
	 * Appends a single {@link Component} line to the HUD.
	 *
	 * @param text the text of the line to append
	 * @return this builder instance
	 * @see #append(HudTextLine)
	 */
	public HudElementTextBuilder append(@NotNull Component text) {
		return append(new HudTextLine(text, false));
	}

	/**
	 * Appends a single {@link HudTextLine} to the HUD.
	 *
	 * @param element the element of the line to append
	 * @return this builder instance
	 * @see #append(Component)
	 */
	public HudElementTextBuilder append(@NotNull HudTextLine element) {
		elements.add(element);
		return this;
	}

	/**
	 * Converts the last appended element into one vertical space.
	 * <p>
	 * Replaced with a copy that has {@code spaceAfter = true}.
	 *
	 * @return this builder instance
	 */
	public HudElementTextBuilder appendSpace() {
		if (!elements.isEmpty()) {
			HudTextLine last = elements.removeLast();
			elements.add(new HudTextLine(last.text(), true));
		}

		return this;
	}

	/**
	 * Returns the internally maintained list of {@link HudTextLine}s.
	 * <p>
	 * This method does not create a new list; it returns the same instance each time.
	 *
	 * @return the list of HudTextLine under construction
	 */
	public List<HudTextLine> build() {
		return elements;
	}
}
