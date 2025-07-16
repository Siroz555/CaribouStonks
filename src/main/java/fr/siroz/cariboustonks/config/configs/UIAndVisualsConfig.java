package fr.siroz.cariboustonks.config.configs;

import dev.isxander.yacl3.config.v2.api.SerialEntry;
import fr.siroz.cariboustonks.manager.hud.HudPosition;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import java.awt.Color;

public class UIAndVisualsConfig {

	@SerialEntry
	public boolean beaconBeamWithNoDepthTest = true;

	@SerialEntry
	public boolean highlightSelectedPet = false;

	@SerialEntry
	public ObjectOpenHashSet<String> favoriteAbiphoneContacts = new ObjectOpenHashSet<>();

	@SerialEntry
	public ColoredEnchantment coloredEnchantment = new ColoredEnchantment();

	@SerialEntry
	public Zoom zoom = new Zoom();

	@SerialEntry
	public ScrollableTooltip scrollableTooltip = new ScrollableTooltip();

	@SerialEntry
	public SharedPositionWaypoint sharedPositionWaypoint = new SharedPositionWaypoint();

	@SerialEntry
	public ToolTipDecorator toolTipDecorator = new ToolTipDecorator();

	@SerialEntry
	public Overlay overlay = new Overlay();

	@SerialEntry
	public PingHud pingHud = new PingHud();

	@SerialEntry
	public FPSHud fpsHud = new FPSHud();

	@SerialEntry
	public TpsHud tpsHud = new TpsHud();

	@SerialEntry
	public DayHud dayHud = new DayHud();

	public static class Overlay {

		@SerialEntry
		public boolean gyrokineticWand = false;

		@SerialEntry
		public boolean etherWarp = false;
	}

	public static class ColoredEnchantment {

		@SerialEntry
		public boolean showMaxEnchants = false;

		@SerialEntry
		public boolean maxEnchantsRainbow = false;

		@SerialEntry
		public Color maxEnchantsColor = Color.RED;

		@SerialEntry
		public boolean showGoodEnchants = false;

		@SerialEntry
		public Color goodEnchantsColor = Color.ORANGE;
	}

	public static class Zoom {

		@SerialEntry
		public boolean enabled = true;

		@SerialEntry
		public boolean mouseScrolling = false;
	}

	public static class ScrollableTooltip {

		@SerialEntry
		public boolean enabled = true;

		@SerialEntry
		public boolean reverseScroll = false;
	}

	public static class SharedPositionWaypoint {

		@SerialEntry
		public boolean enabled = true;

		@SerialEntry
		public int showTime = 15;

		@SerialEntry
		public boolean rainbow = true;

		@SerialEntry
		public Color color = Color.RED;

		@SerialEntry
		public boolean shareWithArea = false;
	}

	public static class ToolTipDecorator {

		@SerialEntry
		public boolean enabled = false;
	}

	public static class PingHud implements HudPosition {

		@SerialEntry
		public boolean enabled = false;

		@SerialEntry
		public int x = 58;

		@SerialEntry
		public int y = 8;

		@SerialEntry
		public float scale = 1f;

		@Override
		public int x() {
			return this.x;
		}

		@Override
		public void setX(int x) {
			this.x = x;
		}

		@Override
		public int y() {
			return this.y;
		}

		@Override
		public void setY(int y) {
			this.y = y;
		}

		@Override
		public float scale() {
			return this.scale;
		}

		@Override
		public void setScale(float scale) {
			this.scale = scale;
		}

		@Override
		public boolean shouldRender() {
			return this.enabled;
		}
	}

	public static class FPSHud implements HudPosition {

		@SerialEntry
		public boolean enabled = false;

		@SerialEntry
		public int x = 6;

		@SerialEntry
		public int y = 64;

		@SerialEntry
		public float scale = 1f;

		@Override
		public int x() {
			return this.x;
		}

		@Override
		public void setX(int x) {
			this.x = x;
		}

		@Override
		public int y() {
			return this.y;
		}

		@Override
		public void setY(int y) {
			this.y = y;
		}

		@Override
		public float scale() {
			return this.scale;
		}

		@Override
		public void setScale(float scale) {
			this.scale = scale;
		}

		@Override
		public boolean shouldRender() {
			return this.enabled;
		}
	}

	public static class TpsHud implements HudPosition {

		@SerialEntry
		public boolean enabled = false;

		@SerialEntry
		public int x = 6;

		@SerialEntry
		public int y = 8;

		@SerialEntry
		public float scale = 1f;

		@Override
		public int x() {
			return this.x;
		}

		@Override
		public void setX(int x) {
			this.x = x;
		}

		@Override
		public int y() {
			return this.y;
		}

		@Override
		public void setY(int y) {
			this.y = y;
		}

		@Override
		public float scale() {
			return this.scale;
		}

		@Override
		public void setScale(float scale) {
			this.scale = scale;
		}

		@Override
		public boolean shouldRender() {
			return this.enabled;
		}
	}

	public static class DayHud implements HudPosition {

		@SerialEntry
		public boolean enabled = false;

		@SerialEntry
		public int x = 20;

		@SerialEntry
		public int y = 64;

		@SerialEntry
		public float scale = 1f;

		@Override
		public int x() {
			return this.x;
		}

		@Override
		public void setX(int x) {
			this.x = x;
		}

		@Override
		public int y() {
			return this.y;
		}

		@Override
		public void setY(int y) {
			this.y = y;
		}

		@Override
		public float scale() {
			return this.scale;
		}

		@Override
		public void setScale(float scale) {
			this.scale = scale;
		}

		@Override
		public boolean shouldRender() {
			return this.enabled;
		}
	}
}
