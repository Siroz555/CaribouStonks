package fr.siroz.cariboustonks.core.component;

import fr.siroz.cariboustonks.util.render.gui.ColorHighlight;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * {@code Component} used for creating overlays within a container.
 * <p>
 * This follows a two-phase rendering architecture:
 * <ol>
 *   <li>The {@link #analyzeContent(Int2ObjectMap)} method analyzes container items and collects data</li>
 *   <li>The {@link #render(GuiGraphics, int, int, int, int)} method uses this data to render visual elements</li>
 * </ol>
 * <p>
 * Even when no highlights are needed, the {@code content} method provides access to container
 * items for data collection, which can then be used by the {@code render} method to create
 * customized overlays. The separation of data collection and rendering
 * allows for more flexible overlay implementations.
 * <p>
 * To use this Component correctly, the feature class must also
 * have the {@link ContainerMatcherComponent} to define where and how
 * the overlay will be applied. This trait allows specifying a pattern to detect
 * the containers with which the overlay will be associated.
 *
 * @see ContainerMatcherComponent
 */
public final class ContainerOverlayComponent implements Component {
	private final ContentAnalyzer contentAnalyzer;
	private final OverlayRenderer renderer;
	private final ResetHandler resetHandler;

	private ContainerOverlayComponent(
			ContentAnalyzer contentAnalyzer,
			OverlayRenderer renderer,
			ResetHandler resetHandler
	) {
		this.contentAnalyzer = contentAnalyzer;
		this.renderer = renderer;
		this.resetHandler = resetHandler;
	}

	/**
	 * Analyzes container content and returns highlights.
	 */
	@NonNull
	public List<ColorHighlight> analyzeContent(@NonNull Int2ObjectMap<ItemStack> slots) {
		return contentAnalyzer.analyze(slots);
	}

	/**
	 * Renders the overlay visuals.
	 */
	public void render(GuiGraphics guiGraphics, int screenWidth, int screenHeight, int x, int y) {
		if (renderer != null) {
			renderer.render(guiGraphics, screenWidth, screenHeight, x, y);
		}
	}

	/**
	 * Resets the overlay state.
	 */
	public void reset() {
		if (resetHandler != null) {
			resetHandler.reset();
		}
	}

	@NonNull
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Functional interface for analyzing container content.
	 */
	@FunctionalInterface
	public interface ContentAnalyzer {
		@NonNull List<ColorHighlight> analyze(@NonNull Int2ObjectMap<ItemStack> slots);
	}

	/**
	 * Functional interface for rendering overlay visuals.
	 */
	@FunctionalInterface
	public interface OverlayRenderer {
		void render(@NonNull GuiGraphics guiGraphics, int screenWidth, int screenHeight, int x, int y);
	}

	/**
	 * Functional interface for resetting overlay state.
	 */
	@FunctionalInterface
	public interface ResetHandler {
		void reset();
	}

	public static class Builder {
		private ContentAnalyzer contentAnalyzer;
		private OverlayRenderer renderer;
		private ResetHandler resetHandler;

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
		 * @param analyzer the ContentAnalyzer with a mapping of slot indices to the {@code ItemStack} objects present in each slot.
		 * @return Builder
		 */
		public Builder content(@NonNull ContentAnalyzer analyzer) {
			this.contentAnalyzer = analyzer;
			return this;
		}

		/**
		 * Renders the overlay visuals within the container. (Optional)
		 *
		 * @param renderer the gui renderer used for rendering graphical elements.
		 * @return Builder
		 */
		public Builder render(@Nullable OverlayRenderer renderer) {
			this.renderer = renderer;
			return this;
		}

		/**
		 * Resets the state of the overlay (Optional)
		 * <p>
		 * Clearing any customizations or configurations that
		 * may have been applied. This is typically invoked to ensure the overlay is in a neutral
		 * state when it is no longer active or associated with a container.
		 *
		 * @param handler the reset handler
		 * @return Builder
		 */
		public Builder onReset(@Nullable ResetHandler handler) {
			this.resetHandler = handler;
			return this;
		}

		public ContainerOverlayComponent build() {
			Objects.requireNonNull(contentAnalyzer, "Content analyzer must be set");

			return new ContainerOverlayComponent(contentAnalyzer, renderer, resetHandler);
		}
	}
}
