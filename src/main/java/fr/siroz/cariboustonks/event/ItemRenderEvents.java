package fr.siroz.cariboustonks.event;

import java.util.List;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Events related to rendering item tooltips and associated behaviors.
 */
public final class ItemRenderEvents {

	private ItemRenderEvents() {
	}

	/**
	 * Allows getting the last rendering phase of a Tooltip for an ItemStack in
	 * {@link net.minecraft.client.gui.screens.inventory.AbstractContainerScreen} with {@link GuiGraphics}.
	 * <p>
	 * Not to be confused with {@link net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback} which only manages
	 * the Tooltip lines without handling the general appearance of the Tooltip, such as background or dimensions.
	 */
	public static final Event<@NotNull PostTooltip> POST_TOOLTIP = EventFactory.createArrayBacked(PostTooltip.class, listeners -> (guiGraphics, itemStack, x, y, width, height, font, components) -> {
		for (PostTooltip listener : listeners) {
			listener.onPostTooltip(guiGraphics, itemStack, x, y, width, height, font, components);
		}
	});

	/**
	 * Called when a {@link ItemStack} append Tooltip's Component.
	 * <p>
	 * Allows adding one or more lines to the Tooltip after the existing one
	 */
	public static final Event<@NotNull TooltipAppender> TOOLTIP_APPENDER = EventFactory.createArrayBacked(TooltipAppender.class, listeners -> (itemStack, lore) -> {
		for (TooltipAppender listener : listeners) {
			return listener.lines(itemStack, lore);
		}
		return null;
	});

	/**
	 * Called when a {@link GuiGraphics} draw the ItemStack {@code Tooltips}
	 */
	public static final Event<@NotNull TooltipTracker> TOOLTIP_TRACKER = EventFactory.createArrayBacked(TooltipTracker.class, listeners -> (components) -> {
		for (TooltipTracker listener : listeners) {
			listener.onTooltipTracker(components);
		}
	});

	@FunctionalInterface
	public interface PostTooltip {
		void onPostTooltip(GuiGraphics guiGraphics, ItemStack itemStack, int x, int y, int width, int height, Font font, List<ClientTooltipComponent> components);
	}

	@FunctionalInterface
	public interface TooltipAppender {
		@Nullable ItemLore lines(ItemStack itemStack, ItemLore lore);
	}

	@FunctionalInterface
	public interface TooltipTracker {
		void onTooltipTracker(List<ClientTooltipComponent> components);
	}
}
