package fr.siroz.cariboustonks.core.component;

import fr.siroz.cariboustonks.core.module.gui.MatcherTrait;
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
 * have the {@link MatcherTrait} to define where and how
 * the appender will be applied. This trait allows specifying a pattern to detect
 * the containers with which the appender will be associated.
 *
 * @see MatcherTrait
 */
public final class TooltipAppenderComponent implements Component { // SIROZ-NOTE: documentation
	private final int priority;
	private final MatcherTrait trait;
	private final AppenderProvider provider;

	private TooltipAppenderComponent(int priority, MatcherTrait trait, AppenderProvider provider) {
		this.priority = priority;
		this.trait = trait;
		this.provider = provider;
	}

	public int getPriority() {
		return priority;
	}

	/**
	 * Returns the {@code Trait} matching for this Component.
	 *
	 * @return the {@link MatcherTrait}
	 */
	@NonNull
	public MatcherTrait getTrait() {
		return trait;
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
		private MatcherTrait trait;
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
		 * Sets the {@link MatcherTrait} to provide a container (Screen) matching
		 *
		 * @param trait the trait
		 * @return Builder
		 */
		public Builder trait(@NonNull MatcherTrait trait) {
			this.trait = trait;
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
			Objects.requireNonNull(trait, "Trait must be set");
			Objects.requireNonNull(provider, "Provider must be set");
			return new TooltipAppenderComponent(priority, trait, provider);
		}
	}
}
