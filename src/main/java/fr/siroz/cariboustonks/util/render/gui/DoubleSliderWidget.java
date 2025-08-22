package fr.siroz.cariboustonks.util.render.gui;

import fr.siroz.cariboustonks.util.math.MathUtils;
import it.unimi.dsi.fastutil.doubles.DoubleConsumer;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

/**
 * This class represents a slider widget for double precision floating-point numbers.
 * <p>
 * Siroz-Note : Clamp la valeur afin de garantir qu’elle reste toujours dans l’intervalle minimum/maximum spécifié.
 *
 * @see SliderWidget
 */
public final class DoubleSliderWidget extends SliderWidget {

	private final double min;
	private final double max;
	private final DoubleConsumer changeCallback;

	/**
	 * Creates a new DoubleSliderWidget
	 *
	 * @see SliderWidget
	 */
	public DoubleSliderWidget(int x, int y, int width, int height, @NotNull Text text, double min, double max, double value, @NotNull DoubleConsumer changeCallback) {
		super(x, y, width, height, text, value);
		this.min = min;
		this.max = max;
		this.value = (MathUtils.clamp(value, min, max) - min) / (max - min);
		this.changeCallback = changeCallback;
		this.updateMessage();
	}

	@Override
	protected void updateMessage() {
		this.setMessage(Text.of(String.format("%.2f", getValue())));
	}

	@Override
	protected void applyValue() {
		changeCallback.accept(getValue());
	}

	/**
	 * Computes the current value of the slider based on its clamped normalized position within the range.
	 * The slider's normalized position is mapped into the range defined by the minimum and maximum values.
	 *
	 * @return the current value of the slider, clamped and interpolated within the defined range
	 */
	private double getValue() {
		return MathUtils.lerp(min, max, MathUtils.clamp(this.value, 0.0f, 1.0f));
	}
}
