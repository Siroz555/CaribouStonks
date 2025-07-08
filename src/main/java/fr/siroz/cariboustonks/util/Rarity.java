package fr.siroz.cariboustonks.util;

import dev.isxander.yacl3.api.NameableEnum;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

public enum Rarity implements NameableEnum {
    UNKNOWN(0, Formatting.GRAY),
	COMMON(1, Formatting.WHITE),
	UNCOMMON(2, Formatting.GREEN),
	RARE(3, Formatting.BLUE),
	EPIC(4, Formatting.DARK_PURPLE),
	LEGENDARY(5, Formatting.GOLD),
	SPECIAL(6, Formatting.RED),
	VERY_SPECIAL(7, Formatting.RED),
	MYTHIC(8, Formatting.LIGHT_PURPLE),
	DIVINE(9, Formatting.AQUA),
	ULTIMATE(10, Formatting.DARK_RED),
	;

	private static final Rarity[] VALUES = values();

	private final int power;
	private final int color;
	private final Formatting formatting;
	private final float r;
	private final float g;
	private final float b;

	Rarity(int power, Formatting formatting) {
		this.power = power;
		//noinspection DataFlowIssue
		this.color = formatting.getColorValue();
		this.formatting = formatting;
		this.r = ((color >> 16) & 0xFF) / 255f;
		this.g = ((color >> 8) & 0xFF) / 255f;
		this.b = (color & 0xFF) / 255f;
	}

	public static Rarity getRarity(@Nullable String name) {
		if (name == null) return UNKNOWN;

		for (Rarity rarity : VALUES) {
			if (rarity.name().equalsIgnoreCase(name)) {
				return rarity;
			}
		}

		return UNKNOWN;
	}

	public int getPower() {
		return power;
	}

	public int getColor() {
		return color;
	}

	public Formatting getFormatting() {
		return formatting;
	}

	public float r() {
		return r;
	}

	public float g() {
		return g;
	}

	public float b() {
		return b;
	}

	@Override
	public Text getDisplayName() {
		return Text.of(name());
	}
}
