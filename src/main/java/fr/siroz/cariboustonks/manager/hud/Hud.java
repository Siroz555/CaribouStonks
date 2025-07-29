package fr.siroz.cariboustonks.manager.hud;

import fr.siroz.cariboustonks.mixin.accessors.PlayerListHudAccessor;
import java.util.function.Supplier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import org.jetbrains.annotations.NotNull;

/**
 * Abstract base class representing a configurable HUD.
 *
 * @see TextHud
 * @see MultiElementHud
 */
public abstract class Hud {

	protected static final MinecraftClient CLIENT = MinecraftClient.getInstance();

	private final Supplier<Boolean> enabled;
	protected final HudConfig hudConfig;
	private final int defaultX;
	private final int defaultY;
	private int x;
	private int y;
	private float scale;

	protected Hud(@NotNull Supplier<Boolean> enabled, @NotNull HudConfig hudConfig, int defaultX, int defaultY) {
		this.enabled = enabled;
		this.hudConfig = hudConfig;
		this.defaultX = defaultX;
		this.defaultY = defaultY;
		this.x = hudConfig.x();
		this.y = hudConfig.y();
		this.scale = hudConfig.scale();
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
		boolean changed = x != hudConfig.x() || y != hudConfig.y() || scale != hudConfig.scale();
		hudConfig.setX(x);
		hudConfig.setY(y);
		hudConfig.setScale(scale);

		return changed;
	}

	public void reset() {
		x = defaultX;
		y = defaultY;
		scale = 1f;
	}

	protected boolean shouldRender() {
		return hudConfig.shouldRender()
				&& enabled.get()
				&& !CLIENT.getDebugHud().shouldShowDebugHud()
				&& !((PlayerListHudAccessor) CLIENT.inGameHud.getPlayerListHud()).isVisible();
	}

	public abstract void renderScreen(DrawContext context);

	public abstract void renderHud(DrawContext context, RenderTickCounter tickCounter);
}
