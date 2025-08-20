package fr.siroz.cariboustonks.util;

import java.util.List;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

public final class InventoryUtils {

	private InventoryUtils() {
	}

	/**
	 * Retrieves a list of armor items currently equipped by the given {@link LivingEntity}.
	 *
	 * @param entity the living entity
	 * @return list of {@link ItemStack} representing the armor items equipped by the entity
	 */
	@NotNull
	@Unmodifiable
	public static List<ItemStack> getArmor(@NotNull LivingEntity entity) {
		return AttributeModifierSlot.ARMOR.getSlots().stream()
				.filter(slot -> slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR)
				.map(entity::getEquippedStack)
				.toList();
	}
}
