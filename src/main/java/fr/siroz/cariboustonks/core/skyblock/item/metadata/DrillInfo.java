package fr.siroz.cariboustonks.core.skyblock.item.metadata;

import java.util.Optional;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the {@code Drill Parts} applied to a Drill.
 *
 * @param fuelTank      the fuel tank module
 * @param engine        the engine module
 * @param upgradeModule the upgrade module
 */
public record DrillInfo(
		Optional<String> fuelTank,
		Optional<String> engine,
		Optional<String> upgradeModule
) {

	public static final DrillInfo EMPTY = new DrillInfo(
			Optional.empty(),
			Optional.empty(),
			Optional.empty()
	);

	public static DrillInfo ofNbt(@NotNull CompoundTag customData) {
		try {
			Optional<String> fuelTankPart = customData.getString("drill_part_fuel_tank");
			Optional<String> enginePart = customData.getString("drill_part_engine");
			Optional<String> upgradeModulePart = customData.getString("drill_part_upgrade_module");
			return new DrillInfo(fuelTankPart, enginePart, upgradeModulePart);
		} catch (Exception ignored) {
			return EMPTY;
		}
	}
}
