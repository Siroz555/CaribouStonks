package fr.siroz.cariboustonks.core.module.hud.builder;

import fr.siroz.cariboustonks.core.module.hud.element.HudElement;
import fr.siroz.cariboustonks.core.module.hud.element.HudIconLine;
import fr.siroz.cariboustonks.core.module.hud.element.HudTableRow;
import fr.siroz.cariboustonks.core.module.hud.element.HudTextLine;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;

/**
 * A builder for assembling a sequence of {@link HudElement}s to be rendered in the HUD.
 */
public final class HudElementBuilder {

	private final List<HudElement> elements = new ArrayList<>();

	/**
	 * Clears all elements from this builder, returning it to an empty state.
	 *
	 * @return this builder instance
	 */
	public HudElementBuilder clear() {
		elements.clear();
		return this;
	}

	/**
	 * Inserts a title line at the very beginning of the HUD.
	 * The appended title will always include a trailing space after it.
	 *
	 * @param text the title text to insert
	 * @return this builder instance
	 */
	public HudElementBuilder appendTitle(@NonNull Component text) {
		elements.addFirst(new HudTextLine(text, true));
		return this;
	}

	/**
	 * Appends a single {@link Component} line to the HUD.
	 *
	 * @param text the text of the line to append
	 * @return this builder instance
	 */
	public HudElementBuilder appendLine(@NonNull Component text) {
		elements.add(new HudTextLine(text, false));
		return this;
	}

	/**
	 * Appends a table row consisting of exactly three text columns.
	 *
	 * @param c1 the text for the first column
	 * @param c2 the text for the second column
	 * @param c3 the text for the third column
	 * @return this builder instance
	 */
	public HudElementBuilder appendTableRow(@NonNull Component c1, @NonNull Component c2, @NonNull Component c3) {
		elements.add(new HudTableRow(c1, c2, c3, false));
		return this;
	}

	/**
	 * Appends a single {@link ItemStack} with a {@link Component} on the same line to the HUD.
	 *
	 * @param stack the ItemStack
	 * @param text  the text
	 * @return this builder instance
	 */
	public HudElementBuilder appendIconLine(@NonNull ItemStack stack, @NonNull Component text) {
		elements.add(new HudIconLine(stack, text, false));
		return this;
	}

	/**
	 * Converts the last appended element into one vertical space.
	 *
	 * @return this builder instance
	 */
	public HudElementBuilder appendSpace() {
		if (!elements.isEmpty()) {
			HudElement last = elements.removeLast();
			if (last instanceof HudTextLine textLine) {
				elements.add(new HudTextLine(textLine.text(), true));
			} else if (last instanceof HudTableRow row) {
				elements.add(new HudTableRow(row.getCells(), true));
			} else if (last instanceof HudIconLine iconLine) {
				elements.add(new HudIconLine(iconLine.stack(), iconLine.text(), true));
			}
		}

		return this;
	}

	/**
	 * Returns the internally maintained list of {@link HudElement}s.
	 * <p>
	 * This method does not create a new list; it returns the same instance each time.
	 * To rebuild, call {@link #clear()} followed by append methods before retrieving again.
	 *
	 * @return the list of HUD elements under construction
	 */
	public List<HudElement> build() {
		return elements;
	}
}
