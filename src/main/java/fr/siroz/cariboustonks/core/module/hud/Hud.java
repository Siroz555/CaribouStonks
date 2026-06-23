package fr.siroz.cariboustonks.core.module.hud;

import fr.siroz.cariboustonks.platform.mixin.accessors.PlayerTabOverlayAccessor;
import java.util.function.Supplier;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.jspecify.annotations.NonNull;

/**
 * Abstract base class for a configurable, anchor-aware HUD element.
 * <p>
 * Position is stored as an {@code (offsetX, offsetY)} pair relative to a {@link HudAnchor} corner.
 * Absolute screen coordinates are computed on demand, ensuring HUDs stay correctly placed
 * across different screen resolutions.
 * <p>
 * The runtime state ({@link #anchor}, {@link #offsetX}, {@link #offsetY}, {@link #scale}) tracks
 * pending edits made in the {@code HudConfigScreen} and diverges from {@link HudConfig} until
 * {@link #apply()} is called on screen close.
 *
 * @see TextHud
 * @see MultiElementHud
 */
public abstract class Hud {
	public static final float SCALE_MIN = 0.25f;
	public static final float SCALE_MAX = 5.0f;
	public static final float SCALE_STEP = 0.1f;
	protected static final Minecraft CLIENT = Minecraft.getInstance();

	private final Supplier<Boolean> enabled;
	protected final HudConfig hudConfig;

	private final int defaultOffsetX;
	private final int defaultOffsetY;

	private HudAnchor anchor;
	private int offsetX;
	private int offsetY;
	private float scale;

	protected Hud(
			@NonNull Supplier<Boolean> enabled,
			@NonNull HudConfig hudConfig,
			int defaultOffsetX,
			int defaultOffsetY
	) {
		this.enabled = enabled;
		this.hudConfig = hudConfig;
		this.defaultOffsetX = defaultOffsetX;
		this.defaultOffsetY = defaultOffsetY;
		this.anchor = hudConfig.anchor();
		this.offsetX = hudConfig.x();
		this.offsetY = hudConfig.y();
		this.scale = hudConfig.scale();
	}

	public int x() {
		return anchor.resolveX(offsetX, width(), guiWidth());
	}

	public int y() {
		return anchor.resolveY(offsetY, height(), guiHeight());
	}

	public float scale() {
		return scale;
	}

	public void setScale(float scale) {
		this.scale = Math.clamp(scale, SCALE_MIN, SCALE_MAX);
	}

	/**
	 * Moves the HUD to the given absolute screen position.
	 * <p>
	 * The position is clamped to keep the HUD fully within the screen bounds.
	 * The {@link HudAnchor} is automatically chosen based on which screen
	 * quadrant the position falls in, and the stored offset is recomputed accordingly.
	 *
	 * @param absX target absolute screen X (top-left of HUD)
	 * @param absY target absolute screen Y (top-left of HUD)
	 */
	public final void setAbsolutePosition(int absX, int absY) {
		int sw = guiWidth();
		int sh = guiHeight();
		// Prévention des HUD zero-size pour éviter des limites de clamp absurdes
		int w = Math.max(width(), 1);
		int h = Math.max(height(), 1);

		absX = Math.clamp(absX, 0, Math.max(0, sw - w));
		absY = Math.clamp(absY, 0, Math.max(0, sh - h));

		anchor = HudAnchor.fromAbsolutePosition(absX, absY, sw, sh);
		offsetX = anchor.computeOffsetX(absX, w, sw);
		offsetY = anchor.computeOffsetY(absY, h, sh);
	}

	public HudAnchor getAnchor() {
		return anchor;
	}

	public abstract int width();

	public abstract int height();

	public boolean apply() {
		boolean changed = offsetX != hudConfig.x()
				|| offsetY != hudConfig.y()
				|| scale != hudConfig.scale()
				|| anchor != hudConfig.anchor();
		hudConfig.setAnchor(anchor);
		hudConfig.setX(offsetX);
		hudConfig.setY(offsetY);
		hudConfig.setScale(scale);

		return changed;
	}

	public void reset() {
		anchor = HudAnchor.TOP_LEFT;
		offsetX = defaultOffsetX;
		offsetY = defaultOffsetY;
		scale = 1f;
	}

	protected boolean shouldRender() {
		return isConfigEnabled()
				&& enabled.get()
				&& !CLIENT.getDebugOverlay().showDebugScreen()
				&& !((PlayerTabOverlayAccessor) CLIENT.gui.hud.getTabList()).isVisible();
	}

	public boolean isConfigEnabled() {
		return hudConfig.shouldRender();
	}

	public abstract void renderScreen(GuiGraphicsExtractor guiGraphics);

	public abstract void renderHud(GuiGraphicsExtractor guiGraphics, DeltaTracker tickCounter);

	/**
	 * Absolute screen X derived from the saved {@link HudConfig}, for use in {@link #renderHud}.
	 */
	protected final int configX() {
		return hudConfig.anchor().resolveX(hudConfig.x(), width(), guiWidth());
	}

	/**
	 * Absolute screen Y derived from the saved {@link HudConfig}, for use in {@link #renderHud}.
	 */
	protected final int configY() {
		return hudConfig.anchor().resolveY(hudConfig.y(), height(), guiHeight());
	}

	private int guiWidth() {
		return CLIENT.getWindow().getGuiScaledWidth();
	}

	private int guiHeight() {
		return CLIENT.getWindow().getGuiScaledHeight();
	}
}
