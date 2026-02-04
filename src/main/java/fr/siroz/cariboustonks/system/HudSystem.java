package fr.siroz.cariboustonks.system;

import fr.siroz.cariboustonks.core.component.HudComponent;
import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.core.module.hud.Hud;
import fr.siroz.cariboustonks.core.system.System;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import org.jetbrains.annotations.NotNull;

/**
 * Manages and handles the registration and rendering of HUD elements within the game
 * with the Fabric Rendering (v1) HUD API
 * <p>
 * This manager retrieves HUD elements from features with the {@link HudComponent} and stores them
 * in an internal list for rendering during each frame. The rendering logic ensures all registered
 * elements are displayed in the correct order.
 */
public final class HudSystem implements System {

	private final List<Hud> providers = new ArrayList<>();

	@Override
	public void register(@NotNull Feature feature) {
		feature.getComponent(HudComponent.class).ifPresent(this::registerComponent);
	}

	private void registerComponent(HudComponent component) {
		Hud hud = component.getHud();
		providers.add(hud);
		HudElementRegistry.attachElementAfter(component.getLayerType(), component.getHudId(), hud::renderHud);
	}

	public List<Hud> getHudList() {
		return providers;
	}
}
