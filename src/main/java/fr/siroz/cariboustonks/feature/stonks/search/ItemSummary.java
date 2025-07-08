package fr.siroz.cariboustonks.feature.stonks.search;

import net.minecraft.item.ItemStack;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

public record ItemSummary(
		String hypixelSkyBlockId,
		Formatting color,
		String name,
		ItemStack icon
) implements Comparable<ItemSummary> {

	@Override
	public int compareTo(@NotNull ItemSummary itemSummary) {
		return this.name.compareToIgnoreCase(itemSummary.name);
	}
}
