package fr.siroz.cariboustonks.features.events.hoppity;

import java.util.Arrays;
import net.minecraft.ChatFormatting;
import org.jspecify.annotations.Nullable;

/**
 * Represents all Egg's type of Hoppity's Hunt
 */
public enum EggType {
	BREAKFAST("Breakfast", ChatFormatting.GOLD, 7),
	LUNCH("Lunch", ChatFormatting.BLUE, 14),
	DINNER("Dinner", ChatFormatting.GREEN, 21),
	BRUNCH("Brunch", ChatFormatting.GOLD, 7),
	DEJEUNER("Déjeuner", ChatFormatting.BLUE, 14),
	SUPPER("Supper", ChatFormatting.GREEN, 21);

	private final String name;
	private final ChatFormatting color;
	private final int resetDay;

	public static final EggType[] VALUES = values();

	EggType(String name, ChatFormatting color, int resetDay) {
		this.name = name;
		this.color = color;
		this.resetDay = resetDay;
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
}
