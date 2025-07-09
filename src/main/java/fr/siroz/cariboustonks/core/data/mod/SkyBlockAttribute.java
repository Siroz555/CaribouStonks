package fr.siroz.cariboustonks.core.data.mod;

import fr.siroz.cariboustonks.util.Rarity;

public record SkyBlockAttribute(
		String name,
		String shardName,
		String id,
		String skyBlockApiId) {

	public Rarity getRarityFromId() {
		if (id == null || id.isEmpty()) return Rarity.UNKNOWN;

		char letter = id.charAt(0);
		return Rarity.fromCode(letter);
	}
}
