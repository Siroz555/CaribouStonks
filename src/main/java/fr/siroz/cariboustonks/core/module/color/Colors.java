package fr.siroz.cariboustonks.core.module.color;

import net.minecraft.ChatFormatting;

public final class Colors {

	private Colors() {
	}

	public static final Color RAINBOW = Color.fromInt(0xf0f0f0).withAlpha(0);

	public static final Color RED = Color.fromFormatting(ChatFormatting.RED);
	public static final int RED_RGB = RED.asInt();

	public static final Color DARK_RED = Color.fromFormatting(ChatFormatting.DARK_RED);
	public static final int DARK_RED_RGB = DARK_RED.asInt();

	public static final Color GREEN = Color.fromFormatting(ChatFormatting.GREEN);
	public static final int GREEN_RGB = GREEN.asInt();

	public static final Color DARK_GREEN = Color.fromFormatting(ChatFormatting.DARK_GREEN);
	public static final int DARK_GREEN_RGB = DARK_GREEN.asInt();

	public static final Color YELLOW = Color.fromFormatting(ChatFormatting.YELLOW);
	public static final int YELLOW_RGB = YELLOW.asInt();

	public static final Color GOLD = Color.fromFormatting(ChatFormatting.GOLD);
	public static final int GOLD_RGB = GOLD.asInt();

	public static final Color AQUA = Color.fromFormatting(ChatFormatting.AQUA);
	public static final int AQUA_RGB = AQUA.asInt();

	public static final Color DARK_AQUA = Color.fromFormatting(ChatFormatting.DARK_AQUA);
	public static final int DARK_AQUA_RGB = DARK_AQUA.asInt();

	public static final Color BLUE = Color.fromFormatting(ChatFormatting.BLUE);
	public static final int BLUE_RGB = BLUE.asInt();

	public static final Color DARK_BLUE = Color.fromFormatting(ChatFormatting.DARK_BLUE);
	public static final int DARK_BLUE_RGB = DARK_BLUE.asInt();

	public static final Color LIGHT_PURPLE = Color.fromFormatting(ChatFormatting.LIGHT_PURPLE);
	public static final int LIGHT_PURPLE_RGB = LIGHT_PURPLE.asInt();

	public static final Color DARK_PURPLE = Color.fromFormatting(ChatFormatting.DARK_PURPLE);
	public static final int DARK_PURPLE_RGB = DARK_PURPLE.asInt();

	public static final Color WHITE = Color.fromFormatting(ChatFormatting.WHITE);
	public static final int WHITE_RGB = RED.asInt();

	public static final Color GRAY = Color.fromFormatting(ChatFormatting.GRAY);
	public static final int GRAY_RGB = GRAY.asInt();

	public static final Color DARK_GRAY = Color.fromFormatting(ChatFormatting.DARK_GRAY);
	public static final int DARK_GRAY_RGB = RED.asInt();

	public static final Color BLACK = Color.fromFormatting(ChatFormatting.BLACK);
	public static final int BLACK_RGB = RED.asInt();
}
