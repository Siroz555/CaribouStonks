package fr.siroz.cariboustonks.core.component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.world.entity.Entity;
import org.jspecify.annotations.NonNull;

public final class EntityGlowComponent implements Component {  // SIROZ-NOTE: documentation
	private final EntityGlowStrategy strategy;

	private EntityGlowComponent(EntityGlowStrategy strategy) {
		this.strategy = strategy;
	}

	public int getGlowColor(Entity entity) {
		return strategy.getGlowColor(entity);
	}

	@NonNull
	public static Builder builder() {
		return new Builder();
	}

	@NonNull
	public static EntityGlowComponent of(@NonNull EntityGlowStrategy strategy) {
		return new EntityGlowComponent(strategy);
	}

	public static class Builder {
		private final CompositeGlowStrategy composite = new CompositeGlowStrategy();

		public Builder when(@NonNull Predicate<Entity> condition, int color) {
			Objects.requireNonNull(condition, "condition cannot be null");
			composite.addRule(condition, color);
			return this;
		}

		public <T extends Entity> Builder whenType(@NonNull Class<T> entityType, @NonNull Function<T, Integer> colorFunction) {
			Objects.requireNonNull(entityType, "entityType cannot be null");
			Objects.requireNonNull(colorFunction, "colorFunction cannot be null");
			composite.addTypeRule(entityType, colorFunction);
			return this;
		}

		public Builder defaultColor(int color) {
			composite.setDefaultColor(color);
			return this;
		}

		public EntityGlowComponent build() {
			return new EntityGlowComponent(composite);
		}
	}

	@FunctionalInterface
	public interface EntityGlowStrategy {
		int DEFAULT = 0;

		int getGlowColor(Entity entity);
	}

	private static class CompositeGlowStrategy implements EntityGlowStrategy {
		private final List<GlowRule> rules = new ArrayList<>();
		private int defaultColor = DEFAULT;

		void addRule(Predicate<Entity> condition, int color) {
			rules.add(new GlowRule(condition, e -> color));
		}

		<T extends Entity> void addTypeRule(@NonNull Class<T> type, Function<T, Integer> colorFunction) {
			rules.add(new GlowRule(
					type::isInstance,
					e -> colorFunction.apply(type.cast(e))
			));
		}

		void setDefaultColor(int color) {
			this.defaultColor = color;
		}

		@Override
		public int getGlowColor(Entity entity) {
			for (GlowRule rule : rules) {
				if (rule.condition.test(entity)) {
					return rule.colorProvider.apply(entity);
				}
			}
			return defaultColor;
		}

		private record GlowRule(
				Predicate<Entity> condition,
				Function<Entity, Integer> colorProvider
		) {
		}
	}
}
