package fr.siroz.cariboustonks.core.data.mod;

import java.util.OptionalInt;

public record SkyBlockEnchantment(
		String id,
		String name,
		int maxLevel,
		OptionalInt goodLevel
) {

	public boolean isMaxLevel(int level) {
		return level >= maxLevel;
	}

	public boolean isGoodLevel(int level) {
		return goodLevel.isPresent() && level >= goodLevel.getAsInt() && !isMaxLevel(level);
	}

	public boolean isGoodOrMaxLevel(int level) {
		return isMaxLevel(level) || isGoodLevel(level);
	}
}
