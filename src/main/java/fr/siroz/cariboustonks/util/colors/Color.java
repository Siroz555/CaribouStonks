package fr.siroz.cariboustonks.util.colors;

import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Représente une couleur {@code R G B A}.
 */
public class Color {

	public static final Color DEFAULT = new Color(-1, -1, -1, -1);
	private static final Pattern HEX_PATTERN = Pattern.compile("#?([0-9a-fA-F]{6})([0-9a-fA-F]{2})?");

	public final int r;
	public final int g;
	public final int b;
	public final int a;

	/**
	 * Créer une {@link Color} à partir d'une autre couleur.
	 *
	 * @param color la couleur
	 */
	@Contract(pure = true)
	public Color(@NotNull Color color) {
		this(color.r, color.g, color.b, color.a);
	}

	/**
	 * Créer une {@link Color} à partir d'une autre couleur, avec un {@code alpha}.
	 *
	 * @param color la couleur
	 * @param alpha l'alpha
	 */
	@Contract(pure = true)
	public Color(@NotNull Color color, int alpha) {
		this(color.r, color.g, color.b, alpha);
	}

	/**
	 * Créer une {@link Color} à partir d'Integers.
	 *
	 * @param r red
	 * @param g green
	 * @param b blue
	 */
	public Color(int r, int g, int b) {
		this(r, g, b, 255);
	}

	/**
	 * Créer une {@link Color} à partir de Floats.
	 *
	 * @param r red
	 * @param g green
	 * @param b blue
	 */
	public Color(float r, float g, float b) {
		this(r, g, b, 1f);
	}

	/**
	 * Créer une {@link Color} à partir d'Integers.
	 *
	 * @param r red
	 * @param g green
	 * @param b blue
	 * @param a alpha
	 */
	public Color(int r, int g, int b, int a) {
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;
	}

	/**
	 * Créer une {@link Color} à partir de Floats.
	 *
	 * @param r red
	 * @param g green
	 * @param b blue
	 * @param a alpha
	 */
	public Color(float r, float g, float b, float a) {
		this.r = (int) (r * 255);
		this.g = (int) (g * 255);
		this.b = (int) (b * 255);
		this.a = (int) (a * 255);
	}

	/**
	 * Récupère la couleur d'un {@link Formatting}.
	 *
	 * @param formatting le formatting type
	 * @return la couleur obtenue
	 */
	public static Color fromFormatting(@NotNull Formatting formatting) {
		if (formatting.getColorValue() == null) {
			return DEFAULT;
		}

		return fromInt(formatting.getColorValue() | 0xFF000000);
	}

	/**
	 * Récupère la couleur d'un {@link DyeColor}.
	 *
	 * @param color la dye color type
	 * @return la couleur obtenue
	 */
	@Contract("_ -> new")
	public static @NotNull Color fromDyeColor(@NotNull DyeColor color) {
		return fromInt(color.getEntityColor() | 0xFF000000);
	}

	/**
	 * Récupère la couleur sous format {@code 0x(AA)RRGGBB}.
	 * Si l'alpha n'est pas présent ou qu'il est à 0, il sera de 255.
	 *
	 * @param num la couleur sous Integer
	 * @return la couleur obtenue
	 * @see #withAlpha(int)
	 */
	@Contract(value = "_ -> new", pure = true)
	public static @NotNull Color fromInt(int num) {
		if ((num & 0xFF000000) == 0) {
			num |= 0xFF000000; // (0) / 0 -> 255
		}

		return new Color(num >> 16 & 255, num >> 8 & 255, num & 255, num >> 24 & 255);
	}

	/**
	 * Récupère la couleur sous format {@code #rrggbb(aa)} ou {@code rrggbb(aa)}.
	 *
	 * @param hex la couleur hexadécimale
	 * @return la couleur obtenue ou {@link #DEFAULT} si le {@code HEX} n'est pas valide
	 */
	public static Color fromHexString(@NotNull String hex) {
		Matcher hexMatcher = HEX_PATTERN.matcher(hex.trim());
		if (!hexMatcher.matches()) {
			return Color.DEFAULT;
		}

		if (hexMatcher.group(2) == null) {
			return fromInt(Integer.parseInt(hexMatcher.group(1), 16)).withAlpha(255);
		} else {
			return fromInt(Integer.parseInt(hexMatcher.group(1), 16)).withAlpha(Integer.parseInt(hexMatcher.group(2), 16));
		}
	}

	/**
	 * Retourne la couleur avec un {@code Alpha}.
	 *
	 * @param a alpha
	 * @return la nouvelle couleur avec alpha
	 */
	public Color withAlpha(int a) {
		return new Color(this, a);
	}

	/**
	 * Retourne la couleur avec un {@code Alpha}.
	 *
	 * @param a alpha
	 * @return la nouvelle couleur avec alpha
	 */
	public Color withAlpha(float a) {
		return new Color(this, (int) (a * 255));
	}

	/**
	 * Transforme la couleur en {@code Integer} format.
	 * <p>
	 * {@code 0xAARRGGBB} format.
	 *
	 * @return int format
	 */
	public int asInt() {
		int a = Math.min(this.a, 255);
		int r = Math.min(this.r, 255);
		int g = Math.min(this.g, 255);
		int b = Math.min(this.b, 255);
		return (a << 24) | (r << 16) | (g << 8) | b;
	}

	/**
	 * Récupère la couleur RGB en integer et la retourne dans une array représentant les composants de la
	 * couleur en float, dans le format RGB.
	 *
	 * @return array représentant les composants de la couleur en float
	 */
	public float[] asFloatComponents() {
		return new float[]{r / 255f, g / 255f, b / 255f};
	}

	/**
	 * Transforme la couleur en {@code HEX} format avec {@link #a} (alpha).
	 * <p>
	 * {@code #rrggbbaa} format.
	 *
	 * @return hex string format
	 */
	public String toHexString() {
		return "#" + String.format("%08x", ((r << 24) | (g << 16) | (b << 8) | a));
	}

	/**
	 * Transforme la couleur en {@link java.awt.Color}.
	 *
	 * @return {@link java.awt.Color}
	 */
	@Contract(value = " -> new", pure = true)
	public @NotNull java.awt.Color toAwtColor() {
		int ra = Math.max(0, Math.min(255, this.r));
		int ga = Math.max(0, Math.min(255, this.g));
		int ba = Math.max(0, Math.min(255, this.b));
		int aa = Math.max(0, Math.min(255, this.a));
		return new java.awt.Color(ra, ga, ba, aa);
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Color color)) {
			return false;
		}

		return (this.r == color.r && this.g == color.g && this.b == color.b && this.a == color.a);
	}

	@Override
	public String toString() {
		return toHexString();
	}
}
