package fr.siroz.cariboustonks.events;

import java.util.List;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Events related to client gui.
 */
public final class GuiEvents {

	private GuiEvents() {
	}

	/**
	 * Called when a screen is closed
	 */
	public static final Event<ScreenClose> SCREEN_CLOSE_EVENT = EventFactory.createArrayBacked(ScreenClose.class, listeners -> (screen) -> {
		for (ScreenClose listener : listeners) {
			listener.onScreenClose(screen);
		}
	});

	/**
	 * Called when a key is pressed within a screen.
	 */
	public static final Event<KeyPress> SCREEN_KEY_PRESS_EVENT = EventFactory.createArrayBacked(KeyPress.class, listeners -> (screen, keyInput, slot) -> {
		for (KeyPress listener : listeners) {
			listener.onKeyPressed(screen, keyInput, slot);
		}
	});

	/**
	 * Allows getting the last rendering phase of a Tooltip for an ItemStack in
	 * {@link net.minecraft.client.gui.screens.inventory.AbstractContainerScreen} with {@link GuiGraphics}.
	 * <p>
	 * Not to be confused with {@link net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback} which only manages
	 * the Tooltip lines without handling the general appearance of the Tooltip, such as background or dimensions.
	 */
	public static final Event<PostTooltip> POST_TOOLTIP_EVENT = EventFactory.createArrayBacked(PostTooltip.class, listeners -> (guiGraphics, itemStack, x, y, width, height, font, components) -> {
		for (PostTooltip listener : listeners) {
			listener.onPostTooltip(guiGraphics, itemStack, x, y, width, height, font, components);
		}
	});

	/**
	 * Called when a {@link ItemStack} append Tooltip's Component.
	 * <p>
	 * Allows adding one or more lines to the Tooltip after the existing one
	 */
	public static final Event<TooltipAppender> TOOLTIP_APPENDER_EVENT = EventFactory.createArrayBacked(TooltipAppender.class, listeners -> (itemStack, lore) -> {
		for (TooltipAppender listener : listeners) {
			return listener.lines(itemStack, lore);
		}
		return null;
	});

	/**
	 * Called when a {@link GuiGraphics} draw the ItemStack {@code Tooltips}
	 */
	public static final Event<TooltipTracker> TOOLTIP_TRACKER_EVENT = EventFactory.createArrayBacked(TooltipTracker.class, listeners -> (components) -> {
		for (TooltipTracker listener : listeners) {
			listener.onTooltipTracker(components);
		}
	});

	@FunctionalInterface
	public interface ScreenClose {
		void onScreenClose(@NonNull Screen screen);
	}

	@FunctionalInterface
	public interface KeyPress {
		void onKeyPressed(@NonNull Screen screen, @NonNull KeyEvent input, @NonNull Slot slot);
	}

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
