package fr.siroz.cariboustonks.system.container.overlay;

import fr.siroz.cariboustonks.system.container.ContainerMatcherTrait;
import fr.siroz.cariboustonks.util.render.gui.ColorHighlight;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Represents an interface used for creating overlays within a container.
 * Implementations of this interface provide mechanisms for highlighting specific content
 * within container UIs or providing additional visual overlays.
 * <p>
 * This interface follows a two-phase rendering architecture:
 * <ol>
 *   <li>The {@link #content(Int2ObjectMap)} method analyzes container items and collects data</li>
 *   <li>The {@link #render(GuiGraphics, int, int, int, int)} method uses this data to render visual elements</li>
 * </ol>
 * <p>
 * Even when no highlights are needed, the {@code content} method provides access to container
 * items for data collection, which can then be used by the {@code render} method to create
 * customized overlays. The separation of data collection and rendering
 * allows for more flexible overlay implementations.
 * <p>
 * To use this interface correctly, the implementing class must also
 * implement the {@link ContainerMatcherTrait} to define where and how
 * the overlay will be applied. This trait allows specifying a pattern to detect
 * the containers with which the overlay will be associated.
 * <p>
 * Without implementing the {@code ContainerMatcherTrait}, the overlay will not
 * be properly associated with containers.
 */
public interface ContainerOverlay {

	/**
	 * Defines the content highlights to be displayed for specific slots in a container.
	 * This method processes the provided slot-to-item mappings, determining which slots
	 * should have visual highlights and their corresponding highlight colors.
	 * <p>
	 * This method can return an empty list if no highlights are needed, but it still provides
	 * access to container items for gathering information. The collected data can then be used
	 * by the {@link #render} method to display custom overlays or visual elements.
	 * <p>
	 * The relationship between this method and {@code render} allows for a two-step process:
	 * first gathering information about items, then using that information to render appropriate visuals.
	 *
	 * @param slots a mapping of slot indices to the {@code ItemStack} objects present in each slot.
	 *              Each key represents a slot index, and each value represents the item present in that slot.
	 * @return a list of {@code ColorHighlight} objects defining the slots to be highlighted and their respective colors.
	 * The entries in the list determine how and where visual highlights are rendered in the container.
	 */
	@NotNull
	List<ColorHighlight> content(@NotNull Int2ObjectMap<ItemStack> slots);

	/**
	 * Renders the overlay visuals within the container.
	 *
	 * @param guiGraphics  the {@link GuiGraphics} used for rendering graphical elements.
	 * @param screenWidth  the width of the screen in pixels
	 * @param screenHeight the height of the screen in pixels
	 * @param x            the X-coordinate offset from the screen
	 * @param y            the Y-coordinate offset from the screen
	 */
	default void render(@NotNull GuiGraphics guiGraphics, int screenWidth, int screenHeight, int x, int y) {
	}

	/**
	 * Resets the state of the overlay, clearing any customizations or configurations that
	 * may have been applied. This is typically invoked to ensure the overlay is in a neutral
	 * state when it is no longer active or associated with a container.
	 */
	default void reset() {
	}
}
