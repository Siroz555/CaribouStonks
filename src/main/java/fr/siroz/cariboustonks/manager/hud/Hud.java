package fr.siroz.cariboustonks.manager.hud;

import fr.siroz.cariboustonks.mixin.accessors.PlayerListHudAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import org.jetbrains.annotations.NotNull;

/**
 * Abstract base class representing a configurable HUD.
 */
public abstract class Hud {

	protected static final MinecraftClient CLIENT = MinecraftClient.getInstance();

	protected final HudPosition hudPosition;
	private final int defaultX;
	private final int defaultY;
	private int x;
	private int y;
	private float scale;

	protected Hud(@NotNull HudPosition hudPosition, int defaultX, int defaultY) {
		this.hudPosition = hudPosition;
		this.defaultX = defaultX;
		this.defaultY = defaultY;
		this.x = hudPosition.x();
		this.y = hudPosition.y();
		this.scale = hudPosition.scale();
	}

	public int x() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int y() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public float scale() {
		return scale;
	}

	public void setScale(float scale) {
		this.scale = scale;
	}

	public abstract int width();

	public abstract int height();

	public boolean apply() {
		boolean changed = x != hudPosition.x() || y != hudPosition.y() || scale != hudPosition.scale();
		hudPosition.setX(x);
		hudPosition.setY(y);
		hudPosition.setScale(scale);

		return changed;
	}

	public void reset() {
		x = defaultX;
		y = defaultY;
		scale = 1f;
	}

	protected boolean shouldRender() { // TODO - TAB isVisible correct ? normalement oui
		return !CLIENT.getDebugHud().shouldShowDebugHud()
				&& !((PlayerListHudAccessor) CLIENT.inGameHud.getPlayerListHud()).isVisible()
				&& hudPosition.shouldRender();
	}

	public abstract void renderScreen(DrawContext context);

	public abstract void renderHud(DrawContext context, RenderTickCounter tickCounter);
}
