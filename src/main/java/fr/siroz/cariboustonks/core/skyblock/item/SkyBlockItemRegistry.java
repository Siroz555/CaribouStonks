package fr.siroz.cariboustonks.core.skyblock.item;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public final class SkyBlockItemRegistry {

	private record AttributeSnapshot(
			Map<String, SkyBlockAttribute> byShardName,
			Map<String, SkyBlockAttribute> byId,
			Map<String, SkyBlockAttribute> bySkyBlockApiId
	) {
		static final AttributeSnapshot EMPTY = new AttributeSnapshot(
				Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap()
		);

		static @NonNull AttributeSnapshot of(@NonNull Collection<SkyBlockAttribute> attributes) {
			return new AttributeSnapshot(
					indexBy(attributes, SkyBlockAttribute::shardName),
					indexBy(attributes, SkyBlockAttribute::id),
					indexBy(attributes, SkyBlockAttribute::skyBlockApiId)
			);
		}

		private static Map<String, SkyBlockAttribute> indexBy(
				@NonNull Collection<SkyBlockAttribute> attributes,
				@NonNull Function<SkyBlockAttribute, String> key
		) {
			return attributes.stream().collect(Collectors.toUnmodifiableMap(key, Function.identity()));
		}
	}

	private record EnchantmentSnapshot(
			Map<String, SkyBlockEnchantment> byId
	) {
		static final EnchantmentSnapshot EMPTY = new EnchantmentSnapshot(Collections.emptyMap());

		static @NonNull EnchantmentSnapshot of(@NonNull Collection<SkyBlockEnchantment> enchantments) {
			return new EnchantmentSnapshot(enchantments.stream()
					.collect(Collectors.toUnmodifiableMap(SkyBlockEnchantment::id, Function.identity()))
			);
		}
	}

	private static volatile AttributeSnapshot attributeSnapshot = AttributeSnapshot.EMPTY;
	private static volatile EnchantmentSnapshot enchantmentSnapshot = EnchantmentSnapshot.EMPTY;

	private SkyBlockItemRegistry() {
	}

	public static void loadAttributes(@NonNull Collection<SkyBlockAttribute> attributes) {
		attributeSnapshot = AttributeSnapshot.of(attributes);
	}

	public static void loadEnchantments(@NonNull Collection<SkyBlockEnchantment> enchantments) {
		enchantmentSnapshot = EnchantmentSnapshot.of(enchantments);
	}

	public static @Nullable SkyBlockAttribute getAttributeByShardName(@Nullable String name) {
		if (name == null || name.isEmpty()) return null;
		return attributeSnapshot.byShardName().get(normalizeShardName(name));
	}

	public static @Nullable SkyBlockAttribute getAttributeById(@Nullable String id) {
		if (id == null || id.isEmpty()) return null;
		return attributeSnapshot.byId().get(id);
	}

	public static @Nullable SkyBlockAttribute getAttributeBySkyBlockApiId(@Nullable String skyBlockId) {
		if (skyBlockId == null || skyBlockId.isEmpty()) return null;
		return attributeSnapshot.bySkyBlockApiId().get(skyBlockId);
	}

	public static @Nullable SkyBlockEnchantment getEnchantmentById(@Nullable String id) {
		if (id == null) return null;
		return enchantmentSnapshot.byId().get(id);
	}

	public static int sizeOfAttributes() {
		return attributeSnapshot.byId().size();
	}

	public static int sizeOfEnchantments() {
		return enchantmentSnapshot.byId().size();
	}

	private static @NonNull String normalizeShardName(@NonNull String name) {
		int shardIndex = name.indexOf("Shard");
		String base = shardIndex > 0 ? name.substring(0, shardIndex - 1) : name;
		return base.replace("BUY ", "").replace("SELL ", "");
	}
}
