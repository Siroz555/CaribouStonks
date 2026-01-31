package fr.siroz.cariboustonks.skyblock.item.metadata;

import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Optional;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the {@code Normal Enchantments} and {@code Ultimate Enchantment} applied to an item.
 *
 * @param enchantments        the {@code Normal Enchantments} applied
 * @param ultimateEnchantment the {@code Ultimate Enchantment} applied
 */
public record Enchantments(
		@NotNull Object2IntMap<String> enchantments,
		@NotNull Optional<Pair<String, Integer>> ultimateEnchantment
) {

	public static final Enchantments EMPTY = new Enchantments(Object2IntMaps.emptyMap(), Optional.empty());

	public static Enchantments ofNbt(@NotNull CompoundTag customData) {
		try {
			Object2IntMap<String> enchantments = new Object2IntOpenHashMap<>();
			Optional<Pair<String, Integer>> ultimateEnchantment = Optional.empty();
			CompoundTag enchantData = customData.getCompoundOrEmpty("enchantments");
			for (String id : enchantData.keySet()) {
				int level = enchantData.getIntOr(id, 0);
				if (id.startsWith("ultimate_")) {
					ultimateEnchantment = Optional.of(Pair.of(id, level));
				} else {
					enchantments.put(id, level);
				}
			}
			return new Enchantments(enchantments, ultimateEnchantment);
		} catch (Exception ignored) {
			return EMPTY;
		}
	}
}
