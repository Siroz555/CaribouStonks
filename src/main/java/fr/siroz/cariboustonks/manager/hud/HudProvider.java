package fr.siroz.cariboustonks.manager.hud;

import it.unimi.dsi.fastutil.Pair;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.fabricmc.fabric.api.client.rendering.v1.LayeredDrawerWrapper;
import net.minecraft.client.gui.LayeredDrawer;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

/**
 * Interface to be implemented by features that provide a HUD.
 * <p>
 * Implementations are responsible for supplying a specific {@link Hud} instance.
 */
public interface HudProvider {

	/**
	 * Returns the attached layer after the layer with the specified identifier.
	 * The render condition of the layer being attached to.
	 * <p>
	 * {@code Identifier <L>}: the identifier of the layer to add the new layer after.
	 * See {@link IdentifiedLayer} for a list of built-in identifiers.
	 * <p>
	 * {@code Identifier <R>}: the layer to add. {@code Unique} identifier for the HUD from {@link #getHud()}.
	 * <p>
	 * <pre>{@code
	 * private static final Identifier HUD_ID = CaribouStonks.identifier("hub_id");
	 *
	 * @Override
	 * public Pair<Identifier, Identifier> getAttachLayerAfter() {
	 *     return Pair.of(IdentifiedLayer.STATUS_EFFECTS, HUD_ID);
	 * }
	 * }</pre>
	 * See {@link LayeredDrawerWrapper#attachLayerAfter(Identifier, Identifier, LayeredDrawer.Layer)}
	 *
	 * @return a pair of identifiers
	 */
	@NotNull
	Pair<Identifier, Identifier> getAttachLayerAfter();

	/**
	 * Returns the HUD associated with this provider.
	 *
	 * @return an instance of {@link Hud}
	 */
	@NotNull
	Hud getHud();
}
