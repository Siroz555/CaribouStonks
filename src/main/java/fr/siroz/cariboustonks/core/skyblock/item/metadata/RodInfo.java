package fr.siroz.cariboustonks.core.skyblock.item.metadata;

import java.util.Optional;
import java.util.function.Function;
import net.minecraft.nbt.CompoundTag;
import org.jspecify.annotations.NonNull;

/**
 * Represents the {@code Rod Parts} applied to a Rod.
 *
 * @param line   the line part
 * @param hook   the hook part
 * @param sinker the sinker part
 */
public record RodInfo(
		Optional<String> line,
		Optional<String> hook,
		Optional<String> sinker
) {

	public static final RodInfo EMPTY = new RodInfo(
			Optional.empty(),
			Optional.empty(),
			Optional.empty()
	);

	private static final Function<CompoundTag, Optional<String>> MAPPER = nbt -> nbt.getString("part");

	public static RodInfo ofNbt(@NonNull CompoundTag customData) {
		try {
			Optional<String> line = customData.getCompoundOrEmpty("line")
					.asCompound()
					.flatMap(MAPPER);
			Optional<String> hook = customData.getCompoundOrEmpty("hook")
					.asCompound()
					.flatMap(MAPPER);
			Optional<String> sinker = customData.getCompoundOrEmpty("sinker")
					.asCompound()
					.flatMap(MAPPER);
			return new RodInfo(line, hook, sinker);
		} catch (Exception ignored) {
			return EMPTY;
		}
	}
}
