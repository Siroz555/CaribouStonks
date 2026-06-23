package fr.siroz.cariboustonks.core.module.hud;

/**
 * Defines which corner of the screen a {@link Hud} is positioned relative to.
 * <p>
 * The {@code (offsetX, offsetY)} stored in {@link HudConfig} are measured from the HUD's
 * corresponding corner to the matching screen corner, and are always non-negative.
 * This ensures HUDs remain correctly placed across different screen resolutions.
 *
 * <pre>
 *  TOP_LEFT ──────── TOP_RIGHT
 *      │                  │
 *  BOTTOM_LEFT ────── BOTTOM_RIGHT
 * </pre>
 */
public enum HudAnchor {
	TOP_LEFT,
	TOP_RIGHT,
	BOTTOM_LEFT,
	BOTTOM_RIGHT;

	/**
	 * Returns the anchor corner that best represents the given absolute HUD top-left position.
	 *
	 * @param absX         absolute screen X of the HUD's top-left corner
	 * @param absY         absolute screen Y of the HUD's top-left corner
	 * @param screenWidth  current GUI-scaled screen width
	 * @param screenHeight current GUI-scaled screen height
	 * @return the nearest corner anchor
	 */
	public static HudAnchor fromAbsolutePosition(int absX, int absY, int screenWidth, int screenHeight) {
		boolean left = (absX * 2) < screenWidth;
		boolean top = (absY * 2) < screenHeight;
		if (left) return top ? TOP_LEFT : BOTTOM_LEFT;
		return top ? TOP_RIGHT : BOTTOM_RIGHT;
	}

	/**
	 * Resolves the HUD's absolute screen X from an anchor-relative offset.
	 *
	 * @param offsetX     X offset relative to this anchor corner (always >= 0)
	 * @param hudWidth    HUD width in pixels
	 * @param screenWidth current GUI-scaled screen width
	 * @return absolute screen X of the HUD's top-left corner
	 */
	public int resolveX(int offsetX, int hudWidth, int screenWidth) {
		return switch (this) {
			case TOP_LEFT, BOTTOM_LEFT -> offsetX;
			case TOP_RIGHT, BOTTOM_RIGHT -> screenWidth - offsetX - hudWidth;
		};
	}

	/**
	 * Resolves the HUD's absolute screen Y from an anchor-relative offset.
	 *
	 * @param offsetY      Y offset relative to this anchor corner (always >= 0)
	 * @param hudHeight    HUD height in pixels
	 * @param screenHeight current GUI-scaled screen height
	 * @return absolute screen Y of the HUD's top-left corner
	 */
	public int resolveY(int offsetY, int hudHeight, int screenHeight) {
		return switch (this) {
			case TOP_LEFT, TOP_RIGHT -> offsetY;
			case BOTTOM_LEFT, BOTTOM_RIGHT -> screenHeight - offsetY - hudHeight;
		};
	}

	/**
	 * Computes the anchor-relative X offset from the HUD's absolute screen position.
	 *
	 * @param absX        absolute screen X of the HUD's top-left corner
	 * @param hudWidth    HUD width in pixels
	 * @param screenWidth current GUI-scaled screen width
	 * @return non-negative X offset relative to this anchor corner
	 */
	public int computeOffsetX(int absX, int hudWidth, int screenWidth) {
		return switch (this) {
			case TOP_LEFT, BOTTOM_LEFT -> absX;
			case TOP_RIGHT, BOTTOM_RIGHT -> screenWidth - absX - hudWidth;
		};
	}

	/**
	 * Computes the anchor-relative Y offset from the HUD's absolute screen position.
	 *
	 * @param absY         absolute screen Y of the HUD's top-left corner
	 * @param hudHeight    HUD height in pixels
	 * @param screenHeight current GUI-scaled screen height
	 * @return non-negative Y offset relative to this anchor corner
	 */
	public int computeOffsetY(int absY, int hudHeight, int screenHeight) {
		return switch (this) {
			case TOP_LEFT, TOP_RIGHT -> absY;
			case BOTTOM_LEFT, BOTTOM_RIGHT -> screenHeight - absY - hudHeight;
		};
	}
}
