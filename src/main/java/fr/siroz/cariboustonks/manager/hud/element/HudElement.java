package fr.siroz.cariboustonks.manager.hud.element;

import net.minecraft.network.chat.Component;

/**
 * An {@code HudElement}.
 *
 * @see HudTextLine
 * @see HudTableRow
 * @see HudIconLine
 */
public interface HudElement {

	Component[] getCells();

	boolean hasSpaceAfter();
}
