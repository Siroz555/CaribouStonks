package fr.siroz.cariboustonks.core.module.color;

import net.minecraft.network.chat.TextColor;

public final class Colors {

	private Colors() {
	}

	public static final Color RAINBOW = Color.fromInt(0xf0f0f0).withAlpha(0);

	public static final Color RED = Color.fromTextColor(TextColor.RED);
	public static final int RED_RGB = RED.asInt();

	public static final Color DARK_RED = Color.fromTextColor(TextColor.DARK_RED);
	public static final int DARK_RED_RGB = DARK_RED.asInt();

	public static final Color GREEN = Color.fromTextColor(TextColor.GREEN);
	public static final int GREEN_RGB = GREEN.asInt();

	public static final Color DARK_GREEN = Color.fromTextColor(TextColor.DARK_GREEN);
	public static final int DARK_GREEN_RGB = DARK_GREEN.asInt();

	public static final Color YELLOW = Color.fromTextColor(TextColor.YELLOW);
	public static final int YELLOW_RGB = YELLOW.asInt();

	public static final Color GOLD = Color.fromTextColor(TextColor.GOLD);
	public static final int GOLD_RGB = GOLD.asInt();

	public static final Color AQUA = Color.fromTextColor(TextColor.AQUA);
	public static final int AQUA_RGB = AQUA.asInt();

	public static final Color DARK_AQUA = Color.fromTextColor(TextColor.DARK_AQUA);
	public static final int DARK_AQUA_RGB = DARK_AQUA.asInt();

	public static final Color BLUE = Color.fromTextColor(TextColor.BLUE);
	public static final int BLUE_RGB = BLUE.asInt();

	public static final Color DARK_BLUE = Color.fromTextColor(TextColor.DARK_BLUE);
	public static final int DARK_BLUE_RGB = DARK_BLUE.asInt();

	public static final Color LIGHT_PURPLE = Color.fromTextColor(TextColor.LIGHT_PURPLE);
	public static final int LIGHT_PURPLE_RGB = LIGHT_PURPLE.asInt();

	public static final Color DARK_PURPLE = Color.fromTextColor(TextColor.DARK_PURPLE);
	public static final int DARK_PURPLE_RGB = DARK_PURPLE.asInt();

	public static final Color WHITE = Color.fromTextColor(TextColor.WHITE);
	public static final int WHITE_RGB = WHITE.asInt();

	public static final Color GRAY = Color.fromTextColor(TextColor.GRAY);
	public static final int GRAY_RGB = GRAY.asInt();

	public static final Color DARK_GRAY = Color.fromTextColor(TextColor.DARK_GRAY);
	public static final int DARK_GRAY_RGB = DARK_GRAY.asInt();

	public static final Color BLACK = Color.fromTextColor(TextColor.BLACK);
	public static final int BLACK_RGB = BLACK.asInt();
}
