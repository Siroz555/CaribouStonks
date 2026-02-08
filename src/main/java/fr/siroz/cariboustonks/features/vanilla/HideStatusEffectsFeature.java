package fr.siroz.cariboustonks.features.vanilla;

import fr.siroz.cariboustonks.core.feature.Feature;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;

public class HideStatusEffectsFeature extends Feature {

	public HideStatusEffectsFeature() {
		HudElementRegistry.replaceElement(VanillaHudElements.STATUS_EFFECTS,
				hud -> isEnabled() ? (guiGraphics, deltaTracker) -> {} : hud
		);
	}

	@Override
	public boolean isEnabled() {
		return this.config().vanilla.overlay.hideStatusEffectsOverlay;
	}
}
