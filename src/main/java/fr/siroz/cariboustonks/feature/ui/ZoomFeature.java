package fr.siroz.cariboustonks.feature.ui;

import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.event.MouseEvents;
import fr.siroz.cariboustonks.feature.Feature;
import fr.siroz.cariboustonks.manager.keybinds.KeyBind;
import fr.siroz.cariboustonks.manager.keybinds.KeyBindRegistration;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/**
 * Provides zoom functionality triggered by a user-defined keybind.
 * <p>
 * This feature allows users to temporarily zoom the view while the designated key is pressed.
 * <p>
 * <b>Note:</b> The main logic for visual transformation may be handled in
 * {@code Mixin >} {@link fr.siroz.cariboustonks.mixin.GameRendererMixin}
 */
public final class ZoomFeature extends Feature implements KeyBindRegistration {

    private static final double ZOOM_MULTIPLIER = 0.30D;
    private static final double ZOOM_STEP = 0.05D;
    private static final double MIN_ZOOM = 0.05D;
    private static final double MAX_ZOOM = 1.0D;

    private final KeyBind zoomKeyBind;
    private double currentZoomMultiplier;

    public ZoomFeature() {
        this.zoomKeyBind = new KeyBind("Zoom", GLFW.GLFW_KEY_C, true);
        this.currentZoomMultiplier = ZOOM_MULTIPLIER;
        MouseEvents.ALLOW_MOUSE_SCROLL.register(this::allowMouseScroll);
    }

    @Override
    public boolean isEnabled() {
        // Ignore isOnSkyBlock
        return ConfigManager.getConfig().uiAndVisuals.zoom.enabled;
    }

    @Override
    public @NotNull List<KeyBind> registerKeyBinds() {
        return List.of(zoomKeyBind);
    }

    public boolean isZooming() {
        if (!isEnabled()) {
			return false;
		}

        return zoomKeyBind.isPressed();
    }

    public double getCurrentZoomMultiplier() {
        return currentZoomMultiplier;
    }

    public void resetZoomMultiplier() {
        currentZoomMultiplier = ZOOM_MULTIPLIER;
    }

	@EventHandler(event = "MouseEvents.ALLOW_MOUSE_SCROLL")
    private boolean allowMouseScroll(double horizontal, double vertical) {
        if (!isZooming()) return true;
        if (!ConfigManager.getConfig().uiAndVisuals.zoom.mouseScrolling) return true;

        if (vertical > 0) {
			decreaseZoom();
		} else if (vertical < 0) {
			increaseZoom();
		}

        return false;
    }

    private void decreaseZoom() {
        currentZoomMultiplier = Math.max(MIN_ZOOM, currentZoomMultiplier - ZOOM_STEP);
    }

    private void increaseZoom() {
        currentZoomMultiplier = Math.min(MAX_ZOOM, currentZoomMultiplier + ZOOM_STEP);
    }
}
