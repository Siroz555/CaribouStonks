package fr.siroz.cariboustonks.feature.vanilla;

import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.event.CustomScreenEvents;
import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.event.ItemRenderEvents;
import fr.siroz.cariboustonks.event.MouseEvents;
import fr.siroz.cariboustonks.mixin.accessors.ClientTextTooltipAccessor;
import fr.siroz.cariboustonks.util.Client;
import fr.siroz.cariboustonks.util.cooldown.Cooldown;
import fr.siroz.cariboustonks.util.math.MathUtils;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTextTooltip;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.FormattedCharSink;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

public class ScrollableTooltipFeature extends Feature {

	private static final Cooldown COOLDOWN = Cooldown.of(100, TimeUnit.MILLISECONDS);
	private static final int SCROLL_AMOUNT = 10;
	private static final double SMOOTHNESS_MULTIPLIER = 0.25D;
	private static final int MIN_SCROLLABLE_TOOLTIPS = 25;

	private final BooleanSupplier reverseScrollConfig =
			() -> ConfigManager.getConfig().vanilla.scrollableTooltip.reverseScroll;

	private double currentXOffset = 0;
	private double currentYOffset = 0;
	private double xOffset = 0;
	private double yOffset = 0;
	private boolean hasMoved = false;
	@Nullable
	private List<ClientTooltipComponent> currentTooltips;

	public ScrollableTooltipFeature() {
		CustomScreenEvents.CLOSE.register(screen -> this.reset());
		MouseEvents.ALLOW_MOUSE_SCROLL.register(this::allowMouseScroll);
		ItemRenderEvents.TOOLTIP_TRACKER.register(this::onTooltipTracker);
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock() && ConfigManager.getConfig().vanilla.scrollableTooltip.enabled;
	}

	public int getXOffset() {
		return isEnabled() ? MathUtils.floor(currentXOffset) : 0;
	}

	public int getYOffset() {
		return isEnabled() ? MathUtils.floor(currentYOffset) : 0;
	}

	public void initOffsetY(int offsetY) {
		yOffset += offsetY;
		currentYOffset = yOffset;
	}

	public boolean canStartFromTop() {
		return ConfigManager.getConfig().vanilla.scrollableTooltip.startOnTop && !hasMoved;
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

		if (Client.isKeyPressed(GLFW.GLFW_KEY_LEFT_SHIFT)) {
			if (vertical > 0) {
				scrollLeft();
			} else if (vertical < 0) {
				scrollRight();
			}
		} else {
			if (vertical > 0) {
				if (reverseScrollConfig.getAsBoolean()) {
					scrollDown();
				} else {
					scrollUp();
				}
			} else if (vertical < 0) {
				if (reverseScrollConfig.getAsBoolean()) {
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

	@EventHandler(event = "ItemRenderEvents.TOOLTIP_TRACKER")
	private void onTooltipTracker(List<ClientTooltipComponent> tooltipComponents) {
		if (!isEnabled()) return;

		if (tooltipComponents != null && tooltipComponents.size() < MIN_SCROLLABLE_TOOLTIPS) {
			resetScroll();
			return;
		}

		if (COOLDOWN.testSilently()) {
			resetScroll();
		}

		COOLDOWN.reset();

		update();
		setTooltipComponents(tooltipComponents);
	}

	private void update() {
		currentXOffset += (xOffset - currentXOffset) * SMOOTHNESS_MULTIPLIER;
		currentYOffset += (yOffset - currentYOffset) * SMOOTHNESS_MULTIPLIER;
	}

	private void setTooltipComponents(List<ClientTooltipComponent> tooltipComponents) {
		if (!isEqual(currentTooltips, tooltipComponents)) {
			resetScroll();
			currentTooltips = tooltipComponents;
		}
	}

	private void scrollUp() {
		if (!COOLDOWN.testSilently()) yOffset -= SCROLL_AMOUNT;
		hasMoved = true;
	}

	private void scrollDown() {
		if (!COOLDOWN.testSilently()) yOffset += SCROLL_AMOUNT;
		hasMoved = true;
	}

	private void scrollLeft() {
		if (!COOLDOWN.testSilently()) xOffset -= SCROLL_AMOUNT;
		hasMoved = true;
	}

	private void scrollRight() {
		if (!COOLDOWN.testSilently()) xOffset += SCROLL_AMOUNT;
		hasMoved = true;
	}

	private void resetScroll() {
		currentXOffset = 0;
		currentYOffset = 0;
		xOffset = 0;
		yOffset = 0;
		hasMoved = false;
	}

	private boolean isEqual(@Nullable List<ClientTooltipComponent> item1, @Nullable List<ClientTooltipComponent> item2) {
		if (item1 == null || item2 == null || item1.size() != item2.size()) {
			return false;
		}

		for (int i = 0; i < item1.size(); ++i) {
			if (item1.get(i) instanceof ClientTextTooltip
					&& !(item2.get(i) instanceof ClientTextTooltip)) {
				return false;
			}

			if (item2.get(i) instanceof ClientTextTooltip
					&& !(item1.get(i) instanceof ClientTextTooltip)) {
				return false;
			}

			if (!(item1.get(i) instanceof ClientTextTooltip)
					&& !(item2.get(i) instanceof ClientTextTooltip)) {
				continue;
			}

			String text1 = OrderedTextReader.read(((ClientTextTooltipAccessor) item1.get(i)).getText());
			String text2 = OrderedTextReader.read(((ClientTextTooltipAccessor) item2.get(i)).getText());
			if (!text1.equals(text2)) {
				return false;
			}
		}

		return true;
	}

	private static final class OrderedTextReader {
		private static class Visitor implements FormattedCharSink {
			private int finalIndex = -1;
			private final StringBuilder builder = new StringBuilder();

			Visitor() {
			}

			@Override
			public boolean accept(int index, @NotNull Style style, int codePoint) {
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

		public static @NotNull String read(@NotNull FormattedCharSequence text) {
			Visitor visitor = new Visitor();
			text.accept(visitor);
			return visitor.getString();
		}
	}
}
