package fr.siroz.cariboustonks.util.colors;

import com.mojang.serialization.Codec;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;

public final class ColorUtils {

	public static final Codec<Color> COLOR_CODEC = Codec.INT.xmap(argb -> new Color(argb, true), Color::getRGB);

	private ColorUtils() {
	}

	/**
	 * Computes an interpolated color between two given colors based on a factor.
	 *
	 * @param startColor the color to interpolate from
	 * @param endColor   the color to interpolate to
	 * @param factor     a float value between 0 and 1 representing the interpolation factor;
	 *                   values outside this range are clamped
	 * @return a new color representing the interpolated color
	 */
	@Contract("_, _, _ -> new")
	public static @NotNull Color interpolatedColor(@NotNull Color startColor, @NotNull Color endColor, float factor) {
		factor = Math.max(0, Math.min(1f, factor)); // 0.0 et 1.0

		int red = (int) (startColor.getRed() + factor * (endColor.getRed() - startColor.getRed()));
		int green = (int) (startColor.getGreen() + factor * (endColor.getGreen() - startColor.getGreen()));
		int blue = (int) (startColor.getBlue() + factor * (endColor.getBlue() - startColor.getBlue()));
		int alpha = (int) (startColor.getAlpha() + factor * (endColor.getAlpha() - startColor.getAlpha()));

		return new Color(red, green, blue, alpha);
	}

	/**
	 * Linearly interpolates between the RGB components of two colors while
	 * preserving the alpha channel externally.
	 *
	 * @param startColor the starting color
	 * @param endColor   the target color
	 * @param factor     interpolation factor
	 * @return a blended RGB color encoded as 0xRRGGBB with no alpha
	 */
	public static int lerpRGB(int startColor, int endColor, float factor) {
		int rA = (startColor >> 16) & 0xFF;
		int gA = (startColor >> 8) & 0xFF;
		int bA = startColor & 0xFF;

		int rB = (endColor >> 16) & 0xFF;
		int gB = (endColor >> 8) & 0xFF;
		int bB = endColor & 0xFF;

		int r = (int) (rA + (rB - rA) * factor);
		int g = (int) (gA + (gB - gA) * factor);
		int b = (int) (bA + (bB - bA) * factor);

		return (r << 16) | (g << 8) | b;
	}

	/**
	 * Changes the alpha value of the input color while retaining its RGB components.
	 *
	 * @param originalColor the original color represented as an integer, where the highest-order byte is the alpha value
	 * @param alpha         the new alpha value to be applied, ranging from 0 (completely transparent) to 255
	 *                      (completely opaque)
	 * @return an integer representing the color with the updated alpha value, preserving the RGB components
	 */
	public static int changeAlpha(int originalColor, int alpha) {
		int color = originalColor & 0x00ffffff;
		return (alpha << 24) | color;
	}

	/**
	 * Récupère la couleur RGB en integer et la retourne dans une array représentant les composants de la
	 * couleur en float, dans le format RGB.
	 *
	 * @return array représentant les composants de la couleur en float
	 */
	@Contract(value = "_ -> new", pure = true)
	public static float @NotNull [] getFloatComponents(int color) {
		return new float[]{((color >> 16) & 0xFF) / 255f, ((color >> 8) & 0xFF) / 255f, (color & 0xFF) / 255f};
	}

	public static int hsbToRGB(float hueComponent, float saturationColor, float brightnessColor) {
		return Color.getHSBColor(hueComponent, saturationColor, brightnessColor).getRGB();
	}

	public static Color getAwtColor(@NotNull Formatting formatting) {
		if (formatting.getColorValue() == null) {
			return Color.WHITE;
		}

		float[] components = getFloatComponents(formatting.getColorValue());
		return new Color(components[0], components[1], components[2]);
	}
}
