package fr.siroz.cariboustonks.skyblock.item;

import fr.siroz.cariboustonks.skyblock.data.hypixel.item.Rarity;

public record SkyBlockAttribute(
		String name,
		String shardName,
		String id,
		String skyBlockApiId
) {

	public Rarity getRarityFromId() {
		if (id == null || id.isEmpty()) return Rarity.UNKNOWN;

		char letter = id.charAt(0);
		return Rarity.fromCode(letter);
	}
}
