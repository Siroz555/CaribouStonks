package fr.siroz.cariboustonks.feature.vanilla;

import fr.siroz.cariboustonks.core.component.KeybindComponent;
import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.core.module.input.KeyBind;
import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.event.MouseEvents;
import org.lwjgl.glfw.GLFW;

/**
 * Provides zoom functionality triggered by a user-defined keybind.
 * <p>
 * This feature allows users to temporarily zoom the view while the designated key is pressed.
 * <p>
 * <b>Note:</b> The main logic for visual transformation may be handled in
 * {@code Mixin >} {@link fr.siroz.cariboustonks.mixin.GameRendererMixin}
 */
public final class ZoomFeature extends Feature {

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

		this.addComponent(KeybindComponent.class, KeybindComponent.builder()
				.add(this.zoomKeyBind)
				.build());
    }

    @Override
    public boolean isEnabled() {
        return this.config().vanilla.zoom.enabled;
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
        if (!this.config().vanilla.zoom.mouseScrolling) return true;

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
