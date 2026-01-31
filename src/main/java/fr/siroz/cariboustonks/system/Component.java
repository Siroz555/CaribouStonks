package fr.siroz.cariboustonks.system;

import fr.siroz.cariboustonks.system.command.CommandComponent;
import fr.siroz.cariboustonks.system.keybinds.KeyBindComponent;

/**
 * Marker interface for all feature “capabilities.”
 * <p>
 * A {@link Component} represents an attachable, registration-only capability of a {@code Feature}.
 * Managers discover these capabilities on a feature and perform the appropriate registration.
 *
 * @see CommandComponent
 * @see KeyBindComponent
 */
public interface Component {
}
