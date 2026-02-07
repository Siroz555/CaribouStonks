package fr.siroz.cariboustonks.core.skyblock.item.calculator;

import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Represents the result of an {@link ItemValueCalculator} execution.
 *
 * @param state              the {@link State} of the execution
 * @param price              the final price of the item
 * @param base               the base price of the item
 * @param calculations       the list of {@link Calculation}s
 * @param calculationsByType the map of {@link Calculation}s grouped by {@link Calculation.Type}
 * @param error              the error that occurred during the execution
 */
public record ItemValueResult(
		@NonNull State state,
		double price,
		double base,
		@NonNull List<Calculation> calculations,
		Map<Calculation.Type, List<Calculation>> calculationsByType,
		@Nullable Throwable error
) {

	public static final ItemValueResult EMPTY = new ItemValueResult(
			State.EMPTY,
			0,
			0,
			Collections.emptyList(),
			Collections.emptyMap(),
			null
	);

	public enum State {
		FAIL, SUCCESS, EMPTY
	}

	/**
	 * Returns the first {@link Calculation} of the given type.
	 *
	 * @param type the type of the calculation to retrieve
	 * @return the first {@link Calculation} of the given type or {@code null} if not found
	 */
	public @Nullable Calculation get(Calculation.@NonNull Type type) {
		return calculationsByType.getOrDefault(type, Collections.emptyList()).stream()
				.findFirst()
				.orElse(null);
	}

	/**
	 * Returns the list of {@link Calculation}s of the given type.
	 *
	 * @param type the type of the calculation to retrieve
	 * @return the list of {@link Calculation}s of the given type
	 */
	public @NonNull List<Calculation> getList(Calculation.@NonNull Type type) {
		return calculationsByType.getOrDefault(type, Collections.emptyList());
	}

	static @NonNull ItemValueResult success(double price, double base, List<Calculation> calculations) {
		return new ItemValueResult(State.SUCCESS, price, base, Collections.unmodifiableList(calculations), groupCalculationsByType(calculations), null);
	}

	static @NonNull ItemValueResult fail(@Nullable Throwable throwable) {
		return new ItemValueResult(State.FAIL, 0, 0, Collections.emptyList(), Collections.emptyMap(), throwable);
	}

	private static Map<Calculation.Type, List<Calculation>> groupCalculationsByType(@NonNull List<Calculation> calculations) {
		return calculations.stream()
				.collect(Collectors.groupingBy(
						Calculation::type,
						() -> new EnumMap<>(Calculation.Type.class),
						Collectors.collectingAndThen(Collectors.toList(), list -> {
							list.sort(Comparator.comparingDouble(Calculation::price).reversed());
							return List.copyOf(list);
						})
				));
	}
}
