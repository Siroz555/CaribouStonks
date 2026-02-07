package fr.siroz.cariboustonks.core.skyblock.item;

import fr.siroz.cariboustonks.core.skyblock.item.metadata.Books;
import fr.siroz.cariboustonks.core.skyblock.item.metadata.CosmeticInfo;
import fr.siroz.cariboustonks.core.skyblock.item.metadata.DrillInfo;
import fr.siroz.cariboustonks.core.skyblock.item.metadata.Enchantments;
import fr.siroz.cariboustonks.core.skyblock.item.metadata.Gemstones;
import fr.siroz.cariboustonks.core.skyblock.item.metadata.Modifiers;
import fr.siroz.cariboustonks.core.skyblock.item.metadata.PetInfo;
import fr.siroz.cariboustonks.core.skyblock.item.metadata.RodInfo;
import fr.siroz.cariboustonks.core.skyblock.item.metadata.SpecialAuctionInfo;
import java.util.Optional;
import net.minecraft.nbt.CompoundTag;
import org.jspecify.annotations.NonNull;

/**
 * The {@code ItemMetadata} class represents the metadata of an SkyBlock item.
 *
 * @param reforge            the {@code Reforge Stone} applied
 * @param enchantments       the {@code Enchantments} applied
 * @param modifiers          the {@code Modifiers} applied
 * @param gemstones          the {@code Gemstones} applied
 * @param drillInfo          the {@code Drill Info}
 * @param rodInfo            the {@code Rod Info}
 * @param petInfo            the {@code Pet Info}
 * @param cosmeticInfo       the {@code Cosmetic Info}
 * @param specialAuctionInfo the {@code Special Auction Info}
 */
public record ItemMetadata(
		Optional<String> reforge,
		@NonNull Enchantments enchantments,
		@NonNull Books books,
		@NonNull Modifiers modifiers,
		@NonNull Gemstones gemstones,
		@NonNull DrillInfo drillInfo,
		@NonNull RodInfo rodInfo,
		@NonNull PetInfo petInfo,
		@NonNull CosmeticInfo cosmeticInfo,
		@NonNull SpecialAuctionInfo specialAuctionInfo
) {

	public static final ItemMetadata EMPTY = new ItemMetadata(
			Optional.empty(),
			Enchantments.EMPTY,
			Books.EMPTY,
			Modifiers.EMPTY,
			Gemstones.EMPTY,
			DrillInfo.EMPTY,
			RodInfo.EMPTY,
			PetInfo.EMPTY,
			CosmeticInfo.EMPTY,
			SpecialAuctionInfo.EMPTY
	);

	/**
	 * Creates a new {@code ItemMetadata} instance from the given {@code NbtCompound}.
	 *
	 * @param customData the {@code NbtCompound} containing the metadata
	 * @return the {@code ItemMetadata} parsed from the given {@code NbtCompound}
	 */
	public static @NonNull ItemMetadata ofNbt(@NonNull CompoundTag customData) {
		try {
			// Base
			Optional<String> reforge = customData.getString("modifier");
			Enchantments enchantments = Enchantments.ofNbt(customData);
			Books books = Books.ofNbt(customData);
			Modifiers modifiers = Modifiers.ofNbt(customData);
			Gemstones gemstones = Gemstones.ofNbt(customData);
			// Infos
			DrillInfo drillInfo = DrillInfo.ofNbt(customData);
			RodInfo rodInfo = RodInfo.ofNbt(customData);
			PetInfo petInfo = PetInfo.ofNbt(customData);
			CosmeticInfo cosmeticInfo = CosmeticInfo.ofNbt(customData);
			SpecialAuctionInfo specialAuctionInfo = SpecialAuctionInfo.ofNbt(customData);

			return new ItemMetadata(
					reforge,
					enchantments,
					books,
					modifiers,
					gemstones,
					drillInfo,
					rodInfo,
					petInfo,
					cosmeticInfo,
					specialAuctionInfo
			);
		} catch (Exception ignored) {
			// Ne se fera jamais, chaque metadata a un try-catch
			return EMPTY;
		}
	}
}
