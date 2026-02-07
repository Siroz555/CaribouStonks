package fr.siroz.cariboustonks.core.module.waypoint;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.core.module.waypoint.options.IconOption;
import fr.siroz.cariboustonks.core.module.waypoint.options.TextOption;
import fr.siroz.cariboustonks.system.WaypointSystem;
import fr.siroz.cariboustonks.util.Ticks;
import fr.siroz.cariboustonks.util.colors.Color;
import fr.siroz.cariboustonks.util.colors.Colors;
import fr.siroz.cariboustonks.util.position.Position;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * The {@code Waypoint} class represents a visual marker in the world that can be rendered in various forms.
 * It provides a flexible and customizable way to highlight positions, areas,
 * or points of interest in the world.
 * <h3>Basic Waypoint Creation:</h3>
 * <pre>{@code
 * Waypoint waypoint = Waypoint.builder(Position.ORIGIN)
 *     .type(Type.BEAM)
 *     .color(Colors.RED)
 *     .buildAndRegister();
 * }</pre>
 *
 * <h3>Advanced Configuration:</h3>
 * <pre>{@code
 * Waypoint waypoint = Waypoint.builder(Position.of(0, 69, 0))
 *     .type(Type.WAYPOINT)
 *     .color(Colors.BLUE)
 *     .alpha(0.75f)
 *     .timeout(30, TimeUnit.SECONDS)
 *     .textOption(TextOption.builder()
 *          .setText("Important Location")
 *          .build())
 *     .destroyListener(wp -> System.out.println("Waypoint destroyed!"))
 *     .buildAndRegister();
 * }</pre>
 *
 * <h2>Rendering:</h2>
 * Waypoints can be rendered in two ways:
 * <ul>
 *   <li>Through the {@link WaypointRenderer} associated with each waypoint instance</li>
 *   <li>Via the {@link WaypointSystem} which manages all registered waypoints</li>
 * </ul>
 *
 * <h2>Types of Waypoints:</h2>
 * <ul>
 *   <li>{@link Type#BEAM} - Renders a colored beacon beam</li>
 *   <li>{@link Type#WAYPOINT} - Combines a beacon beam with a box at its base</li>
 *   <li>{@link Type#OUTLINED_WAYPOINT} - Similar to WAYPOINT but with an outlined box</li>
 *   <li>{@link Type#HIGHLIGHT} - Highlights block(s) with a filled box</li>
 *   <li>{@link Type#OUTLINED_HIGHLIGHT} - Highlights block(s) with an outlined box</li>
 *   <li>{@link Type#OUTLINE} - Creates only an outline around the specified block(s)</li>
 * </ul>
 * <h2>Integration:</h2>
 * <pre>{@code
 * // Registering with WaypointSystem
 * WaypointSystem ws = CaribouStonks.systems().getSystem(WaypointSystem.class);
 * ws.addWaypoint(waypoint);
 *
 * // Manual rendering
 * waypoint.getRenderer().render(WorldRenderContext);
 * }</pre>
 * <p>
 * Each Waypoint instance is identified by a unique {@link UUID} and can be configured
 * with various visual options including text labels, icons, and rendering properties.
 *
 * @see WaypointSystem
 * @see WaypointRenderer
 * @see TextOption
 * @see IconOption
 * @see Type
 */
public final class Waypoint {
	private Position position;
	private AABB box;
	private Type type;
	private boolean enabled;
	private final UUID uuid;
	private int timeoutTicks;
	private final boolean resetBetweenWorlds;

	private Color color;
	private float alpha;

	private final float boxLineWidth;
	private final boolean boxThroughBlocks;

	private final Consumer<Waypoint> destroyListener;

	private final TextOption textOption;
	private final IconOption iconOption;

	private final WaypointRenderer renderer;

	private Waypoint(
			@NonNull Position position,
			@NonNull Type type,
			boolean enabled,
			@NonNull UUID uuid,
			int timeoutTicks,
			boolean resetBetweenWorlds,
			@NonNull Color color,
			float alpha,
			float boxLineWidth,
			boolean boxThroughBlocks,
			@Nullable Consumer<Waypoint> destroyListener,
			@NonNull TextOption textOption,
			@NonNull IconOption iconOption
	) {
		this.position = position;
		this.box = new AABB(position.toBlockPos());
		this.type = type;
		this.enabled = enabled;
		this.uuid = uuid;
		this.timeoutTicks = timeoutTicks;
		this.resetBetweenWorlds = resetBetweenWorlds;
		this.color = color;
		this.alpha = alpha;
		this.boxLineWidth = boxLineWidth;
		this.boxThroughBlocks = boxThroughBlocks;
		this.destroyListener = destroyListener;
		this.textOption = textOption;
		this.iconOption = iconOption;
		this.renderer = new WaypointRenderer(this);
	}

	/**
	 * Creates a new {@link Builder} instance for constructing a {@link Waypoint} object.
	 *
	 * @param position the initial {@link Position} for the new Waypoint. This position represents
	 *                 the location of the Waypoint in the world and is required to initialize
	 *                 the builder.
	 * @return a new {@link Builder} instance configured with the specified position.
	 */
	public static @NonNull Builder builder(@NonNull Position position) {
		return new Builder(position);
	}

	/**
	 * Creates a new {@link Waypoint} instance using a builder pattern.
	 * This method provides a way to configure and construct a {@link Waypoint}
	 * instance through the provided {@link Consumer} of {@link Builder}.
	 *
	 * @param builderConsumer a {@link Consumer} that accepts a {@link Builder}.
	 *                        The consumer is used to configure the builder
	 *                        with the desired properties for the {@link Waypoint}.
	 * @return a newly constructed {@link Waypoint} instance with the configuration
	 * specified by the provided {@link Builder} consumer.
	 */
	public static Waypoint builder(@NonNull Consumer<Builder> builderConsumer) {
		Builder builder = builder(Position.ORIGIN);
		builderConsumer.accept(builder);
		return builder.build();
	}

	/**
	 * Retrieves the current position associated with this Waypoint.
	 *
	 * @return the current {@link Position} of the Waypoint. This position is immutable and represents
	 * the Waypoint's coordinates in the world.
	 */
	@NonNull
	public Position getPosition() {
		return position;
	}

	/**
	 * Updates the current position of the Waypoint and adjusts the associated bounding box
	 * based on the given position.
	 *
	 * @param position the new {@link Position} to update the Waypoint to
	 */
	public void updatePosition(@NonNull Position position) {
		this.position = position;
		this.box = new AABB(position.toBlockPos());
	}

	public void updateType(@NonNull Type type) {
		this.type = type;
	}

	/**
	 * Returns the {@link AABB} associated with this {@link Waypoint}.
	 *
	 * @return the {@link AABB} instance for this Waypoint
	 */
	public AABB getBox() {
		return box;
	}

	/**
	 * Returns the {@link Type} of this Waypoint. The Type determines the visual
	 * and functional behavior of the Waypoint within the renderer.
	 *
	 * @return the {@link Type} associated with this Waypoint
	 */
	@NonNull
	public Type getType() {
		return type;
	}

	/**
	 * Determines whether the Waypoint is currently enabled.
	 *
	 * @return {@code true} if the Waypoint is enabled, {@code false} otherwise
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * Enables or disables the Waypoint.
	 *
	 * @param enabled a boolean indicating whether the Waypoint should be enabled
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * Returns the unique identifier (UUID) of this Waypoint.
	 *
	 * @return the {@link UUID} associated with this Waypoint
	 */
	@NonNull
	public UUID getUuid() {
		return uuid;
	}

	public int getTimeoutTicks() {
		return timeoutTicks;
	}

	public void decreaseTimeout() {
		if (timeoutTicks > 0) {
			timeoutTicks--;
		}
	}

	public boolean isResetBetweenWorlds() {
		return resetBetweenWorlds;
	}

	/**
	 * Returns the color associated with this Waypoint.
	 *
	 * @return the {@link Color} of the Waypoint
	 */
	@NonNull
	public Color getColor() {
		return color;
	}

	/**
	 * Updates the color of the Waypoint.
	 *
	 * @param color the new {@link Color} to set for the Waypoint
	 */
	public void updateColor(@NonNull Color color) {
		this.color = color;
	}

	/**
	 * Retrieves the alpha transparency value of the Waypoint.
	 *
	 * @return the alpha value as a floating-point number
	 */
	public float getAlpha() {
		return alpha;
	}

	/**
	 * Updates the alpha of the Waypoint.
	 *
	 * @param alpha the new {@code Alpha} to set for the Waypoint
	 */
	public void updateAlpha(float alpha) {
		this.alpha = alpha;
	}

	/**
	 * Retrieves the width of the box line associated with the Waypoint.
	 *
	 * @return the line width of the box as a floating-point value
	 */
	public float getBoxLineWidth() {
		return boxLineWidth;
	}

	/**
	 * Determines whether the box associated with this Waypoint is visible through blocks.
	 *
	 * @return {@code true} if the box is visible through blocks, {@code false} otherwise
	 */
	public boolean isBoxThroughBlocks() {
		return boxThroughBlocks;
	}

	/**
	 * Destroys the current Waypoint instance.
	 * <p>
	 * If a destroyListener is defined, it is invoked by passing this Waypoint instance and
	 * removes this Waypoint instance from the {@link WaypointSystem}.
	 */
	public void destroy() {
		if (destroyListener != null) {
			destroyListener.accept(this);
		}

		CaribouStonks.systems().getSystem(WaypointSystem.class).removeWaypoint(this);
	}

	/**
	 * Returns the {@link TextOption} associated with this {@link Waypoint}.
	 *
	 * @return the {@link TextOption} instance for this Waypoint
	 */
	public TextOption getTextOption() {
		return textOption;
	}

	/**
	 * Returns the {@link IconOption} associated with this Waypoint.
	 *
	 * @return the {@link IconOption} instance for this Waypoint
	 */
	public IconOption getIconOption() {
		return iconOption;
	}

	/**
	 * Returns the {@link WaypointRenderer} associated with this {@link Waypoint}.
	 *
	 * @return the {@link WaypointRenderer} responsible for rendering this Waypoint
	 */
	public WaypointRenderer getRenderer() {
		return renderer;
	}

	/**
	 * A builder class used to configure and construct {@link Waypoint} instances.
	 * Provides methods to set various parameters of a waypoint including position,
	 * type, color, alpha value, and other options, offering a flexible
	 * and fluent-style API for creation.
	 * <p>
	 * The builder supports both creating and registering the waypoint directly
	 * into the {@link WaypointSystem}, or simply constructing it for manual use.
	 */
	public static class Builder {
		private Position position;
		private Type waypointType = Type.WAYPOINT;
		private boolean enabled = true;
		private UUID uuid = UUID.randomUUID();
		private int timeoutTicks = -1;
		private boolean resetBetweenWorlds = false;

		private float alpha = 0.5f;
		private Color color = Colors.RED;

		private float boxLineWidth = 1f;
		private boolean boxThroughBlocks = true;
		private Consumer<Waypoint> destroyListener = null;

		private TextOption textOption = new TextOption();
		private IconOption iconOption = new IconOption();

		/**
		 * Constructs a new {@code Builder} instance with the specified position.
		 *
		 * @param position the {@link Position} associated with this builder
		 */
		public Builder(@NonNull Position position) {
			this.position = position;
		}

		/**
		 * Sets the {@code Position} for the builder.
		 *
		 * @param position the {@link Type} to set
		 * @return the builder instance for chaining
		 */
		public Builder position(@NonNull Position position) {
			this.position = position;
			return this;
		}

		/**
		 * Sets the {@code Type} for this builder. The type determines the visual representation
		 * and behavior of the waypoint.
		 *
		 * @param waypointType the {@link Type} to assign to the waypoint
		 * @return the builder instance for method chaining
		 */
		public Builder type(@NonNull Type waypointType) {
			this.waypointType = waypointType;
			return this;
		}

		/**
		 * Sets the enabled state for the {@code Builder}.
		 * <p>
		 * Default to {@code true}.
		 *
		 * @param enabled a boolean indicating whether the enabled state should be set to true or false
		 * @return the builder instance for method chaining
		 */
		public Builder enabled(boolean enabled) {
			this.enabled = enabled;
			return this;
		}

		public Builder uuid(@NonNull UUID uuid) {
			this.uuid = uuid;
			return this;
		}

		/**
		 * Sets the timeout duration before the Waypoint expires.
		 * The duration is internally converted into game ticks from the specified time unit.
		 *
		 * @param timeout the duration of the timeout
		 * @param unit    the time unit of the timeout duration
		 * @return the builder instance for method chaining
		 */
		public Builder timeout(int timeout, @NonNull TimeUnit unit) {
			this.timeoutTicks = Ticks.from(timeout, unit);
			return this;
		}

		public Builder resetBetweenWorlds(boolean resetBetweenWorlds) {
			this.resetBetweenWorlds = resetBetweenWorlds;
			return this;
		}

		/**
		 * Sets the {@code Color} for this builder.
		 *
		 * @param color the {@link Color} to assign to the builder
		 * @return the builder instance for method chaining
		 */
		public Builder color(@NonNull Color color) {
			this.color = color;
			return this;
		}

		/**
		 * Sets the alpha transparency value for the builder.
		 * The alpha value determines the transparency level, where 0 is fully transparent and 1 is fully opaque.
		 *
		 * @param alpha the transparency value to set, ranging from 0.0 to 1.0
		 * @return the builder instance for method chaining
		 */
		public Builder alpha(float alpha) {
			this.alpha = alpha;
			return this;
		}

		/**
		 * Sets the line width for the outline box to be rendered in this builder.
		 *
		 * @param boxLineWidth the width of the box's line; must be a positive floating-point value
		 * @return the builder instance for method chaining
		 */
		public Builder boxLineWidth(float boxLineWidth) {
			this.boxLineWidth = boxLineWidth;
			return this;
		}

		/**
		 * Sets whether the outline box should render through blocks.
		 *
		 * @param boxThroughBlocks a boolean indicating whether the box should be rendered through blocks
		 * @return the builder instance for method chaining
		 */
		public Builder boxThroughBlocks(boolean boxThroughBlocks) {
			this.boxThroughBlocks = boxThroughBlocks;
			return this;
		}

		/**
		 * Sets the destroy listener for the waypoint. The destroy listener is invoked
		 * when the waypoint is destroyed.
		 *
		 * @param destroyListener a {@link Consumer} that accepts a {@link Waypoint} instance,
		 *                        representing the action to be performed when the waypoint is destroyed
		 * @return the builder instance for method chaining
		 */
		public Builder destroyListener(@NonNull Consumer<Waypoint> destroyListener) {
			this.destroyListener = destroyListener;
			return this;
		}

		/**
		 * Sets the {@link TextOption} for the builder. The {@code TextOption} specifies
		 * the text-related configuration for the waypoint, such as its content, visibility
		 * through blocks, vertical offset, and distance visibility.
		 *
		 * @param textOption the {@link TextOption} instance to use for the waypoint's text configuration
		 * @return the builder instance for chaining
		 */
		public Builder textOption(@NonNull TextOption textOption) {
			this.textOption = textOption;
			return this;
		}

		/**
		 * Sets the {@link IconOption} for the builder. The {@code IconOption} specifies
		 * the settings related to the waypoint's icon, such as its dimensions, render offset,
		 * color, transparency, and whether it scales with distance or renders through blocks.
		 *
		 * @param iconOption the {@link IconOption} instance to configure the waypoint's icon
		 * @return the builder instance for method chaining
		 */
		public Builder iconOption(@NonNull IconOption iconOption) {
			this.iconOption = iconOption;
			return this;
		}

		/**
		 * Builds a {@link Waypoint} instance using the current state of the {@code Builder}.
		 * The {@code Waypoint} is instantiated with the configured position, type, enabled state,
		 * timeout duration, color, transparency level, box properties, destroy listener, text configuration,
		 * and icon configuration.
		 *
		 * @return a new {@link Waypoint} instance reflecting the current state of the builder
		 */
		public Waypoint build() {
			return new Waypoint(
					position,
					waypointType,
					enabled,
					uuid,
					timeoutTicks,
					resetBetweenWorlds,
					color,
					alpha,
					boxLineWidth,
					boxThroughBlocks,
					destroyListener,
					textOption,
					iconOption
			);
		}

		/**
		 * Constructs a new {@link Waypoint} instance using the current state of the {@code Builder}
		 * and registers it with the {@link WaypointSystem}.
		 * <p>
		 * The method calls the {@code build()} method to create the {@link Waypoint} and then
		 * registers the resulting waypoint with the {@code WaypointSystem} for further management.
		 *
		 * @return the newly created and registered {@link Waypoint} instance
		 */
		@SuppressWarnings("UnusedReturnValue")
		public Waypoint buildAndRegister() {
			Waypoint waypoint = build();
			CaribouStonks.systems().getSystem(WaypointSystem.class).addWaypoint(waypoint);
			return waypoint;
		}
	}

	/**
	 * Represents the different types of a {@link Waypoint}. Each type defines the visual
	 * and functional behavior of the corresponding waypoint in the context of the rendering
	 * and interaction within the application.
	 */
	public enum Type {
		/**
		 * Renders a colored beacon beam
		 */
		BEAM,
		/**
		 * Combines a beacon beam with a box at its base
		 */
		WAYPOINT,
		/**
		 * Similar to WAYPOINT but with an outlined box
		 */
		OUTLINED_WAYPOINT,
		/**
		 * Highlights block(s) with a filled box
		 */
		HIGHLIGHT,
		/**
		 * Highlights block(s) with an outlined box
		 */
		OUTLINED_HIGHLIGHT,
		/**
		 * Creates only an outline around the specified block(s)
		 */
		OUTLINE
	}
}
