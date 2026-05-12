package fr.siroz.cariboustonks.features.events.hoppity;

import java.util.Arrays;
import net.minecraft.ChatFormatting;
import org.jspecify.annotations.Nullable;

/**
 * Represents all Egg's type of Hoppity's Hunt
 */
public enum EggType {
	BREAKFAST("Breakfast", ChatFormatting.GOLD, 7, false),
	LUNCH("Lunch", ChatFormatting.BLUE, 14, false),
	DINNER("Dinner", ChatFormatting.GREEN, 21, false),
	BRUNCH("Brunch", ChatFormatting.GOLD, 7, true),
	DEJEUNER("Déjeuner", ChatFormatting.BLUE, 14, true),
	SUPPER("Supper", ChatFormatting.GREEN, 21, true);

	private final String name;
	private final ChatFormatting color;
	private final int resetDay;
	private final boolean alternateDay;

	public static final EggType[] VALUES = values();

	EggType(String name, ChatFormatting color, int resetDay, boolean alternateDay) {
		this.name = name;
		this.color = color;
		this.resetDay = resetDay;
		this.alternateDay = alternateDay;
	}

	public static @Nullable EggType getByName(@Nullable String name) {
		if (name == null) return null;
		return Arrays.stream(VALUES)
				.filter(eggType -> eggType.getName().equals(name))
				.findFirst()
				.orElse(null);
	}

	public String getName() {
		return name;
	}

	public ChatFormatting getColor() {
		return color;
	}

	public int getResetDay() {
		return resetDay;
	}

	public boolean isAlternateDay() {
		return alternateDay;
	}
}
