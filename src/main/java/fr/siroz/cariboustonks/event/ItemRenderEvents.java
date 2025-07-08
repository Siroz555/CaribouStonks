package fr.siroz.cariboustonks.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Events related to rendering item tooltips and associated behaviors.
 */
public final class ItemRenderEvents {

	private ItemRenderEvents() {
	}

	/**
	 * Allows getting the last rendering phase of a Tooltip for an ItemStack in
	 * {@link net.minecraft.client.gui.screen.ingame.HandledScreen} with {@link DrawContext}.
	 * <p>
	 * Not to be confused with {@link net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback} which only manages
	 * the Tooltip lines without handling the general appearance of the Tooltip, such as background or dimensions.
	 */
	public static final Event<PostTooltip> POST_TOOLTIP = EventFactory.createArrayBacked(PostTooltip.class, listeners -> (context, itemStack, x, y, width, height, textRenderer, components) -> {
		for (PostTooltip listener : listeners) {
			listener.onPostTooltip(context, itemStack, x, y, width, height, textRenderer, components);
		}
	});

	/**
	 * Called when a {@link ItemStack} append Tooltip's Component.
	 * <p>
	 * Allows adding one or more lines to the Tooltip after the existing one
	 */
	public static final Event<TooltipAppender> TOOLTIP_APPENDER = EventFactory.createArrayBacked(TooltipAppender.class, listeners -> (itemStack, lore) -> {
		for (TooltipAppender listener : listeners) {
			return listener.lines(itemStack, lore);
		}
		return null;
	});

	/**
	 * Called when a {@link DrawContext} draw the ItemStack {@code Tooltips}
	 */
	public static final Event<TooltipTracker> TOOLTIP_TRACKER = EventFactory.createArrayBacked(TooltipTracker.class, listeners -> (components) -> {
		for (TooltipTracker listener : listeners) {
			listener.onTooltipTracker(components);
		}
	});

	@FunctionalInterface
	public interface PostTooltip {
		void onPostTooltip(DrawContext context, ItemStack itemStack, int x, int y, int width, int height, TextRenderer textRenderer, List<TooltipComponent> components);
	}

	@FunctionalInterface
	public interface TooltipAppender {
		@Nullable LoreComponent lines(ItemStack itemStack, LoreComponent lore);
	}

	@FunctionalInterface
	public interface TooltipTracker {
		void onTooltipTracker(List<TooltipComponent> components);
	}
}
