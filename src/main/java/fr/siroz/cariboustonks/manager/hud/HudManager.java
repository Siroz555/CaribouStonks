package fr.siroz.cariboustonks.manager.hud;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.feature.Feature;
import fr.siroz.cariboustonks.manager.Manager;
import java.util.ArrayList;
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Manages and handles the registration and rendering of HUD elements within the game
 * with the Fabric Rendering (v1) HUD API
 * <p>
 * This manager retrieves HUD elements from features implementing {@link HudProvider} and stores them
 * in an internal list for rendering during each frame. The rendering logic ensures all registered
 * elements are displayed in the correct order.
 */
public final class HudManager implements Manager {

	private final List<Hud> hudList = new ArrayList<>();

	@Override
	public void register(@NotNull Feature feature) {
		if (feature instanceof HudProvider hud) {
			handleHudRegistration(hud);
		}
	}

	private void handleHudRegistration(@NotNull HudProvider provider) {
		Identifier afterThis = provider.getAttachLayerAfter().left();
		Identifier identifier = provider.getAttachLayerAfter().right();
		if (afterThis == null && identifier == null) {
			CaribouStonks.LOGGER.error(
					"[HudManager] Failed to register HUD for {}. Both LEFT and RIGHT Attached Layers are null.",
					provider.getClass().getSimpleName());
			return;
		} else if (afterThis == null) {
			CaribouStonks.LOGGER.error(
					"[HudManager] Failed to register HUD for {}. LEFT Attached Layer is null.",
					provider.getClass().getSimpleName());
			return;
		} else if (identifier == null) {
			CaribouStonks.LOGGER.error(
					"[HudManager] Failed to register HUD for {}. RIGHT Attached Layer is null.",
					provider.getClass().getSimpleName());
			return;
		}

		Hud hud = provider.getHud();
		hudList.add(hud);

		HudLayerRegistrationCallback.EVENT.register(wrapper -> wrapper.attachLayerAfter(
				afterThis, identifier, hud::renderHud
		));
	}

	public List<Hud> getHudList() {
		return hudList;
	}
}
