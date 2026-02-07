package fr.siroz.cariboustonks.core.skyblock.item.metadata;

import java.util.OptionalInt;
import net.minecraft.nbt.CompoundTag;
import org.jspecify.annotations.NonNull;

/**
 * Represents the {@code books} applied to an item.
 *
 * @param hotPotatoes       the number of Hot Potato and Fuming Potato (5/15 | 10/15 | 15/15)
 * @param stats             the Stat Book (1 time applied)
 * @param artOfWar          the Art of War (1 time applied)
 * @param artOfPeace        the Art of Peace (1 time applied)
 * @param farmingForDummies the Farming for Dummies (X time applied)
 * @param polarvoid         the Polarvoid Book (X time applied)
 * @param jalapeno          the Jalapeno Book (1 time applied)
 * @param wet               the Wet Book (X time applied)
 */
public record Books(
		OptionalInt hotPotatoes,
		OptionalInt stats,
		OptionalInt artOfWar,
		OptionalInt artOfPeace,
		OptionalInt farmingForDummies,
		OptionalInt polarvoid,
		OptionalInt jalapeno,
		OptionalInt wet
) {

	public static final Books EMPTY = new Books(
			OptionalInt.empty(),
			OptionalInt.empty(),
			OptionalInt.empty(),
			OptionalInt.empty(),
			OptionalInt.empty(),
			OptionalInt.empty(),
			OptionalInt.empty(),
			OptionalInt.empty()
	);

	public static Books ofNbt(@NonNull CompoundTag customData) {
		// Peut être utiliser des boolean ? Les autres keep des int donc why not
		// .stream().mapToInt(Integer::intValue).filter(n -> n == 1).findFirst();
		try {
			OptionalInt totalPotatoCountData = customData.getInt("hot_potato_count")
					.map(OptionalInt::of)
					.orElse(OptionalInt.empty());
			OptionalInt statsData = customData.getInt("stats_book")
					.map(OptionalInt::of)
					.orElse(OptionalInt.empty());
			OptionalInt artOfWarData = customData.getInt("art_of_war_count")
					.map(OptionalInt::of)
					.orElse(OptionalInt.empty());
			OptionalInt artOfPeaceData = customData.getInt("artOfPeaceApplied")
					.map(OptionalInt::of)
					.orElse(OptionalInt.empty());
			OptionalInt farmingForDummiesData = customData.getInt("farming_for_dummies_count")
					.map(OptionalInt::of)
					.orElse(OptionalInt.empty());
			OptionalInt polarvoidData = customData.getInt("polarvoid")
					.map(OptionalInt::of)
					.orElse(OptionalInt.empty());
			OptionalInt jalapenoData = customData.getInt("jalapeno_count")
					.map(OptionalInt::of)
					.orElse(OptionalInt.empty());
			OptionalInt wetData = customData.getInt("wet_book_count")
					.map(OptionalInt::of)
					.orElse(OptionalInt.empty());

			return new Books(
					totalPotatoCountData,
					statsData,
					artOfWarData,
					artOfPeaceData,
					farmingForDummiesData,
					polarvoidData,
					jalapenoData,
					wetData
			);
		} catch (Exception ignored) {
			return EMPTY;
		}
	}
}
