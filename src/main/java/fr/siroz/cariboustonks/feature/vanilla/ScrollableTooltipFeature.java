package fr.siroz.cariboustonks.feature.vanilla;

import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.event.CustomScreenEvents;
import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.event.ItemRenderEvents;
import fr.siroz.cariboustonks.event.MouseEvents;
import fr.siroz.cariboustonks.feature.Feature;
import fr.siroz.cariboustonks.mixin.accessors.OrderedTextTooltipComponentAccessor;
import fr.siroz.cariboustonks.util.math.MathUtils;
import fr.siroz.cariboustonks.util.cooldown.Cooldown;
import net.minecraft.client.gui.tooltip.OrderedTextTooltipComponent;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.CharacterVisitor;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class ScrollableTooltipFeature extends Feature {

	private static final Cooldown COOLDOWN = Cooldown.of(100, TimeUnit.MILLISECONDS);
	private static final int SCROLL_AMOUNT = 10;
	private static final double SMOOTHNESS_MULTIPLIER = 0.25D;
	//private static final int MIN_SCROLLABLE_TOOLTIPS = 28;

	private double currentXOffset = 0;
	private double currentYOffset = 0;
	private double xOffset = 0;
	private double yOffset = 0;
	private List<TooltipComponent> currentTooltips;

	public ScrollableTooltipFeature() {
		CustomScreenEvents.CLOSE.register(screen -> reset());
		MouseEvents.ALLOW_MOUSE_SCROLL.register(this::allowMouseScroll);
		ItemRenderEvents.TOOLTIP_TRACKER.register(this::onTooltipTracker);
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock() && ConfigManager.getConfig().uiAndVisuals.scrollableTooltip.enabled;
	}

	public int getXOffset() {
		return isEnabled() ? MathUtils.floor(currentXOffset) : 0;
	}

	public int getYOffset() {
		return isEnabled() ? MathUtils.floor(currentYOffset) : 0;
	}

	@EventHandler(event = "CustomScreenEvents.CLOSE")
	private void reset() {
		resetScroll();
		currentTooltips = null;
	}

	@EventHandler(event = "MouseEvents.ALLOW_MOUSE_SCROLL")
	private boolean allowMouseScroll(double horizontal, double vertical) {
		boolean allowScroll = true;
		if (!isEnabled()) {
			return allowScroll;
		}

		if (InputUtil.isKeyPressed(CLIENT.getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT)) {
			if (vertical > 0) {
				scrollLeft();
			} else if (vertical < 0) {
				scrollRight();
			}
		} else {
			if (vertical > 0) {
				if (isReverseScroll()) {
					scrollDown();
				} else {
					scrollUp();
				}
			} else if (vertical < 0) {
				if (isReverseScroll()) {
					scrollUp();
				} else {
					scrollDown();
				}
			}
		}

		if (horizontal > 0) {
			scrollLeft();
		} else if (horizontal < 0) {
			scrollRight();
		}

		return allowScroll;
	}

	private boolean isReverseScroll() {
		return ConfigManager.getConfig().uiAndVisuals.scrollableTooltip.reverseScroll;
	}

	@EventHandler(event = "ItemRenderEvents.TOOLTIP_TRACKER")
	private void onTooltipTracker(List<TooltipComponent> tooltipComponents) {
		if (!isEnabled()) return;
		//if (tooltipComponents != null && tooltipComponents.size() < MIN_SCROLLABLE_TOOLTIPS) return;
		if (COOLDOWN.testSilently()) resetScroll();

		COOLDOWN.reset();

		update();
		setTooltipComponents(tooltipComponents);
	}

	private void update() {
		currentXOffset += (xOffset - currentXOffset) * SMOOTHNESS_MULTIPLIER;
		currentYOffset += (yOffset - currentYOffset) * SMOOTHNESS_MULTIPLIER;
	}

	private void setTooltipComponents(List<TooltipComponent> tooltipComponents) {
		if (!isEqual(currentTooltips, tooltipComponents)) {
			resetScroll();
			currentTooltips = tooltipComponents;
		}
	}

	private void scrollUp() {
		if (!COOLDOWN.testSilently()) yOffset -= SCROLL_AMOUNT;
	}

	private void scrollDown() {
		if (!COOLDOWN.testSilently()) yOffset += SCROLL_AMOUNT;
	}

	private void scrollLeft() {
		if (!COOLDOWN.testSilently()) xOffset -= SCROLL_AMOUNT;
	}

	private void scrollRight() {
		if (!COOLDOWN.testSilently()) xOffset += SCROLL_AMOUNT;
	}

	private void resetScroll() {
		currentXOffset = 0;
		currentYOffset = 0;
		xOffset = 0;
		yOffset = 0;
	}

	private boolean isEqual(@Nullable List<TooltipComponent> item1, @Nullable List<TooltipComponent> item2) {
		if (item1 == null || item2 == null || item1.size() != item2.size()) {
			return false;
		}

		for (int i = 0; i < item1.size(); ++i) {
			if (item1.get(i) instanceof OrderedTextTooltipComponent
					&& !(item2.get(i) instanceof OrderedTextTooltipComponent)) {
				return false;
			}

			if (item2.get(i) instanceof OrderedTextTooltipComponent
					&& !(item1.get(i) instanceof OrderedTextTooltipComponent)) {
				return false;
			}

			if (!(item1.get(i) instanceof OrderedTextTooltipComponent)
					&& !(item2.get(i) instanceof OrderedTextTooltipComponent)) {
				continue;
			}

			String text1 = OrderedTextReader.read(((OrderedTextTooltipComponentAccessor) item1.get(i)).getText());
			String text2 = OrderedTextReader.read(((OrderedTextTooltipComponentAccessor) item2.get(i)).getText());
			if (!text1.equals(text2)) {
				return false;
			}
		}

		return true;
	}

	private static final class OrderedTextReader {
		private static class Visitor implements CharacterVisitor {
			private int finalIndex = -1;
			private final StringBuilder builder = new StringBuilder();

			Visitor() {
			}

			@Override
			public boolean accept(int index, Style style, int codePoint) {
				if (index > finalIndex) finalIndex = index;
				else return false;

				builder.append((char) codePoint);
				return true;
			}

			@Contract(pure = true)
			public @NotNull String getString() {
				return builder.toString();
			}
		}

		public static @NotNull String read(@NotNull OrderedText text) {
			Visitor visitor = new Visitor();
			text.accept(visitor);
			return visitor.getString();
		}
	}
}
