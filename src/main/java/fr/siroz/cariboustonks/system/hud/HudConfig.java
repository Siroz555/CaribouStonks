package fr.siroz.cariboustonks.system.hud;

/**
 * Implementations provide getters and setters for interactive manipulation
 * of HUDs, allowing dynamic updates in configuration changes.
 */
public interface HudConfig {

	int x();

	void setX(int x);

	int y();

	void setY(int y);

	float scale();

	void setScale(float scale);

	boolean shouldRender();
}
