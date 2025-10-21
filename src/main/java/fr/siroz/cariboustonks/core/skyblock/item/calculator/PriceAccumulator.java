package fr.siroz.cariboustonks.core.skyblock.item.calculator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.Nullable;

/**
 * Accumulator that centralizes price mutation behavior and collects {@link Calculation} entries.
 */
class PriceAccumulator {

	private double price;
	private final double base;
	private final List<Calculation> calculations = new ArrayList<>();

	PriceAccumulator(double initialPrice) {
		this.price = initialPrice;
		this.base = initialPrice;
	}

	public void add(double v) {
		price += v;
	}

	public void set(double v) {
		price = v;
	}

	public double price() {
		return price;
	}

	public double base() {
		return base;
	}

	public void push(@Nullable Calculation calc) {
		if (calc != null) {
			calculations.add(calc);
		}
	}

	public List<Calculation> calculations() {
		return Collections.unmodifiableList(calculations);
	}
}
