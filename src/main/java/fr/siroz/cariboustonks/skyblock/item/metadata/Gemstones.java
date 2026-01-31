package fr.siroz.cariboustonks.skyblock.item.metadata;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.ChatFormatting;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents the {@code Gemstones} of an SkyBlock item.
 *
 * @param unlockedSlots   the list of unlocked slots (e.g. [COMBAT_0, SAPPHIRE_0] [PERIDOT_0, PERIDOT_1])
 * @param gemstoneApplied the list of {@link GemstoneApplied}
 */
public record Gemstones(
		@NotNull List<String> unlockedSlots,
		@NotNull List<GemstoneApplied> gemstoneApplied
) {

	// DEV-NOTE : La facon de récupérer les Gemstones est sujet a changement.
	// Tout marche parfaitement, cependant, le "gemData" ne peux pas être récupéré
	// via "customData.getString("gems", "");" contrairement au PetInfo, je ne comprends pas..
	// Le String est bien un JsonObject comme pour PetInfo mais ici NbtCompound doit être utilisé.

	public static final Gemstones EMPTY = new Gemstones(List.of(), List.of());

	public static Gemstones ofNbt(@NotNull CompoundTag customData) {
		try {
			CompoundTag gemData = customData.getCompoundOrEmpty("gems");
			if (gemData.isEmpty()) {
				return EMPTY;
			}

			// Faire un .toString() du NbtCompound pour parser le Json, c'est.. dommage, voir DEV-NOTE.
			JsonObject json = JsonParser.parseString(gemData.toString()).getAsJsonObject();

			List<String> unlockedSlots = json.has("unlocked_slots") ?
					json.getAsJsonArray("unlocked_slots").asList()
							.stream()
							.map(JsonElement::getAsString)
							.toList()
					: List.of();

			List<GemstoneApplied> gemstoneList = json.entrySet().stream()
					.filter(e -> !"unlocked_slots".equals(e.getKey()))
					.filter(e -> !e.getKey().endsWith("_gem"))
					.sorted(Comparator.comparingInt(e -> slotIndex(e.getKey())))
					.map(e -> parseGemstone(e.getKey(), json))
					.flatMap(Optional::stream)
					.toList();

			return new Gemstones(unlockedSlots, gemstoneList);
		} catch (Exception ignored) {
			return EMPTY;
		}
	}

	private static @NotNull Optional<GemstoneApplied> parseGemstone(String key, @NotNull JsonObject gemsObject) {
		try {
			JsonElement valueElement = gemsObject.get(key);

			String qualityStr = null;
			if (valueElement.isJsonPrimitive()) { // Format simple: "COMBAT_0": "PERFECT"
				qualityStr = valueElement.getAsString();
			} else if (valueElement.isJsonObject()) { // Format objet: "PERIDOT_0": {"uuid": "...", "quality": "PERFECT"}
				JsonObject gemObj = valueElement.getAsJsonObject();
				// Pas de qualité ?, Impossible normalement, mais bon...
				if (gemObj.has("quality")) {
					qualityStr = gemObj.get("quality").getAsString();
				}
			} else {
				return Optional.empty(); // Format inconnu
			}

			GemstoneQuality quality = GemstoneQuality.fromName(qualityStr);
			if (quality == null) {
				return Optional.empty();
			}

			String typeStr;
			String gemKey = key + "_gem";

			if (gemsObject.has(gemKey)) { // Si une clé {key}_gem existe, utiliser sa valeur
				typeStr = gemsObject.get(gemKey).getAsString();
			} else { // Sinon, extraire le type du nom de la clé (partie avant _N)
				typeStr = key.replaceAll("_\\d+$", "");
			}

			GemstoneType type = GemstoneType.fromName(typeStr);
			if (type == null) {
				return Optional.empty();
			}

			return Optional.of(new GemstoneApplied(type, quality));
		} catch (Exception ignored) {
			// Un gros catch pour éviter tout changement d'Hypixel, comme le cas avec la Fermento Armor
			// avec des JsonObject qui est vérifié en amont pour déterminer le "qualityStr".
			return Optional.empty();
		}
	}

	private static int slotIndex(@NotNull String key) {
		// JADE_0 -> 0
		int idx = key.lastIndexOf('_');
		if (idx == -1 || idx == key.length() - 1) return Integer.MAX_VALUE;
		String suffix = key.substring(idx + 1);
		try {
			return Integer.parseInt(suffix);
		} catch (NumberFormatException ignored) {
			return Integer.MAX_VALUE;
		}
	}

	/**
	 * Represents a {@code Gemstone} (A Gemstone Slot on the Skyblock Item).
	 *
	 * @param type    the {@link GemstoneType} of the gemstone
	 * @param quality the {@link GemstoneQuality} of the gemstone
	 */
	public record GemstoneApplied(@NotNull GemstoneType type, @NotNull GemstoneQuality quality) {

		/**
		 * Returns the full {@code SkyBlockId} of this Gemstone Slot.
		 *
		 * @return the skyBlockId (API)
		 */
		public @NotNull String getSkyBlockGemstoneId() {
			return (quality.name() + "_" + type.name() + "_GEM").toUpperCase(Locale.ENGLISH);
		}

		/**
		 * Try to parse a {@code GemstoneSlot} from a {@code SkyBlockId}.
		 *
		 * @param skyBlockId the {@code SkyBlockId} to parse (API)
		 * @return the parsed {@code GemstoneSlot}, or empty if parsing failed
		 */
		public static Optional<GemstoneApplied> fromSkyBlockGemstoneId(@Nullable String skyBlockId) {
			if (skyBlockId == null) return Optional.empty();

			String id = skyBlockId.trim().toUpperCase(Locale.ENGLISH);
			if (id.isEmpty()) {
				return Optional.empty();
			}

			if (id.endsWith("_GEM")) {
				id = id.substring(0, id.length() - 4); // enlève le "_GEM"
			}

			int firstUnderscore = id.indexOf('_');
			if (firstUnderscore <= 0 || firstUnderscore == id.length() - 1) {
				// pas de séparation quality/type valide ? Normalement, mais bon...
				return Optional.empty();
			}

			String qualityToken = id.substring(0, firstUnderscore);
			String typeToken = id.substring(firstUnderscore + 1);

			try {
				GemstoneQuality quality = GemstoneQuality.valueOf(qualityToken);
				GemstoneType type = GemstoneType.valueOf(typeToken);
				return Optional.of(new GemstoneApplied(type, quality));
			} catch (IllegalArgumentException ignored) {
				return Optional.empty();
			}
		}
	}

	/**
	 * Represents the {@code type} of a {@link GemstoneApplied}.
	 */
	public enum GemstoneType {
		RUBY("Ruby", "❤", ChatFormatting.RED),
		AMBER("Amber", "⸕", ChatFormatting.GOLD),
		TOPAZ("Topaz", "✧", ChatFormatting.YELLOW),
		JADE("Jade", "☘", ChatFormatting.GREEN),
		SAPPHIRE("Sapphire", "✎", ChatFormatting.BLUE),
		AMETHYST("Amethyst", "❈", ChatFormatting.DARK_PURPLE),
		JASPER("Jasper", "❁", ChatFormatting.LIGHT_PURPLE),
		OPAL("Opal", "❂", ChatFormatting.WHITE),
		AQUAMARINE("Aquamarine", "☂", ChatFormatting.AQUA),
		CITRINE("Citrine", "☘", ChatFormatting.DARK_RED),
		ONYX("Onyx", "☠", ChatFormatting.DARK_GRAY),
		PERIDOT("Peridot", "☘", ChatFormatting.DARK_GREEN),
		;

		private final String displayName;
		private final String statIcon;
		private final ChatFormatting color;

		private static final Map<String, GemstoneType> BY_NAME = Arrays.stream(values())
				.collect(Collectors.toUnmodifiableMap(GemstoneType::name, Function.identity()));

		GemstoneType(String displayName, String statIcon, ChatFormatting color) {
			this.displayName = displayName;
			this.statIcon = statIcon;
			this.color = color;
		}

		public static @Nullable GemstoneType fromName(@NotNull String name) {
			return BY_NAME.get(name.toUpperCase(Locale.ENGLISH));
		}

		public String getDisplayName() {
			return displayName;
		}

		public String getStatIcon() {
			return statIcon;
		}

		public ChatFormatting getColor() {
			return color;
		}
	}

	/**
	 * Represents the {@code quality} of a {@link GemstoneApplied}.
	 */
	public enum GemstoneQuality {
		ROUGH("Rough", ChatFormatting.WHITE),
		FLAWED("Flawed", ChatFormatting.GREEN),
		FINE("Fine", ChatFormatting.BLUE),
		FLAWLESS("Flawless", ChatFormatting.DARK_PURPLE),
		PERFECT("Perfect", ChatFormatting.GOLD),
		;

		private final String displayName;
		private final ChatFormatting color;

		private static final Map<String, GemstoneQuality> BY_NAME = Arrays.stream(values())
				.collect(Collectors.toUnmodifiableMap(GemstoneQuality::name, Function.identity()));

		GemstoneQuality(String displayName, ChatFormatting color) {
			this.displayName = displayName;
			this.color = color;
		}

		public static @Nullable GemstoneQuality fromName(@Nullable String name) {
			if (name == null) return null;
			return BY_NAME.get(name.toUpperCase(Locale.ENGLISH));
		}

		public String getDisplayName() {
			return displayName;
		}

		public ChatFormatting getColor() {
			return color;
		}
	}
}
