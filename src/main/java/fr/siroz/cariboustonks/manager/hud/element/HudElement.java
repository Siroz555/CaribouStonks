package fr.siroz.cariboustonks.manager.hud.element;

import net.minecraft.text.Text;

/**
 * An {@code HudElement}.
 *
 * @see HudTextLine
 * @see HudTableRow
 * @see HudIconLine
 */
public interface HudElement {

	Text[] getCells();

	boolean hasSpaceAfter();
}
