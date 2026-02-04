package fr.siroz.cariboustonks.core.component;

import java.util.List;
import java.util.Objects;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * {@code Component} that allows appending custom {@code tooltip} to items within a container.
 * <p>
 * To use this Component correctly, the feature class must also
 * have the {@link ContainerMatcherComponent} to define where and how
 * the appender will be applied. This trait allows specifying a pattern to detect
 * the containers with which the appender will be associated.
 *
 * @see ContainerMatcherComponent
 */
public final class TooltipAppenderComponent implements Component { // SIROZ-NOTE: documentation
	private final int priority;
	private final AppenderProvider provider;

	private TooltipAppenderComponent(int priority, AppenderProvider provider) {
		this.priority = priority;
		this.provider = provider;
	}

	public int getPriority() {
		return priority;
	}

	public void appendToTooltip(@Nullable Slot focusedSlot, @NonNull ItemStack stack, @NonNull List<net.minecraft.network.chat.Component> lines) {
		provider.append(focusedSlot, stack, lines);
	}

	@NonNull
	public static Builder builder() {
		return new Builder();
	}

	@FunctionalInterface
	public interface AppenderProvider {
		void append(@Nullable Slot focusedSlot, @NonNull ItemStack item, @NonNull List<net.minecraft.network.chat.Component> lines);
	}

	public static class Builder {
		private Integer priority;
		private AppenderProvider provider;

		/**
		 * The priority of this tooltip appender. Higher values indicate lower priority,
		 * meaning the appender will insert its tooltip content closer to the bottom.
		 *
		 * @param priority an integer representing the priority
		 * @return Builder
		 */
		public Builder priority(int priority) {
			this.priority = priority;
			return this;
		}

		/**
		 * Appends custom tooltip information to an item within a container.
		 * <h3>Append:</h3>
		 * <pre>{@code
		 * lines.add(Text.literal("A"));
		 * lines.add(Text.empty());
		 * lines.add(Text.literal("Z"));
		 * }</pre>
		 *
		 * @param provider the appender provider
		 * @return Builder
		 */
		public Builder appender(@NonNull AppenderProvider provider) {
			this.provider = provider;
			return this;
		}

		public TooltipAppenderComponent build() {
			Objects.requireNonNull(priority, "Priority must be set");
			Objects.requireNonNull(provider, "Provider must be set");
			return new TooltipAppenderComponent(priority, provider);
		}
	}
}
